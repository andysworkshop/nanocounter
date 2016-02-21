/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


class LedBase {

  protected:
    GpioPinRef _pin;

  public:
    LedBase(GPIO_TypeDef *port,uint16_t pin);

    void set() const;
    void reset() const;
    void setState(bool state) const;
};


/*
 * Constructor
 */

inline LedBase::LedBase(GPIO_TypeDef *port,uint16_t pin)
  : _pin(port,pin) {

  // initialise the pin for output and reset it

  GpioPinInitialiser::initialise(port,pin,Gpio::OUTPUT);
  reset();
}


/*
 * Set a pin
 */

inline void LedBase::set() const {
  _pin.set();
}


/*
 * Reset a pin
 */

inline void LedBase::reset() const {
  _pin.reset();
}


/*
 * Set pin state
 */

inline void LedBase::setState(bool state) const {
  _pin.setState(state);
}


/*
 * Generic template for any LED. Very little is done here to avoid pointless
 * duplication of template methods in flash that don't depend on a template parameter.
 */

template<class TGpio>
struct Led : LedBase {

  public:
    Led()
      : LedBase(reinterpret_cast<GPIO_TypeDef *>(TGpio::Port),TGpio::Pin) {
    }
};


/*
 * Types for the LEDs on this board
 */

typedef Led<stm32plus::gpio::PB4> LinkLed;
typedef Led<stm32plus::gpio::PB13> XrefLed;
typedef Led<stm32plus::gpio::PB14> ActiveLed;
typedef Led<stm32plus::gpio::PB12> LockLed;
