/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Response to SET_REF_CLOCK_SOURCE
 */

struct SetReferenceClockSourceResponse : Response {

  /*
   * Constructor
   */

  SetReferenceClockSourceResponse() {
    static_assert(sizeof(SetReferenceClockSourceResponse)==6,"compile error: sizeof(SetReferenceClockSourceResponse)!=6");
    requestId=Request::SET_REF_CLOCK_SOURCE;
  }
} __attribute__((packed));
