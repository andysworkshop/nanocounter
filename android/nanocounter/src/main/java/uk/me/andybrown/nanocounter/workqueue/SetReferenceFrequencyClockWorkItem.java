// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.workqueue;

import uk.me.andybrown.nanocounter.BluetoothService;
import uk.me.andybrown.nanocounter.ReferenceFrequencySource;
import uk.me.andybrown.nanocounter.commands.SetReferenceClockSourceCommand;

/*
 * Set the reference frequency source
 */

public class SetReferenceFrequencyClockWorkItem extends CommandWorkItem {

  /*
   * The new reference frequency source
   */

  protected final ReferenceFrequencySource _source;


  /*
   * Constructor
   */

  public SetReferenceFrequencyClockWorkItem(BluetoothService service,ReferenceFrequencySource source) {
    super(service);
    _source=source;
  }


  /*
   * Do the actual work
   */

  @Override
  public void doWork() throws Exception {
    sendCommand(new SetReferenceClockSourceCommand(_source));
  }
}
