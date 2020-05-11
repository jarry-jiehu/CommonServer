
package com.stv.commonservice.appupdate.net;

import android.content.Context;
import android.text.TextUtils;

import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;

import java.util.HashMap;

import eui.lighthttp.Helper;
import eui.lighthttp.Response;
import eui.lighthttp.scloud.ScloudHelper;

/**
 * report server
 */

public class UpdateReporter {
    /**
     * Request: http {@link #getReqUrl(String, OperatingResults, String)}
     */
    private static final String REQ_HTTP = "http://";

    /**
     * Request:state {@link #reportDate(String, String, OperatingResults)}
     */
    private static final int REQ_SUCCESS = 10000;
    /**
     * Request:state {@link #reportDate(String, String, OperatingResults)}
     */
    private static final int REQ_SERVER_SUCCESS = 200;
    /**
     * Request:api {@link #getReqUrl(String, OperatingResults, String)}
     */
    private static String REQ_API_INSTALL_REPORT = "/apk/api/v1/installReport";

    private static String STR_REPORT_DOWNLOWD = "downlowdFlag";
    private static String STR_REPORT_INSTALL = "installFlag";
    private static String STR_REPORT_UPDATE = "updateFlag";
    private static String FLAG_DEVICE_TYPE = "TV";

    private static UpdateReporter sInstance;
    private final Helper mHelpler;
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_APPUPDATE, "UpdateReporter");
    private Context mContext;

    private UpdateReporter(Context context) {
        mContext = context;
        mHelpler = new Helper();
    }

    public synchronized static UpdateReporter getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new UpdateReporter(context);
        }
        return sInstance;
    }

    /**
     * 上报下载是否成功
     * @param otherdata
     * @param results
     */
    public void reportDownlowd(String otherdata, OperatingResults results) {
        reportDate(STR_REPORT_DOWNLOWD, otherdata, results);
    }

    public void reportInstall(String otherdata, OperatingResults results) {
        reportDate(STR_REPORT_INSTALL, otherdata, results);
    }

    /**
     * 上报升级结果
     * @param otherdata
     * @param results
     */
    public void reportupdate(String otherdata, OperatingResults results) {
        reportDate(STR_REPORT_UPDATE, otherdata, results);
    }

    private void reportDate(String flag, String otherdata, OperatingResults results) {
        final String reqUrl = getReqUrl(flag, results, otherdata);
        if (TextUtils.isEmpty(reqUrl)) {
            mLog.i("request url is null!");
            return;
        }
        int requestCount = 0;
        while (requestCount < 3) {
            requestCount++;
            try {
                Response response = mHelpler.requestByGet(reqUrl);
                int code = response.getStatusCode();
                String jsonString = response.getContent();
                mLog.v("update reporter response data :" + jsonString);
                if (REQ_SERVER_SUCCESS != code) {
                    mLog.i("response code is " + code + "request update date error!");

                    mLog.w("check Update requestCount " + requestCount);
                    Thread.sleep(5 * (requestCount + 1) * 1000);
                }

                mLog.i("repoter success!");
                return;
            } catch (Exception e) {
                mLog.w("check Update error ,e: " + e.getMessage());
            }
        }
    }

    private String getReqUrl(String flag, OperatingResults results, String otherdata) {
        String domain = StvHideApi.getDomain(mContext, Constants.Y_OTA);
        if (TextUtils.isEmpty(domain)) {
            mLog.i("reporte getdomain is null!");
            return null;
        }
        String url = REQ_HTTP + domain + REQ_API_INSTALL_REPORT + getReqParame(flag, results, otherdata);
        mLog.v("report url is :" + url);
        return url;
    }

    private String getReqParame(String flag, OperatingResults results, String otherdata) {
        String reqParame = "";

        reqParame += "?" + otherdata;

        reqParame += "&deviceType=";
        reqParame += FLAG_DEVICE_TYPE;

        reqParame += "&deviceId=";
        reqParame += StvHideApi.getLetvMac();

        reqParame += "&" + StvHideApi.getGeneralParam();

        reqParame += "&eui=";
        reqParame += StvHideApi.getLetvUiVersion();

        reqParame += "&" + flag + "=";
        reqParame += results.getOperatingType();

        long time = System.currentTimeMillis();
        reqParame += "&_time=";
        reqParame += time;

        reqParame += "&_ak=";
        reqParame += Constants.REQ_AK;

        HashMap<String, String> generalParam = getGeneralParam(reqParame);

        reqParame += "&_sign=";
        reqParame += ScloudHelper.getSignature1(Constants.REQ_AK, Constants.REQ_SK, generalParam, time);

        return reqParame;
    }

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

    public enum OperatingResults {
        SUCCESS("success"), FAILURE("failure");
        private String type;

        private OperatingResults(String type) {
            this.type = type;
        }

        public String getOperatingType() {
            return this.type;
        }
    }
}
