/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Bandwidth for the LTC6957 filter
 */

enum class FilterBandwidth : uint8_t {
  BW_FULL = 0,    // 1200MHz
  BW_500  = 1,    // 500MHz
  BW_160  = 2,    // 160MHz
  BW_50   = 3     // 50MHz
};
