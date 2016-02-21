// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter.commands;

import java.io.IOException;
import java.io.OutputStream;

import uk.me.andybrown.nanocounter.FilterBandwidth;


/*
 * Command to set the sample filter bandwidth
 */

public class SetSampleFilterCommand extends Command {

  /*
   * The new filter bandwidth
   */

  protected FilterBandwidth _filter;


  /*
   * Constructor
   */

  public SetSampleFilterCommand(FilterBandwidth filter) {
    super(CommandId.SET_SAMPLE_CLOCK_FILTER_BANDWIDTH);
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
