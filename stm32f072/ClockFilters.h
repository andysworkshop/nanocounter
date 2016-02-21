/*
 * This file is a part of the firmware supplied with Andy's Workshop Nanocounter
 * Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
 * Please see website for licensing terms.
 */


#pragma once


/*
 * Manage both the clock filters (sample/xref)
 */

class ClockFilters {

  protected:
    enum {
      PIN_SAM_A  = 0,     // PB0
      PIN_SAM_B  = 1,     // PB1
      PIN_XREF_A = 10,    // PA10
      PIN_XREF_B = 9      // PA9
    };

    ClockFilter *_sample;
    ClockFilter *_xref;

  public:
    ClockFilters(const Preferences& preferences);
    ~ClockFilters();

    void setFiltersFromPreferences(const Preferences& preferences) const;
};


/*
 * Constructor
 */

inline ClockFilters::ClockFilters(const Preferences& preferences) {

  // create the filter objects

  GpioB<DefaultDigitalOutputFeature<PIN_SAM_A,PIN_SAM_B>> pb;
  GpioA<DefaultDigitalOutputFeature<PIN_XREF_A,PIN_XREF_B>> pa;

  _sample=new ClockFilter(pb[PIN_SAM_A],pb[PIN_SAM_B]);
  _xref=new ClockFilter(pa[PIN_XREF_A],pa[PIN_XREF_B]);

  // set the states

  setFiltersFromPreferences(preferences);
}


/*
 * Set the filters from the preferences values
 */

inline void ClockFilters::setFiltersFromPreferences(const Preferences& preferences) const {
  _sample->setBandwidth(preferences.getSettings().sampleClockFilter);
  _xref->setBandwidth(preferences.getSettings().refClockFilter);
}


/*
 * Destructor
 */

inline ClockFilters::~ClockFilters() {
  delete _xref;
  delete _sample;
}

