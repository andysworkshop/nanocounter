package uk.me.andybrown.nanocounter.commands;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;


/*
 * Command response processor
 */

public abstract class CommandResponse implements Parcelable {

  private static final String LOGGER=Command.class.getName();

  protected int _requestId;
  protected int _responseCode;
  protected String _errorText;
  protected int _requestSequenceNumber;


  /*
   * Constructor
   */

  protected CommandResponse(InputStream is) throws IOException {
    _responseCode=is.read();
    _requestSequenceNumber=readInt32(is);
  }


  /*
   * Construct from parcel
   */

  protected CommandResponse(Parcel in) {
    _requestId=in.readInt();
    _responseCode=in.readInt();
    _errorText=in.readString();
    _requestSequenceNumber=in.readInt();
  }


  /*
   * Get an appropriate command response from the serialized bytes
   */

  public static CommandResponse instanceOf(InputStream is) throws Exception {

    int id;

    id=is.read();
    Log.d(LOGGER,"Read command response code: "+id);

    switch(id) {

      case CommandId.SET_GATE_COUNTER:
        Log.i(LOGGER,"Creating SET_GATE_COUNTER response");
        return new SetGateCounterResponse(is);

      case CommandId.GET_MEASURED_FREQUENCY:
        Log.d(LOGGER,"Creating GET_MEASURED_FREQUENCY response");
        return new GetMeasuredFrequencyResponse(is);

      case CommandId.SET_REFERENCE_CLOCK_SOURCE:
        Log.i(LOGGER,"Creating SET_REFERENCE_CLOCK_SOURCE response");
        return new SetReferenceClockSourceResponse(is);

      case CommandId.SET_XREF_FILTER_CLOCK_BANDWIDTH:
        Log.i(LOGGER,"Creating SET_XREF_FILTER_CLOCK_BANDWIDTH response");
        return new SetReferenceFilterResponse(is);

      case CommandId.SET_SAMPLE_CLOCK_FILTER_BANDWIDTH:
        Log.i(LOGGER,"Creating SET_SAMPLE_CLOCK_FILTER_BANDWIDTH response");
        return new SetSampleFilterResponse(is);

      case CommandId.RESET_PLL:
        Log.i(LOGGER,"Creating RESET_PLL response");
        return new ResetPllResponse(is);

      case CommandId.SET_CALIBRATION:
        Log.i(LOGGER,"Creating SET_CALIBRATION response");
        return new SetCalibrationResponse(is);

      case CommandId.GET_CALIBRATION:
        Log.i(LOGGER,"Creating GET_CALIBRATION response");
        return new GetCalibrationResponse(is);

      case CommandId.GET_CALIBRATION_BITMAP:
        Log.i(LOGGER,"Creating GET_CALIBRATION_BITMAP response");
        return new GetCalibrationBitmapResponse(is);

      default:
        Log.e(LOGGER,"Unexpected command response id: "+id);
        throw new Exception("Unexpected command response id "+id);
    }
  }


  /*
   * Read an encoded 32 bit integer
   */

  protected int readInt32(InputStream is) throws IOException {

    int value;

    value=is.read() |
            is.read() << 8 |
            is.read() << 16 |
            is.read() << 24;

    return value;
  }


  /*
   * Read an encoded 16 bit integer
   */

  protected int readInt16(InputStream is) throws IOException {

    int value;

    value=is.read() | is.read() << 8;
    return value;
  }


  /*
   * Read from a stream, blocking
   */

  protected void blockingRead(InputStream is,byte[] buffer) throws IOException {

    int read,offset,remaining;

    offset=0;
    for(remaining=buffer.length;remaining>0;remaining-=read) {
      read=is.read(buffer,offset,remaining);
      offset+=read;
    }
  }


  /*
   * blocking skip forward
   */

  protected void blockingSkip(InputStream is,int size) throws IOException {

    int i;

    for(i=0;i<size;i++)
      is.read();
  }


  /*
   * Get the error text or null if there was no error
   */

  public String getErrorText() {
    return _errorText;
  }


  /*
   * Describe parcel content
   */

  @Override
  public int describeContents() {
    return 0;
  }


  /*
   * Write to parcel
   */

  public void writeToParcel(Parcel out, int flags) {
    out.writeInt(_requestId);
    out.writeInt(_responseCode);
    out.writeString(_errorText);
    out.writeInt(_requestSequenceNumber);
  }
}
