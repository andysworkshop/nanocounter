package uk.me.andybrown.nanocounter;

import android.os.Parcel;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
 * Class to hold raw frequency sample data
 */

public class FrequencySampleData {


  final protected long _referenceCounter;
  final protected long _referenceFrequency;
  final protected long _sampleCounter;
  final protected long _gateCounter;

  protected BigDecimal _sampleFrequency;


  /*
   * Constructor, always calculate the sample frequency = sampleCount*referenceFrequency/referenceCount
   */

  public FrequencySampleData(long referenceCounter,long referenceFrequency,long sampleCounter,long gateCounter) {

    _referenceCounter=referenceCounter;
    _referenceFrequency=referenceFrequency;
    _sampleCounter=sampleCounter;
    _gateCounter=gateCounter;
  }


  /*
   * Construct from a parcel
   */

  public FrequencySampleData(Parcel in) {

    _referenceCounter=in.readLong();
    _referenceFrequency=in.readLong();
    _sampleCounter=in.readLong();
    _gateCounter=in.readLong();
  }


  /*
   * Construct an error response
   */

  static public FrequencySampleData error() {
    return new FrequencySampleData(1,1,1,1);
  }


  /*
   * Get the sample frequency
   */

  public BigDecimal getSampleFrequency() {

    // check for error

    if(_referenceFrequency==1)
      return BigDecimal.ZERO;

    // lazy calculation of the frequency

    if(_sampleFrequency==null) {
      _sampleFrequency=BigDecimal.valueOf(_sampleCounter*_referenceFrequency)
              .setScale(Preferences.DEFAULT_SCALE)
              .divide(BigDecimal.valueOf(_referenceCounter),RoundingMode.HALF_UP);
    }

    return _sampleFrequency;
  }


  /*
   * Get the sample frequency with calibration
   */

  public BigDecimal getCalibratedSampleFrequency(BigDecimal calibrationOffset) {

    // check for error

    if(_referenceFrequency==1)
      return BigDecimal.ZERO;

    // lazy calculation of the frequency

    if(_sampleFrequency==null) {

      BigDecimal referenceFrequency=BigDecimal.valueOf(_referenceFrequency).add(calibrationOffset);

      _sampleFrequency=BigDecimal.valueOf(_sampleCounter)
              .multiply(referenceFrequency)
              .setScale(Preferences.DEFAULT_SCALE)
              .divide(BigDecimal.valueOf(_referenceCounter),RoundingMode.HALF_UP);
    }

    return _sampleFrequency;
  }


  /*
   * Calculate the frequency of the onboard reference when a calibrated 10Mhz standard is connected
   * to the sample input = 10000000*referenceCount/sampleCount
   */

  public BigDecimal getOnboardReferenceFrequency() {

    BigDecimal f;

    f=BigDecimal.valueOf(_referenceCounter*10000000).setScale(Preferences.DEFAULT_SCALE);
    f=f.divide(BigDecimal.valueOf(_sampleCounter),RoundingMode.HALF_UP);

    return f;
  }


  /*
   * Write to a parcel
   */

  public void writeToParcel(Parcel out) {

    out.writeLong(_referenceCounter);
    out.writeLong(_referenceFrequency);
    out.writeLong(_sampleCounter);
    out.writeLong(_gateCounter);
  }
}
