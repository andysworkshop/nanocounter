package uk.me.andybrown.nanocounter.commands;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;


/*
 * Response to the set gate counter command
 */

public class SetGateCounterResponse extends CommandResponse {

  private static final String LOGGER=SetGateCounterResponse.class.getName();

  /*
   * Constructor
   */

  SetGateCounterResponse(InputStream is) throws IOException {

    // call the base

    super(is);

    // decode any error

    if(_responseCode==1) {
      _errorText="Requested gate counter out of range";
      Log.e(LOGGER,_errorText);
    }
  }


  /*
   * Parcelable constructor
   */

  public SetGateCounterResponse(Parcel in) {
    super(in);
  }


  /*
   * Static creator
   */

  public static final Parcelable.Creator<SetGateCounterResponse> CREATOR=new Parcelable.Creator<SetGateCounterResponse>() {
    public SetGateCounterResponse createFromParcel(Parcel in) {
      return new SetGateCounterResponse(in);
    }
    public SetGateCounterResponse[] newArray(int size) {
      return new SetGateCounterResponse[size];
    }
  };
}
