/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Frequency counter class. Orchestrates all the comms with the FPGA
 */

DECLARE_EVENT_SIGNATURE(FrequencyCountReady,void());

class FrequencyCounter {

  protected:
    FpgaCommunicator& _fpgaCommunicator;
    Exti15 *_exti;
    GpioPinRef _fpgaCountEnable;
    GpioPinRef _fpgaDoneCounting;
    GpioPinRef _activeLed;
    volatile bool _started;

    enum {
      FPGA_COUNT_ENABLE_PIN = 0,      // PF0
      FPGA_DONE_COUNTING_PIN = 15,    // PC15
      ACTIVE_LED_PIN = 14             // PB14
    };

  protected:
    void onInterrupt(uint8_t extiLine);

  public:
    FrequencyCounter(FpgaCommunicator& fpgaCommunicator);

    DECLARE_EVENT_SOURCE(FrequencyCountReady);

    void start();
    void stop();

    bool isStarted() const;
    void readCounters(Counters& counters) const;
};


/*
 * Constructor
 */

inline FrequencyCounter::FrequencyCounter(FpgaCommunicator& fpgaCommunicator)
  : _fpgaCommunicator(fpgaCommunicator) {

  GpioF<DefaultDigitalOutputFeature<FPGA_COUNT_ENABLE_PIN>> pf;
  GpioC<DefaultDigitalInputFeature<FPGA_DONE_COUNTING_PIN>> pc;
  GpioB<DefaultDigitalOutputFeature<ACTIVE_LED_PIN>> pb;

  // not started

  _started=false;

  // initialise the count enable pin and disable it

  _fpgaCountEnable=pf[FPGA_COUNT_ENABLE_PIN];
  _fpgaCountEnable.reset();

  // get a reference to the active LED and turn it off

  _activeLed=pb[ACTIVE_LED_PIN];
  _activeLed.reset();

  // set up an EXTI subscriber on the done counting pin

  _fpgaDoneCounting=pc[FPGA_DONE_COUNTING_PIN];

  _exti=new Exti15(EXTI_Mode_Interrupt,EXTI_Trigger_Rising_Falling,pc[FPGA_DONE_COUNTING_PIN]);
  _exti->ExtiInterruptEventSender.insertSubscriber(
    ExtiInterruptEventSourceSlot::bind(this,&FrequencyCounter::onInterrupt)
  );
}


/*
 * Start the FPGA counter. We will be notified asynchronously when it's completed.
 */

inline void FrequencyCounter::start() {

  Counters c;

  _fpgaCommunicator.read(c);      // initialise the gate counter
  _fpgaCountEnable.set();

  _started=true;
}


/*
 * Stop the FPGA counter.
 */

inline void FrequencyCounter::stop() {
  _fpgaCountEnable.reset();
  _started=false;
}


/*
 * Interrupt callback from the done counting FPGA pin changing state
 */

__attribute__((noinline)) inline void FrequencyCounter::onInterrupt(uint8_t /* extiLine */) {

  bool doneCounting=_fpgaDoneCounting.read();

  // active LED lit while still counting

  _activeLed.setState(!doneCounting);

  if(_started && doneCounting) {

    // notify that a new count is ready

    FrequencyCountReadyEventSender.raiseEvent();
  }
}


/*
 * Read the counters from the FPGA
 */

inline void FrequencyCounter::readCounters(Counters& counters) const {
  _fpgaCommunicator.read(counters);
}


/*
 * Check if started
 */

inline bool FrequencyCounter::isStarted() const {
  return _started;
}
