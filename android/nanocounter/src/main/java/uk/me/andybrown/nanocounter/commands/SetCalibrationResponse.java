package uk.me.andybrown.nanocounter.commands;

import android.os.Parcel;

import java.io.IOException;
import java.io.InputStream;


/*
 * Response to the set calibration command
 */

public class SetCalibrationResponse extends CommandResponse {

  private static final String LOGGER=SetCalibrationResponse.class.getName();

  /*
   * Constructor
   */

  SetCalibrationResponse(InputStream is) throws IOException {

    // call the base

    super(is);

    // no custom stuff
  }


  /*
   * Parcelable constructor
   */

  public SetCalibrationResponse(Parcel in) {
    super(in);
  }


  /*
   * Static creator
   */

  public static final Creator<SetCalibrationResponse> CREATOR=new Creator<SetCalibrationResponse>() {
    public SetCalibrationResponse createFromParcel(Parcel in) {
      return new SetCalibrationResponse(in);
    }
    public SetCalibrationResponse[] newArray(int size) {
      return new SetCalibrationResponse[size];
    }
  };
}
