
package com.stv.commonservice.domain;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.stv.commonservice.domain.util.DomainUtil;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.FileUtils;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import eui.lighthttp.Response;
import eui.lighthttp.scloud.ScloudHelper;

/**
 * Request domain form internet
 */
public class DomainRequestTask {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_DOMAIN, DomainRequestTask.class.getSimpleName());

    private final static int READ_TIME_OUT = 30000;
    private final static int CONNECT_TIME_OUT = 30000;
    private final String DOMAIN_SAVE_PATH = DomainUtil.getDomainSavePath();
    // private final String GET_DOMAIN_SUCCESSFUL = "com.stv.activation.action.getdomian.successful";

    private AtomicBoolean mIsSuccess = new AtomicBoolean(false);
    private String mRequestResult = "";
    private Context mContext;
    private Map<String, String> mDomainMaps = new HashMap<String, String>();
    private HashMap<String, String> mParam;
    private DataPref mDataPref;
    private ScloudHelper mScloudHelper;

    public DomainRequestTask(Context context) {
        this.mContext = context;
        mDataPref = DataPref.getInstance(context);
        mScloudHelper = new ScloudHelper();
        mScloudHelper.getClient().setConnectTimeout(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS);
        mScloudHelper.getClient().setReadTimeout(READ_TIME_OUT, TimeUnit.MILLISECONDS);
        setDefaultDomainList();
        mLog.i("get domain list start ");

    }

    public void execute() {
        // 标记域名下发请求正在进行，防止重复请求
        DataStorageManager.getIntance(mContext).isRequestingForDomain = true;
        onExecute();
        afterExecute();
        // 请求结束，将正在请求的标记置为false
        DataStorageManager.getIntance(mContext).isRequestingForDomain = false;
    }

    protected void onExecute() {
        mParam = getParam();
        String host = DomainUtil.getHostFromInternet(mContext, mDataPref);
        if (host != null) {
            mLog.i("getHostFromInternet() exist");
            if (toDoRequest(host)) {
                return;
            }
        } else {
            mLog.i("getHostFromInternet() doesn't exist");
        }
        host = DomainUtil.getFistHost();
        mLog.i("getFistHost : " + host);
        if (toDoRequest(host)) {
            return;
        }
        host = DomainUtil.getDefaultHost();
        mLog.i("getDefaultHost : " + host);
        toDoRequest(host);

    }

    private boolean toDoRequest(String host) {
        if (host != null && host.length() < 91) {
            StvHideApi.setSystemProperties(mContext, "sys.letv.getdomain.host", host);// for test
        }
        // 三次重试
        for (int i = 0; i < 3; i++) {
            mLog.i("for i = " + i);
            String url = "https://" + host + Constants.SUB_DOMAIN_URL;
            mIsSuccess.set(httpGet(url));
            if (mIsSuccess.get()) {
                break;
            }
            mLog.i("https get fail");
            url = "http://" + host + Constants.SUB_DOMAIN_URL;
            mIsSuccess.set(httpGet(url));
            if (mIsSuccess.get()) {
                break;
            } else {
                try {
                    mLog.i("toDoRequest i = " + i);
                    Thread.sleep(5 * (i + 1) * 1000);
                } catch (InterruptedException e) {
                    if (e != null) {
                        mLog.i("toDoRequest exception " + e.toString());
                    }
                }
            }
        }
        return mIsSuccess.get();
    }

    private boolean httpGet(String url) {
        try {
            Response response = mScloudHelper.requestWithSecurity1(url, Constants.DOMAIN_AK, Constants.DOMAIN_SK, mParam);
            int code = response.getStatusCode();
            mRequestResult = response.getContent();
            mLog.i("===response.toString()==========" + response.toString());
            return code == 200;
        } catch (IOException e1) {
            mLog.e(e1.toString());
        }
        return false;
    }

    protected void afterExecute() {
        setDomainListAmler();
        mLog.i("get domain list result " + mIsSuccess.get());
        if (mIsSuccess.get()) {
            boolean isSaveSuccess = domainListIsvalid(mRequestResult);
            if (isSaveSuccess) {
                // 请求成功，将已经请求成功的标记置为true
                DataStorageManager.getIntance(mContext).isRequestedForDomain = true;
                mLog.i("save success ,after 24h  request data again");
                // mContext.sendBroadcast(new Intent(GET_DOMAIN_SUCCESSFUL));
            } else {
                mLog.i("save fail ,after 24h  request data again");
            }
        }
    }

    private boolean domainListIsvalid(String json) {
        boolean isSaveSuccess = false;
        mLog.i("json is" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);
            int errno = jsonObject.optInt("errno");
            mLog.i("errno is" + errno);
            if (10000 == errno) {
                JSONObject jsonObjectData = new JSONObject(json).getJSONObject("data");
                JSONArray jsonArray = jsonObjectData.getJSONArray("groups");
                if (jsonArray.length() <= 0) {
                    mLog.i("this mac is not exists");
                    jsonArray = jsonObjectData.getJSONArray("regions");
                }
                DataStorageManager.getIntance(mContext).setDomains(DomainUtil.parseDomainList(jsonArray.toString()));// 并刷新map中的域名
                isSaveSuccess = FileUtils.write(DOMAIN_SAVE_PATH, jsonArray.toString(),
                        "UTF-8");
                // 2016-11-10 去掉对老域名下发的兼容
                saveOldDomain(jsonArray.toString());// 保留 persist.letv.logAnalysisUrl 属性
                return isSaveSuccess;
            } else {
                return false;

            }
        } catch (Exception e) {
            if (null != e) {
                mLog.e("jsonobject erro is ==>" + e.toString());
            }
            return false;
        }
    }

    private void saveOldDomain(String domainRead) {
        try {
            JSONArray jsonArray = new JSONArray(domainRead);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject subObject = jsonArray.getJSONObject(i);
                if (isContains(mDomainMaps, subObject.optString("label"))) {
                    String domian = subObject.optString("domain");
                    if (null != domian && 0 < domian.length()) {
                        StvHideApi.setSystemProperties(mContext,
                                mDomainMaps.get(subObject.optString("label")), domian);
                    }
                }
            }
        } catch (Exception e) {
            if (null != e) {
                mLog.e("save old domain fail e: " + e.toString());
            }
        }
    }

    private boolean isContains(Map<String, String> domainMaps, String str) {
        return domainMaps.containsKey(str);
    }

    public void setDomainListAmler() {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(Constants.ALARM_BROADCAST_FOR_DOMAIN);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, Constants.SALE_DOMAIN_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + Constants.GET_DOMAIN_DELAY,
                pendingIntent);
    }

    public HashMap<String, String> getParam() {
        String terminalType = "tv";// 终端类型
        String deviceLabel = StvHideApi.getLetvMac();// 设备标识 (手机端上传imei，tv端上传mac)
        HashMap<String, String> generalParam = new HashMap<String, String>();
        generalParam.put("terminalType", terminalType);
        generalParam.put("deviceLabel", deviceLabel);
        String str = StvHideApi.getGeneralParam();// version=V2201RCN01C055183B03142T&versionName=5.5.183T&model=S240F&ui=5.5&hwVersion=H3000&mac=c80e777e348a
        mLog.i("DomainRequestTesk getParam str : " + str);
        if (str != null && str.length() > 0) {
            String values[] = str.split("&");
            if (values == null) {
                return generalParam;
            }
            for (int i = 0; i < values.length; i++) {
                String s[] = values[i].split("=");
                if (s == null) {
                    mLog.i("DomainRequestTesk getParam is null !");
                    break;
                }
                if (s.length != 2) {
                    mLog.i("DomainRequestTesk getParam is erro s.length = " + s.length);
                    break;
                }
                generalParam.put(s[0], s[1]);
            }
        }
        return generalParam;
    }

    private void setDefaultDomainList() {
        mDomainMaps.put("logAnalysisUrl", "persist.letv.logAnalysisUrl");
    }

}
