package uk.me.andybrown.nanocounter.commands;

import android.os.Parcel;

import java.io.IOException;
import java.io.InputStream;


/*
 * Response to the set sample filter command
 */

public class SetSampleFilterResponse extends CommandResponse {

  /*
   * Constructor
   */

  SetSampleFilterResponse(InputStream is) throws IOException {

    // call the base

    super(is);

    // no custom response
  }


  /*
   * Parcelable constructor
   */

  public SetSampleFilterResponse(Parcel in) {
    super(in);
  }


  /*
   * Static creator
   */

  public static final Creator<SetSampleFilterResponse> CREATOR=new Creator<SetSampleFilterResponse>() {
    public SetSampleFilterResponse createFromParcel(Parcel in) {
      return new SetSampleFilterResponse(in);
    }
    public SetSampleFilterResponse[] newArray(int size) {
      return new SetSampleFilterResponse[size];
    }
  };
}
