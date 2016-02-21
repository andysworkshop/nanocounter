-- This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
-- Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
-- Please see website for licensing terms.

library ieee;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.constants.all;


--
-- This module manages the equal precision counter. The control module sets gate_en based
-- on a division of the reference clock. This module enables the actual counters on the
-- soonest rising edge of the sample clock after gate_en is set. Counting is stopped after
-- the soonest rising edge of the sample clock after gate_en is reset.
--

entity equal_precision_counter is

  port(
    count_start        : in  std_logic;
    ref_clk            : in  std_logic;
    sample_clk         : in  std_logic;
    ref_counter        : out counter_t;
    gate_counter_limit : in counter_t;
    sample_counter     : out counter_t;
    done_counting      : out std_logic
  );
end equal_precision_counter;

architecture behavioral of equal_precision_counter is

  -- internal signals

  signal gate_counter_i   : counter_t := ZERO_COUNTER;
  signal ref_counter_i    : counter_t := ZERO_COUNTER;
  signal sample_counter_i : counter_t := ZERO_COUNTER;
  signal done_counting_i  : std_logic := '0';
  signal t0_i             : std_logic := '0';
  signal t_i              : std_logic := '0';
  signal ref_clk_div      : std_logic := '0';

  -- set a 100MHz constraint for ref_clk_div

  attribute period : string;
  attribute period of ref_clk_div : signal is "10 ns";

  -- pdchain counter component

  component pdchain
    port (
      nreset : in std_logic;
      clock  : in std_logic;
      en     : in std_logic;
      q      : out counter_t
    );
  end component;

begin
  
  -- instantiate the ref counter

  pdchain_ref: pdchain
    port map (
      nreset => count_start,
      clock  => ref_clk,
      en     => t_i,
      q      => ref_counter_i
    );

  -- instantiate the sample counter

  pdchain_sample: pdchain
    port map (
      nreset => count_start,
      clock  => sample_clk,
      en     => t_i,
      q      => sample_counter_i
    );

  -- register the outputs

  ref_counter <= ref_counter_i;
  sample_counter <= sample_counter_i;
  done_counting <= done_counting_i;

  --
  -- generate ref_clk/2 to use as a control clock
  --

  process(ref_clk)
  begin

    if rising_edge(ref_clk) then
      ref_clk_div <= not ref_clk_div;
    end if;

  end process;

  --
  -- control the pre-gate T0 signal
  --

  process(ref_clk,count_start,t0_i) is
  begin

    if rising_edge(ref_clk) and ref_clk_div = '1' then

      if count_start = '0' then

        t0_i <= '0';
        gate_counter_i <= (others => '0');

      elsif gate_counter_i = gate_counter_limit then

        -- gate limit hit, close it

        t0_i <= '0';
    
      else
        
        t0_i <= '1';
        gate_counter_i <= counter_t(unsigned(gate_counter_i)+1);

      end if;

    end if;
  
  end process;


  -- control the sync gate T signal
  -- signal T follows T0 synchronised to the sample clock

  process(sample_clk,t_i)
  begin

    if rising_edge(sample_clk) then

      t_i <= t0_i;
      done_counting_i <= (not t0_i) and count_start;

    end if;
  end process;

end architecture behavioral;
