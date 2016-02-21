// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.workqueue;

import uk.me.andybrown.nanocounter.BluetoothService;
import uk.me.andybrown.nanocounter.commands.SetGateCounterCommand;


/*
 * Work item to set a new gate counter
 */

public class SetGateCounterWorkItem extends CommandWorkItem {

  /*
   * The new gate counter
   */

  protected final long _gateCounter;


  /*
   * Constructor
   */

  public SetGateCounterWorkItem(BluetoothService service,long gateCounter) {
    super(service);
    _gateCounter=gateCounter;
  }


  /*
   * Do the actual work
   */

  @Override
  public void doWork() throws Exception {
    sendCommand(new SetGateCounterCommand(_gateCounter));
  }
}
