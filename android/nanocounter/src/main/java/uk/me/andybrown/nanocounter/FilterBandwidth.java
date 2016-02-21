package uk.me.andybrown.nanocounter;


/*
 * Filter bandwidth enumeration. Values must match those received by the firmware.
 */

public enum FilterBandwidth {

  BW_FULL(0),    // 1200MHz
  BW_500(1),     // 500MHz
  BW_160(2),     // 160MHz
  BW_50(3);      // 50MHz

  int _value;

  FilterBandwidth(int value) {
    _value=value;
  }

  public int getValue() {
    return _value;
  }

  public static FilterBandwidth fromValue(int value) {

    switch(value) {
      case 1:
        return BW_500;
      case 2:
        return BW_160;
      case 3:
        return BW_50;
      default:
        return BW_FULL;
    }
  }
}
