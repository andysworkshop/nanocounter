// This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
// Copyright (c) 2016 Andy Brown <www.andybrown.me.uk>
// Please see website for licensing terms.

package uk.me.andybrown.nanocounter;

/*
 * Status of the bluetooth connection
 */

public enum LinkStatus {
  UNKNOWN,
  NOT_SUPPORTED,
  ENABLED,
  DISABLED,
  NOT_PAIRED,
  CONNECTING,
  CONNECTION_FAILED,
  CONNECTED;
};

