
package com.stv.commonservice.control.window;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.stv.commonservice.common.AppApplication;
import com.stv.commonservice.control.protocol.bean.CallBundle;
import com.stv.commonservice.control.protocol.bean.MotionEventBean;
import com.stv.commonservice.control.protocol.bean.SwitchEventBean;
import com.stv.commonservice.control.protocol.listener.BrashSwitchListener;
import com.stv.commonservice.control.protocol.listener.MotionListener;
import com.stv.commonservice.control.protocol.listener.PcModeChangedListener;
import com.stv.commonservice.control.protocol.listener.SwitchPositonListener;
import com.stv.commonservice.control.window.brush.RemoteBrushView;
import com.stv.commonservice.control.window.prompt.RemotePromptView;
import com.stv.commonservice.control.window.prompt.WindowPositionListener;

public class RemoteControlWindow implements WindowPositionListener {
    private static final String TAG = RemoteControlWindow.class.getSimpleName();
    private WindowManager mWindowManager;
    private RemoteBrushView mBrushSurface;
    private RemotePromptView mPromptLayout;
    private boolean isBrushMode = false;
    private Context mContext;
    private CallBundle mCallBundle;
    private Handler mUiHandler = AppApplication.getHandler();

    public RemoteControlWindow() {
        mWindowManager = (WindowManager) AppApplication.getInstance().getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        mContext = AppApplication.getInstance();
    }

    public void setCallBundle(CallBundle callBundle) {
        Log.d(TAG, "setCallBundle CallBundle=" + callBundle);
        mCallBundle = callBundle;
    }

    public void reset() {
        removeBrushView();
        addPromptView();
    }

    public void show() {
        addPromptView();
    }

    private void addPromptView() {
        Log.d(TAG, " addPromptView");
        if (null == mPromptLayout) {
            mPromptLayout = new RemotePromptView(mContext, this);
        }
        if (!mPromptLayout.isAddWindow()) {
            Log.d(TAG, "addPromptView isAddWindow callBundle=" + mCallBundle);
            setCallBundle(mCallBundle);
            mPromptLayout.reset();
            mPromptLayout.setAddWindow(true);
            WindowManager.LayoutParams wmParams = mPromptLayout.getWindowParam();
            mWindowManager.addView(mPromptLayout, wmParams);
        }
    }

    private void removePromptView() {
        if (null != mPromptLayout) {
            if (mPromptLayout.isAddWindow()) {
                mWindowManager.removeView(mPromptLayout);
                mPromptLayout.setAddWindow(false);
            }
        }
    }

    private void addBrushView() {
        if (null == mBrushSurface) {
            mBrushSurface = new RemoteBrushView(mContext);
        }
        if (!mBrushSurface.isAddWindow()) {
            mBrushSurface.setAddWindow(true);
            WindowManager.LayoutParams wmParams = mBrushSurface.getWindowParam();
            Log.d(TAG, "addBrushView");
            mWindowManager.addView(mBrushSurface, wmParams);
        }
    }

    private void removeBrushView() {
        if (null != mBrushSurface) {
            mBrushSurface.clear();
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBrushSurface.isAddWindow()) {
                        mWindowManager.removeView(mBrushSurface);
                        mBrushSurface.setAddWindow(false);
                    }
                }
            });
        }
    }

    public void setBrashMode(boolean isBrushMode) {
        Log.d(TAG, "setBrashMode isBrashMode=" + isBrushMode);
        this.isBrushMode = isBrushMode;
        if (isBrushMode) {
            addBrushView();
        } else {
            removeBrushView();
        }
    }

    public boolean isBrashMode() {
        return isBrushMode;
    }

    public void stop() {
        removeView();
    }

    private void removeView() {
        removePromptView();
        removeBrushView();
    }

    public MotionListener getTouchListener() {
        return mTouchListener;
    }

    public BrashSwitchListener getBrashSwitch() {
        return mBrashSwitchListener;
    }

    public PcModeChangedListener getPcModeChangedListener() {
        return mPcModeChangedListener;
    }

    public SwitchPositonListener getSwitchPositonListener() {
        return mSwitchPositonListener;
    }

    private PcModeChangedListener mPcModeChangedListener = new PcModeChangedListener() {
        @Override
        public boolean onPcModeChanged(boolean isPcMode) {
            Log.d(TAG, "onPcModeChanged isPcMode=" + isPcMode + " mPromptLayout=" + mPromptLayout);
            if (isPcMode) {
                if (null != mPromptLayout) {
                    mPromptLayout.onPcModeChanged(isPcMode);
                }
            }
            return false;
        }
    };

    // 处理custom发送的touch事件
    private MotionListener mTouchListener = new MotionListener() {
        private long mLocalLastDownTime = -1;

        @Override
        public boolean onTouch(MotionEventBean event) {
            Log.d(TAG, "onTouch  mBrushSurface"
                    + (mBrushSurface == null ? "=null" : " isadd=" + mBrushSurface.isAddWindow()));
            if (mBrushSurface != null && mBrushSurface.isAddWindow()) {
                MotionEvent bean = MotionEventUtils.getInstance().createMotionEvent(event);
                mBrushSurface.onTouchEvent(bean);
            } else {
                sendMotion(event);
            }
            return false;
        }

        private void sendMotion(MotionEventBean bean) {
            if (null != bean) {
                if (bean.getAction() == MotionEvent.ACTION_DOWN) {
                    mLocalLastDownTime = SystemClock.uptimeMillis();
                    bean.setDownTime(mLocalLastDownTime);
                    bean.setEventTime(mLocalLastDownTime);
                } else {
                    bean.setDownTime(mLocalLastDownTime);
                    bean.setEventTime(SystemClock.uptimeMillis());
                }
                MotionEventUtils.getInstance()
                        .sendPointerEvent(MotionEventUtils.getInstance().createMotionEvent(bean));
            }
        }
    };

    private BrashSwitchListener mBrashSwitchListener = new BrashSwitchListener() {
        @Override
        public void changBrashType(boolean isBrash) {
            Log.d(TAG, "changBrashType isBrash=" + isBrash);
            setBrashMode(isBrash);
        }
    };
    private SwitchPositonListener mSwitchPositonListener = new SwitchPositonListener() {
        @Override
        public void onSwitch(SwitchEventBean event) {
            Log.d(TAG, "onSwitch postion event=" + event.toString());
            if (mPromptLayout != null) {
                mPromptLayout.updateViewPosition(event.getAction());
            }
        }
    };

    public boolean isShowing() {
        return mPromptLayout != null && mPromptLayout.isAddWindow();
    }

    @Override
    public void onWindowUpdateView(View view, WindowManager.LayoutParams params) {
        Log.d(TAG, "onWindowUpdateView mWindowManager =" + mWindowManager);
        if (null != mWindowManager) {
            mWindowManager.updateViewLayout(view, params);
        }
    }
}
