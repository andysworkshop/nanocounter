/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Class to read the temperature once per second
 */


DECLARE_EVENT_SIGNATURE(TemperatureReady,void(uint16_t));

class TemperatureSensor {

  protected:
    typedef Adc1DmaChannel<AdcDmaFeature<Adc1PeripheralTraits>,Adc1DmaChannelInterruptFeature> MyDma;
    typedef Timer3<Timer3InternalClockFeature> MyTimer;
    typedef Adc1<
      AdcAsynchronousClockModeFeature,                          // the free-running 14MHz HSI
      AdcResolutionFeature<12>,                                 // 12 bit resolution
      Adc1Cycle239TemperatureSensorFeature,                     // temperature sensor
      Adc1Cycle239InternalReferenceVoltageFeature,              // internal reference voltage
      AdcTimer3TriggerRisingFeature<AdcTriggerSource::Update>   // using timer 3 trigger-on-update
    > MyAdc;

    volatile uint16_t _adcBuffer[2];

    MyDma *_dma;
    MyAdc *_adc;
    MyTimer *_timer;

  protected:
    void onInterrupt(DmaEventType det);

  public:
    TemperatureSensor();

    DECLARE_EVENT_SOURCE(TemperatureReady);
};


/*
 * Constructor
 */

inline TemperatureSensor::TemperatureSensor() {

  // set up the timer

  _timer=new MyTimer;
  _timer->setTimeBaseByFrequency(8000,7999);
  _timer->enablePeripheral();

  // set up DMA

  _dma=new MyDma;
  _dma->DmaInterruptEventSender.insertSubscriber(
      DmaInterruptEventSourceSlot::bind(this,&TemperatureSensor::onInterrupt)
    );

  // set up the ADC

  _adc=new MyAdc;

  _dma->enableInterrupts(Adc1DmaChannelInterruptFeature::COMPLETE);
  _dma->beginRead(_adcBuffer,2);

  _adc->startRegularConversion();
}


/*
 * The temperature is ready
 */

inline void TemperatureSensor::onInterrupt(DmaEventType det) {

  if(det==DmaEventType::EVENT_COMPLETE) {

    int32_t temp30,temp110,vrefint33,temperature;

    temp30=*reinterpret_cast<const int16_t *>(0x1FFFF7B8);
    temp110=*reinterpret_cast<const int16_t *>(0x1FFFF7C2);
    vrefint33=*reinterpret_cast<const int16_t *>(0x1FFFF7BA);

    temperature=((_adcBuffer[0]*vrefint33)/_adcBuffer[1])-temp30;
    temperature*=80000;    // 110-30
    temperature/=(temp110-temp30);
    temperature+=30000;

    TemperatureReadyEventSender.raiseEvent(temperature);
  }
}
