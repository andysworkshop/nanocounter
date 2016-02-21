/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Manage the setting of the filters for one of the clocks
 */

class ClockFilter {

  protected:
    GpioPinRef _apin;
    GpioPinRef _bpin;

  public:
    ClockFilter(GpioPinRef apin,GpioPinRef bpin);

    void setBandwidth(FilterBandwidth bw) const;
};


/*
 * Constructor
 */

inline ClockFilter::ClockFilter(GpioPinRef apin,GpioPinRef bpin)
  : _apin(apin),
    _bpin(bpin) {
}


/*
 * Set a new bandwidth
 */

inline void ClockFilter::setBandwidth(FilterBandwidth bw) const {

  static const bool table[4][2]={
    { false,false },
    { true,false },
    { false,true },
    { true,true }
  };

  _apin.setState(table[static_cast<uint8_t>(bw)][0]);
  _bpin.setState(table[static_cast<uint8_t>(bw)][1]);
}
