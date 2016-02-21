/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Reference clock source input to the AD9553. Matches the values
 * in the android app.
 */

enum class RefClockSource : uint8_t {
  INTERNAL_10M = 0,       // 10Mhz internal
  EXTERNAL_10M = 1        // 10Mhz external
};
