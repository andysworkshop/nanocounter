-- This file is a part of the firmware supplied with Andy's Workshop Sprite Engine (ASE)
-- Copyright (c) 2014 Andy Brown <www.andybrown.me.uk>
-- Please see website for licensing terms.

library IEEE;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.constants.all;


entity equal_precision_counter_tb is
end equal_precision_counter_tb;


-- 
-- test bench for the equal_precision_counter component
-- recommended simulation time is 11ms
--

architecture behavior of equal_precision_counter_tb is 

  component equal_precision_counter is
    port(
      count_start        : in  std_logic;
      ref_clk            : in  std_logic;
      sample_clk         : in  std_logic;
      ref_counter        : out counter_t;
      gate_counter_limit : in counter_t;
      sample_counter     : out counter_t;
      done_counting      : out std_logic
    );
  end component;

  -- inputs
  
  signal count_start        : std_logic := '0';
  signal ref_clk            : std_logic;
  signal sample_clk         : std_logic;
  signal ref_counter        : counter_t := (others => '1');
  signal gate_counter_limit : counter_t := (others => '0');
  signal sample_counter     : counter_t := (others => '1');
  signal done_counting      : std_logic := '0';
  
  -- constants
  
  constant ref_clk_period : time := 10ns;     -- 100MHz
  constant sample_clk_period : time := 32ns;  -- 31.25MHz
  
begin

  -- instantiate the unit under test

  uut : equal_precision_counter port map (
    count_start        => count_start,
    ref_clk            => ref_clk,
    sample_clk         => sample_clk,
    ref_counter        => ref_counter,
    gate_counter_limit => gate_counter_limit,
    sample_counter     => sample_counter,
    done_counting      => done_counting
  );

  -- tick the ref clock

  ref_clk_process : process
  begin
    ref_clk <= '0';
    wait for ref_clk_period/2;
    ref_clk <= '1';
    wait for ref_clk_period/2;
  end process;
  
  -- tick the sample clock

  sample_clk_process : process
  begin
    sample_clk <= '0';
    wait for sample_clk_period/2;
    sample_clk <= '1';
    wait for sample_clk_period/2;
  end process;

  -- stimulate the unit under test

  stim_proc : process
  begin

    -- stop the counting, set parameters and start counting
    
    wait for 20ns;
    count_start <= '0';
    gate_counter_limit <= counter_t(to_unsigned(200000,gate_counter_limit'length));
    wait for 200ns;
    count_start <= '1';

    -- wait until the counting is complete

    wait until done_counting = '1';

    -- wait for the counters to stabilise

    for i in 1 to 30 loop
      wait until rising_edge(ref_clk);
      wait until rising_edge(sample_clk);
    end loop;

    assert unsigned(sample_counter) = 125000 report "Expected sample counter to be 31250";
    assert unsigned(ref_counter) = 400000 report "Expected ref counter to be 100000";

    -- reset for a new count

    count_start <= '0';
    gate_counter_limit <= counter_t(to_unsigned(200000,gate_counter_limit'length));
    wait for 200ns;
    count_start <= '1';

    wait until done_counting = '1';

    -- wait for the counters to stabilise

    for i in 1 to 30 loop
      wait until rising_edge(ref_clk);
      wait until rising_edge(sample_clk);
    end loop;

    assert unsigned(sample_counter) = 125000 report "Expected sample counter to be 15625";
    assert unsigned(ref_counter) = 400000 report "Expected ref counter to be 50000";

    wait;

  end process;

end architecture;

