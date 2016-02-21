package uk.me.andybrown.nanocounter.commands;

import java.io.IOException;
import java.io.OutputStream;

/*
 * Command to set a new gate counter value
 */

public class SetGateCounterCommand extends Command {

  /*
   * The new gate counter
   */

  protected long _gateCounter;


  /*
   * Constructor
   */

  public SetGateCounterCommand(long gateCounter) {
    super(CommandId.SET_GATE_COUNTER);
    _gateCounter=gateCounter;
  }


  /*
   * Serialize this command
   */

  @Override
  protected void serialize(OutputStream os) throws IOException {
    writeInt(os,_gateCounter);
  }
}
