// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.workqueue;

import uk.me.andybrown.nanocounter.BluetoothService;


/*
 * Abstract base for work items
 */

public abstract class WorkItem {

  protected BluetoothService _service;


  /*
   * Constructor
   */

  protected WorkItem(BluetoothService service) {
    _service=service;
  }

  /*
   * Subclass must implement
   */

  public abstract void doWork() throws Exception;
}
