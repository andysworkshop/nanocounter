package uk.me.andybrown.nanocounter.commands;

/*
 * "enumeration" of command ids
 */

public class CommandId {
  static final int GET_MEASURED_FREQUENCY = 0x27;
  static final int SET_GATE_COUNTER = 0x28;
  static final int SET_SAMPLE_CLOCK_FILTER_BANDWIDTH = 0x29;
  static final int SET_XREF_FILTER_CLOCK_BANDWIDTH = 0x2a;
  static final int SET_REFERENCE_CLOCK_SOURCE = 0x2b;
  static final int RESET_PLL = 0x2c;
  static final int RESET_MCU_PLL = 0x2d;
  static final int GET_CALIBRATION = 0x2e;
  static final int SET_CALIBRATION = 0x2f;
  static final int GET_CALIBRATION_BITMAP = 0x30;
}
