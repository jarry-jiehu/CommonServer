
package com.stv.commonservice.library.base.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

public class AnimationView {

    private AnimatorSet mEnlargeSet;
    private AnimatorSet mEnNormalSet;
    private View mView;

    public AnimationView(View view) {
        super();

        mView = view;

        if (null != mView) {
            ObjectAnimator enlargeXl = ObjectAnimator.ofFloat(view, "scaleX", 1.2f);
            ObjectAnimator enlargeYl = ObjectAnimator.ofFloat(view, "scaleY", 1.2f);
            mEnlargeSet = new AnimatorSet();
            mEnlargeSet.setDuration(100);
            mEnlargeSet.play(enlargeXl).with(enlargeYl);

            ObjectAnimator enNormalX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f);
            ObjectAnimator enNormalY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f);
            mEnNormalSet = new AnimatorSet();
            mEnNormalSet.setDuration(100);
            mEnNormalSet.play(enNormalX).with(enNormalY);
        }
    }

    public void setFocusEnlarge() {
        if (null != mEnlargeSet) {
            mEnlargeSet.start();
        }
    }


    public void setFocusNormal() {
        if (null != mEnNormalSet) {
            mEnNormalSet.start();
        }
    }
}
