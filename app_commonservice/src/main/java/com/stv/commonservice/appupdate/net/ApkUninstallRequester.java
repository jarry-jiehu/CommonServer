
package com.stv.commonservice.appupdate.net;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.stv.commonservice.appupdate.model.UninstallBean;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.InstallUtils;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;

import java.util.HashMap;
import java.util.List;

import eui.lighthttp.Helper;
import eui.lighthttp.Response;
import eui.lighthttp.scloud.ScloudHelper;

/**
 * 获取需要卸载的Apk列表请求
 */

public class ApkUninstallRequester {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_APPUPDATE,
            "ApkUninstallRequester");
    private static String FLAG_DEVICE_TYPE = "TV";
    private static final String REQ_HTTP = "http://";
    private static String REQ_API_ALL_UPGRADE_INFO = "/apk/api/v1/getAllUninstall";
    private static ApkUninstallRequester sInstance;
    private Context mContext;
    private Helper mHelper;

    private ApkUninstallRequester(Context context) {
        mContext = context;
        mHelper = new Helper();
    }

    public synchronized static ApkUninstallRequester getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new ApkUninstallRequester(context);
        }
        return sInstance;
    }

    /**
     * 检测需要卸载的应用
     */
    public void checkApkUninstall() {
        final String reqUrl = getServerUrl(mContext);
        if (TextUtils.isEmpty(reqUrl)) {
            mLog.i("request url is null!");
            return;
        }
        int requestCount = 0;
        while (requestCount < 3) {
            requestCount++;
            try {
                Response response = mHelper.requestByGet(reqUrl);
                int code = response.getStatusCode();
                String jsonString = response.getContent();
                mLog.d("reporter code:" + code);
                mLog.d("update reporter response data :" + jsonString);
                if (200 == code) {
                    UninstallBean bean = new Gson().fromJson(jsonString, UninstallBean.class);
                    mLog.i("UninstallBean:" + bean.toString());
                    if (bean.getErrno() == 10000) {
                        List<UninstallBean.AppInfo> list = bean.getData();
                        if (list != null && list.size() > 0) {
                            mLog.i("AppInfo  list size:" + list.size());
                            for (UninstallBean.AppInfo info : list) {
                                mLog.i("AppInfo  info:" + info.toString());
                                if (InstallUtils.isApplicationAvilible(mContext,
                                        info.getPackageName())) {
                                    InstallUtils.quietUninstall(mContext, info.getPackageName());
                                }
                            }
                        }
                    }
                    break;
                }
                mLog.w("check Update requestCount " + requestCount);
                Thread.sleep(5 * (requestCount + 1) * 1000);
            } catch (Exception e) {
                mLog.w("check Update error ,e: " + e.getMessage());
            }
        }
    }

    /**
     * 获取卸载的请求地址
     * @param context
     * @return
     */
    private String getServerUrl(Context context) {
        String host_addr = StvHideApi.getDomain(context, Constants.Y_OTA);
        if (TextUtils.isEmpty(host_addr)) {
            mLog.i("update getdomain is null!");
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(REQ_HTTP);
        builder.append(host_addr);
        builder.append(REQ_API_ALL_UPGRADE_INFO);
        builder.append(getReqParame());
        mLog.v("REQ URL :" + builder.toString());
        return builder.toString();
    }

    /**
     * 生成请求参数
     * @return
     */
    private String getReqParame() {
        long time = System.currentTimeMillis();
        StringBuilder builder = new StringBuilder();
        builder.append("?_ak=" + Constants.REQ_AK);
        builder.append("&deviceType=" + FLAG_DEVICE_TYPE);
        builder.append("&deviceId=" + StvHideApi.getLetvMac());
        builder.append("&" + StvHideApi.getGeneralParam());
        builder.append("&_time=" + time);
        builder.append("&region=CN");
        HashMap<String, String> generalParam = getGeneralParam(builder.toString());
        builder.append("&_sign=" + ScloudHelper.getSignature1(Constants.REQ_AK, Constants.REQ_SK,
                generalParam, time));
        return builder.toString();
    }

    /**
     * 生成sign规则
     * @param reqParame
     * @return
     */
    private HashMap<String, String> getGeneralParam(String reqParame) {
        HashMap<String, String> param = new HashMap<String, String>();
        String substring = reqParame.substring(1);
        String[] split = substring.split("&");
        for (int i = 0; i < split.length; i++) {
            String[] strings = split[i].split("=");
            if (strings.length == 2) {
                param.put(strings[0], strings[1]);
            } else {
                param.put(strings[0], "");
            }
        }
        return param;
    }

}
