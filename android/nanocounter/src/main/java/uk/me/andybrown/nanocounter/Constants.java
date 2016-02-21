package uk.me.andybrown.nanocounter;

import java.math.BigDecimal;

/**
 * Big decimal constants
 */

public class Constants {
  public static final BigDecimal THOUSAND = new BigDecimal(1000).setScale(Preferences.DEFAULT_SCALE);
  public static final BigDecimal MILLION = new BigDecimal(1000000).setScale(Preferences.DEFAULT_SCALE);
  public static final BigDecimal MSPERHOUR = new BigDecimal(3600000).setScale(Preferences.DEFAULT_SCALE);
}
