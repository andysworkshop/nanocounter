package uk.me.andybrown.nanocounter;

/*
 * Ideal frequency source
 */

public enum IdealFrequencySource {

  MIN_MAX(0),
  MIN(1),
  MAX(2),
  AVERAGE(3),
  LAST(4);

  int _value;

  IdealFrequencySource(int value) {
    _value=value;
  }

  public int getValue() {
    return _value;
  }

  public static IdealFrequencySource fromValue(int value) {

    switch(value) {
      case 1:
        return MIN;
      case 2:
        return MAX;
      case 3:
        return AVERAGE;
      case 4:
        return LAST;
      default:
        return MIN_MAX;
    }
  }}
