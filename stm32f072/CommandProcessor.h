/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Class to handle the request/response management
 */

class CommandProcessor {

  protected:
    Preferences& _preferences;
    ClockFilters _clockFilters;
    AD9553& _pll;
    BluetoothCommunicator _bluetoothCommunicator;
    volatile bool _pllLockState;
    volatile uint32_t _lastRequestTime;
    Counters _counters;
    uint32_t _countersSequenceNumber;
    const LinkLed& _linkLed;
    TemperatureSensor _temperatureSensor;

    volatile uint16_t _temperature;       // zero if not ready
    uint8_t _response[28];                // 28 is the largest response (access to this is validated with static_assert's to avoid accidental buffer overflow)

  protected:
    void onPllLockChanged(bool lockState);
    void onRequestReceived(const Request& request);
    void onTemperatureReady(uint16_t temperature);

  public:
    CommandProcessor(Preferences& preferences,PllLockIndicator& pllLockIndicator,AD9553& pll,const LinkLed& linkLed);
    void start();

    void run();

    void setCounters(const Counters& counters);

    uint16_t getMeasuredFrequency(const Request& request);
    uint16_t setGateCounter(const SetGateCounterRequest& request);
    uint16_t setSampleFilter(const SetSampleFilterRequest& request);
    uint16_t setReferenceFilter(const SetReferenceFilterRequest& request);
    uint16_t setReferenceClockSource(const SetReferenceClockSourceRequest& request);
    uint16_t setCalibration(const SetCalibrationRequest& request);
    uint16_t resetPll(const ResetPllRequest& request);
    uint16_t getCalibration(const GetCalibrationRequest& request);
    uint16_t getCalibrationBitmap(const GetCalibrationBitmapRequest& request);
};


/*
 * Constructor
 */

inline CommandProcessor::CommandProcessor(Preferences& preferences,PllLockIndicator& pllLockIndicator,AD9553& pll,const LinkLed& linkLed)
  : _preferences(preferences),
    _clockFilters(preferences),
    _pll(pll),
    _linkLed(linkLed) {

  // reset the temperature

  _temperature=0;

  // reset the last request time to some large value

  _lastRequestTime=UINT32_MAX/2;

  // reset the counters sequence number

  _countersSequenceNumber=0;

  // subscribe to new temperature readings

  _temperatureSensor.TemperatureReadyEventSender.insertSubscriber(
      TemperatureReadyEventSourceSlot::bind(this,&CommandProcessor::onTemperatureReady));

  // subscribe to changes from the lock indicator

  _pllLockState=pllLockIndicator.isLocked();

  pllLockIndicator.PllLockChangedEventSender.insertSubscriber(
      PllLockChangedEventSourceSlot::bind(this,&CommandProcessor::onPllLockChanged));

  // subscribe to the request received event

  _bluetoothCommunicator.RequestReceivedEventSender.insertSubscriber(
      RequestReceivedEventSourceSlot::bind(this,&CommandProcessor::onRequestReceived));
}


/*
 * Startup
 */

inline void CommandProcessor::start() {

  // start bluetooth

  _bluetoothCommunicator.start();
}


/*
 * The state of the PLL lock changed. This is IRQ code.
 */

inline void CommandProcessor::onPllLockChanged(bool lockState) {
  _pllLockState=lockState;
}


/*
 * Set new counters
 */

inline void CommandProcessor::setCounters(const Counters& counters) {
  _counters=counters;
  _countersSequenceNumber++;
}


/*
 * Check for activity and turn off the link LED if no command has been received in the last second
 */

inline void CommandProcessor::run() {
  _linkLed.setState(!MillisecondTimer::hasTimedOut(_lastRequestTime,1000));
}


/*
 * A valid request received from app
 */

