# Nanocounter: an FPGA frequency counter

Welcome to the github repo for Nanocounter, my FPGA-based frequency counter with an Android user interface.

![Nanocounter board](http://andybrown.me.uk/wp-content/images/nanocounter/built.jpg)

The main Nanocounter board houses the input stages, the FPGA counter, the STM32F072 controller and the HC-06 bluetooth daughterboard used for communicating with the android app.

![Android app](http://andybrown.me.uk/wp-content/images/nanocounter/calibration.jpg)

The FPGA communicates with the STM32F072 to receive counts and sends them over bluetooth to the Android app.

## Read the full write up

The full writeup for this project can be found at my website. [Click here to read it](http://andybrown.me.uk/2016/02/21/nanocounter). 

## Building the project

I build my projects on Windows 10 using Cygwin to deliver a Linux-alike command-line experience. The following tools are required as dependencies before you can build anything:

* The `Scons` build system.
* The GCC ARM Embedded toolchain. [Click here](https://launchpad.net/gcc-arm-embedded) to get it.
* The Xilinx ISE webpack. I'm using v14.7. The `ISE_DS/ISE/bin/nt64` subdirectory should be in your PATH (Xilinx don't make this obvious!)
* Android Studio

The main SConstruct file in the project root is sufficient to build everything apart from the Android app. The correct `scons` invocation is

    scons mode=small

To build the Android app, open it in Android Studio and build from there. Since Android studio is little more than a clunky wrapper around a gradle target you can of course run the gradle build targets from the command line if you so prefer. 

If you're not intending to do any actual project development then you can just install the app from the Google Play Store. Search for _nanocounter_ or _Andy's Workshop_.
