-- This file is a part of the firmware supplied with Andy's Workshop Sprite Engine (ASE)
-- Copyright (c) 2014 Andy Brown <www.andybrown.me.uk>
-- Please see website for licensing terms.

library IEEE;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.constants.all;


entity spi_interface_tb is
end spi_interface_tb;


-- 
-- test bench for the spi_interface component
-- Recommended simulation time: 10us
--

architecture behavior of spi_interface_tb is 

  component spi_interface is
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
  end component;

  -- port signals
  
  signal ncs              : std_logic := '1';
  signal clk              : std_logic := '1';
  signal mosi             : std_logic;
  signal miso             : std_logic;     
  signal debug_i          : std_logic := '0';
  signal sample_counter_i : counter_t := (others => '1');
  signal ref_counter_i    : counter_t := (others => '1');

  -- internal signals

  signal gate_counter_limit_i     : std_logic_vector(31 downto 0) := (others => '1');
  signal gate_counter_limit_out_i : counter_t := (others => '1');
  signal sample_counter_out_i     : std_logic_vector(31 downto 0) := (others => '1');
  signal ref_counter_out_i        : std_logic_vector(31 downto 0) := (others => '1');

  -- constants

  constant clk_period : time := 100ns;     -- 10MHz
  
begin

  -- instantiate the unit under test

  uut : spi_interface port map (
    ncs  => ncs,
    clk  => clk,
    mosi => mosi,
    miso => miso,

    sample_counter     => sample_counter_i,
    ref_counter        => ref_counter_i,
    gate_counter_limit => gate_counter_limit_out_i,
    debug => debug_i
  );

  -- stimulate the unit under test

  stim_proc : process
  begin

    -- set the counters

    ref_counter_i <= counter_t(to_unsigned(123456789,counter_t'length));
    sample_counter_i <= counter_t(to_unsigned(135792468,counter_t'length));
    gate_counter_limit_i <= std_logic_vector(to_unsigned(1000000000,gate_counter_limit_i'length));

    -- deselect

    ncs <= '1';
    wait for 10ns;

    -- select the unit
    
    ncs <= '0';
    wait for 10ns;

    -- first 32 clocks

    for i in 1 to 32 loop

      -- clock low, prepare data

      wait for clk_period/2;
      clk <= '0';

      mosi <= gate_counter_limit_i(gate_counter_limit_i'left);
      gate_counter_limit_i <= gate_counter_limit_i(gate_counter_limit_i'left-1 downto 0) & '0';

      -- clock high, get data

      wait for clk_period/2;
      clk <= '1';

      ref_counter_out_i <= ref_counter_out_i(ref_counter_out_i'left-1 downto 0) & miso;

    end loop;

    -- second 32 clocks

    for i in 1 to 32 loop

      -- clock low, prepare data

      wait for clk_period/2;
      clk <= '0';

      -- clock high, get data

      wait for clk_period/2;
      clk <= '1';

      sample_counter_out_i <= sample_counter_out_i(sample_counter_out_i'left-1 downto 0) & miso;

    end loop;

    wait for 100ns;

    assert ref_counter_out_i =        "00000111010110111100110100010101" report "incorrect ref_counter_out_i";
    assert sample_counter_out_i =     "00001000000110000000011101010100" report "incorrect sample_counter_out_i";

    -- gate counter limit is half the input value
    
    assert gate_counter_limit_out_i = "0011101110011010110010100000000" report "incorrect gate_counter_limit_out_i";

    -- deselect the unit
    
    ncs <= '1';

    wait;

  end process;

end architecture;
