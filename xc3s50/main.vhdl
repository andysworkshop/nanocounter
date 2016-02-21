-- This file is a part of the firmware supplied with Andy's Workshop Frequency Counter
-- Copyright (c) 2015 Andy Brown <www.andybrown.me.uk>
-- Please see website for licensing terms.

library ieee;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.constants.all;


--
-- main is the top level entity that instantiates all lower level components, connects together their
-- signals and handles asynchronous arbitration for the bi-direction buses (SRAM, flash)
--

entity main is
  port(
  
    -- the counters

    ref_clk    : in std_logic;        -- reference clock
    sample_clk : in std_logic;        -- the clock being counted

    -- start counting and output ready flags

    count_start  : in std_logic;      -- counting should start (high)
    done_counting : out std_logic;    -- set to high when a started count has finished

    -- the SPI interface (slave mode)

    spi_ncs  : in  std_logic;         -- slave select
    spi_clk  : in  std_logic;         -- clock input
    spi_mosi : in  std_logic;         -- data in
    spi_miso : out std_logic;         -- data out

    -- general purpose debug line (connected to a LED)

    debug : out std_logic

--pragma synthesis_off
    ;
    sample_count_out_sim : out counter_t;
    ref_count_out_sim : out counter_t
--pragma synthesis_on
  );
end main;

architecture behavioral of main is

  ---
  --- the equal precision counter component
  ---

  component equal_precision_counter port(
    count_start        : in  std_logic;
    ref_clk            : in  std_logic;
    sample_clk         : in  std_logic;
    ref_counter        : out counter_t;
    gate_counter_limit : in counter_t;
    sample_counter     : out counter_t;
    done_counting      : out std_logic
  );
  end component;

  ---
  --- the SPI interface component
  ---

  component spi_interface port(
    ncs  : in std_logic; 
    clk  : in std_logic;
    mosi : in std_logic;
    miso : out std_logic;
    debug : out std_logic;

    sample_counter     : in counter_t;
    ref_counter        : in counter_t;
    gate_counter_limit : out counter_t
  );
  end component;

  --- internal signals 

  signal gate_counter_limit_i : counter_t := ZERO_COUNTER;
  signal done_counting_i      : std_logic := '0';
  signal ref_counter_i        : counter_t := ZERO_COUNTER;
  signal sample_counter_i     : counter_t := ZERO_COUNTER;
  signal debug_i              : std_logic := '0';

  -- apply clock constraints
  --   200MHz for the ref clk
  --   50Mhz for the sample clock
  --   50MHz for the SPI clock

  attribute period : string;
  attribute period of ref_clk     : signal is "5 ns";
  attribute period of sample_clk  : signal is "20 ns";
  attribute period of spi_clk     : signal is "20 ns";

begin

 --pragma synthesis_off
  sample_count_out_sim <= sample_counter_i;
  ref_count_out_sim <= ref_counter_i;
  --pragma synthesis_on

  -- register the outputs
  
  done_counting <= done_counting_i;

  debug <= debug_i;

  -- instantiate equal_precision_counter

  inst_equal_precision_counter : equal_precision_counter port map(
    count_start        => count_start,
    ref_clk            => ref_clk,
    sample_clk         => sample_clk,
    ref_counter        => ref_counter_i,
    gate_counter_limit => gate_counter_limit_i,
    sample_counter     => sample_counter_i,
    done_counting      => done_counting_i
  );

  -- instantiate the spi_interface

  inst_spi_interface : spi_interface port map(
    ncs  => spi_ncs,
    clk  => spi_clk,
    mosi => spi_mosi,
    miso => spi_miso,
    debug => debug_i,

    sample_counter     => sample_counter_i,
    ref_counter        => ref_counter_i,
    gate_counter_limit => gate_counter_limit_i
  );

end architecture behavioral;
