
package com.stv.commonservice.common.service;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.letv.android.lcm.LetvPushBaseIntentService;
import com.stv.commonservice.appupdate.manager.UpdateManager;
import com.stv.commonservice.common.threadpool.PushTimingExecutor;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.LogUploadUtils;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class LetvPushIntentService extends LetvPushBaseIntentService {

    private LogUtils mLog = LogUtils.getInstance("Common", LetvPushIntentService.class.getSimpleName());
    // 拉取日志
    private static final int PUSH_TYPE_UPLOAD_LOG = 1;
    // 设置用户体验计划开关
    private static final int PUSH_TYPE_SET_LOGCATVD = 2;
    // 打开第三方应用
    private static final int PUSH_TYPE_START_APP = 3;
    // 用户体验计划属性key
    private static final String KEY_PROPERTIES_LOGCATVD = "persist.sys.logcatvd";
    // 打开用户体验计划
    private static final String LOGCATVD_ON = "1";
    // 关闭用户体验计划
    private static final String LOGCATVD_OFF = "0";

    public LetvPushIntentService(String name) {
        super(name);
    }

    public LetvPushIntentService() {
        super("LetvPushIntentService");
    }

    @Override
    protected void onMessage(Context context, String message) {
        mLog.i("push data is :" + message);
        try {
            JSONObject jsonObject = new JSONObject(message);
            int type = jsonObject.optInt("type");
            mLog.i("type :" + type);
            if (PUSH_TYPE_UPLOAD_LOG == type) {
                uploadLog(jsonObject);
                return;
            } else if (PUSH_TYPE_SET_LOGCATVD == type) {
                setLogcatvd(jsonObject);
                return;
            }else if (PUSH_TYPE_START_APP == type){
               if (startApp(context,jsonObject))
                 return;
            }
            String pushVersion = jsonObject.optString("pushVersion");
            if (!TextUtils.isEmpty(pushVersion)) {
                sendAppUpdate(context, jsonObject);
            }

        } catch (JSONException e) {
            mLog.e("push message is error," + e.getMessage());
        }
    }

    private void uploadLog(JSONObject jsonObject) {
        String phone = jsonObject.optString("phone");
        String path = jsonObject.optString("path");
        String describe = jsonObject.optString("describe");
        LogUploadUtils.uploadLogs(LetvPushIntentService.this, phone, path, describe);
    }

    private void setLogcatvd(JSONObject jsonObject) {
        String logcatvd = jsonObject.optString("logcatvd");
        if (LOGCATVD_OFF.equals(logcatvd) || LOGCATVD_ON.equals(logcatvd)) {
            StvHideApi.setSystemProperties(this,KEY_PROPERTIES_LOGCATVD, logcatvd);
            mLog.i("logcatvd : " + StvHideApi.getSystemProperties(this,KEY_PROPERTIES_LOGCATVD));
        }
    }

    private void sendAppUpdate(Context context, JSONObject jsonObject) {
        JSONObject data = jsonObject.optJSONObject("data");
        String packageName = data.optString("packageName");
        int packageVersion = data.optInt("apkVersion");
        int randMin = data.optInt("randMin");
        int silentType = data.optInt("isSilent", -1);
        mLog.i("push data packageName:" + packageName + ",packageVersion:" + packageVersion);
        switch (silentType) {
            case 1:
                sendSilent(context, packageName, randMin);
                break;
        }
    }

    /**
     * 打开第三方App
     * @param jsonObject
     */
    private boolean startApp(Context context,JSONObject jsonObject){
        String action = jsonObject.optString("action");
        mLog.d("action: "+action);
        if (!TextUtils.isEmpty(action)&&action.equals("app_start")){
            String packageName = jsonObject.optString("packageName");
            String param = jsonObject.optString("param");
            mLog.d("packageName:"+packageName+"   ;param:"+param);
            if (!TextUtils.isEmpty(packageName)){
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    context.startActivity(intent);
                    return  true;
                } else {
                    mLog.w("can not find app:  "+packageName);
                }
            }

        }
         return false;
    }

    private void sendSilent(final Context context, final String packageName, int randMin) {
        int nextInt = 0;
        if (randMin > 0) {
            Random random = new Random();
            nextInt = random.nextInt(randMin * 60);
        }
        mLog.i("push: delay " + nextInt + " second to install.  ");
        PushTimingExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                UpdateManager.getInstance(context).checkOneUpdate(packageName);
            }
        }, nextInt);
    }

    private void sendUnsilent(Context context, String packageName, String message) {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_RECEIVE_PUSH_UPDATE);
        intent.addCategory(packageName);
        if (Build.VERSION.SDK_INT >= 12) {
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        }
        intent.putExtra(Constants.EXTRA_VALUE_MESSAGE, message);
        context.sendBroadcast(intent, packageName + Constants.RECEIVE_PERMISSION);
        mLog.i("push to package <" + packageName + "> 1 times.");
    }
}
