/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Set a new reference clock source
 */

struct SetReferenceClockSourceRequest : Request {

  RefClockSource refClockSource;
  uint8_t _padding[19];

  /*
   * Constructor
   */

  SetReferenceClockSourceRequest() {
    static_assert(sizeof(SetReferenceClockSourceRequest)==REQUEST_BYTE_SIZE,"Compile error: SetReferenceClockSourceRequest struct size is incorrect");
  }
} __attribute__((packed));
