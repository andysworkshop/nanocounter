--
-- * pipelined synchronous pulse counter *
--  pdchain -- multi-bit counter top-level entity
--
-- fast counter for slow-carry architectures
-- non-monotonic counting, value calculable by HDL/CPU
--
-- idea&code by Marek Peca <mp@duch.cz> 08/2012
-- Vyzkumny a zkusebni letecky ustav, a.s. http://vzlu.cz/
-- thanks to Michael Vacek <michael.vacek@vzlu.cz> for testing
--
 
library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.constants.all;


entity pdchain is
  port (
    nreset : in  std_logic;
    clock  : in  std_logic;
    en     : in  std_logic;
    q      : out counter_t
  );
end pdchain;
 
architecture behavioral of pdchain is
  component pdivtwo
    port (
      nreset : in  std_logic;
      clock  : in  std_logic;
      en     : in  std_logic;
      p      : out std_logic;
      q      : out std_logic
    );
  end component;

  signal b : std_logic_vector (q'range);

begin

  q0: pdivtwo
    port map (
      nreset => nreset,
      clock  => clock, 
      en     => en, 
      p      => b(0), 
      q      => q(0)
    );
  
  ch: for k in 1 to b'high generate
    qk: pdivtwo
      port map (
        nreset => nreset,
        clock  => clock, 
        en     => b(k-1), 
        p      => b(k), 
        q      => q(k)
      );
  end generate;

end behavioral;
