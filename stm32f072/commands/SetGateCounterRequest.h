/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Set a new gate counter
 */

struct SetGateCounterRequest : Request {

  uint32_t gateCounter;
  uint8_t _padding[16];

  /*
   * Constructor
   */

  SetGateCounterRequest() {
    static_assert(sizeof(SetGateCounterRequest)==REQUEST_BYTE_SIZE,"Compile error: SetGateCounterRequest struct size is incorrect");
  }
} __attribute__((packed));
