
package com.stv.commonservice.control.window.brush;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.stv.commonservice.R;
import com.stv.commonservice.control.util.ThreadPoolManager;
import com.stv.commonservice.control.window.MotionEventUtils;
import com.stv.commonservice.control.window.brush.anim.FullScreenAlphaMotion;
import com.stv.commonservice.control.window.brush.anim.IMotionAnimType;

public class RemoteBrushView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = RemoteBrushView.class.getSimpleName();
    private Context mContext;
    // 默认画笔风格
    private static final Paint.Style PAINT_STYLE = Paint.Style.STROKE;
    // 初始不透明
    private static final int PAINT_ALPHA = 0xFF;
    /**
     * 每50帧刷新一次屏幕
     **/
    private static final float TIME_IN_FRAME = 50;
    // 默认画笔颜色
    private static final int PAINT_COLOR = Color.RED;
    /**
     * 控制游戏循环
     **/
    boolean mIsRunning = false;
    // 默认画笔宽度
    private float mPaintWidth;
    // 画笔
    private Paint mPaint;
    private SurfaceHolder surfaceHolder;
    public Canvas mCanvas;
    private IMotionAnimType mMotionAnimator;
    private boolean isAddWindow = false;

    public RemoteBrushView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        mContext = context;
    }

    public RemoteBrushView(Context context) {
        super(context);
        init();
        mContext = context;
    }

    public void setAddWindow(boolean isAddWindow) {
        this.isAddWindow = isAddWindow;
    }

    public boolean isAddWindow() {
        return isAddWindow;
    }

    private void init() {
        // 设置当前view拥有触摸事件
        this.setFocusable(true);
        this.setClickable(true);
        mPaintWidth = getResources().getDimension(R.dimen.remote_brush_paint_width);
        Log.d(TAG, "TIME_IN_FRAME=" + TIME_IN_FRAME + " mPaintWidth=" + mPaintWidth
                + " PAINT_ALPHA=" + PAINT_ALPHA);
        setBackgroundColor(mContext.getResources().getColor(R.color.black_transparent_20));
        surfaceHolder = getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(this);
        initPaint();
        mMotionAnimator = new FullScreenAlphaMotion(mPaint);
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(PAINT_COLOR);
        mPaint.setAlpha(PAINT_ALPHA);
        mPaint.setStyle(PAINT_STYLE);
        mPaint.setStrokeWidth(mPaintWidth);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != mMotionAnimator) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 手接触屏幕时触发
                    mMotionAnimator.doTouchDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    // 手滑动时触发
                    mMotionAnimator.doTouchMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                    // 手抬起时触发
                    mMotionAnimator.doTouchUp(event);
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    @Override
    public void run() {
        while (mIsRunning) {
            long startTime = System.currentTimeMillis();
            synchronized (surfaceHolder) {
                doDraw();
            }
            long endTime = System.currentTimeMillis();
            int diffTime = (int) (endTime - startTime);
            while (diffTime <= TIME_IN_FRAME) {
                diffTime = (int) (System.currentTimeMillis() - startTime);
                Thread.yield();
            }
        }
        release();
    }

    public void release() {
        if (mIsRunning) {
            mIsRunning = false;
        } else {
            if (null != mMotionAnimator) {
                mMotionAnimator.release();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCanvas = surfaceHolder.lockCanvas();
        surfaceHolder.unlockCanvasAndPost(mCanvas);

        mIsRunning = true;
        ThreadPoolManager.getInstance().getRemoteControlThreadPool().execute(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsRunning = false;
    }

    private void doDraw() {
        mCanvas = surfaceHolder.lockCanvas();
        mMotionAnimator.doDraw(mCanvas);
        surfaceHolder.unlockCanvasAndPost(mCanvas);
    }

    public void clear() {
        Log.d(TAG, "clear");
        release();
        SurfaceHolder holder = getHolder();
        Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            synchronized (holder) {
                if (null != canvas) {
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                }
            }
        } finally {
            try {
                if (null != holder && null != canvas) {
                    holder.unlockCanvasAndPost(canvas);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    public WindowManager.LayoutParams getWindowParam() {
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.width = MotionEventUtils.getInstance().mScreenWidth;
        wmParams.height = MotionEventUtils.getInstance().mScreenHeight;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        return wmParams;
    }
}
