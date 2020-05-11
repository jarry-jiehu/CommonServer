
package com.stv.commonservice.activation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.stv.commonservice.domain.DataStorageManager;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.DateUtils;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;

import org.json.JSONObject;

import java.io.IOException;

import eui.lighthttp.Helper;
import eui.lighthttp.Response;

/**
 * 给终端进行激活请求， 激活分为 首次激活 ，二次联网激活（距首次激活半小时后再次进行激活），两次都激活成功则视为激活。
 */
public class ActivationTask {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_ACTIVATION, ActivationTask.class.getSimpleName());
    private Context mContext;
    private boolean mFirst;
    private DataPref mDataPref;
    private Helper helper;

    public ActivationTask(Context context, boolean first) {
        mContext = context;
        mDataPref = DataPref.getInstance(mContext);
        mFirst = first;
        helper = new Helper();
        mLog.i("ActivationTask start ");
    }

    public void execute() {
        // 标记正在请求
        DataStorageManager.getIntance(mContext).isRequestingForActivation = true;
        onExecute();
        // 标记请求结束
        DataStorageManager.getIntance(mContext).isRequestingForActivation = false;
    }

    public String httpPost(String url, String json) {
        String result = null;
        try {
            Response response = helper.requestByPost(url, json);
            mLog.i("respones: " + response.toString());
            result = response.getContent();
        } catch (IOException e) {
            mLog.e("httpPost e : " + e.toString());
        }
        return result;
    }

    public String getParam() {
        String mac = StvHideApi.getLetvMac();
        String key = StvHideApi.getStvDeviceKey(mContext);
        if (key == null) {
            key = "";
        }
        String time = DateUtils.getUtcTime();
        int halfhour;
        if (mFirst) {
            halfhour = 0;
        } else {
            halfhour = 1;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.putOpt("mac", mac);
            obj.putOpt("key", key);
            obj.putOpt("halfhour", halfhour);
            obj.putOpt("time", time);
        } catch (Exception e) {
            if (null != e) {
                mLog.e("ActivationTask getParam erro : " + e.toString());
            }
        }
        return obj.toString();
    }

    protected void onExecute() {
        String url;
        String host = StvHideApi.getDomain(mContext, "device");
        mLog.i("activation get domain host is : " + host);
        url = "https://" + host + Constants.URL_interface;
        // url = "https://" + "device.scloud.letv.com" + Constants.URL_interface;
        String json = getParam();
        mLog.i("activation body : " + json);
        String generalparam = StvHideApi.getGeneralParam();
        if (generalparam != null) {
            url = url + "?" + generalparam;
        }
        mLog.i("url : " + url);
        String result = httpPost(url, json);
        mLog.i("ActiviationTask result is " + result);
        if (null != result) {
            try {
                JSONObject js = new JSONObject(result);
                int errnum = js.optInt("errno");
                if (errnum == 10000) {
                    if (!mFirst) {
                        // 联网半小时激活成功，电视激活成功，将激活结果保存到SharedPreferences中
                        mDataPref.setACTIVE(true);
                    } else {
                        // 标记本次开机首次激活成功
                        DataStorageManager.getIntance(mContext).isRequestedForActivation = true;
                        // 定时半小时后进行联网半小时激活
                        setAlarm(false);
                    }
                }
                if (errnum == 10500) {
                    mLog.i("webserver error!! retry 30m later");
                    setAlarm(mFirst);
                }
            } catch (Exception e) {
                if (null != e) {
                    mLog.e("ActivationTask doInBackground erro : " + e.toString());
                }
            }
        }
    }

    public void setAlarm(boolean first) {
        PendingIntent pIntent = null;
        Intent intent = new Intent();
        intent.putExtra("first", first);
        intent.setAction(Constants.ALARM_BROADCAST_FOR_ACTIVATION);
        pIntent = PendingIntent
                .getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + Constants.ALARM_TIME;
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pIntent);
    }
}
