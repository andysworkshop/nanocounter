/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Set a new sample filter
 */

struct SetSampleFilterRequest : Request {

  FilterBandwidth filterBandwidth;
  uint8_t _padding[19];

  /*
   * Constructor
   */

  SetSampleFilterRequest() {
    static_assert(sizeof(SetSampleFilterRequest)==REQUEST_BYTE_SIZE,"Compile error: SetSampleFilterRequest struct size is incorrect");
  }
} __attribute__((packed));
