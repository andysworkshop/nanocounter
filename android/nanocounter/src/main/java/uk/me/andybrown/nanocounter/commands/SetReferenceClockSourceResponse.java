package uk.me.andybrown.nanocounter.commands;

import android.os.Parcel;

import java.io.IOException;
import java.io.InputStream;


/*
 * Response to the set reference clock source command
 */

public class SetReferenceClockSourceResponse extends CommandResponse {

  /*
   * Constructor
   */

  SetReferenceClockSourceResponse(InputStream is) throws IOException {

    // call the base

    super(is);

    // no custom response
  }


  /*
   * Parcelable constructor
   */

  public SetReferenceClockSourceResponse(Parcel in) {
    super(in);
  }


  /*
   * Static creator
   */

  public static final Creator<SetReferenceClockSourceResponse> CREATOR=new Creator<SetReferenceClockSourceResponse>() {
    public SetReferenceClockSourceResponse createFromParcel(Parcel in) {
      return new SetReferenceClockSourceResponse(in);
    }
    public SetReferenceClockSourceResponse[] newArray(int size) {
      return new SetReferenceClockSourceResponse[size];
    }
  };
}