inline void CommandProcessor::onRequestReceived(const Request& request) {

  uint16_t size;

  _lastRequestTime=MillisecondTimer::millis();

  // process the request

  switch(request.requestId) {

    case Request::GET_MEASURED_FREQUENCY:
      size=getMeasuredFrequency(request);
      break;

    case Request::SET_REF_CLOCK_SOURCE:
      size=setReferenceClockSource(static_cast<const SetReferenceClockSourceRequest&>(request));
      break;

    case Request::SET_GATE_COUNTER:
      size=setGateCounter(static_cast<const SetGateCounterRequest&>(request));
      break;

    case Request::SET_XREF_CLOCK_FILTER:
      size=setReferenceFilter(static_cast<const SetReferenceFilterRequest&>(request));
      break;

    case Request::SET_SAMPLE_CLOCK_FILTER:
      size=setSampleFilter(static_cast<const SetSampleFilterRequest&>(request));
      break;

    case Request::RESET_PLL:
      size=resetPll(static_cast<const ResetPllRequest&>(request));
      break;

    case Request::GET_CALIBRATION:
      size=getCalibration(static_cast<const GetCalibrationRequest&>(request));
      break;

    case Request::SET_CALIBRATION:
      size=setCalibration(static_cast<const SetCalibrationRequest&>(request));
      break;

    case Request::GET_CALIBRATION_BITMAP:
      size=getCalibrationBitmap(static_cast<const GetCalibrationBitmapRequest&>(request));
      break;

    default:
      size=0;
      break;
  }

  // send the response back

  if(size)
    _bluetoothCommunicator.sendResponse(_response,size);
}


/*
 * GET_MEASURED_FREQUENCY handler
 */

inline uint16_t CommandProcessor::getMeasuredFrequency(const Request& request) {

  GetMeasuredFrequencyResponse response;

  static_assert(sizeof(_response)>=sizeof(response),"_response needs to be increased in size");

  // copy over the request sequence number

  response.requestSequenceNumber=request.requestSequenceNumber;

  // we don't want interrupts to happen while we're preparing a response

  Nvic::disableAllInterrupts();

  // some errors are possible

  if(!_pllLockState)
    response.responseCode=GetMeasuredFrequencyResponse::E_PLL_NOT_LOCKED;
  else if(_countersSequenceNumber==0)
    response.responseCode=GetMeasuredFrequencyResponse::E_NO_COUNTERS;
  else {

    // it's OK

    response.responseCode=Response::OK;

    response.sampleSequenceNumber=_countersSequenceNumber;
    response.referenceFrequency=_preferences.getSettings().refClock;
    response.referenceCounter=_counters.reference;
    response.sampleCounter=_counters.sample;
    response.mcuTemperature=_temperature;
  }

  // interrupts are OK now

  Nvic::enableAllInterrupts();

  memcpy(_response,&response,sizeof(response));
  return sizeof(response);
}


/*
 * SET_GATE_COUNTER command
 */

inline uint16_t CommandProcessor::setGateCounter(const SetGateCounterRequest& request) {

  SetGateCounterResponse response;

  static_assert(sizeof(_response)>=sizeof(response),"_response needs to be increased in size");

  // one second is the lowest that can be set and the highest is 2^31-1 (31 bits)
  // this an effective range of 1 to 10 seconds at refclk=200MHz

  if(request.gateCounter>=_preferences.getSettings().refClock &&
     request.gateCounter<0x80000000) {

    // change the preferences. the new gate counter will be updated in the next poll to the FPGA.

    _preferences.getSettings().gateCounter=request.gateCounter;
    _preferences.write();

    response.responseCode=Response::OK;
  }
  else
    response.responseCode=SetGateCounterResponse::E_OUT_OF_RANGE;

  // copy over the request sequence number

  response.requestSequenceNumber=request.requestSequenceNumber;

  memcpy(_response,&response,sizeof(response));
  return sizeof(response);
}


/*
 * SET_REF_CLOCK_SOURCE command
 */

inline uint16_t CommandProcessor::setReferenceClockSource(const SetReferenceClockSourceRequest& request) {

  SetReferenceClockSourceResponse response;

  static_assert(sizeof(_response)>=sizeof(response),"_response needs to be increased in size");

  // change the preferences if the new value is different

  if(_preferences.getSettings().refClockSource!=request.refClockSource) {

    _preferences.getSettings().refClockSource=request.refClockSource;
    _preferences.write();

    _pll.updateFromPreferences(_preferences);
  }

  // copy over the request sequence number

  response.requestSequenceNumber=request.requestSequenceNumber;
  response.responseCode=Response::OK;

  memcpy(_response,&response,sizeof(response));
  return sizeof(response);
}


/*
 * SET_XREF_CLOCK_FILTER command
 */

inline uint16_t CommandProcessor::setReferenceFilter(const SetReferenceFilterRequest& request) {

  SetReferenceFilterResponse response;

  static_assert(sizeof(_response)>=sizeof(response),"_response needs to be increased in size");

  // change the preferences if the new value is different

  if(_preferences.getSettings().refClockFilter!=request.filterBandwidth) {

    _preferences.getSettings().refClockFilter=request.filterBandwidth;
    _preferences.write();

    _clockFilters.setFiltersFromPreferences(_preferences);
  }

  // copy over the request sequence number

  response.requestSequenceNumber=request.requestSequenceNumber;
  response.responseCode=Response::OK;

  memcpy(_response,&response,sizeof(response));
  return sizeof(response);
}


