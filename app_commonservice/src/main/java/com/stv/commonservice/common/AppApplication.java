
package com.stv.commonservice.common;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;

import com.stv.commonservice.common.service.SystemCommonServcice;
import com.stv.commonservice.library.base.BaseHelper;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.PushManagerUtils;
import com.stv.commonservice.util.StorageUtils;
import com.stv.library.common.util.ServiceHelper;

public class AppApplication extends MultiDexApplication {
    private LogUtils mLog = LogUtils.getInstance("Common", AppApplication.class.getSimpleName());
    public static AppApplication sStvApplication;
    private static Handler handler;
    public static int mVisible = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        sStvApplication = this;
        StorageUtils.initExtDir(this);
        // 处理crash消息
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        startCommonService();
        registePush();
        handler = new Handler();
        if (Build.VERSION.SDK_INT >= 24) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        BaseHelper.init(this, handler);
    }

    public static AppApplication getInstance() {
        return sStvApplication;
    }

    public static Handler getHandler() {
        return handler;
    }

    private void startCommonService() {
        Intent i = new Intent();
        i.setClass(getApplicationContext(), SystemCommonServcice.class);
        ServiceHelper.startService(this, i);
    }

    private void registePush() {
        PushManagerUtils.registerPush(this);
    }
}
