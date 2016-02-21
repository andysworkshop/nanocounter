// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter;

import android.os.Binder;


/*
 * Binder class for the bluetooth service
 */

public class BluetoothServiceBinder extends Binder {

  protected final BluetoothService _service;


  /*
   * Constructor
   */

  public BluetoothServiceBinder(BluetoothService service) {
    _service=service;
  }


  /*
   * Get the service
   */
  public BluetoothService getService() {
    return _service;
  }
}
