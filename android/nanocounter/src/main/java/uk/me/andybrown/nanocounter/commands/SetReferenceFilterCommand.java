// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.commands;

import java.io.IOException;
import java.io.OutputStream;

import uk.me.andybrown.nanocounter.FilterBandwidth;


/*
 * Command to set the reference filter command
 */

public class SetReferenceFilterCommand extends Command {

  /*
   * The new filter bandwidth
   */

  protected FilterBandwidth _filter;


  /*
   * Constructor
   */

  public SetReferenceFilterCommand(FilterBandwidth filter) {
    super(CommandId.SET_XREF_FILTER_CLOCK_BANDWIDTH);
    _filter=filter;
  }


  /*
   * Serialize this command
   */

  @Override
  protected void serialize(OutputStream os) throws IOException {
    writeInt(os,_filter.getValue());
  }
}
