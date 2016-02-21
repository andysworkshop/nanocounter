-- This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
-- Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
-- Please see website for licensing terms.

library ieee;

use ieee.std_logic_1164.all;


package constants is

  --
  -- The counter is a 31-bit value which is sufficient to hold up to 10s of counts
  --

  subtype counter_t is std_logic_vector(30 downto 0);

  --
  -- constant values
  -- 

  constant ZERO_COUNTER : counter_t := (counter_t'range => '0');

end constants;

package body constants is
end constants;
