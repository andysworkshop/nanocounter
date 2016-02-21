package uk.me.andybrown.nanocounter;

import android.text.InputFilter;
import android.text.Spanned;

import java.math.BigDecimal;


/*
 * An input filter to implement min/max values in a
 * numeric editor
 */

public class InputFilterMinMax implements InputFilter {

  protected BigDecimal _min,_max;

  /*
   * Constructor
   */

  public InputFilterMinMax(String min,String max) {
    _min=new BigDecimal(min);
    _max=new BigDecimal(max);
  }


  /*
   * Filter the text entered by the user: block input that would go out of range
   */

  @Override
  public CharSequence filter(CharSequence source,int start,int end,Spanned dest, int dstart, int dend) {

    int cmin,cmax;

    try {
      BigDecimal input=new BigDecimal(dest.toString()+source.toString());

      cmin=input.compareTo(_min);
      cmax=input.compareTo(_max);

      if(cmin>=0 && cmax<=0)
        return null;
    }
    catch(Exception nfe) {
    }

    return "";
  }
}
