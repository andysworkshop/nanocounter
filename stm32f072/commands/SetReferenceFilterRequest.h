/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Set a new reference filter
 */

struct SetReferenceFilterRequest : Request {

  FilterBandwidth filterBandwidth;
  uint8_t _padding[19];

  /*
   * Constructor
   */

  SetReferenceFilterRequest() {
    static_assert(sizeof(SetReferenceFilterRequest)==REQUEST_BYTE_SIZE,"Compile error: SetReferenceFilterRequest struct size is incorrect");
  }
} __attribute__((packed));
