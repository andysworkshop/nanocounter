/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Response to RESET_PLL
 */

struct ResetPllResponse : Response {

  /*
   * Constructor
   */

  ResetPllResponse() {
    static_assert(sizeof(ResetPllResponse)==6,"compile error: sizeof(ResetPllResponse)!=6");
    requestId=Request::RESET_PLL;
  }
} __attribute__((packed));
