/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */

#pragma once


/*
 * Class to manage the communication with the AD9553
 */

class AD9553 {

  protected:

    enum {
      SEL_REFB = 1,         // PA1
      XREF_SHUTDOWN = 2,    // PA2
      PLL_RESET = 15        // PA15
    };

    typedef Spi1<SpiFifoNotifyQuarterFullFeature> MySpi;
    MySpi *_spi;

    XrefLed _xrefLed;
    GpioPinRef _resetPin;
    GpioPinRef _selXrefPin;
    GpioPinRef _xrefShutdownPin;      // this is on the LTC6957

  protected:
    void reset() const;
    void commit() const;
    void calibrate() const;

    uint8_t readRegister(uint8_t reg) const;
    void writeRegister(uint8_t reg,uint8_t value) const;
    void setReferenceInput(const Preferences& preferences) const;

  public:
    AD9553(const Preferences& preferences);
    void start() const;

    void updateFromPreferences(const Preferences& preferences) const;
};


/*
 * Constructor
 */

inline AD9553::AD9553(const Preferences& preferences) {

  // initialise the pins

  GpioA<DefaultDigitalOutputFeature<SEL_REFB,PLL_RESET,XREF_SHUTDOWN>> pa;

  _resetPin=pa[PLL_RESET];
  _selXrefPin=pa[SEL_REFB];
  _xrefShutdownPin=pa[XREF_SHUTDOWN];

  // set the external reference selection

  setReferenceInput(preferences);

  // initialise the SPI interface at the slowest speed

  MySpi::Parameters params;

  params.spi_mode=SPI_Mode_Master;
  params.spi_direction=SPI_Direction_1Line_Tx;
  params.spi_baudRatePrescaler=SPI_BaudRatePrescaler_256;

  _spi=new MySpi(params);
}


/*
 * Reset the device
 */

inline void AD9553::reset() const {

  // hard reset

  _resetPin.set();
  MillisecondTimer::delay(100);
  _resetPin.reset();
  MillisecondTimer::delay(100);
  _resetPin.set();
  MillisecondTimer::delay(300);
}


/*
 * Set the reference input pins and LEDs
 */

inline void AD9553::setReferenceInput(const Preferences& preferences) const {

  if(preferences.getSettings().refClockSource==RefClockSource::INTERNAL_10M) {

    // the reference clock source is internal

    _selXrefPin.reset();        // SEL REFB = false
    _xrefShutdownPin.set();     // XREF shutdown = true
    _xrefLed.reset();           // XREF LED = off
  }
  else {

    // the reference clock source is external

    _selXrefPin.set();          // SEL REFB = true
    _xrefShutdownPin.reset();   // XREF shutdown = false
    _xrefLed.set();             // XREF LED = on
  }
}


/*
 * Update the external reference clock source from preferences and restart the PLL
 */

inline void AD9553::updateFromPreferences(const Preferences& preferences) const {

  setReferenceInput(preferences);
  start();
}


/*
 * Initialise the device
 */

inline void AD9553::start() const {

  uint8_t i;

  // reset the device

  reset();

  // the full register map exported from the EV module software

  static const uint8_t regmap[][2] = {
    { 0x00, 0x18 },
    { 0x02, 0x00 },
    { 0x04, 0x00 },
    { 0x05, 0x00 },
    { 0x0A, 0xD0 },
    { 0x0B, 0xB0 },
    { 0x0C, 0x00 },
    { 0x0D, 0x00 },
    { 0x0E, 0x74 },
    { 0x0F, 0x80 },
    { 0x10, 0x80 },
    { 0x11, 0x00 },
    { 0x12, 0x1D },
    { 0x13, 0xB0 },
    { 0x14, 0x0C },

    { 0x15, 0x00 },   // 200MHz
    { 0x16, 0x80 },

  //  { 0x15, 0x01 },   // 100MHz
  //  { 0x16, 0x00 },

    { 0x17, 0x21 },
    { 0x18, 0xC0 },

    { 0x19, 0x20 },
    { 0x1A, 0x00 },
    { 0x1B, 0x80 },
    { 0x1C, 0x00 },
    { 0x1D, 0x00 },
    { 0x1F, 0x04 },
    { 0x20, 0xC2 },
    { 0x21, 0xA0 },
    { 0x23, 0x04 },
    { 0x24, 0xC2 },
    { 0x25, 0xA0 },
    { 0x27, 0x40 },
    { 0x28, 0x00 },
    { 0x29, 0x00 },
    { 0x32, 0x89 },
    { 0x34, 0xC9 },
    { 0x35, 0x00 },
    { 0x36, 0x80 },
    { 0x37, 0x0C },

    { 0x38, 0xDC },
    { 0x39, 0xFF },
    { 0xFE, 0x00 },
    { 0xFF, 0x00 }
  };

  // program all registers in sequence

  for(i=0;i<sizeof(regmap)/sizeof(regmap[0]);i++)
    writeRegister(regmap[i][0],regmap[i][1]);

  commit();

  // calibrate and delay to allow it to complete

  calibrate();
  MillisecondTimer::delay(300);
}


/*
 * Commit changes to the registers
 */

inline void AD9553::commit() const {
  writeRegister(0x5,1);     // I/O update
}


/*
 * Calibrate the device. This command commits itself
 */

inline void AD9553::calibrate() const {

  uint8_t value;

  // calibrate VCO

  value=readRegister(0xe);
  value|=0x80;

  MillisecondTimer::delay(10);

  writeRegister(0xe,value);
  commit();
}


/*
 * Write a value to a register
 */

inline void AD9553::writeRegister(uint8_t reg,uint8_t value) const {

  uint8_t bytes[3];

  // set up the command bytes

  bytes[0]=0;
  bytes[1]=reg;
  bytes[2]=value;

  // send the command

  _spi->enablePeripheral();
  _spi->setNss(false);
  _spi->send(bytes,sizeof(bytes),nullptr);
  _spi->waitForIdle();
  _spi->setNss(true);
}


/*
 * Send command, receive response
 */

inline uint8_t AD9553::readRegister(uint8_t reg) const {

  uint8_t bytes[2],value;

  // set up the command bytes

  bytes[0]=0x80;
  bytes[1]=reg;

  // send the command

  _spi->enablePeripheral();
  _spi->setNss(false);
  _spi->send(bytes,sizeof(bytes),nullptr);
  _spi->waitForIdle();

  // read the value back

  _spi->drainFifo();
  _spi->set1WireReadMode();
  _spi->enablePeripheral();

  // the SPI clock is free running so do this efficiently

   while(!_spi->readyToReceive());
   value=_spi->receiveData8(*_spi);

   // got it, set CS and stop the clock

   _spi->setNss(true);
   _spi->set1WireWriteMode();

   return value;
}
