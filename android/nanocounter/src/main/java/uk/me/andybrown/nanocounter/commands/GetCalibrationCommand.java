package uk.me.andybrown.nanocounter.commands;

import java.io.IOException;
import java.io.OutputStream;


/*
 * Command to get a calibration entry
 */

public class GetCalibrationCommand extends Command {

  protected int _index;

  /*
   * Constructor
   */

  public GetCalibrationCommand(int index) {

    super(CommandId.GET_CALIBRATION);
    _index=index;
  }


  /*
   * Serialize this command
   */

  @Override
  protected void serialize(OutputStream os) throws IOException {

    // write the index

    writeInt16(os,_index);
  }
}
