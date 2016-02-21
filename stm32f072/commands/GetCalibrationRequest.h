/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Get a calibration entry
 */

struct GetCalibrationRequest : Request {

  uint16_t index;
  uint8_t _padding[18];

  /*
   * Constructor
   */

  GetCalibrationRequest() {
    static_assert(sizeof(GetCalibrationRequest)==REQUEST_BYTE_SIZE,"Compile error: GetCalibrationRequest struct size is not correct");
  }
} __attribute__((packed));
