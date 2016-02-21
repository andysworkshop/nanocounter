// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter;

/*
 * Custom intents used by our application
 */

public class CustomIntent {

  /*
   * Intent strings
   */

  public static final String LINK_STATUS = "uk.me.andybrown.nanocounter.LINK_STATUS";
  public static final String COMMAND_FAILED = "uk.me.andybrown.nanocounter.COMMAND_FAILED";
  public static final String COMMAND_RESPONSE = "uk.me.andybrown.nanocounter.COMMAND_RESPONSE";
  public static final String ACTIVATE_CALIBRATION = "uk.me.andybrown.nanocounter.ACTIVATE_CALIBRATION";

  /*
   * Extra intent data names
   */

  public static final String LINK_STATUS_EXTRA = "link_status";
  public static final String COMMAND_FAILED_EXTRA = "command_failed";
  public static final String COMMAND_RESPONSE_EXTRA = "command_response";
}
