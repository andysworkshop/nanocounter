package uk.me.andybrown.nanocounter.commands;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

import uk.me.andybrown.nanocounter.Preferences;


/*
 * Response to the set gate counter command
 */

public class GetMeasuredFrequencyResponse extends CommandResponse {

  private static final String LOGGER=GetMeasuredFrequencyResponse.class.getName();

  public static final int E_NO_COUNTERS = 1;
  public static final int E_PLL_NOT_LOCKED =2;

  protected int _sampleSequenceNumber;
  protected BigDecimal _frequency;
  protected int _temperature;


  /*
   * Constructor
   */

  GetMeasuredFrequencyResponse(InputStream is) throws IOException {

    // call the base

    super(is);

    long referenceCounter,referenceFrequency,sampleCounter,gateCounter;

    // read the values

    _sampleSequenceNumber=readInt32(is);
    referenceFrequency=readInt32(is);
    referenceCounter=readInt32(is);
    sampleCounter=readInt32(is);
    gateCounter=readInt32(is);
    _temperature=readInt16(is);

    Log.d(LOGGER,"ref freq/ref cnt/sam cnt/gate cnt = "+referenceFrequency+"/"+referenceCounter+"/"+sampleCounter+"/"+gateCounter);

    // decode any error

    switch(_responseCode) {

      case 0:
        _frequency=BigDecimal.valueOf(sampleCounter*referenceFrequency).setScale(Preferences.DEFAULT_SCALE);
        _frequency=_frequency.divide(BigDecimal.valueOf(referenceCounter),RoundingMode.HALF_UP);
        _errorText=null;
        break;

      case E_NO_COUNTERS:
        _errorText="Counters not received from FPGA";
        Log.i(LOGGER,_errorText);
        break;

      case E_PLL_NOT_LOCKED:
        _errorText="PLL not locked";
        Log.e(LOGGER,_errorText);
        break;
    }
  }


  /*
   * Static creator
   */

  public static final Parcelable.Creator<GetMeasuredFrequencyResponse> CREATOR=new Parcelable.Creator<GetMeasuredFrequencyResponse>() {
    public GetMeasuredFrequencyResponse createFromParcel(Parcel in) {
      return new GetMeasuredFrequencyResponse(in);
    }
    public GetMeasuredFrequencyResponse[] newArray(int size) {
      return new GetMeasuredFrequencyResponse[size];
    }
  };


  /*
   * Parcelable constructor
   */

  public GetMeasuredFrequencyResponse(Parcel in) {
    super(in);
    _sampleSequenceNumber=in.readInt();
    _frequency=new BigDecimal(in.readString());
    _temperature=in.readInt();
  }


  /*
   * Write to parcel
   */

  @Override
  public void writeToParcel(Parcel out,int flags) {
    super.writeToParcel(out,flags);
    out.writeInt(_sampleSequenceNumber);
    out.writeString(_frequency.toPlainString());
    out.writeInt(_temperature);
  }


  /*
   * Get the frequency
   */

  public BigDecimal getFrequency() {
    return _frequency;
  }


  /*
   * Get the sequence number of this sample
   */

  public int getSampleSequenceNumber() {
    return _sampleSequenceNumber;
  }


  /*
   * Get the temperature
   */

  public int getTemperature() {
    return _temperature;
  }
}
