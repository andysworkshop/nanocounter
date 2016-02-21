/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Structure to hold a single calibration value.
 */

struct Calibration {
  char date[8];           // YYYYMMDD in ASCII
  uint16_t temperature;   // deg C x 10. e.g. 25.3C = 253
  uint32_t cal_int;       // integer part of the cal value. e.g. 4.723 = 4
  uint32_t cal_frac;      // fractional part of the cal value. e.g. 4.723 = 723
} __attribute__((packed));


/*
 * Class to hold and manipulate calibration values. Calibrations are held as an array in a 2Kb flash
 * page. A history of calibration values is maintained. The android device requests the whole array
 * and then sends back individual array entries to be written. It's up to the device how it chooses
 * to present the array to the user although in practice this means a sorted list that can be appended
 * to with new values. If the array fills up then the device will overwrite the oldest.
 */

class CalibrationArray {

  public:

    enum {
      BASE_ADDRESS=FLASH_BASE+0x1F800,
      MAX_ENTRIES=112
    };

  public:
    static void writeCalibration(uint8_t index,const Calibration& calibration);
};


/*
 * Write a calibration array entry to flash. This results in an erase/program cycle which means
 * that we can assume about 10K calls to this method before the page wears out. That should be more
 * than enough. Who calibrates more than 10K times?
 */

inline void CalibrationArray::writeCalibration(uint8_t index,const Calibration& calibration) {

  // declare a flash class

  InternalFlashDevice<InternalFlashWriteFeature> flash;
  const uint32_t *ptr;

  // this is large but we've got 16Kb total SRAM in this STM32F072 device and we're
  // not keeping it around for long.

  uint8_t buffer[2048];

  // copy out the current content of the page

  memcpy(buffer,reinterpret_cast<const void *>(BASE_ADDRESS),sizeof(buffer));

  // copy in the new calibration entry

  memcpy(&buffer[index*sizeof(Calibration)],&calibration,sizeof(Calibration));

  // flash needs to be unlocked for the erase/program steps to work

  InternalFlashLockFeature::LockManager lm;

  // erase the page

  flash.pageErase(BASE_ADDRESS);

  // program the whole page in 32-bit steps

  ptr=reinterpret_cast<const uint32_t *>(buffer);

  for(uint16_t i=0;i<2048/4;i++)
    flash.wordProgram(BASE_ADDRESS+i*4,*ptr++);
}
