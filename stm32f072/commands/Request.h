/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Request structure
 */

struct Request {
  enum {
    GET_MEASURED_FREQUENCY = 0x27,
    SET_GATE_COUNTER = 0x28,
    SET_SAMPLE_CLOCK_FILTER = 0x29,
    SET_XREF_CLOCK_FILTER = 0x2a,
    SET_REF_CLOCK_SOURCE = 0x2b,
    RESET_PLL = 0x2c,
    RESET_MCU = 0x2d,
    GET_CALIBRATION = 0x2e,
    SET_CALIBRATION = 0x2f,
    GET_CALIBRATION_BITMAP = 0x30,

    REQUEST_BYTE_SIZE = 25
  };

  uint8_t requestId;
  uint32_t requestSequenceNumber;

  /*
   * Check if this request has a valid identifier
   */

  bool isValid() const {
    return requestId>=GET_MEASURED_FREQUENCY && requestId<=GET_CALIBRATION_BITMAP;
  }

} __attribute__((packed));
