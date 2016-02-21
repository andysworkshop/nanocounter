// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.workqueue;

import uk.me.andybrown.nanocounter.BluetoothService;
import uk.me.andybrown.nanocounter.commands.GetCalibrationBitmapCommand;


/*
 * Get the calibration bitmap
 */

public class GetCalibrationBitmapWorkItem extends CommandWorkItem {


  /*
   * Constructor
   */

  public GetCalibrationBitmapWorkItem(BluetoothService service) {
    super(service);
  }


  /*
   * Do the actual work
   */

  @Override
  public void doWork() throws Exception {
    sendCommand(new GetCalibrationBitmapCommand());
  }
}
