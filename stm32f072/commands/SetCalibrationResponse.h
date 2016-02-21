/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Response to SET_CALIBRATION
 */

struct SetCalibrationResponse : Response {

  /*
   * Constructor
   */

  SetCalibrationResponse() {
    static_assert(sizeof(SetCalibrationResponse)==6,"compile error: sizeof(SetCalibrationResponse)!=6");
    requestId=Request::SET_CALIBRATION;
  }
} __attribute__((packed));
