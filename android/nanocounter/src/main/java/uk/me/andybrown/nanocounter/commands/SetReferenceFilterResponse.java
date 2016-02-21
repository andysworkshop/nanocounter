package uk.me.andybrown.nanocounter.commands;

import android.os.Parcel;

import java.io.IOException;
import java.io.InputStream;


/*
 * Response to the set reference filter command
 */

public class SetReferenceFilterResponse extends CommandResponse {

  /*
   * Constructor
   */

  SetReferenceFilterResponse(InputStream is) throws IOException {

    // call the base

    super(is);

    // no custom response
  }


  /*
   * Parcelable constructor
   */

  public SetReferenceFilterResponse(Parcel in) {
    super(in);
  }


  /*
   * Static creator
   */

  public static final Creator<SetReferenceFilterResponse> CREATOR=new Creator<SetReferenceFilterResponse>() {
    public SetReferenceFilterResponse createFromParcel(Parcel in) {
      return new SetReferenceFilterResponse(in);
    }
    public SetReferenceFilterResponse[] newArray(int size) {
      return new SetReferenceFilterResponse[size];
    }
  };
}
