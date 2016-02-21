// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.commands;

import java.io.IOException;
import java.io.OutputStream;

import uk.me.andybrown.nanocounter.ReferenceFrequencySource;


/*
 * Command to set the reference frequency source
 */

public class SetReferenceClockSourceCommand extends Command {

  /*
   * The new source
   */

  protected ReferenceFrequencySource _source;


  /*
   * Constructor
   */

  public SetReferenceClockSourceCommand(ReferenceFrequencySource source) {
    super(CommandId.SET_REFERENCE_CLOCK_SOURCE);
    _source=source;
  }


  /*
   * Serialize this command
   */

  @Override
  protected void serialize(OutputStream os) throws IOException {
    writeInt(os,_source.getValue());
  }
}
