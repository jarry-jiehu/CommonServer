
package com.stv.commonservice.control.window.brush.anim;

import android.graphics.Canvas;
import android.view.MotionEvent;

public interface IMotionAnimType {
    String TAG = IMotionAnimType.class.getSimpleName();

    void doTouchUp(MotionEvent event);

    void doTouchDown(MotionEvent event);

    void doTouchMove(MotionEvent event);

    void doDraw(Canvas canvas);

    void release();
}
