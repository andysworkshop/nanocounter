package uk.me.andybrown.nanocounter.commands;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;


/*
 * Response to the set gate counter command
 */

public class GetCalibrationBitmapResponse extends CommandResponse {

  private static final String LOGGER=GetCalibrationResponse.class.getName();

  protected byte[] _calibrationBitmap;

  /*
   * Constructor
   */

  GetCalibrationBitmapResponse(InputStream is) throws IOException {

    // call the base

    super(is);

    // read the bitmap

    _calibrationBitmap=new byte[15];
    blockingRead(is,_calibrationBitmap);

    Log.i(LOGGER,"Received calibration bitmap");
  }


  /*
   * Static creator
   */

  public static final Parcelable.Creator<GetCalibrationBitmapResponse> CREATOR=new Parcelable.Creator<GetCalibrationBitmapResponse>() {
    public GetCalibrationBitmapResponse createFromParcel(Parcel in) {
      return new GetCalibrationBitmapResponse(in);
    }
    public GetCalibrationBitmapResponse[] newArray(int size) {
      return new GetCalibrationBitmapResponse[size];
    }
  };


  /*
   * Parcelable constructor
   */

  public GetCalibrationBitmapResponse(Parcel in) {

    super(in);

    _calibrationBitmap=new byte[15];
    in.readByteArray(_calibrationBitmap);
  }


  /*
   * Write to parcel
   */

  @Override
  public void writeToParcel(Parcel out,int flags) {

    super.writeToParcel(out,flags);
    out.writeByteArray(_calibrationBitmap);
  }


  /*
   * Get the calibration bitmap
   */

  public byte[] getCalibrationBitmap() {
    return _calibrationBitmap;
  }
}
