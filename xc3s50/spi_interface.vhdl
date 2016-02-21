-- This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
-- Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
-- Please see website for licensing terms.

library ieee;

use ieee.std_logic_1164.all;

use work.constants.all;


--
-- SPI implementation to the outside world. SPI mode 3 (CPOL = 1, CPHA = 1).
-- Here's how to use it:
--
--  1. Make a high to low transition on ncs
--
--  2. Send 64 clocks.
--    2a. On the first 32 clocks you will receive ref_count
--    2b. On the first 32 clocks you will send the ref clock gate counter limit
--    2c. On the second 32 clocks you will receive sample_count
--    2d. On the second 32 clocks you will send [don't care] data. 
--
--  3. Make a low to high transition on ncs
--
-- note that the counters are 31 bits wide so bit 32 in each 32-bit word will
-- always be zero.

entity spi_interface is

  port(
    ncs  : in std_logic;
    clk  : in std_logic;
    mosi : in std_logic;
    miso : out std_logic;
    debug : out std_logic;

    sample_counter     : in counter_t;
    ref_counter        : in counter_t;
    gate_counter_limit : out counter_t
  );
end spi_interface;

architecture behavioral of spi_interface is

  signal miso_i               : std_logic;
  signal gate_counter_limit_i : counter_t;
  signal output_bits_i        : std_logic_vector(63 downto 0);
  signal input_bits_i         : std_logic_vector(63 downto 0);
  signal bit_counter_i        : natural range 0 to 63;

begin

  -- register the outputs

  gate_counter_limit <= gate_counter_limit_i;
  miso <= miso_i;

  --
  -- process for writing out the 64 bits
  -- 

  spi_output : process(ncs,clk) is
  begin

    if ncs = '1' then

      bit_counter_i <= 0;

    elsif falling_edge(clk) then

      if bit_counter_i = 0 then
      
        -- bit zero always outputs zero so we use this clock as the setup stage

        output_bits_i <= ref_counter & "0" & sample_counter & "0";
        miso_i <= '0';
      
      else

        -- shift out the next bit

        miso_i <= output_bits_i(output_bits_i'left);
        output_bits_i <= output_bits_i(output_bits_i'left-1 downto 0) & '0';
      
      end if;

      bit_counter_i <= bit_counter_i+1;

    end if;

  end process spi_output;

  --
  -- process for reading in the 64 bits
  --

  spi_input : process(ncs,clk) is
  begin

    if ncs = '1' then

      input_bits_i <= (others => '0');

    elsif rising_edge(clk) then

      -- shift in the next bit

      input_bits_i <= input_bits_i(input_bits_i'left-1 downto 0) & mosi;

      if bit_counter_i = 63 then
        
        -- read in the new gate counter limit (divided by 2)

        gate_counter_limit_i <= "0" & input_bits_i(60 downto 31);

      end if;
    
    end if;
  
  end process spi_input;

end architecture behavioral;
