/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Response to SET_XREF_CLOCK_FILTER
 */

struct SetReferenceFilterResponse : Response {

  /*
   * Constructor
   */

  SetReferenceFilterResponse() {
    static_assert(sizeof(SetReferenceFilterResponse)==6,"compile error: sizeof(SetReferenceFilterResponse)!=6");
    requestId=Request::SET_XREF_CLOCK_FILTER;
  }
} __attribute__((packed));
