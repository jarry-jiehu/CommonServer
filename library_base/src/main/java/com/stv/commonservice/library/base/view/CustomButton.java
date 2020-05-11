
package com.stv.commonservice.library.base.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.stv.commonservice.library.base.R;

public class CustomButton extends AppCompatButton {
    private AnimatorSet mEnlargeSet;
    private AnimatorSet mEnNormalSet;
    private boolean mFocusEnlarge = false;

    public CustomButton(Context context) {
        super(context, null, 0);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFocusEnlarge(boolean focusEnlarge) {
        if (focusEnlarge) {
            ObjectAnimator enlargeXl = ObjectAnimator.ofFloat(this, "scaleX", 1.2f);
            ObjectAnimator enlargeYl = ObjectAnimator.ofFloat(this, "scaleY", 1.2f);
            mEnlargeSet = new AnimatorSet();
            mEnlargeSet.setDuration(100);
            mEnlargeSet.play(enlargeXl).with(enlargeYl);

            ObjectAnimator enNormalX = ObjectAnimator.ofFloat(this, "scaleX", 1.0f);
            ObjectAnimator enNormalY = ObjectAnimator.ofFloat(this, "scaleY", 1.0f);
            mEnNormalSet = new AnimatorSet();
            mEnNormalSet.setDuration(100);
            mEnNormalSet.play(enNormalX).with(enNormalY);
        } else {
            if (mEnlargeSet != null)
                mEnlargeSet.cancel();
            if (mEnNormalSet != null) {
                mEnNormalSet.cancel();
            }
            setScaleX(1.0f);
            setScaleY(1.0f);
        }
        this.mFocusEnlarge = focusEnlarge;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        requestFocusFromTouch();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            setTextColor(getResources().getColor(R.color.btn_color_focus));
            if (mFocusEnlarge && mEnlargeSet != null)
                mEnlargeSet.start();
        } else {
            setTextColor(getResources().getColor(R.color.btn_color_normal));
            if (mFocusEnlarge && mEnNormalSet != null)
                mEnNormalSet.start();
        }
    }
}
