/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * The FPGA communicator operates the SPI mode 3 protocol to transfer a new gate count
 * into the FPGA and retrieve the last count
 */

class FpgaCommunicator {

  protected:
    enum {
      NCS = 14,     // PC14
      MOSI = 3,     // PB3
      MISO = 8,     // PB8
      SCK  = 9,     // PB9
    };

    GpioPinRef _ncs;
    GpioPinRef _sck;
    GpioPinRef _mosi;
    GpioPinRef _miso;

    const Preferences& _preferences;

  protected:
    uint32_t sendReceive32(uint32_t dataToSend);

  public:
    FpgaCommunicator(const Preferences& preferences);

    void read(Counters& counters);
};


/*
 * Constructor
 */

inline FpgaCommunicator::FpgaCommunicator(const Preferences& preferences)
  : _preferences(preferences) {

  GpioB<DefaultDigitalOutputFeature<MOSI,SCK>,DefaultDigitalInputFeature<MISO>> pb;
  GpioC<DefaultDigitalOutputFeature<NCS>> pc;

  _ncs=pc[NCS];
  _sck=pb[SCK];
  _mosi=pb[MOSI];
  _miso=pb[MISO];

  // default states

  _ncs.set();
  _sck.set();
}


/*
 * Read the counters from the FPGA
 */

inline void FpgaCommunicator::read(Counters& counters) {

  // enable SPI

  _ncs.reset();

  counters.reference=sendReceive32(_preferences.getSettings().gateCounter);
  counters.sample=sendReceive32(0);

  // disable SPI

  _ncs.set();
}


/*
 * Send and receive 32 bits
 */

inline uint32_t FpgaCommunicator::sendReceive32(uint32_t dataToSend) {

  uint8_t i;
  uint32_t received;

  received=0;

  for(i=0;i<32;i++) {

    // clock out a bit

    _sck.reset();
    _mosi.setState((dataToSend & 0x80000000)!=0);
    _sck.set();

    dataToSend<<=1;

    // read the clocked in bit

    received<<=1;

    if(_miso.read())
      received|=1;
  }

  return received;
}
