package uk.me.andybrown.nanocounter.commands;

/*
 * Command to set a new gate counter value
 */

public class GetMeasuredFrequencyCommand extends Command {

  /*
   * Constructor
   */

  public GetMeasuredFrequencyCommand() {
    super(CommandId.GET_MEASURED_FREQUENCY);
  }
}
