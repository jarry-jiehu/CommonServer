
package com.stv.commonservice.control.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;

import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;
import com.stv.stvpush.util.SystemUtils;
import com.stv.videochatsdk.api.InitParams;
import com.stv.videochatsdk.api.LetvCallManager;
import com.stv.videochatsdk.api.VersionInfo;
import com.stv.videochatsdk.manager.LeVideoChatPushManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SdkManager {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_REMOTE_CONTROL,
            SdkManager.class.getSimpleName());
    private final String REQ_HTTP = "https://";
    private final String VIDEOCHAT_URL = "vcontrol-scloud.cp21.ott.cibntv.net";
    private final String KEY_CONTROL_URL = "vcontrol";
    public static final String VALUE_VIDEOCHAT = "video_chat";
    public static final String VALUE_REMOTECONTROL = "remote_control";
    public static final String KEY_FEATURE = "feature";
    public static final String KEY_SUPPORT_FEATURE = "SupportFeature";

    static class Inner {
        static SdkManager instance = new SdkManager();
    }

    public static SdkManager getInstance() {
        return Inner.instance;
    }

    public static interface SDKInit {
        void onComplete();
    }

    private Executor threadPool = Executors.newCachedThreadPool();
    private Handler handler = new Handler();
    /**
     * 安天病毒查杀
     */
    private AtomicBoolean mALVHasInit = new AtomicBoolean(false);
    private AtomicBoolean mALVIniting = new AtomicBoolean(false);
    /**
     * 乐视远程控制
     */
    private AtomicBoolean mImSdkHasInit = new AtomicBoolean(false);
    private AtomicBoolean mImSdkIniting = new AtomicBoolean(false);

    public void initImSdk(final Context context, final SDKInit callback) {
        if (!mImSdkHasInit.get() && !mImSdkIniting.get()) {
            mImSdkIniting.set(true);
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    mLog.i("******init imsdk start******");
                    long start = System.currentTimeMillis();
                    InitParams initParams = new InitParams();
                    initParams.devRegions = "region_cn";
                    String controlUrl = StvHideApi.getDomain(context, KEY_CONTROL_URL);
                    mLog.d("Control Url:" + controlUrl);
                    if (TextUtils.isEmpty(controlUrl)) {
                        controlUrl = VIDEOCHAT_URL;
                    }
                    initParams.url = REQ_HTTP + controlUrl;
                    initParams.isLeDev = true;
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("Model", Build.DEVICE);
                        jsonObject.put("HwVersion", "1.0");
                        jsonObject.put("ReleaseVersion", "1");
                        jsonObject.put("SwVersion", "1000");
                        jsonObject.put("AppVersion", "1000");
                        jsonObject.put("SdkVersion", VersionInfo.VERSION_NAME);
                        jsonObject.put("Id", "Model");
                        jsonObject.put("Mac", "");
                        jsonObject.put("UiType", "full");
                        jsonObject.put("DeviceName", "shc");
                        JSONArray jsonArray = new JSONArray();
                        JSONObject object = null;
                        if (LetvCallManager.getInstance().isSupportVideoChat()) {
                            object = new JSONObject();
                            object.put(KEY_FEATURE, VALUE_VIDEOCHAT);
                            jsonArray.put(object);
                        }
                        if (LetvCallManager.getInstance().isSupportRemoteControl()) {
                            object = new JSONObject();
                            object.put(KEY_FEATURE, VALUE_REMOTECONTROL);
                            jsonArray.put(object);
                        }
                        jsonObject.put(KEY_SUPPORT_FEATURE, jsonArray.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    initParams.userData = jsonObject.toString();
                    // initParams.devRegions = st.strDevRegions;
                    LetvCallManager.getInstance().init(context.getApplicationContext(), initParams);
                    // LetvCallManager.getInstance().login(SystemUtils.getDeviceId(context));
                    LetvCallManager.getInstance().setUser(SystemUtils.getDeviceId(context));
                    mLog.d("device id :" + SystemUtils.getDeviceId(context));
                    LetvCallManager.getInstance().setVideoCodec("VP8");
                    LeVideoChatPushManager.getInstance().initAndRegisterPush(context);
                    long end = System.currentTimeMillis();
                    mLog.i("******Init ImSdk spend******" + (end - start));
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mImSdkHasInit.set(true);
                            mImSdkIniting.set(false);
                            if (callback != null) {
                                callback.onComplete();
                            }
                        }
                    });
                }
            });
        } else if (callback != null) {
            callback.onComplete();
        }
    }

    private int getAppVersion(Context context) {
        int version = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            version = pi.versionCode;
            if (version == 0) {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return version;
    }
}
