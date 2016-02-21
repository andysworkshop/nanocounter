// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import uk.me.andybrown.nanocounter.commands.GetCalibrationBitmapResponse;
import uk.me.andybrown.nanocounter.commands.GetCalibrationResponse;
import uk.me.andybrown.nanocounter.commands.GetMeasuredFrequencyResponse;
import uk.me.andybrown.nanocounter.commands.SetCalibrationResponse;
import uk.me.andybrown.nanocounter.workqueue.ConnectWorkItem;
import uk.me.andybrown.nanocounter.workqueue.GetMeasuredFrequencyWorkItem;
import uk.me.andybrown.nanocounter.workqueue.ResetPllWorkItem;
import uk.me.andybrown.nanocounter.workqueue.SetGateCounterWorkItem;
import uk.me.andybrown.nanocounter.workqueue.SetReferenceFilterWorkItem;
import uk.me.andybrown.nanocounter.workqueue.SetReferenceFrequencyClockWorkItem;
import uk.me.andybrown.nanocounter.workqueue.SetSampleFilterWorkItem;


/*
 * The main activity
 */

public class MainActivity extends AppCompatActivity implements SensorEventListener {

  private static final String LOGGER=MainActivity.class.getName();

  protected BluetoothService _service;
  protected ServiceConnection _serviceConnection;
  protected ScheduledExecutorService _executor;
  protected BroadcastReceiver _broadcastReceiver;
  protected SharedPreferences.OnSharedPreferenceChangeListener _preferenceChangeListener;
  protected int _lastSampleSequenceNumber;
  protected LastAnimator _lastAnimator;
  protected DecimalFormat _frequencyFormatter;      // not thread safe!
  protected LineChart _chart;
  protected YAxisValueFormatter _valueFormatter;
  protected SensorManager _sensorManager;
  protected Sensor _temperatureSensor;
  protected float _lastAmbientTemperature;
  protected BigDecimal _lastFrequency;
  protected BigDecimal _lastUncalibratedFrequency;
  protected BigDecimal _sumFrequency;
  protected BigDecimal _minimumFrequency;
  protected BigDecimal _maximumFrequency;
  protected long _sampleCount;
  protected CalibrationArray _calibrationArray;
  protected Calibration _activeCalibration;


  /*
   * Activity creation
   */

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    Intent intent;
    IntentFilter ifilter;

    Log.d(LOGGER,"Creating main activity");

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    // set the member controls

    createFrequencyFormatter();
    createChart();

    // create the calibration array

    _calibrationArray=new CalibrationArray();

    // set the last sample counter

    _lastSampleSequenceNumber=0;

    // create the broadcast receiver

    _broadcastReceiver=new BroadcastReceiver() {
      @Override
      public void onReceive(Context context,Intent intent) {

        switch(intent.getAction()) {

          case CustomIntent.LINK_STATUS:
            onLinkStatusChange(
                    LinkStatus.values()[intent.getIntExtra(CustomIntent.LINK_STATUS_EXTRA,LinkStatus.UNKNOWN.ordinal())]
            );
            break;

          case CustomIntent.COMMAND_FAILED:
            onCommandFailed(intent.getStringExtra(CustomIntent.COMMAND_FAILED_EXTRA));
            break;

          case CustomIntent.COMMAND_RESPONSE:
            onCommandResponse(intent.getParcelableExtra(CustomIntent.COMMAND_RESPONSE_EXTRA));
            break;

          case CustomIntent.ACTIVATE_CALIBRATION:
            onActivateCalibration();
            break;
        }
      }
    };

    // get the temperature sensor

    _lastAmbientTemperature=Float.MIN_VALUE;
    _sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
    _temperatureSensor=_sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

    // create the preferences listener

    createPreferencesListener();

    // create the 'last value' animator

    _lastAnimator=new LastAnimator(this);

    // reset statistics

    resetStatistics();

    // create filter for interesting values

    ifilter=new IntentFilter();
    ifilter.addAction(CustomIntent.LINK_STATUS);
    ifilter.addAction(CustomIntent.COMMAND_FAILED);
    ifilter.addAction(CustomIntent.COMMAND_RESPONSE);
    ifilter.addAction(CustomIntent.ACTIVATE_CALIBRATION);

    registerReceiver(_broadcastReceiver,ifilter);

    // create the service connection

