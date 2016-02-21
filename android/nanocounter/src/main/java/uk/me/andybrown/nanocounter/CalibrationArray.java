package uk.me.andybrown.nanocounter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import uk.me.andybrown.nanocounter.commands.GetCalibrationBitmapResponse;
import uk.me.andybrown.nanocounter.commands.GetCalibrationResponse;
import uk.me.andybrown.nanocounter.workqueue.GetCalibrationBitmapWorkItem;
import uk.me.andybrown.nanocounter.workqueue.GetCalibrationWorkItem;
import uk.me.andybrown.nanocounter.workqueue.SetCalibrationWorkItem;


/*
 * Manage the array of calibration data
 */

class CalibrationArray {

  private static final String LOGGER=CalibrationArray.class.getName();

  public static final int MAX_ENTRIES = 113;    // 113 x 18 byte entries in a 2K page on the STM32F072

  protected final List<Calibration> _calibrations=new ArrayList<>();
  protected int _lastRequestedIndex=-1;
  protected boolean _readCompleted=false;
  protected byte[] _calibrationBitmap;


  /*
   * Start requesting the calibration data
   */

  public void startRequesting(BluetoothService service) {

    // we start by requesting the calibration bitmap

    service.addWorkItem(new GetCalibrationBitmapWorkItem(service));
  }


  /*
   * The calibration bitmap has arrived, start requesting
   */

  public void onCalibrationBitmapReceived(BluetoothService service,GetCalibrationBitmapResponse response) {

    // get the usage bitmap

    _calibrationBitmap=response.getCalibrationBitmap();
    _lastRequestedIndex=-1;
    _readCompleted=false;

    requestNext(service);
  }


  /*
   * request the next calibration entry. return true if all done, false if not
   */

  protected void requestNext(BluetoothService service) {

    // potentially iterate over all

    for(_lastRequestedIndex++;_lastRequestedIndex<MAX_ENTRIES;_lastRequestedIndex++) {
      if(isUsedEntry(_lastRequestedIndex)) {
        service.addWorkItem(new GetCalibrationWorkItem(service,_lastRequestedIndex));
        break;
      }
    }

    // check for last

    if(_lastRequestedIndex>=MAX_ENTRIES) {

      Log.i(LOGGER,"Finished requesting calibration entries");
      _readCompleted=true;
      sortAndNotify(service);
    }
  }


  /*
   * New calibration received from the MCU. return true if done, false if not
   */

  public void onCalibrationReceived(BluetoothService service,GetCalibrationResponse response) {

    // store the calibration response if it's valid

    if(response.getCalibration().getDate().getTime()==0)
      Log.i(LOGGER,"Ignoring empty calibration response");
    else {
      response.getCalibration().setIndex(_lastRequestedIndex);
      _calibrations.add(response.getCalibration());
    }

    // request the next one

    requestNext(service);
  }


  /*
   * Sort the array and notify
   */

  protected void sortAndNotify(BluetoothService service) {

    // finished, sort calibrations by date

    Collections.sort(_calibrations,new Comparator<Calibration>() {
      @Override
      public int compare(Calibration lhs,Calibration rhs) {
        return lhs.getDate().compareTo(rhs.getDate());
      }
    });

    // notify the most recent calibration

    notifyActivateCalibration(service);
  }


  /*
   * Send a notification that calibration needs to be activated
   */

  protected void notifyActivateCalibration(Context context) {

    Intent intent;

    intent=new Intent(CustomIntent.ACTIVATE_CALIBRATION);
    context.sendBroadcast(intent);
  }


  /*
   * Check if this entry is used
   */

  public boolean isUsedEntry(int index) {

    int byteIndex,bitIndex;

    byteIndex=index/8;
    bitIndex=index % 8;

    return (_calibrationBitmap[byteIndex] & (1 << bitIndex))!=0;
  }


  /*
   * Get the most recent calibration or null if there isn't one
   */

