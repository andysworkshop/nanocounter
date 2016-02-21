/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Preferences are stored in MCU flash and read on-demand.
 */

class Preferences {

  public:
    struct Settings {
      uint32_t gateCounter;
      uint32_t refClock;
      FilterBandwidth refClockFilter;
      FilterBandwidth sampleClockFilter;
      RefClockSource refClockSource;

      Settings() {
        gateCounter=200000000;
        refClock=200000000;
        refClockFilter=FilterBandwidth::BW_FULL;
        sampleClockFilter=FilterBandwidth::BW_FULL;
        refClockSource=RefClockSource::INTERNAL_10M;
      }
    };

  protected:
    typedef InternalFlashDevice<InternalFlashWriteFeature> MyFlash;
    typedef InternalFlashSettingsStorage<Settings,MyFlash> MySettingsStorage;

    MyFlash _flash;
    Settings _settings;

  protected:
    MySettingsStorage *getStorage() const;

  public:
    Preferences();

    Settings& getSettings();
    const Settings& getSettings() const;

    void read();
    void write();
};


/*
 * Constructor
 */

inline Preferences::Preferences() {
  read();
}


/*
 * Read settings from flash
 */

inline void Preferences::read() {

  MySettingsStorage *storage;

  // read the settings if there are any

  storage=getStorage();
  storage->read(_settings);

  delete storage;
}


/*
 * Write settings to flash
 */

inline void Preferences::write() {

  MySettingsStorage *storage;

  // write settings to flash

  storage=getStorage();
 // storage->write(_settings);  XXX: re-enable when stm32plus supports 072

  delete storage;
}


/*
 * Get the settings reference
 */

inline Preferences::Settings& Preferences::getSettings() {
  return _settings;
}

inline const Preferences::Settings& Preferences::getSettings() const {
  return _settings;
}


/*
 * Get the storage class
 */

inline Preferences::MySettingsStorage *Preferences::getStorage() const {

  MySettingsStorage::Parameters params;

  // 2Kb at the top of the 128Kb flash is used (1 page on the STM32F072)

  params.firstLocation=FLASH_BASE+(128*1024)-2048;
  params.memorySize=2048;

  return new MySettingsStorage(_flash,params);
}
