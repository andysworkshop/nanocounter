// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.workqueue;

import uk.me.andybrown.nanocounter.BluetoothService;
import uk.me.andybrown.nanocounter.Calibration;
import uk.me.andybrown.nanocounter.commands.SetCalibrationCommand;


/*
 * Set a calibration entry
 */

public class SetCalibrationWorkItem extends CommandWorkItem {

  /*
   * The new calibration data
   */

  protected final Calibration _calibration;


  /*
   * Constructor
   */

  public SetCalibrationWorkItem(BluetoothService service,Calibration calibration) {
    super(service);
    _calibration=calibration;
  }


  /*
   * Do the actual work
   */

  @Override
  public void doWork() throws Exception {
    sendCommand(new SetCalibrationCommand(_calibration));
  }
}
