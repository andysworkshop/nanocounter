/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


struct Response {

  enum {
    OK = 0
  };

  uint8_t requestId;
  uint8_t responseCode;
  uint32_t requestSequenceNumber;
} __attribute__((packed));
