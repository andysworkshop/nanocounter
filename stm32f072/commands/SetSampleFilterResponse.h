/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Response to SET_SAMPLE_CLOCK_FILTER
 */

struct SetSampleFilterResponse : Response {

  /*
   * Constructor
   */

  SetSampleFilterResponse() {
    static_assert(sizeof(SetSampleFilterResponse)==6,"compile error: sizeof(SetSampleFilterResponse)!=6");
    requestId=Request::SET_SAMPLE_CLOCK_FILTER;
  }
} __attribute__((packed));
