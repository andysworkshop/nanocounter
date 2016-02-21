package uk.me.andybrown.nanocounter;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;


/*
 * Simple extension of text view to permit HTML
 */

public class HtmlTextView extends TextView {

  public HtmlTextView(Context context) {
    super(context);
    init();
  }

  public HtmlTextView(Context context,AttributeSet attrs) {
    super(context,attrs);
    init();
  }

  public HtmlTextView(Context context,AttributeSet attrs,int defStyleAttr) {
    super(context,attrs,defStyleAttr);
    init();
  }

  protected void init(){
    setText(Html.fromHtml(getText().toString()));
    setMovementMethod(LinkMovementMethod.getInstance());
  }
}