  public Calibration getRecentCalibration() {

    // null is returned if no entries

    if(_calibrations.size()==0)
      return null;

    // it's ordered by ascending date

    return _calibrations.get(_calibrations.size()-1);
  }


  /*
   * Save a new calibration entry to the device. If there's a cal for this
   * day then we overwrite it otherwise we add to a free slot and if there
   * are no free slots then we overwrite the oldest one
   */

  public void save(BluetoothService service,Calibration cal) {

    int slot;

    // find an appropriate slot

    if((slot=findByDate(cal.getDate()))==-1)
      if((slot=findEmptySlot())==-1)
        slot=findOldest();

    // save to the device

    cal.setIndex(slot);
    service.addWorkItem(new SetCalibrationWorkItem(service,cal));

    // replace or add to the device

    replaceOrAdd(cal);

    // sort and notify

    sortAndNotify(service);
  }


  /*
   * Replace in the array or add to the end. Also update the calibration bitmap
   */

  protected void replaceOrAdd(Calibration cal) {

    int i,byteIndex,bitIndex;

    // try to replace in the array

    for(i=0;i<_calibrations.size();i++) {
      if(_calibrations.get(i).getIndex()==cal.getIndex()) {
        _calibrations.set(i,cal);
        break;
      }
    }

    // if not found then append

    if(i==_calibrations.size())
      _calibrations.add(cal);

    // update the usage bitmap

    byteIndex=cal.getIndex()/8;
    bitIndex=cal.getIndex() % 8;

    _calibrationBitmap[byteIndex]|=(1 << bitIndex);
  }


  /*
   * Comparator that just compares the date parts of a calendar
   */

  static class TimeIgnoringComparator implements Comparator<Calendar> {
    public int compare(Calendar c1, Calendar c2) {
      if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
        return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
      if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
        return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
      return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
    }
  }


  /*
   * Find a calibration for the given date or -1 if not found
   */

  public int findByDate(Date date) {

    int i;
    Calendar cal1,cal2;
    TimeIgnoringComparator comparator;

    // create the calendar instance

    cal1=Calendar.getInstance();
    cal2=Calendar.getInstance();
    cal1.setTime(date);
    comparator=new TimeIgnoringComparator();

    // find by date. not many entries so a simple linear search will suffice

    for(i=0;i<_calibrations.size();i++) {

      cal2.setTime(_calibrations.get(i).getDate());
      if(comparator.compare(cal1,cal2)==0)
        return i;
    }

    // not found

    return -1;
  }


  /*
   * Find an empty calibration slot
   */

  public int findEmptySlot() {

    int i;

    for(i=0;i<MAX_ENTRIES;i++)
      if(!isUsedEntry(i))
        return i;

    return -1;
  }


  /*
   * Find the oldest entry
   */

  public int findOldest() {

    // the array is sorted so the oldest is at the beginning

    return _calibrations.size()==0 ? -1 : _calibrations.get(0).getIndex();
  }


  /*
   * Get the history array as formatted strings, newest first
   */

  public String[] getHistoryArray() {

    String[] values;
    int i,j;

    values=new String[_calibrations.size()];
    j=0;

    for(i=_calibrations.size()-1;i>=0;i--)
      values[j++]=formatCalibration(_calibrations.get(i));

    if(values.length>0)
      values[0]+=" (active)";

    return values;
  }


  /*
   * Format a calibration as a string
   */

  protected String formatCalibration(Calibration cal) {

    SimpleDateFormat df;
    StringBuilder sb;

    df=new SimpleDateFormat("d MMMM yyyy");
    sb=new StringBuilder();

    sb.append(df.format(cal.getDate()));

    sb.append(' ');
    if(cal.getOffset().signum()>=0)
      sb.append('+');
    sb.append(cal.getOffset().toPlainString());

    sb.append(' ');
    sb.append(cal.getTemperature().toPlainString());
    sb.append((char)0x00B0);
    sb.append('C');

    return sb.toString();
  }


  /*
   * Return true if all cal data read OK
   */

  public boolean isReadCompleted() {
    return _readCompleted;
  }
}
