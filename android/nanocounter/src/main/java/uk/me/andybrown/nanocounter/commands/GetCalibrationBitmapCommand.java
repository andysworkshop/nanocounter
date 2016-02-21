package uk.me.andybrown.nanocounter.commands;

/*
 * Command to get the bitmap of used calibration entries
 */

public class GetCalibrationBitmapCommand extends Command {

  /*
   * Constructor
   */

  public GetCalibrationBitmapCommand() {
    super(CommandId.GET_CALIBRATION_BITMAP);
  }
}
