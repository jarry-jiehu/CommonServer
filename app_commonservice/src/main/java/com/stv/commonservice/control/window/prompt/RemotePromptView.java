
package com.stv.commonservice.control.window.prompt;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.stv.commonservice.R;
import com.stv.commonservice.common.AppApplication;
import com.stv.commonservice.control.util.DensityUtils;

public class RemotePromptView extends RelativeLayout {
    private static final String TAG = RemotePromptView.class.getSimpleName();
    private final int mScreenWidth, mScreenHeight;
    private boolean isAddWindow = false;
    // 真实显示图标内容区
    private View mLayout;
    // window使用的param
    private WindowManager.LayoutParams mWindowParam;
    // mLayout使用的param
    private final WindowPositionListener mListener;
    // 按下位置距离左上角的相对位置
    private Point mDownRelativePoint;

    public RemotePromptView(Context context, WindowPositionListener listener) {
        super(context);
        mListener = listener;
        mDownRelativePoint = new Point(0, 0);
        Point screenPoint = DensityUtils
                .getScreenMetrics(AppApplication.getInstance().getApplicationContext());
        mScreenWidth = screenPoint.x;
        mScreenHeight = screenPoint.y;
        initView();
        mWindowParam = getDefaultWindowParam();
    }

    public void setAddWindow(boolean isAddWindow) {
        this.isAddWindow = isAddWindow;
    }

    public boolean isAddWindow() {
        return isAddWindow;
    }

    private void initView() {
        inflate(getContext(), R.layout.layout_remote_prompt, this);
        mLayout = findViewById(R.id.prompt_layout);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d(TAG, "dispatchTouchEvent touch action=" + ev.getAction() + " x=" + ev.getX() + " y="
                + ev.getY());
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // // 设置偏移点,计算按下位置和view左上角0,0距离
                mDownRelativePoint.set(x, y);
            case MotionEvent.ACTION_MOVE:
                // 在全屏window中,改变margin
                // setLayoutParamMargin(x - mDownRelativePoint.x, y - mDownRelativePoint.y);
                setLayoutParamMargin((int) ev.getRawX() - mDownRelativePoint.x,
                        (int) ev.getRawY() - mDownRelativePoint.y);
                mListener.onWindowUpdateView(this, mWindowParam);
                break;
        }
        return super.onTouchEvent(ev);
    }

    public WindowManager.LayoutParams getWindowParam() {
        return mWindowParam;
    }

    private void setLayoutParamMargin(int left, int top) {
        if (null != mWindowParam) {
            // 边界检查
            if (left < 0) {
                left = 0;
            }
            if (left > mScreenWidth - mLayout.getWidth()) {
                left = mScreenWidth - mLayout.getWidth();
            }
            if (top < 0) {
                top = 0;
            }
            if (top > mScreenHeight - mLayout.getHeight()) {
                top = mScreenHeight - mLayout.getHeight();
            }
            mWindowParam.x = left;
            mWindowParam.y = top;
        }
    }

    public void updateViewPosition(int position) {
        // 更新浮动窗口位置参数
        if (mWindowParam != null) {
            if (position == 0) {
                // 默认位置 右上
                int left = mScreenWidth - mLayout.getLayoutParams().width;
                int top = mScreenHeight / 6;
                setLayoutParamMargin(left, top);
            } else if (position == 1) {
                // 左下
                int left = 0;
                int top = mScreenHeight - mLayout.getLayoutParams().height - mScreenHeight / 6;
                setLayoutParamMargin(left, top);
            }
            mListener.onWindowUpdateView(this, mWindowParam);
        }
    }

    // 默认初始位置的Param
    private WindowManager.LayoutParams getDefaultWindowParam() {
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.gravity = Gravity.TOP | Gravity.LEFT;
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.format = PixelFormat.RGBA_8888;
        // 设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = getWindowFlag(true);
        Log.d(TAG, "getDefaultWindowParam mLayout=" + mLayout);
        if (null != mLayout) {
            // 默认位置
            wmParams.x = mScreenWidth - mLayout.getLayoutParams().width - dip2px(getContext(), 20);
            wmParams.y = dip2px(getContext(), 20);
            wmParams.width = mLayout.getLayoutParams().width;
            wmParams.height = mLayout.getLayoutParams().height;
        } else {
            wmParams.x = 0;
            wmParams.y = 0;
            wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        }
        return wmParams;
    }

    private int getWindowFlag(boolean touchable) {
        int flag = WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        if (!touchable) {
            flag |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        return flag;
    }

    public void onPcModeChanged(boolean isPcModeChanged) {
        Log.d(TAG, "onPcModeChanged " + isPcModeChanged + " window=" + mWindowParam + " listener="
                + mListener);
        if (null != mWindowParam) {
            if (isPcModeChanged) {
                // PC模式下隐藏,设置0,0无效
                Log.d(TAG, "turn hide");
                mWindowParam.width = 1;
                mWindowParam.height = 1;
                mWindowParam.flags = getWindowFlag(false);
            } else {
                mWindowParam.width = mLayout.getLayoutParams().width;
                mWindowParam.height = mLayout.getLayoutParams().height;
                mWindowParam.flags = getWindowFlag(true);
            }
            if (null != mListener) {
                mListener.onWindowUpdateView(this, mWindowParam);
            }
        }
    }

    public void reset() {
        mWindowParam = getDefaultWindowParam();
    }

    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
