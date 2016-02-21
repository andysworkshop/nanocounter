package uk.me.andybrown.nanocounter.commands;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import uk.me.andybrown.nanocounter.Calibration;


/*
 * Command to set a new calibration entry
 */

public class SetCalibrationCommand extends Command {

  /*
   * The calibration entry
   */

  protected Calibration _calibration;


  /*
   * Constructor
   */

  public SetCalibrationCommand(Calibration calibration) {

    super(CommandId.SET_CALIBRATION);
    _calibration=calibration;
  }


  /*
   * Serialize this command
   */

  @Override
  protected void serialize(OutputStream os) throws IOException {

    SimpleDateFormat df;
    int fraction;
    byte[] dateBytes;

    // write the index

    writeInt16(os,_calibration.getIndex());

    // write the calibration

    df=new SimpleDateFormat("yyyyMMdd");
    fraction=_calibration.getOffset().remainder(BigDecimal.ONE).movePointRight(_calibration.getOffset().scale()).abs().intValue();

    dateBytes=df.format(_calibration.getDate()).getBytes("UTF-8");
    os.write(dateBytes);
    writeInt16(os,_calibration.getTemperature().multiply(new BigDecimal("10.0")).intValue());
    writeInt(os,_calibration.getOffset().intValue());
    writeInt(os,fraction);
  }
}
