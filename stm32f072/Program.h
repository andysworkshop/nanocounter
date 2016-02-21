/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once

#include "Application.h"


/*
 * Main program class
 */

class Program {

  protected:
    FpgaProgrammer *_programmer;

    Preferences _preferences;
    LinkLed _linkLed;
    LockLed _lockLed;
    ActiveLed _activeLed;
    AD9553 _pll;
    FpgaCommunicator _fpgaCommunicator;
    FrequencyCounter _frequencyCounter;
    PllLockIndicator _pllLockIndicator;
    CommandProcessor _commandProcessor;

  protected:
    volatile bool _pllLockState;
    volatile bool _countersReady;

    bool _lastLockState;

  protected:
    void onPllLockChanged(bool lockState);
    void onFrequencyCountReady();

  public:
    Program();

    void run();
};


/*
 * Constructor
 */

inline Program::Program()
  : _pll(_preferences),
    _fpgaCommunicator(_preferences),
    _frequencyCounter(_fpgaCommunicator),
    _pllLockIndicator(_lockLed),
    _commandProcessor(_preferences,_pllLockIndicator,_pll,_linkLed) {

  // increase systick to maximum priority

  NVIC_SetPriority(SysTick_IRQn, 0);

  // initialise with current lock state

  _pllLockState=_lastLockState=_pllLockIndicator.isLocked();
  _countersReady=false;

  // subscribe to changes from the lock indicator

  _pllLockIndicator.PllLockChangedEventSender.insertSubscriber(
      PllLockChangedEventSourceSlot::bind(this,&Program::onPllLockChanged));

  // subscribe to frequency count ready indicator

  _frequencyCounter.FrequencyCountReadyEventSender.insertSubscriber(
      FrequencyCountReadyEventSourceSlot::bind(this,&Program::onFrequencyCountReady));
}


/*
 * Run the program
 */

inline void Program::run() {

  // program the FPGA and then discard the programming class

  _programmer=new FpgaProgrammer(_linkLed);
  _programmer->program();
  delete _programmer;

  // start the AD9553

  _pll.start();

  // start the bluetooth communicator

  _commandProcessor.start();

  Counters counters;

  for(;;) {

    // check the command processor for activity

    _commandProcessor.run();

    // if the lock is lost then stop

    if(!_pllLockState && _frequencyCounter.isStarted())
      _frequencyCounter.stop();

    // if not started, locked and counters have been processed then start

    if(_pllLockState && !_frequencyCounter.isStarted() && !_countersReady)
      _frequencyCounter.start();

    // new counters available?

    if(_countersReady) {

      MillisecondTimer::delay(10);

      // read from the FPGA

      _frequencyCounter.readCounters(counters);

      // stop (reset) the counters inside the FPGA

      _frequencyCounter.stop();

      // set the latest counters inside the command processor

      _commandProcessor.setCounters(counters);

      // reset for a new run

      _countersReady=false;
    }
  }
}


/*
 * The state of the PLL lock changed. This is IRQ code.
 */

inline void Program::onPllLockChanged(bool lockState) {
  _pllLockState=lockState;
}


/*
 * There's a new frequency count ready for us to come and collect. This is IRQ code.
 */

inline void Program::onFrequencyCountReady() {
  _countersReady=true;
}

