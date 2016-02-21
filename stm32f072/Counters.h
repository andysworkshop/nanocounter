/*
 * This file is a part of the open source stm32plus library.
 * Copyright (c) 2011,2012,2013,2014 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Counters structure. Carrier for the results read out from the FPGA
 */

struct Counters {
  uint32_t reference;     // reference count
  uint32_t sample;        // sample count
};
