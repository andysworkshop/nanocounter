// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.workqueue;

import uk.me.andybrown.nanocounter.BluetoothService;
import uk.me.andybrown.nanocounter.commands.GetMeasuredFrequencyCommand;


/*
 * Work item to set a new gate counter
 */

public class GetMeasuredFrequencyWorkItem extends CommandWorkItem {

  /*
   * Constructor
   */

  public GetMeasuredFrequencyWorkItem(BluetoothService service) {
    super(service);
  }


  /*
   * Do the actual work
   */

  @Override
  public void doWork() throws Exception {
    sendCommand(new GetMeasuredFrequencyCommand());
  }
}
