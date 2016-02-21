/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Response to GET_CALIBRATION
 */

struct GetCalibrationResponse : Response {

  Calibration calibration;

  /*
   * Constructor
   */

  GetCalibrationResponse() {
    static_assert(sizeof(GetCalibrationResponse)==24,"compile error: sizeof(GetCalibrationResponse)!=24");
    requestId=Request::GET_CALIBRATION;
  }
} __attribute__((packed));
