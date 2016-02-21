package uk.me.andybrown.nanocounter;

/*
 * Reference frequency source enumeration. Matches the values received
 * by the Nanocounter firmware
 */

public enum ReferenceFrequencySource {

  INTERNAL_10M(0),
  EXTERNAL_10M(1);

  protected int _value;

  ReferenceFrequencySource(int value) {
    _value=value;
  }

  public int getValue() {
    return _value;
  }

  static ReferenceFrequencySource fromValue(int value) {
    return value==1 ? EXTERNAL_10M : INTERNAL_10M;
  }
}
