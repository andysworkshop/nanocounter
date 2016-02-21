/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Set a calibration entry
 */

struct SetCalibrationRequest : Request {

  uint16_t index;
  Calibration calibration;

  /*
   * Constructor
   */

  SetCalibrationRequest() {
    static_assert(sizeof(SetCalibrationRequest)==REQUEST_BYTE_SIZE,"Compile error: SetCalibrationRequest struct size is incorrect");
  }
} __attribute__((packed));