/*
 * SET_SAMPLE_CLOCK_FILTER command
 */

inline uint16_t CommandProcessor::setSampleFilter(const SetSampleFilterRequest& request) {

  SetSampleFilterResponse response;

  static_assert(sizeof(_response)>=sizeof(response),"_response needs to be increased in size");

  // change the preferences if the new value is different

  if(_preferences.getSettings().sampleClockFilter!=request.filterBandwidth) {

    _preferences.getSettings().sampleClockFilter=request.filterBandwidth;
    _preferences.write();

    _clockFilters.setFiltersFromPreferences(_preferences);
  }

  // copy over the request sequence number

  response.requestSequenceNumber=request.requestSequenceNumber;
  response.responseCode=Response::OK;

  memcpy(_response,&response,sizeof(response));
  return sizeof(response);
}


/*
 * RESET_PLL command
 */

inline uint16_t CommandProcessor::resetPll(const ResetPllRequest& request) {

  ResetPllResponse response;

  static_assert(sizeof(_response)>=sizeof(response),"_response needs to be increased in size");

  // restart it

  _pll.start();

  // copy over the request sequence number

  response.requestSequenceNumber=request.requestSequenceNumber;
  response.responseCode=Response::OK;

  memcpy(_response,&response,sizeof(response));
  return sizeof(response);
}


/*
 * Get the calibration usage map. Calibration data is stored in flash. Flash is initialized to 0xFF
 * when the page is erased. Any calibration array entry starting with 0xFF is unused.
 */

inline uint16_t CommandProcessor::getCalibrationBitmap(const GetCalibrationBitmapRequest& request) {

  GetCalibrationBitmapResponse response;
  uint8_t bitIndex,byteIndex,i;
  const uint8_t *ptr;

  static_assert(sizeof(_response)>=sizeof(response),"_response needs to be increased in size");

  // copy over the request sequence number

  response.requestSequenceNumber=request.requestSequenceNumber;
  response.responseCode=Response::OK;

  // zero out the bitmap

  memset(&response.bitmap,'\0',sizeof(response.bitmap));

  // process each of the entries

  ptr=reinterpret_cast<const uint8_t *>(CalibrationArray::BASE_ADDRESS);

  for(bitIndex=byteIndex=i=0;i<CalibrationArray::MAX_ENTRIES;i++) {

    // if the calibration array entry is used then mark it in the bitmap

    if(*ptr!=0xff)
      response.bitmap[byteIndex] |= 1<<bitIndex;

    ptr+=sizeof(Calibration);

    if(bitIndex==7) {
      bitIndex=0;
      byteIndex++;
    }
    else
      bitIndex++;
  }

  // done, copy over the response

  memcpy(_response,&response,sizeof(response));
  return sizeof(response);
}


/*
 * Get a calibration entry
 */

inline uint16_t CommandProcessor::getCalibration(const GetCalibrationRequest& request) {

  GetCalibrationResponse response;

  static_assert(sizeof(_response)>=sizeof(response),"_response needs to be increased in size");

  // copy over the request sequence number

  response.requestSequenceNumber=request.requestSequenceNumber;
  response.responseCode=Response::OK;

  // copy the calibration entry from flash. The last 2Kb page in this 128Kb device
  // is at address 0x1F800

  memcpy(&response.calibration,
         reinterpret_cast<const void *>(CalibrationArray::BASE_ADDRESS+sizeof(Calibration)*request.index),
         sizeof(Calibration));

  memcpy(_response,&response,sizeof(response));
  return sizeof(response);
}


/*
 * Set a calibration entry
 */

inline uint16_t CommandProcessor::setCalibration(const SetCalibrationRequest& request) {

  SetCalibrationResponse response;

  static_assert(sizeof(_response)>=sizeof(response),"_response needs to be increased in size");

  // do it

  CalibrationArray::writeCalibration(request.index,request.calibration);

  // copy over the request sequence number

  response.requestSequenceNumber=request.requestSequenceNumber;
  response.responseCode=Response::OK;

  memcpy(_response,&response,sizeof(response));
  return sizeof(response);
}


/*
 * New temperature is ready
 */

inline void CommandProcessor::onTemperatureReady(uint16_t temperature) {
  _temperature=temperature;
}
