/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Reset the AD9553 PLL
 */

struct ResetPllRequest : Request {

  uint8_t _padding[22];

  /*
   * Constructor
   */

  ResetPllRequest() {
    static_assert(sizeof(GetMeasuredFrequencyRequest)==REQUEST_BYTE_SIZE,"Compile error: ResetPllRequest struct size is not correct");
  }
} __attribute__((packed));
