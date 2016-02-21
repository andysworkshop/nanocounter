package uk.me.andybrown.nanocounter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;


/*
 * Manage the animation of the last property change values
 */

public class LastAnimator  {

  protected ImageView[] _images;
  protected ImageView _newImage;
  protected int _marginSize;


  /*
   * Constructor
   */

  public LastAnimator(MainActivity activity) {

    _images=new ImageView[5];
    _marginSize=-1;

    // the current display line

    _images[0]=(ImageView)activity.findViewById(R.id.last_change_icon_1);
    _images[1]=(ImageView)activity.findViewById(R.id.last_change_icon_2);
    _images[2]=(ImageView)activity.findViewById(R.id.last_change_icon_3);
    _images[3]=(ImageView)activity.findViewById(R.id.last_change_icon_4);
    _images[4]=(ImageView)activity.findViewById(R.id.last_change_icon_5);

    // the new one to fade in

    _newImage=(ImageView)activity.findViewById(R.id.last_change_icon_6);
  }


  /*
   * Animate in a new image
   */

  public void animate(int imageId,int duration) {

    AnimatorSet as;
    ObjectAnimator oa;
    int i,sourceMargin;
    ArrayList<Animator> animations;
    RelativeLayout.LayoutParams margins;

    if(_marginSize==-1)
      _marginSize=((RelativeLayout.LayoutParams)_images[1].getLayoutParams()).leftMargin;

    animations=new ArrayList<>();

    // animate the last 4 images to the right

    for(i=sourceMargin=0;i<4;i++,sourceMargin+=_marginSize) {

      final RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)_images[i].getLayoutParams();
      final ValueAnimator va=ValueAnimator.ofInt(sourceMargin,sourceMargin+_marginSize);
      final ImageView image=_images[i];

      va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator)
        {
          params.leftMargin=(Integer)va.getAnimatedValue();
          image.requestLayout();
        }
      });

      va.setDuration(duration);
      animations.add(va);
    }

    // fade in the new image at position 0

    margins=(RelativeLayout.LayoutParams)_newImage.getLayoutParams();
    margins.setMargins(0,margins.topMargin,margins.rightMargin,margins.bottomMargin);
    _newImage.requestLayout();
    _newImage.setImageResource(imageId);

    oa=ObjectAnimator.ofFloat(_newImage,"alpha",1);
    oa.setDuration(duration);
    animations.add(oa);

    // fade out the old image at the end of the array

    oa=ObjectAnimator.ofFloat(_images[4],"alpha",0);
    oa.setDuration(duration);
    animations.add(oa);

    // create a set, start it and get notified when it's complete

    as=new AnimatorSet();
    as.playTogether(animations);

    as.addListener(new AnimatorListenerAdapter() {

      @Override
      public void onAnimationEnd(Animator animation) {

        ImageView nextNew;
        int i;

        // this one on the far right has been faded out and becomes the next new one on the left

        nextNew=_images[4];

        // the line moves to the right

        for(i=3;i>=0;i--)
          _images[i+1]=_images[i];

        // the newly faded in image is at position 0

        _images[0]=_newImage;
        _newImage=nextNew;
      }
    });

    // start the animation

    as.start();
  }
}
