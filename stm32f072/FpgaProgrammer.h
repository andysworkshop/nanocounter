/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


// these are in bitfile.asm and tell us where the FPGA bitstream has landed in flash

extern uint32_t BitFileStart,BitFileSize;


/*
 * Encapsulate the code needed to upload a bitstream to the FPGA from an embedded location
 * in MCU flash using the Xilinx 'slave serial' mode.
 */

class FpgaProgrammer {

  protected:
    
    /*
     * FPGA GPIO pins
     */
    
    enum {
      PROG_B = 13,    // PC13
      INIT_B = 3,     // PA3
      DONE   = 5,     // PB5
      CCLK   = 1,     // PF1
      DIN    = 6,     // PA6
    };

    /*
     * Error codes
     */

    enum {
      E_FPGA_NOT_READY = 1,
      E_WRITE_FAIL = 2,
      E_FINISH = 3
    };

    const LinkLed& _linkLed;
    
  protected:
    void fail(uint8_t errorCode) const;

  public:
    FpgaProgrammer(const LinkLed& linkLed);

  public:
    void program() const;
};


/*
 * Constructor
 */

inline FpgaProgrammer::FpgaProgrammer(const LinkLed& linkLed)
  : _linkLed(linkLed) {

}


/*
 * Do the programming
 */

inline void FpgaProgrammer::program() const {

  uint8_t *ptr,nextByte,i;
  bool doneFlag;
  uint32_t count,bitSize;

  GpioA<
    DefaultDigitalOutputFeature<DIN>,
    DefaultDigitalInputFeature<INIT_B>
  > pa;

  GpioC<DefaultDigitalOutputFeature<PROG_B>> pc;
  GpioF<DefaultDigitalOutputFeature<CCLK>> pf;
  GpioB<DefaultDigitalInputFeature<DONE>> pb;

  // set up the pins for easier access
  
  GpioPinRef progb=pc[PROG_B];
  GpioPinRef initb=pa[INIT_B];
  GpioPinRef done=pb[DONE];
  GpioPinRef cclk=pf[CCLK];
  GpioPinRef din=pa[DIN];
  
  // set the link LED

  _linkLed.set();

  // hold PROG_B low for a few ms and bring the clock low

  progb.reset();
  cclk.reset();
  MillisecondTimer::delay(10);
  progb.set();

  // INIT_B must now go high indicating that the FPGA is ready to receive data.
  // Give it 5 seconds before giving up.

  uint32_t start=MillisecondTimer::millis();
  while(!initb.read()) 
    if(MillisecondTimer::hasTimedOut(start,5000))
      fail(E_FPGA_NOT_READY);

  // supply the data and clocks until INIT_B goes low (error) or DONE goes
  // high (finished).

  // probably unnecessary, but there is a defined min time between INIT_B(low) and first CCLK

  MillisecondTimer::delay(1);

  doneFlag=false;
  count=0;
  bitSize=reinterpret_cast<uint32_t>(&BitFileSize);

  for(ptr=reinterpret_cast<uint8_t *>(&BitFileStart);;ptr++) {

    if(!doneFlag && done.read())
      doneFlag=true;

    // check for error

    if(!doneFlag && !initb.read())
      fail(E_WRITE_FAIL);

    // check for end

    if(count==bitSize)
      break;

    // read the next byte

    nextByte=*ptr;

    /*
     * Generate clocks for the data. The max Spartan 3 CCLK is 66MHz (no compression) / 20MHz
     * (with compression).
     */

    for(i=0;i<8;i++) {

      // set DIN

      if((nextByte & 0x80)==0)
        din.reset();
      else
        din.set();

      // ensure clock is low

      cclk.reset();

      // shift byte for next output

      nextByte<<=1;

      // bring clock high (data transfer)

      cclk.set();
    }

    count++;
  }

  /*
   * Docs say that there may be some extra CCLK cycles at the end of the bitstream
   * but is not clear as to whether they're included in the .bit file or not. To be
   * safe we'll generate extra cycles if DONE has not gone high
   */

  while(!done.read()) {

    if(!initb.read())
      fail(E_FINISH);

    cclk.reset();
    cclk.set();
  }

  // turn off the link LED

  _linkLed.reset();
}


/*
 * Flash an error code and lock up
 */

void FpgaProgrammer::fail(uint8_t errorCode) const {

  uint8_t i;

  for(;;) {

    for(i=0;i<errorCode;i++) {
      _linkLed.set();
      MillisecondTimer::delay(200);
      _linkLed.reset();
      MillisecondTimer::delay(200);
    }

    MillisecondTimer::delay(3000);
  }
}
