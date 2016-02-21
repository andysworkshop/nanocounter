package uk.me.andybrown.nanocounter.commands;

/*
 * Command to reset the PLL
 */

public class ResetPllCommand extends Command {

  /*
   * Constructor
   */

  public ResetPllCommand() {
    super(CommandId.RESET_PLL);
  }
}
