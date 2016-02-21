/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Get the calibration bitmap. The bitmap is returned as 15 bytes of which the first 113 bits
 * are a vector describing the occupancy of the 113 available calibration slots. byte 0 position 0
 * is slot 1. byte 112 bit 7 is slot 112.
 */

struct GetCalibrationBitmapRequest : Request {

  uint8_t _padding[20];

  /*
   * Constructor
   */

  GetCalibrationBitmapRequest() {
    static_assert(sizeof(GetCalibrationBitmapRequest)==REQUEST_BYTE_SIZE,"Compile error: GetCalibrationBitmapRequest struct size is not correct");
  }
} __attribute__((packed));
