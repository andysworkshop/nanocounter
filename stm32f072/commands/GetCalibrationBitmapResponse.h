/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Response to GET_CALIBRATION_BITMAP
 */

struct GetCalibrationBitmapResponse : Response {

  uint8_t bitmap[15];

  /*
   * Constructor
   */

  GetCalibrationBitmapResponse() {
    static_assert(sizeof(GetCalibrationBitmapResponse)==21,"compile error: sizeof(GetCalibrationBitmapResponse)!=21");
    requestId=Request::GET_CALIBRATION_BITMAP;
  }
} __attribute__((packed));
