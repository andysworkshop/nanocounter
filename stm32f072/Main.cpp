/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */

#include "Application.h"


/*
 * Main entry point
 */

int main() {

  // set up SysTick at 1ms resolution

  MillisecondTimer::initialise();

  // declare the program and run it

  Program p;
  p.run();

  // fatal error if run() returns

  for(;;);
}
