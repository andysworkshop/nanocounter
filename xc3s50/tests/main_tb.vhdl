-- This file is a part of the firmware supplied with Andy's Workshop Sprite Engine (ASE)
-- Copyright (c) 2014 Andy Brown <www.andybrown.me.uk>
-- Please see website for licensing terms.

library IEEE;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.constants.all;


entity main_tb is
end main_tb;


-- 
-- test bench for the main component
-- recommended simulation time: 1ms
--

architecture behavior of main_tb is 

  component main is
    port(
      ref_clk       : in  std_logic;       -- reference clock
      sample_clk    : in  std_logic;       -- the clock being counted
      count_start   : in  std_logic;       -- counting is enabled/disabled and reset
      done_counting : out std_logic;       -- done counting output strobe
      spi_ncs       : in  std_logic;       -- slave select
      spi_clk       : in  std_logic;       -- clock input
      spi_mosi      : in  std_logic;       -- data in
      spi_miso      : out std_logic;       -- data out
      debug         : out std_logic

      --sample_count_out_sim : out counter_t;
      --ref_count_out_sim : out counter_t
    );
  end component;

  -- port signals

  signal count_start   : std_logic := '0';
  signal ref_clk       : std_logic;
  signal sample_clk    : std_logic;
  signal done_counting : std_logic;
  signal spi_ncs       : std_logic;
  signal spi_clk       : std_logic := '1';
  signal spi_mosi      : std_logic;
  signal spi_miso      : std_logic;
  
  -- internal signals

  signal gate_counter_i     : std_logic_vector(31 downto 0);
  signal ref_count_out_i    : counter_t;
  signal sample_count_out_i : counter_t;

  --signal sample_count_out_sim_i : counter_t;
  --signal ref_count_out_sim_i : counter_t;

  -- constants
  
  constant ref_clk_period : time := 10ns;     -- 100MHz
  constant sample_clk_period : time := 32ns;  -- 31.25MHz
  
begin

  -- instantiate the unit under test

  uut : main port map (
    ref_clk        => ref_clk,
    sample_clk     => sample_clk,
    count_start    => count_start,
    done_counting  => done_counting,
    spi_ncs        => spi_ncs,
    spi_clk        => spi_clk,
    spi_mosi       => spi_mosi,
    spi_miso       => spi_miso,
    --sample_count_out_sim => sample_count_out_sim_i,
    --ref_count_out_sim => ref_count_out_sim_i,
    debug          => open
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

    -- hold count_start low
    
    wait for 10ns;
    count_start <= '0';

    -- send a new gate count of 10000

    gate_counter_i <= std_logic_vector(to_unsigned(10000,gate_counter_i'length));
    spi_ncs <= '0';

    for i in 1 to 32 loop

      -- clock out the next bit of data

      wait for 10ns;
      spi_clk <= '0';

      spi_mosi <= gate_counter_i(gate_counter_i'left);
      gate_counter_i <= gate_counter_i(gate_counter_i'left-1 downto 0) & '0';

      -- clock high, get data

      wait for 10ns;
      spi_clk <= '1';

    end loop;

    -- 32 don't care clocks

    for i in 1 to 32 loop
      
      wait for 10ns;
      spi_clk <= '0';
      wait for 10ns;
      spi_clk <= '1';
    
    end loop;

    wait for 10ns;
    spi_ncs <= '1';

    -- do two counting loops

    for c in 1 to 2 loop

      gate_counter_i <= std_logic_vector(to_unsigned(10000,gate_counter_i'length));
      ref_count_out_i <= (others => 'U');
      sample_count_out_i <= (others => 'U');

      -- reset the unit
      
      wait for 10ns;
      count_start <= '0';
      wait for 100ns;
      count_start <= '1';
      wait for 10ns;

      -- wait for counting to complete

      wait until done_counting = '1';

      -- wait for the counters to stabilise

      for i in 1 to 30 loop
        wait until rising_edge(ref_clk);
        wait until rising_edge(sample_clk);
      end loop;

      -- use the SPI interface to retrieve the counters
    
      spi_ncs <= '0';

      for i in 1 to 32 loop

        -- clock low, prepare data

        wait for 10ns;
        spi_clk <= '0';

        spi_mosi <= gate_counter_i(gate_counter_i'left);
        gate_counter_i <= gate_counter_i(gate_counter_i'left-1 downto 0) & '0';

        -- clock high, get data

        wait for 10ns;
        spi_clk <= '1';

        ref_count_out_i <= ref_count_out_i(ref_count_out_i'left-1 downto 0) & spi_miso;

      end loop;

      -- second 32 clocks

      for i in 1 to 32 loop

        -- clock low, prepare data

        wait for 10ns;
        spi_clk <= '0';

        -- clock high, get data

        wait for 10ns;
        spi_clk <= '1';

        sample_count_out_i <= sample_count_out_i(sample_count_out_i'left-1 downto 0) & spi_miso;

      end loop;

      wait for 10ns;
      spi_ncs <= '1';
      
      -- assert the expected state

      assert unsigned(sample_count_out_i) = 15625 report "Expected sample counter to be 3125";
      assert unsigned(ref_count_out_i) = 10000 report "Expected ref counter to be 10000";

    end loop;

    wait;

  end process;

end architecture;