    _serviceConnection=new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName className,IBinder binder) {

        // connected to the sevice, remember the reference and get connected

        _service=((BluetoothServiceBinder)binder).getService();
        _service.addWorkItem(new ConnectWorkItem(_service));
      }

      @Override
      public void onServiceDisconnected(ComponentName arg0) {
        _service=null;
      }
    };

    // fill in all the table values

    fillTable();

    // create the service

    intent=new Intent(this,BluetoothService.class);
    bindService(intent,_serviceConnection,Context.BIND_AUTO_CREATE);

    // start the polling executor

    startTimer();
  }


  /*
   * Create the chart object
   */

  protected void createChart() {

    LineData lineData;

    _valueFormatter=new YAxisValueFormatter() {
      @Override
      public String getFormattedValue(float value,YAxis yAxis) {

        String[] strings;

        strings=new String[2];
        formatFrequency(new BigDecimal(value),strings);
        return strings[0];
      }
    };

    _chart=(LineChart)findViewById(R.id.chart_layout);
    _chart.getLegend().setEnabled(false);
    _chart.setDescription("");
    _chart.getAxisLeft().setDrawGridLines(false);
    _chart.getAxisRight().setDrawGridLines(false);
    _chart.getAxisLeft().setShowOnlyMinMax(true);
    _chart.getAxisRight().setShowOnlyMinMax(true);
    _chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
    _chart.getAxisLeft().setValueFormatter(_valueFormatter);
    _chart.getAxisRight().setValueFormatter(_valueFormatter);
    _chart.getAxisLeft().setStartAtZero(false);
    _chart.getAxisRight().setStartAtZero(false);
    _chart.getXAxis().setDrawGridLines(false);

    lineData=new LineData();
    _chart.setData(lineData);
  }


  /*
   * activity paused
   */

  @Override
  public void onPause()  {
    super.onPause();
    _sensorManager.unregisterListener(this);
  }


  /*
   * activity resumed
   */

  @Override
  public void onResume () {

    super.onResume();
    _sensorManager.registerListener(this,_temperatureSensor,SensorManager.SENSOR_DELAY_NORMAL);
  }


  /*
   * Activity destroyed
   */

  @Override
  public void onDestroy() {

    Log.i(LOGGER,"Main activity is being destroyed");

    if(_serviceConnection!=null)
      unbindService(_serviceConnection);

    // stop the timer

    stopTimer();

    // unregister receiver

    unregisterReceiver(_broadcastReceiver);

    // call the base class

    super.onDestroy();
  }


  /*
   * sensor accuracy changed
   */

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
  }


  /*
   * sensor value changed
   */

  @Override
  public final void onSensorChanged(SensorEvent event) {
    _lastAmbientTemperature=event.values[0];
  }


  /*
   * Create a listener for changes to the shared preferences
   */

  protected void createPreferencesListener() {

    _preferenceChangeListener=new SharedPreferences.OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {

        // always do the display updates

        createFrequencyFormatter();
        fillTable();

        // check for items that need a work item sending to nanocounter

        if(Preferences.GATE_SECONDS.equals(key)) {
          _service.addWorkItem(new SetGateCounterWorkItem(_service,Preferences.getGateCounter(MainActivity.this)));
          fillGateTime();
          startTimer();
        }
        else if(Preferences.REFERENCE_FREQUENCY_SOURCE.equals(key)) {
          _service.addWorkItem(new SetReferenceFrequencyClockWorkItem(_service,Preferences.getReferenceFrequencySource(MainActivity.this)));
          fillReferenceFrequencySource();
          startTimer();
        }
        else if(Preferences.REFERENCE_FILTER.equals(key)) {
          _service.addWorkItem(new SetReferenceFilterWorkItem(_service,Preferences.getReferenceFilter(MainActivity.this)));
          fillReferenceFilter();
        }
        else if(Preferences.SAMPLE_FILTER.equals(key)) {
          _service.addWorkItem(new SetReferenceFilterWorkItem(_service,Preferences.getSampleFilter(MainActivity.this)));
          fillSampleFilter();
        }
      }
    };

    PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(_preferenceChangeListener);
  }


  /*
   * Start the polling timer. The polling timer is 3/4 of the gate counter time.
   * e.g. a gate counter time of 1s = polling timer of 750ms
   */

  protected void startTimer() {

    double time;

    // shutdown previous

    stopTimer();

    // calculate the polling time

    time=((double)Preferences.getGateCounter(this)*750.0)/(double)Preferences.getReferenceFrequency(this);

    // start a new timer

    _executor=Executors.newSingleThreadScheduledExecutor();
    _executor.scheduleWithFixedDelay(
            new Runnable() {
              @Override
              public void run() {

                // if connected then schedule a poll

                if(_service.getLinkStatus()==LinkStatus.CONNECTED)
                  _service.addWorkItem(new GetMeasuredFrequencyWorkItem(_service));
              }
            },
            (long)time,
            (long)time,
            TimeUnit.MILLISECONDS);
  }


  /*
   * Stop the timer
   */

  protected void stopTimer() {
    if(_executor!=null)
      _executor.shutdown();
  }


  /*
   * The link status has changed
   */

  protected void onLinkStatusChange(final LinkStatus status) {

    final String str;
    final boolean showMain,retry;

    Log.d(LOGGER,"Updating UI with link status "+status.toString());

    // check

    switch(status) {

      case NOT_SUPPORTED:
        str="Bluetooth not supported";
        showMain=retry=false;
        break;

      case ENABLED:
        str="Bluetooth is enabled";
        showMain=retry=false;
        break;

      case DISABLED:
        str="Bluetooth is disabled";
        showMain=false;
        retry=true;
        break;

      case NOT_PAIRED:
        str="Not paired with Nanocounter";
        showMain=false;
        retry=true;
        break;

      case CONNECTING:
        str="Initiating connection";
        showMain=retry=false;
        break;

      case CONNECTION_FAILED:
        str="Connection failed";
        retry=true;
        showMain=false;
        break;

      case CONNECTED:
        showMain=true;
        retry=false;
        str="Connected to Nanocounter";
        onConnected();
        break;

      default:
        str=null;
        showMain=retry=false;
        break;
    }

    // update the UI

    runOnUiThread(new Runnable() {

      @Override
      public void run() {

        // show/hide the connection/progress icons

        findViewById(R.id.connecting_icons).setVisibility(retry ? View.VISIBLE : View.GONE);
        findViewById(R.id.connecting_progress).setVisibility(retry ? View.GONE : View.VISIBLE);

        // set the connection status text

        ((TextView)findViewById(R.id.connecting_status)).setText(str);

        // show/hide the main views based on the connection status

        findViewById(R.id.main_frame).setVisibility(showMain ? View.VISIBLE : View.GONE);
        findViewById(R.id.connecting_frame).setVisibility(showMain ? View.GONE : View.VISIBLE);
      }
    });
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main,menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id=item.getItemId();

    //noinspection SimplifiableIfStatement
    if(id==R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  /*
   * We have just connected, issue the startup commands
   */

  void onConnected() {

    _service.addWorkItem(new SetGateCounterWorkItem(_service,Preferences.getGateCounter(this)));
    _service.addWorkItem(new SetReferenceFrequencyClockWorkItem(_service,Preferences.getReferenceFrequencySource(this)));
    _service.addWorkItem(new SetReferenceFilterWorkItem(_service,Preferences.getReferenceFilter(this)));
    _service.addWorkItem(new SetSampleFilterWorkItem(_service,Preferences.getSampleFilter(this)));

    _calibrationArray.startRequesting(_service);
  }


  /*
   * Command failed callback
   */

  void onCommandFailed(final String reason) {

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Snackbar.make(findViewById(android.R.id.content),reason,Snackbar.LENGTH_LONG);
      }
    });
  }


  /*
   * Retry connection icon clicked
   */

  public void onClickRetryConnection(View view) {
    _service.addWorkItem(new ConnectWorkItem(_service));
  }


  /*
   * process the command response
   */

  protected void onCommandResponse(Parcelable p) {

    if(p instanceof GetMeasuredFrequencyResponse)
      onGetMeasuredFrequencyResponse((GetMeasuredFrequencyResponse)p);
    else if(p instanceof GetCalibrationBitmapResponse)
      _calibrationArray.onCalibrationBitmapReceived(_service,(GetCalibrationBitmapResponse)p);
    else if(p instanceof GetCalibrationResponse)
      _calibrationArray.onCalibrationReceived(_service,(GetCalibrationResponse)p);
    else if(p instanceof SetCalibrationResponse)
      onSetCalibrationResponse();
  }


  /*
   * response to set calibration received
   */

  protected void onSetCalibrationResponse() {
    Toast.makeText(this,"Calibration saved and applied",Toast.LENGTH_SHORT).show();
  }


  /*
   * The active calibration has been received
   */

  protected void onActivateCalibration() {

    ListView lv;
    String[] values;
    ArrayAdapter<String> adapter;

    // if there is a calibration, use the most recent

    _activeCalibration=_calibrationArray.getRecentCalibration();

    // the displayed frequency is now black

    ((TextView)findViewById(R.id.frequency_text))
            .setTextColor(getResources().getColor(R.color.frequency_text));

    // fill the calibration history list

    lv=(ListView)findViewById(R.id.calibration_list);
    values=_calibrationArray.getHistoryArray();

    adapter=new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            values);

    // assign adapter to ListView

    lv.setAdapter(adapter);
  }


  /*
   * Process a new frequency response
   */

  protected void onGetMeasuredFrequencyResponse(final GetMeasuredFrequencyResponse response) {

    final BigDecimal f;

    // discard if not more recent

    if(response.getSampleSequenceNumber()==_lastSampleSequenceNumber)
      return;

    // get the measured frequency and apply calibration if using the onboard reference
    // and the 'apply calibration' preference is set

    _lastUncalibratedFrequency=response.getFrequency();

    if(_activeCalibration!=null &&
            Preferences.getReferenceFrequencySource(this)==ReferenceFrequencySource.INTERNAL_10M &&
            Preferences.getApplyCalibration(this)) {
      f=_lastUncalibratedFrequency.add(_activeCalibration.getOffset());
    }
    else {
      f=_lastUncalibratedFrequency;
    }

    // add to the UI

    runOnUiThread(new Runnable() {
      @Override
      public void run() {

        int compare, temperature, places;
        double duration;
        String[] frequencyAndUnits;
        StringBuilder sb;

        frequencyAndUnits=new String[2];
        formatFrequency(f,frequencyAndUnits);

        // double check since there's been a thread jump since we first looked

        if(response.getSampleSequenceNumber()==_lastSampleSequenceNumber)
          return;

        // set the frequency and units

        ((TextView)findViewById(R.id.frequency_text)).setText(frequencyAndUnits[0]);
        ((TextView)findViewById(R.id.frequency_units)).setText(frequencyAndUnits[1]);

        if(_lastFrequency!=null) {

          places=Preferences.getFrequencyDecimalPlaces(MainActivity.this);

          // update the last change icon at the desired scale

          compare=f.setScale(places,RoundingMode.HALF_UP).compareTo(_lastFrequency.setScale(places,RoundingMode.HALF_UP));
          duration=((double)Preferences.getGateCounter(MainActivity.this)*500.0)/(double)Preferences.getReferenceFrequency(MainActivity.this);
          if(duration>500)
            duration=500;

          _lastAnimator.animate(
                  compare>0 ? R.drawable.up : compare<0 ? R.drawable.down : R.drawable.no_change,
                  (int)duration);

        }

        // store references to last values

        _lastSampleSequenceNumber=response.getSampleSequenceNumber();
        _lastFrequency=f;

        // update the temperature

        if((temperature=response.getTemperature())!=0) {
          sb=new StringBuilder(Integer.toString(temperature/1000));   // integer part
          sb.append('.');                                             // decimal point
          sb.append((temperature%1000)/100);                        // first fractional digit
          ((TextView)findViewById(R.id.temperature_text)).setText(sb.toString());
        }

        // update the statistics

        updateStatistics(f);

        // update the target frequency values

        updateTargetFrequency();

        // update the chart

        updateChart();
      }
    });
  }


  /*
   * Update the line chart
   */

  protected void updateChart() {

    LineData data;
    LineDataSet set;
    Entry e;
    float minValue,maxValue;

    if((data=_chart.getData())!=null) {

      if((set=data.getDataSetByIndex(0))==null) {
        set=createChartDataSet();
        data.addDataSet(set);
      }

      // the dataset implementation is quite inefficient at removals so
      // the max value should not be very large

      while(set.getEntryCount()>=Preferences.getMaxChartValues(this))
        set.removeFirst();

      // create the data entry

      e=new Entry(_lastFrequency.floatValue(),set.getEntryCount());

      // add to the chart

      data.addXValue(Long.toString(_sampleCount));
      data.addEntry(e,0);

      minValue=_minimumFrequency.floatValue()-10;
      maxValue=_maximumFrequency.floatValue()+10;

      _chart.getAxisLeft().setAxisMinValue(minValue);
      _chart.getAxisRight().setAxisMinValue(minValue);

      _chart.getAxisLeft().setAxisMaxValue(maxValue);
      _chart.getAxisRight().setAxisMaxValue(maxValue);

      // notify changed

      _chart.notifyDataSetChanged();
      _chart.invalidate();
    }
  }


  /*
   * Create the chart data set
   */

  protected LineDataSet createChartDataSet() {

    LineDataSet set;

    set=new LineDataSet(null,"Frequency chart");

    set.setAxisDependency(YAxis.AxisDependency.LEFT);
    set.setColor(ColorTemplate.getHoloBlue());
    set.setCircleColor(Color.argb(0xff,0x40,0x40,0x40));
    set.setLineWidth(2);
    set.setCircleSize(2);
    set.setFillAlpha(65);
    set.setFillColor(ColorTemplate.getHoloBlue());
    set.setHighLightColor(Color.rgb(244,117, 117));
    set.setValueTextColor(Color.WHITE);
    set.setValueTextSize(9f);
    set.setDrawValues(false);

    return set;
  }


  /*
   * Update the target frequency
   */

  protected void updateTargetFrequency() {

    int visibility,comp;
    BigDecimal targetFrequency,average,ppm_low,ppm_high;
    String str;
    TextView compText;

    try {

      if(Preferences.getIdealFrequencyFlag(this)&&_sampleCount>0) {

        // format the target frequency and units

        targetFrequency=new BigDecimal(Preferences.getIdealFrequency(this)).setScale(Preferences.DEFAULT_SCALE);
        ((TextView)findViewById(R.id.target_frequency)).setText(formatFrequency(targetFrequency));

        switch(Preferences.getIdealFrequencySource(this)) {

          case MIN_MAX:

            // work out the two PPM values

            ppm_low=calculatePpm(_minimumFrequency,targetFrequency);
            ppm_high=calculatePpm(_maximumFrequency,targetFrequency);
            str=String.format("%s, %s",formatPpm(ppm_low),formatPpm(ppm_high));
            comp=-1;
            break;

          case MIN:
            ppm_low=calculatePpm(_minimumFrequency,targetFrequency);
            str=String.format("%s",formatPpm(ppm_low));
            comp=calculateCompensation(_minimumFrequency,targetFrequency);
            break;

          case MAX:
            ppm_high=calculatePpm(_maximumFrequency,targetFrequency);
            str=String.format("%s",formatPpm(ppm_high));
            comp=calculateCompensation(_maximumFrequency,targetFrequency);
            break;

          case AVERAGE:
            average=_sumFrequency.divide(new BigDecimal(_sampleCount),BigDecimal.ROUND_HALF_UP);
            ppm_high=calculatePpm(average,targetFrequency);
            str=String.format("%s",formatPpm(ppm_high));
            comp=calculateCompensation(average,targetFrequency);
            break;

          default:
            ppm_high=calculatePpm(_lastFrequency,targetFrequency);
            str=String.format("%s",formatPpm(ppm_high));
            comp=calculateCompensation(_lastFrequency,targetFrequency);
            break;
        }

        // set the offset ppm text

        ((TextView)findViewById(R.id.target_frequency_offset)).setText(str);

        // set the compensation value if possible

        compText=(TextView)findViewById(R.id.target_frequency_compensate);

        if(comp==-1)
          compText.setVisibility(View.INVISIBLE);
        else {
          compText.setVisibility(View.VISIBLE);
          compText.setText(String.format("%+dms/hr",comp));
        }

        visibility=View.VISIBLE;
      } else
        visibility=View.INVISIBLE;
    }
    catch(Exception ex) {
      Log.e(LOGGER,ex.toString());
      visibility=View.INVISIBLE;
    }

    findViewById(R.id.target_frequency_holder).setVisibility(visibility);
  }


  /*
   * Format a ppm value as integer +/-xxxppm/ppb
   */

  protected String formatPpm(BigDecimal ppm) {

    if(ppm.abs().intValue()==0)
      return String.format("%+dppb",ppm.multiply(Constants.THOUSAND).intValue());
    else
      return String.format("%+dppm",ppm.intValue());
  }


  /*
   * Calculate a ppm value from the actual and target frequencies
   */

  protected BigDecimal calculatePpm(BigDecimal actual,BigDecimal target) {

    BigDecimal diff,divider,ppm;

    divider=target.divide(Constants.MILLION,RoundingMode.HALF_UP);
    diff=actual.subtract(target);
    ppm=diff.divide(divider,RoundingMode.HALF_UP);

    return ppm;
  }


  /*
   * Calculate the number of milliseconds compensation per hour if a clock
   * were to be generated from this
   */

  protected int calculateCompensation(BigDecimal actual,BigDecimal target) {

    BigDecimal value;

    value=target.divide(actual,BigDecimal.ROUND_HALF_UP);
    value=Constants.MSPERHOUR.multiply(value);
    value=Constants.MSPERHOUR.subtract(value);

    return value.intValue();
  }


  /*
   * Fill all the values in the table
   */

  protected void fillTable() {
    fillGateTime();
    fillReferenceFrequencySource();
    fillReferenceFilter();
    fillSampleFilter();
  }

  protected void fillGateTime() {

    double time;
    StringBuilder sb;

    time=(double)Preferences.getGateCounter(this)/(double)Preferences.getReferenceFrequency(this);

    DecimalFormat df = (DecimalFormat)NumberFormat.getInstance();
    df.setMaximumFractionDigits(2);

    sb=new StringBuilder(df.format(time));
    sb.append("s at ");
    sb.append(formatFrequency(BigDecimal.valueOf(Preferences.getReferenceFrequency(this)).setScale(Preferences.DEFAULT_SCALE)));

    ((TextView)findViewById(R.id.gate_time_value)).setText(sb.toString());
  }

  protected void fillReferenceFrequencySource() {

    Resources res;
    String str;
    ReferenceFrequencySource rfs;

    res=getResources();
    rfs=Preferences.getReferenceFrequencySource(this);

    if(rfs==ReferenceFrequencySource.INTERNAL_10M)
      str=res.getString(R.string.internal_10MHz);
    else
      str=res.getString(R.string.external_10MHz);

    ((TextView)findViewById(R.id.reference_frequency_source)).setText(str);
  }

  protected void fillSampleFilter() {
    fillFilter(Preferences.getSampleFilter(this),R.id.sample_filter);
  }

  protected void fillReferenceFilter() {

    if(Preferences.getReferenceFrequencySource(this)==ReferenceFrequencySource.INTERNAL_10M)
      ((TextView)findViewById(R.id.reference_filter)).setText(getResources().getString(R.string.not_applicable));
    else
      fillFilter(Preferences.getReferenceFilter(this),R.id.reference_filter);
  }

  protected void fillFilter(FilterBandwidth fb,int textId) {

    Resources res;
    int resId;

    switch(fb) {
      case BW_500:
        resId=R.string.filter_500M;
        break;
      case BW_160:
        resId=R.string.filter_160M;
        break;
      case BW_50:
        resId=R.string.filter_50M;
        break;
      default:
        resId=R.string.filter_full;
        break;
    }

    res=getResources();
    ((TextView)findViewById(textId)).setText(res.getString(resId));
  }


  /*
   * Create a new frequency formatting class
   */

  protected void createFrequencyFormatter() {

    _frequencyFormatter=(DecimalFormat)NumberFormat.getInstance();
    _frequencyFormatter.setMinimumIntegerDigits(1);
    _frequencyFormatter.setDecimalSeparatorAlwaysShown(false);

    if(Preferences.getFrequencyGroupsFlag(this)) {
      _frequencyFormatter.setGroupingUsed(true);
      _frequencyFormatter.setGroupingSize(3);
    }
    else
      _frequencyFormatter.setGroupingUsed(false);

    _frequencyFormatter.setRoundingMode(RoundingMode.HALF_UP);
    _frequencyFormatter.setMaximumFractionDigits(Preferences.getFrequencyDecimalPlaces(this));
  }


  /*
   * Format a frequency for display with units postfix
   */

  protected String formatFrequency(BigDecimal f) {

    int divider;
    StringBuilder sb;

    // get the frequency and divide for display if required

    if((divider=Preferences.getFrequencyDivider(this))!=1)
      f=f.divide(BigDecimal.valueOf(divider).setScale(Preferences.DEFAULT_SCALE));

    sb=new StringBuilder(_frequencyFormatter.format(f));

    switch(divider) {

      case 1:
        sb.append("Hz");
        break;

      case 1000:
        sb.append("kHz");
        break;

      case 1000000:
        sb.append("MHz");
        break;
    }

    return sb.toString();
  }


  /*
   * Format a frequency for display with units postfix separated
   */

  protected void formatFrequency(BigDecimal f,String[] output) {

    int divider;

    // get the frequency and divide for display if required

    divider=Preferences.getFrequencyDivider(this);

    if(!f.equals(BigInteger.ZERO) && divider!=1)
      f=f.divide(BigDecimal.valueOf(divider).setScale(Preferences.DEFAULT_SCALE));

    output[0]=_frequencyFormatter.format(f);

    switch(divider) {

      case 1000:
        output[1]=("kHz");
        break;

      case 1000000:
        output[1]=("MHz");
        break;

      default:
        output[1]=("Hz");
        break;
    }
  }


  /*
   * Reset statistics
   */

  protected void resetStatistics() {

    LineData data;
    LineDataSet dataSet;

    _sampleCount=0;

    if((data=_chart.getData())!=null && (dataSet=data.getDataSetByIndex(0))!=null)
      dataSet.clear();

    _chart.getXAxis().getValues().clear();
  }


  /*
   * Update the statistics
   */

  protected void updateStatistics(BigDecimal f) {

    BigDecimal avg;

    // update the counters

    if(_sampleCount==0)
      _minimumFrequency=_maximumFrequency=_sumFrequency=avg=f;
    else {

      if(f.compareTo(_minimumFrequency)<0)
        _minimumFrequency=f;
      else if(f.compareTo(_maximumFrequency)>0)
        _maximumFrequency=f;

      _sumFrequency=_sumFrequency.add(f);

      try {
        avg=_sumFrequency.divide(BigDecimal.valueOf(_sampleCount+1).setScale(Preferences.DEFAULT_SCALE),RoundingMode.HALF_UP);
      }
      catch(Exception ex) {
        Log.e(LOGGER,ex.toString());
        avg=BigDecimal.ZERO;
      }
    }

    _sampleCount++;

    // write to the display

    ((TextView)findViewById(R.id.samples_text)).setText(Long.toString(_sampleCount));
    ((TextView)findViewById(R.id.minimum_text)).setText(formatFrequency(_minimumFrequency));
    ((TextView)findViewById(R.id.maximum_text)).setText(formatFrequency(_maximumFrequency));
    ((TextView)findViewById(R.id.average_text)).setText(formatFrequency(avg));
  }


  /*
   * Clear clicked
   */

  public void onClickClear(MenuItem item) {

    AlertDialog dialog;
    AlertDialog.Builder builder;

    // build the dialog

    builder=new AlertDialog.Builder(this);
    builder.setTitle(R.string.confirm);
    builder.setMessage(R.string.really_clear);

    // ok button

    builder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog,int item) {
        resetStatistics();
        dialog.dismiss();
      }
    });

    // cancel button

    builder.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog,int item) {
        dialog.dismiss();
      }
    });

    dialog=builder.create();
    dialog.show();
  }


  /*
   * Reset PLL clicked
   */

  public void onClickResetPll(MenuItem item) {

    AlertDialog dialog;
    AlertDialog.Builder builder;

    // build the dialog

    builder=new AlertDialog.Builder(this);
    builder.setTitle(R.string.confirm);
    builder.setMessage(R.string.really_reset);

    // ok button

    builder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int item) {
        _service.addWorkItem(new ResetPllWorkItem(_service));
        dialog.dismiss();
      }});

    // cancel button

    builder.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog,int item) {
        dialog.dismiss();
      }});

    dialog=builder.create();
    dialog.show();
  }


  /*
   * Settings icon/menu item clicked
   */

  public void onClickSettingsIcon(View view) {
    onClickSettings(null);
  }
  public void onClickSettings(MenuItem item) {

    Intent intent;

    intent=new Intent(this,SettingsActivity.class);
    startActivity(intent);
  }


  /*
   * enable/disable menu items
   */

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {

    boolean canApplyCalibration;

    menu.findItem(R.id.action_reset_pll).setEnabled(
            _service!=null&&_service.getLinkStatus()==LinkStatus.CONNECTED);

    menu.findItem(R.id.action_clear).setEnabled(
            _service!=null&&_service.getLinkStatus()==LinkStatus.CONNECTED);

    menu.findItem(R.id.action_calibrate).setEnabled(
            findViewById(R.id.calibration_frame).getVisibility()==View.GONE && _calibrationArray.isReadCompleted());


    canApplyCalibration=_activeCalibration!=null && Preferences.getReferenceFrequencySource(this)==ReferenceFrequencySource.INTERNAL_10M;

    menu.findItem(R.id.action_apply_calibration).setEnabled(canApplyCalibration);
    menu.findItem(R.id.action_apply_calibration).setChecked(canApplyCalibration && Preferences.getApplyCalibration(this));

    return true;
  }


  /*
   * User clicked back button to exit the application
   */

  @Override
  public void onBackPressed() {

    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog,int which) {
        finish();
      }
    });

    alertDialog.setNegativeButton("No",null);

    alertDialog.setMessage("Do you want to exit?");
    alertDialog.setTitle(R.string.app_name);
    alertDialog.show();
  }


  /*
   * About menu item clicked
   */

  public void onClickAbout(MenuItem item) {

    Intent intent;

    intent=new Intent(this,AboutActivity.class);
    startActivity(intent);
  }


  /*
   * Calibrate option clicked
   */

  public void onClickCalibrate(MenuItem item) {

    // show the calibration controls

    findViewById(R.id.table_holder).setVisibility(View.GONE);
    findViewById(R.id.calibration_frame).setVisibility(View.VISIBLE);
    findViewById(R.id.calibrate_auto_temperature).setEnabled(_lastAmbientTemperature!=Float.MIN_VALUE);
  }


  /*
   * Apply calibration toggle option clicked
   */

  public void onClickApplyCalibration(MenuItem item) {

    SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();

    editor.putBoolean(Preferences.APPLY_CALIBRATION_FLAG,!Preferences.getApplyCalibration(this));
    editor.commit();
  }


  /*
   * Close the calibration view
   */

  public void onClickCloseCalibration(View view) {

    // hide the calibration controls

    findViewById(R.id.calibration_frame).setVisibility(View.GONE);
    findViewById(R.id.table_holder).setVisibility(View.VISIBLE);
  }


  /*
   * Set auto-calibration value
   */

  public void onClickAutoCalibration(View view) {

    BigDecimal ideal,diff;
    String[] s;

    if(_sampleCount>0) {

      ideal=new BigDecimal(Preferences.getIdealFrequency(this)).setScale(Preferences.DEFAULT_SCALE);
      diff=ideal.subtract(_lastUncalibratedFrequency);

      s=new String[2];
      formatFrequency(diff,s);

      ((EditText)findViewById(R.id.calibrate_text)).setText(s[0]);
    }
  }


  /*
   * Set the calibration temperature from the sensor (if available)
   */

  public void onClickAutoCalibrationTemperature(View view) {

    String s;

    if(_lastAmbientTemperature!=Float.MIN_VALUE) {
      s=String.format("%.1f",_lastAmbientTemperature);
      ((EditText)findViewById(R.id.calibrate_temperature_text)).setText(s);
    }
  }


  /*
   * Save the calibration data
   */

  public void onClickSaveCalibration(View view) {

    Calibration cal;
    BigDecimal temperature,offset;
    String s;

    try {

      // create a new calibration with the current date

      cal=new Calibration();
      cal.setDate(new Date());

      // offset

      s=((EditText)findViewById(R.id.calibrate_text)).getText().toString();
      if(s==null || s.length()==0)
        throw new Exception("Please enter an offset");

      offset=new BigDecimal(s);
      cal.setOffset(offset);

      // temperature

      s=((EditText)findViewById(R.id.calibrate_temperature_text)).getText().toString();
      if(s==null || s.length()==0)
        throw new Exception("Please enter a temperature");

      temperature=new BigDecimal(s);
      cal.setTemperature(temperature);

      // initiate the save

      _calibrationArray.save(_service,cal);
    }
    catch(Exception ex) {
      Log.e(LOGGER,ex.toString());
      Toast.makeText(this,ex.getMessage(),Toast.LENGTH_SHORT).show();
    }
  }
}
