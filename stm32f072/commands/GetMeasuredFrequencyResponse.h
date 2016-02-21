/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Response to GET_MEASURED_FREQUENCY
 */

struct GetMeasuredFrequencyResponse : Response {

  enum {
    E_NO_COUNTERS = 1,
    E_PLL_NOT_LOCKED =2
  };

  uint32_t sampleSequenceNumber;
  uint32_t referenceFrequency;
  uint32_t referenceCounter;
  uint32_t sampleCounter;
  uint32_t gateCounter;
  uint16_t mcuTemperature;

  /*
   * Constructor
   */

  GetMeasuredFrequencyResponse() {
    static_assert(sizeof(GetMeasuredFrequencyResponse)==28,"compile error: sizeof(GetMeasuredFrequencyResponse)!=28");
    requestId=Request::GET_MEASURED_FREQUENCY;
  }
} __attribute__((packed));
