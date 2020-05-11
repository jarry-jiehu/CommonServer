
package com.stv.commonservice.appupdate.net;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Message;
import android.text.TextUtils;

import com.stv.commonservice.appupdate.manager.UpdateManager.DefaultHandler;
import com.stv.commonservice.appupdate.model.UpdateInfo;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import eui.lighthttp.Helper;
import eui.lighthttp.Response;
import eui.lighthttp.scloud.ScloudHelper;

/**
 * 请求更新列表请求者
 */
public class UpdateInfoRequester {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_APPUPDATE, UpdateInfoRequester.class.getSimpleName());
    private static UpdateInfoRequester sInstance;
    private Context mContext;

    private static final int REQ_SUCCESS = 10000;
    private static String FLAG_DEVICE_TYPE = "TV";
    private static final String REQ_HTTP = "http://";
    private static String REQ_API_ALL_UPGRADE_INFO = "/apk/api/v1/getAllUpgradeInfo";
    private Helper mHelper;

    private UpdateInfoRequester(Context context) {
        mContext = context;
        mHelper = new Helper();
    }

    public synchronized static UpdateInfoRequester getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new UpdateInfoRequester(context);
        }
        return sInstance;
    }

    public void getOneUpdateInfo(String packageName, final DefaultHandler handler) {

        int verCod = getVersionCode(packageName);
        if (verCod < 0) {
            mLog.i("getVersionCode is error!");
            return;
        }

        Map<String, String> reqMap = new HashMap<String, String>();
        JSONArray jArray = new JSONArray();
        JSONObject json = new JSONObject();
        try {
            json.put("packageName", packageName);
            json.put("apkVersion", verCod + "");
        } catch (JSONException e) {
            mLog.e("getOneUpdateInfo is error!" + e.getMessage());
        }
        jArray.put(json);
        reqMap.put(Constants.REQ_DATA, jArray.toString());
        mLog.i("reqMap is :" + jArray.toString());
        getUpdateInfo(Constants.GET_UPDATE_ONE_SUCCESS, Constants.GET_UPDATE_ONE_ERROR, reqMap, handler);
    }

    private int getVersionCode(String packageName) {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName,
                    PackageManager.GET_ACTIVITIES);
            if (null != packageInfo) {
                int versionCode = packageInfo.versionCode;
                return versionCode;
            }
        } catch (Exception e) {
            mLog.e("getVersionCode is error!" + e.getMessage());
        }
        return -1;
    }

    public void getAllUpdateINfo(DefaultHandler handler) {
        List<PackageInfo> pkgs = mContext.getPackageManager().getInstalledPackages(0);
        final Map<String, String> reqMap = generateReqMap(pkgs);
        getUpdateInfo(Constants.GET_UPDATE_LIST_SUCCESS, Constants.GET_UPDATE_LIST_ERROR, reqMap, handler);
    }

    /**
     * 全部检测更新
     * @param handler
     */
    public void getUpdateInfo(final int success, final int error, final Map<String, String> reqMap, final DefaultHandler handler) {
        // List<PackageInfo> pkgs = mContext.getPackageManager().getInstalledPackages(0);
        // final Map<String, String> reqMap = generateReqMap(pkgs);

        final String serverUrl = getServerUrl(mContext);
        if (TextUtils.isEmpty(serverUrl)) {
            return;
        }

        try {
            Response response;
            response = mHelper.requestByPost(serverUrl, reqMap);
            int code = response.getStatusCode();
            String jsonString = response.getContent();
            mLog.i("new http tool ,response code:" + code);
            if (200 != code) {
                Message message = handler.obtainMessage(error, "Unexpected code " + response);
                handler.sendMessageDelayed(message, 0);
                return;
            }

            if (TextUtils.isEmpty(jsonString)) {
                Message message = handler.obtainMessage(error, "request json is null!");
                handler.sendMessageDelayed(message, 0);
                return;
            }

            JSONObject jsonObject = new JSONObject(jsonString);
            int errno = jsonObject.optInt("errno");
            JSONArray data = jsonObject.optJSONArray("data");

            if (errno != REQ_SUCCESS) {
                String errmsg = jsonObject.optString("errmsg");
                Message message = handler.obtainMessage(error, errmsg);
                handler.sendMessageDelayed(message, 0);
                return;
            }
            if (data == null) {
                Message message = handler.obtainMessage(success);
                handler.sendMessageDelayed(message, 0);
                return;
            }
            if (data.length() > 0) {
                mLog.v("REQ UPDATE LIST : Get list success . \n" + data.toString() + "\n");
                List<UpdateInfo> updateInfosJson = getUpdateInfosJson(data);
                Message message = handler.obtainMessage(success, updateInfosJson);
                handler.sendMessageDelayed(message, 0);
            } else {
                Message message = handler.obtainMessage(success);
                handler.sendMessageDelayed(message, 0);
            }

        } catch (Exception e) {
            if (handler != null) {
                Message message = handler.obtainMessage(error, e.getMessage());
                handler.sendMessageDelayed(message, 0);
            }
            mLog.e("getUpdateInfo method error ! e : " + e.getMessage());
        }
    }

    /**
     * { "upgradeType": 1, "apkVersion": 2, "fileUrl": "http://115.182.94.141/upload/tmp/1465281043StvLogReport.apk", "description": "2.0", "packageName": "com.stv.reportlog", "fileMd5": "" }
     * @param data
     * @return
     */
    private List<UpdateInfo> getUpdateInfosJson(JSONArray data) {
        ArrayList<UpdateInfo> updateInfos = new ArrayList<UpdateInfo>();
        try {
            if (data != null && data.length() > 0) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject object = data.optJSONObject(i);
                    UpdateInfo updateInfo = new UpdateInfo();
                    updateInfo.setApkVersion(String.valueOf(object.optInt("apkVersion")));
                    updateInfo.setFileMd5(object.optString("fileMd5"));
                    updateInfo.setDescription(object.optString("description"));
                    updateInfo.setFileUrl(object.optString("fileUrl"));
                    updateInfo.setPackageName(object.optString("packageName"));
                    updateInfo.setUpgradeType(object.optInt("upgradeType"));
                    updateInfo.setOtherdata(object.optString("otherdata"));
                    updateInfos.add(updateInfo);
                }
                return updateInfos;
            }
        } catch (Exception e) {
            mLog.e("getUpdateInfosJson method error ! e : " + e.getMessage());
        }
        return updateInfos;
    }

    /**
     * 组装post请求的参数
     * @param packageInfoList
     * @return
     */
    private Map<String, String> generateReqMap(List<PackageInfo> packageInfoList) {
        Map<String, String> reqMap = new HashMap<String, String>();
        if (packageInfoList == null || packageInfoList.size() == 0) {
            mLog.i("Generating JSON data is empty ！");
            return null;
        }
        JSONArray jArray = new JSONArray();
        try {
            JSONObject json = null;
            for (PackageInfo packageInfo : packageInfoList) {
                json = new JSONObject();
                json.put("packageName", packageInfo.packageName);
                json.put("apkVersion", packageInfo.versionCode + "");
                jArray.put(json);
            }
            mLog.i("All the data in the request update . data length :" + jArray.length());
        } catch (JSONException e) {
            mLog.e("generateReqJson method error ! e : " + e.getMessage());
        }
        if (null != jArray) {
            reqMap.put(Constants.REQ_DATA, jArray.toString());
        }
        return reqMap;
    }

    private String getServerUrl(Context context) {
        String host_addr = StvHideApi.getDomain(context, Constants.Y_OTA);
        if (TextUtils.isEmpty(host_addr)) {
            mLog.i("update getdomain is null!");
            return null;
        }
        String reqParame = getReqParame(context);

        String url = REQ_HTTP + host_addr + REQ_API_ALL_UPGRADE_INFO + "?" + reqParame;
        mLog.v("REQ URL :" + url);
        return url;
    }

    private String getReqParame(Context context) {
        String reqParame = "";

        String letvMac = StvHideApi.getLetvMac();
        long time = System.currentTimeMillis();

        HashMap<String, String> generalParam = generateMap(letvMac, time);

        String signature = ScloudHelper.getSignature1(Constants.REQ_AK, Constants.REQ_SK, generalParam, time);

        generalParam.put("_ak", Constants.REQ_AK);
        generalParam.put("_sign", signature);

        reqParame = map2ReqParame(generalParam);

        return reqParame;
    }

    private HashMap<String, String> generateMap(String letvMac, long time) {
        HashMap<String, String> generalParam = new HashMap<String, String>();

        generalParam.put("deviceType", FLAG_DEVICE_TYPE);
        generalParam.put("deviceId", letvMac);
        generalParam.put("_time", time + "");

        String tvGeneralParam = StvHideApi.getGeneralParam();

        if (tvGeneralParam != null && tvGeneralParam.length() > 0) {
            String values[] = tvGeneralParam.split("&");
            if (values == null || values.length < 1) {
                return generalParam;
            }
            for (int i = 0; i < values.length; i++) {
                String s[] = values[i].split("=");
                if (s == null) {
                    mLog.i(" get param is null !");
                    break;
                }
                if (s.length != 2) {
                    mLog.i(" get param length = " + s.length);
                    break;
                }
                generalParam.put(s[0], s[1]);
            }
        }
        return generalParam;
    }

    private String map2ReqParame(HashMap<String, String> generalParam) {
        String reqParame = "";
        Iterator iterator = generalParam.entrySet().iterator();
        StringBuffer paramBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            paramBuffer.append("&");
            paramBuffer.append(key);
            paramBuffer.append("=");
            paramBuffer.append(value);
        }
        reqParame = paramBuffer.toString();
        if (!TextUtils.isEmpty(reqParame) && reqParame.startsWith("&")) {
            reqParame = reqParame.substring(1);
        }
        return reqParame;
    }
}
