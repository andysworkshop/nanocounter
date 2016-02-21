package uk.me.andybrown.nanocounter.commands;

import android.os.Parcel;

import java.io.IOException;
import java.io.InputStream;


/*
 * Response to the reset PLL command
 */

public class ResetPllResponse extends CommandResponse {

  /*
   * Constructor
   */

  ResetPllResponse(InputStream is) throws IOException {

    // call the base

    super(is);

    // no custom response
  }


  /*
   * Parcelable constructor
   */

  public ResetPllResponse(Parcel in) {
    super(in);
  }


  /*
   * Static creator
   */

  public static final Creator<ResetPllResponse> CREATOR=new Creator<ResetPllResponse>() {
    public ResetPllResponse createFromParcel(Parcel in) {
      return new ResetPllResponse(in);
    }
    public ResetPllResponse[] newArray(int size) {
      return new ResetPllResponse[size];
    }
  };
}
