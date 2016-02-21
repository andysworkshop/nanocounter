/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Response to SET_GATE_COUNTER
 */

struct SetGateCounterResponse : Response {

  /*
   * Error codes
   */

  enum {
    E_OUT_OF_RANGE = 1
  };


  /*
   * Constructor
   */

  SetGateCounterResponse() {
    static_assert(sizeof(SetGateCounterResponse)==6,"compile error: sizeof(SetGateCounterResponse)!=6");
    requestId=Request::SET_GATE_COUNTER;
  }
} __attribute__((packed));
