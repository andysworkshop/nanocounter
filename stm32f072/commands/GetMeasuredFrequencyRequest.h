/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Get the measured frequency
 */

struct GetMeasuredFrequencyRequest : Request {

  uint8_t _padding[20];

  /*
   * Constructor
   */

  GetMeasuredFrequencyRequest() {
    static_assert(sizeof(GetMeasuredFrequencyRequest)==REQUEST_BYTE_SIZE,"Compile error: GetMeasuredFrequencyRequest struct size is not correct");
  }
} __attribute__((packed));
