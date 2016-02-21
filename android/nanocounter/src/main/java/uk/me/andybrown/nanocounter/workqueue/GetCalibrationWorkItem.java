// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.workqueue;

import uk.me.andybrown.nanocounter.BluetoothService;
import uk.me.andybrown.nanocounter.commands.GetCalibrationCommand;


/*
 * Get a calibration entry
 */

public class GetCalibrationWorkItem extends CommandWorkItem {

  protected final int _index;


  /*
   * Constructor
   */

  public GetCalibrationWorkItem(BluetoothService service,int index) {
    super(service);
    _index=index;
  }


  /*
   * Do the actual work
   */

  @Override
  public void doWork() throws Exception {
    sendCommand(new GetCalibrationCommand(_index));
  }
}
