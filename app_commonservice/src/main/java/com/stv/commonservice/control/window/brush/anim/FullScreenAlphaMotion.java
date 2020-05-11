
package com.stv.commonservice.control.window.brush.anim;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.view.MotionEvent;

import java.util.Iterator;
import java.util.LinkedList;

public class FullScreenAlphaMotion implements IMotionAnimType {
    private final Paint mPaint;
    private float mStartX = 0.0f;// 初始x
    private float mStartY = 0.0f;// 初始Y
    // 初始几秒不削减
    private static final long WAIT_DURATION = 1500;
    private DecPath mTempPath;
    // 按时间循序排序
    private final LinkedList<DecPath> mPathList;
    private static final long ALPHA_END_TIME = WAIT_DURATION + 500;
    private long mLastUpTime;
    private static final long DEFUALT_LASTTIME = -1;

    public FullScreenAlphaMotion(Paint paint) {
        mPaint = new Paint(paint);
        mTempPath = new DecPath();
        mPathList = new LinkedList<DecPath>();
        mLastUpTime = DEFUALT_LASTTIME;
    }

    @Override
    public void doTouchUp(MotionEvent event) {
        mLastUpTime = System.currentTimeMillis();
    }

    @Override
    public void doTouchDown(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        mStartX = touchX;
        mStartY = touchY;
        mTempPath = new DecPath();
        mTempPath.path = new Path();
        mTempPath.path.moveTo(touchX, touchY);
        synchronized (FullScreenAlphaMotion.class) {
            mPathList.addLast(mTempPath);
        }
        mLastUpTime = DEFUALT_LASTTIME;
    }

    @Override
    public void doTouchMove(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        float cX = (touchX + mStartX) / 2;
        float cY = (touchY + mStartY) / 2;
        if (null != mTempPath && null != mTempPath.path) {
            mTempPath.path.quadTo(mStartX, mStartY, cX, cY);
        }
        mStartX = touchX;
        mStartY = touchY;
    }

    @Override
    public void doDraw(Canvas canvas) {
        long nowTime = System.currentTimeMillis();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (mPathList.size() > 0) {
            synchronized (FullScreenAlphaMotion.class) {
                Iterator<DecPath> iterator = mPathList.iterator();
                // 截取第一个有效path
                while (iterator.hasNext()) {
                    DecPath path = iterator.next();
                    if (!path.isDisappearing) {
                        // 不足消失条件
                        if (mLastUpTime == DEFUALT_LASTTIME || nowTime - mLastUpTime < WAIT_DURATION) {
                            // 未抬起,或者不足渐隐时间
                            canvas.drawPath(path.path, mPaint);
                            continue;
                        } else {
                            // 超过waitTime记录
                            path.isDisappearing = true;
                            path.upTime = mLastUpTime;
                        }
                    }
                    // 执行自身消失
                    if (nowTime - path.upTime < ALPHA_END_TIME) {
                        // 还在执行消失中
                        canvas.drawPath(path.path, getDispearingPaint(nowTime, path.upTime));
                    } else {
                        // 已应移除
                        iterator.remove();
                    }

                }
            }
        }
    }

    private Paint getDispearingPaint(long nowTime, long upTime) {
        Paint paint = new Paint(mPaint);
        int alpha = (int) (255 * (ALPHA_END_TIME - nowTime + upTime) / (ALPHA_END_TIME - WAIT_DURATION));
        paint.setAlpha(alpha);
        return paint;
    }

    @Override
    public void release() {
        synchronized (FullScreenAlphaMotion.class) {
            mPathList.clear();
            mLastUpTime = DEFUALT_LASTTIME;
        }
    }

    private class DecPath {
        public Path path;
        public boolean isDisappearing = false;
        public long upTime;
    }
}
