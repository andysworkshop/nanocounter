package uk.me.andybrown.nanocounter.commands;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.GregorianCalendar;

import uk.me.andybrown.nanocounter.Calibration;


/*
 * Response to the set gate counter command
 */

public class GetCalibrationResponse extends CommandResponse {

  private static final String LOGGER=GetCalibrationResponse.class.getName();

  protected final Calibration _calibration=new Calibration();


  /*
   * Constructor
   */

  GetCalibrationResponse(InputStream is) throws IOException {

    // call the base

    super(is);

    // read the values

    Calendar cal;
    byte[] date;
    int value,i,f;
    String str;

    // date

    cal=new GregorianCalendar();

    date=new byte[4];
    blockingRead(is,date);

    if(!Character.isDigit((char)date[0]) ||
       !Character.isDigit((char)date[1]) ||
       !Character.isDigit((char)date[2]) ||
       !Character.isDigit((char)date[3])) {

      Log.i(LOGGER,"Received empty calibration response, ignoring");
      blockingSkip(is,14);
      return;
    }

    cal.set(Calendar.YEAR,Integer.parseInt(new String(date)));

    date=new byte[2];
    blockingRead(is,date);
    cal.set(Calendar.MONTH,Integer.parseInt(new String(date))-1);

    blockingRead(is,date);
    cal.set(Calendar.DAY_OF_MONTH,Integer.parseInt(new String(date)));

    _calibration.setDate(cal.getTime());

    // temperature

    value=readInt16(is);
    _calibration.setTemperature(new BigDecimal(value).setScale(1).divide(BigDecimal.TEN));

    // offset

    i=readInt32(is);
    f=readInt32(is);
    str=Integer.toString(i)+'.'+Integer.toString(f);
    _calibration.setOffset(new BigDecimal(str));

    Log.i(LOGGER,"Received valid calibration response");
  }


  /*
   * Static creator
   */

  public static final Parcelable.Creator<GetCalibrationResponse> CREATOR=new Parcelable.Creator<GetCalibrationResponse>() {
    public GetCalibrationResponse createFromParcel(Parcel in) {
      return new GetCalibrationResponse(in);
    }
    public GetCalibrationResponse[] newArray(int size) {
      return new GetCalibrationResponse[size];
    }
  };


  /*
   * Parcelable constructor
   */

  public GetCalibrationResponse(Parcel in) {

    super(in);

    _calibration.readFromParcel(in);
  }


  /*
   * Write to parcel
   */

  @Override
  public void writeToParcel(Parcel out,int flags) {

    super.writeToParcel(out,flags);
    _calibration.writeToParcel(out);
  }


  /*
   * Get the calibration
   */

  public Calibration getCalibration() {
    return _calibration;
  }
}
