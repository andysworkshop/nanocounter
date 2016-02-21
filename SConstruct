# This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
# Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
# Please see website for licensing terms.
#
# Requirements: 
#   1. The ARM EABI toolchain must be in your PATH (e.g. arm-none-eabi-g++)
#   2. The Xilinx ISE 14.7 toolchain binaries must be in your PATH (e.g. xst.exe)
#   3. You must have installed stm32plus for the f072 target with your chosen optimisation level. e.g.
#      "scons mode=small mcu=f072 hse=8000000 install" from the stm32plus installation directory.
#
#
# These variables must be set correctly. The earliest version of stm32plus that you can use is 4.0.3 (040003).

STM32PLUS_INSTALL_DIR = "/usr/local/arm-none-eabi"
STM32PLUS_VERSION     = "040004"

import os
import platform
import subprocess

def usage():

  print """
Usage scons [fpga=<FPGA>] mode=<MODE>

  <FPGA>: synthesize/translate/map/par/bitgen. Default = bitgen.
    synthesize = xst
    translate  = ngdbuild
    map        = map
    par        = place & route + static timing
    bitgen     = create .bit file

  <MODE>: debug/fast/small.
    debug = -O0
    fast  = -O3
    small = -Os

  Examples:
    scons mode=debug
    scons mode=fast
    scons mode=small
"""

# mode argument must be supplied

mode=ARGUMENTS.get('mode')

if not (mode in ['debug', 'fast', 'small']):
    usage()
    Exit(1)

# get the FPGA option

fpga=ARGUMENTS.get("fpga")

if fpga is None:
  fpga="bitgen"
elif not (fpga in ["synthesize","translate","map","par","bitgen"]):
  usage()
  Exit(1)

# set up build environment and pull in OS environment variables

env=Environment(ENV=os.environ)

# verify that stm32plus is installed in the defined location

stm32plus_lib=STM32PLUS_INSTALL_DIR+"/lib/stm32plus-"+STM32PLUS_VERSION+"/libstm32plus-"+mode+"-f051-8000000i.a"
if not os.path.isfile(stm32plus_lib):
    print stm32plus_lib+" does not exist."
    print "Please edit SConstruct and check the STM32PLUS_INSTALL_DIR and STM32PLUS_VERSION variables."
    Exit(1)

# replace the compiler values in the environment

env.Replace(CC="arm-none-eabi-gcc")
env.Replace(CXX="arm-none-eabi-g++")
env.Replace(AS="arm-none-eabi-as")

# create the C and C++ flags that are needed. We can't use the extra or pedantic errors on the ST library code.

env.Replace(CCFLAGS=["-Wall","-Werror","-ffunction-sections","-fdata-sections","-fno-exceptions","-mthumb","-gdwarf-2","-pipe","-mcpu=cortex-m0","-DSTM32PLUS_F0_51","-DHSE_VALUE=8000000"])
env.Replace(CXXFLAGS=["-Wextra","-pedantic-errors","-fno-rtti","-std=gnu++0x","-fno-threadsafe-statics"])
env.Append(ASFLAGS="-mcpu=cortex-m0")
env.Append(LINKFLAGS=["-Xlinker","--gc-sections","-mthumb","-g3","-gdwarf-2","-mcpu=cortex-m0"])
env.Append(LINKFLAGS=["-Wl,-wrap,__aeabi_unwind_cpp_pr0","-Wl,-wrap,__aeabi_unwind_cpp_pr1","-Wl,-wrap,__aeabi_unwind_cpp_pr2"])

# mode specific debug/optimisation levels

if mode=="debug":
    env.Append(CCFLAGS=["-O0","-g3"])
elif mode=="fast":
    env.Append(CCFLAGS=["-O3"])
elif mode=="small":
    env.Append(CCFLAGS=["-Os"])

# set the include directories - not a simple task on cygwin

if "cygwin" in platform.system().lower():

  # g++ must see the windows style C:/foo/bar path, not the cygwin /usr/foo/bar style so we must translate the
  # paths here. also, scons will try to interpret ":" as a separator in cygwin which gives us the additional problem
  # of not being able to use the interpreted CPPPATH. We have to use CXX flags instead. 

  proc=subprocess.Popen("cygpath --mixed "+STM32PLUS_INSTALL_DIR,stdout=subprocess.PIPE,shell=True)
  (cygbasepath,err)=proc.communicate()
  cygbasepath=cygbasepath.rstrip("\n");     # chomp the newline

  env.Append(CCFLAGS="-I"+cygbasepath+"/include/stm32plus-"+STM32PLUS_VERSION)
  env.Append(CXXFLAGS="-I"+cygbasepath+"/include/stm32plus-"+STM32PLUS_VERSION+"/stl")
  env.Append(LINKFLAGS="-L"+cygbasepath+"/lib/stm32plus-"+STM32PLUS_VERSION)

else:
  env.Append(CPPPATH=[
      STM32PLUS_INSTALL_DIR+"/include/stm32plus-"+STM32PLUS_VERSION,
      STM32PLUS_INSTALL_DIR+"/include/stm32plus-"+STM32PLUS_VERSION+"/stl"])
  
  env.Append(LIBPATH=STM32PLUS_INSTALL_DIR+"/lib/stm32plus-"+STM32PLUS_VERSION)

# set the library path

env.Append(LIBS="stm32plus-"+mode+"-f051-8000000i.a")

# replace the compiler values in the environment. The GNU ARM compilers first

env.Replace(CC="arm-none-eabi-gcc")
env.Replace(CXX="arm-none-eabi-g++")
env.Replace(AS="arm-none-eabi-as")
env.Replace(AR="arm-none-eabi-ar")
env.Replace(RANLIB="arm-none-eabi-ranlib")

# and now the Xilinx tools

env.Replace(XST="xst")
env.Replace(NGDBUILD="ngdbuild")
env.Replace(MAP="map")
env.Replace(PAR="par")
env.Replace(TRCE="trce")

# main design

main_bit=SConscript("xc3s50/SConscript",exports=["env","fpga"],duplicate=0);

# mcu controller

main_hex=SConscript("stm32f072/SConscript",
                    exports=["env","main_bit","mode"],
                    variant_dir="stm32f072/build/"+mode,
                    duplicate=0);
