package uk.me.andybrown.nanocounter;

/*
 * Preference string constants
 */

import android.content.Context;
import android.preference.PreferenceManager;

import java.math.BigDecimal;


/*
 * Preferences helper class
 */

public class Preferences {

  /*
   * The default BigDecimal scale (decimal places) for calculations
   */

  public static final int DEFAULT_SCALE = 10;


  /*
   * Preference identifiers
   */

  protected static final String BLUETOOTH_ID = "BLUETOOTH_ID";
  protected static final String GATE_SECONDS = "GATE_SECONDS";
  protected static final String REFERENCE_FREQUENCY = "REFERENCE_FREQUENCY";
  protected static final String FREQUENCY_DIVIDER = "FREQUENCY_DIVIDER";
  protected static final String FREQUENCY_DECIMAL_PLACES = "FREQUENCY_DECIMAL_PLACES";
  protected static final String FREQUENCY_GROUPS_FLAG = "FREQUENCY_GROUPS_FLAG";
  protected static final String REFERENCE_FREQUENCY_SOURCE = "REFERENCE_FREQUENCY_SOURCE";
  protected static final String REFERENCE_FILTER = "REFERENCE_FILTER";
  protected static final String SAMPLE_FILTER = "SAMPLE_FILTER";
  protected static final String IDEAL_FREQUENCY = "IDEAL_FREQUENCY";
  protected static final String IDEAL_FREQUENCY_FLAG = "IDEAL_FREQUENCY_FLAG";
  protected static final String IDEAL_FREQUENCY_SOURCE = "IDEAL_FREQUENCY_SOURCE";
  protected static final String MAX_CHART_VALUES = "MAX_CHART_VALUES";
  protected static final String APPLY_CALIBRATION_FLAG = "APPLY_CALIBRATION";
  protected static final String FREQUENCY_FIXED_DECIMAL_PLACES_FLAG = "FREQUENCY_FIXED_DECIMAL_PLACES_FLAG";

  /*
   * Preference defaults
   */

  protected static final String DEFAULT_BLUETOOTH_ID = "HC-06";
  protected static final String DEFAULT_REFERENCE_FREQUENCY = "200000000";
  protected static final String DEFAULT_GATE_SECONDS = "1";
  protected static final String DEFAULT_FREQUENCY_DIVIDER = "1";
  protected static final String DEFAULT_FREQUENCY_DECIMAL_PLACES = "0";
  protected static final boolean DEFAULT_FREQUENCY_GROUPS_FLAG = true;
  protected static final String DEFAULT_REFERENCE_FREQUENCY_SOURCE = "0";
  protected static final String DEFAULT_REFERENCE_FILTER = "0";
  protected static final String DEFAULT_SAMPLE_FILTER = "0";
  protected static final boolean DEFAULT_IDEAL_FREQUENCY_FLAG = true;
  protected static final String DEFAULT_IDEAL_FREQUENCY = "8000000";
  protected static final String DEFAULT_IDEAL_FREQUENCY_SOURCE = "4";
  protected static final String DEFAULT_MAX_CHART_VALUES = "3600";
  protected static final boolean DEFAULT_APPLY_CALIBRATION_FLAG = true;
  protected static final boolean DEFAULT_FREQUENCY_FIXED_DECIMAL_PLACES_FLAG = false;

  /*
   * Get the bluetooth device id
   */

  public static String getBluetoothId(Context context) {

    // get the device name from the preferences object or default to HC-06

    return PreferenceManager.getDefaultSharedPreferences(context)
           .getString(BLUETOOTH_ID,DEFAULT_BLUETOOTH_ID);
  }


  /*
   * Get the fixed decimal places flag
   */

  public static boolean getFrequencyFixedDecimalPlacesFlag(Context context) {

    return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(FREQUENCY_FIXED_DECIMAL_PLACES_FLAG,DEFAULT_FREQUENCY_FIXED_DECIMAL_PLACES_FLAG);
  }


  /*
   * Get the gate counter
   */

  public static long getGateCounter(Context context) {

    BigDecimal gateSeconds,referenceFrequency;
    long gateCounter;

    referenceFrequency=new BigDecimal(getReferenceFrequency(context));
    gateSeconds=new BigDecimal(
            PreferenceManager.getDefaultSharedPreferences(context)
            .getString(GATE_SECONDS,DEFAULT_GATE_SECONDS));

    // gate counter = reference_frequency * gate_time

    gateCounter=referenceFrequency.multiply(gateSeconds).longValue();
    return gateCounter;
  }


  /*
   * Get the reference frequency
   */

  public static int getReferenceFrequency(Context context) {

    return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(REFERENCE_FREQUENCY,DEFAULT_REFERENCE_FREQUENCY)
    );
  }


  /*
   * Get the frequency divider
   */

  public static int getFrequencyDivider(Context context) {

    return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
            .getString(FREQUENCY_DIVIDER,DEFAULT_FREQUENCY_DIVIDER)
    );
  }


  /*
   * Get the frequency decimal places
   */

  public static int getFrequencyDecimalPlaces(Context context) {

    return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
            .getString(FREQUENCY_DECIMAL_PLACES,DEFAULT_FREQUENCY_DECIMAL_PLACES)
    );
  }


  /*
   * Get the frequency groups flag
   */

  public static boolean getFrequencyGroupsFlag(Context context) {

    return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(FREQUENCY_GROUPS_FLAG,DEFAULT_FREQUENCY_GROUPS_FLAG);
  }


  /*
   * Get the reference frequency source
   */

  public static ReferenceFrequencySource getReferenceFrequencySource(Context context) {

    int value;

    value=Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
            .getString(REFERENCE_FREQUENCY_SOURCE,DEFAULT_REFERENCE_FREQUENCY_SOURCE)
    );

    return ReferenceFrequencySource.fromValue(value);
  }


  /*
   * Get the reference filter
   */

  public static FilterBandwidth getReferenceFilter(Context context) {

    int value;

    value=Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
            .getString(REFERENCE_FILTER,DEFAULT_REFERENCE_FILTER)
    );

    return FilterBandwidth.fromValue(value);
  }


  /*
   * Get the sample filter
   */

  public static FilterBandwidth getSampleFilter(Context context) {

    int value;

    value=Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
            .getString(SAMPLE_FILTER,DEFAULT_SAMPLE_FILTER)
    );

    return FilterBandwidth.fromValue(value);
  }


  /*
   * Get the ideal frequency flag
   */

  public static boolean getIdealFrequencyFlag(Context context) {

    return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(IDEAL_FREQUENCY_FLAG,DEFAULT_IDEAL_FREQUENCY_FLAG);
  }


  /*
   * Get the ideal frequency
   */

  public static int getIdealFrequency(Context context) {

    return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(IDEAL_FREQUENCY,DEFAULT_IDEAL_FREQUENCY)
    );
  }


  /*
   * Get the ideal frequency source
   */

  public static IdealFrequencySource getIdealFrequencySource(Context context) {

    int value;

    value=Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(IDEAL_FREQUENCY_SOURCE,DEFAULT_IDEAL_FREQUENCY_SOURCE)
    );

    return IdealFrequencySource.fromValue(value);
  }


  /*
   * Get the maximum number of values in a chart
   */

  protected static int getMaxChartValues(Context context) {
    return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
            .getString(MAX_CHART_VALUES,DEFAULT_MAX_CHART_VALUES));
  }


  /*
   * Get the apply calibration flag
   */

  public static boolean getApplyCalibration(Context context) {

    return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(APPLY_CALIBRATION_FLAG,DEFAULT_APPLY_CALIBRATION_FLAG);
  }
}
