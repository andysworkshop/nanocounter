/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Class to monitor the PLL lock indicator and raise events when it changes
 */

DECLARE_EVENT_SIGNATURE(PllLockChanged,void(bool));

class PllLockIndicator {

  protected:
    GpioPinRef _pllLocked;
    const LockLed& _led;
    Exti0 *_exti;

  protected:
    void onInterrupt(uint8_t extiLine);

  public:
    PllLockIndicator(const LockLed& led);

    DECLARE_EVENT_SOURCE(PllLockChanged);

    bool isLocked() const;
};


/*
 * Constructor
 */

inline PllLockIndicator::PllLockIndicator(const LockLed& led)
  : _led(led) {

  GpioA<DefaultDigitalInputFeature<0>> pa;

  _exti=new Exti0(EXTI_Mode_Interrupt,EXTI_Trigger_Rising_Falling,pa[0]);

  // save pin reference

  _pllLocked=pa[0];

  // subscribe to EXTI events on the lock pin

  _exti->ExtiInterruptEventSender.insertSubscriber(
      ExtiInterruptEventSourceSlot::bind(this,&PllLockIndicator::onInterrupt)
  );
}


/*
 * Poll the locked pin
 */

inline bool PllLockIndicator::isLocked() const {
  return _pllLocked.read();
}


/*
 * Interrupt received from pin change
 */

inline void PllLockIndicator::onInterrupt(uint8_t /* extiLine */) {

  bool locked=isLocked();

  // set the LED

  _led.setState(locked);

  // forward change to subscribers

  PllLockChangedEventSender.raiseEvent(locked);
}
