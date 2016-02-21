// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.workqueue;

import uk.me.andybrown.nanocounter.BluetoothService;
import uk.me.andybrown.nanocounter.commands.ResetPllCommand;


/*
 * Work item to reset the PLL
 */

public class ResetPllWorkItem extends CommandWorkItem {

  /*
   * Constructor
   */

  public ResetPllWorkItem(BluetoothService service) {
    super(service);
  }


  /*
   * Do the actual work
   */

  @Override
  public void doWork() throws Exception {
    sendCommand(new ResetPllCommand());
  }
}
