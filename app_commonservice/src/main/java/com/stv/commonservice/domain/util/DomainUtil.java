
package com.stv.commonservice.domain.util;

import android.content.Context;
import android.text.TextUtils;

import com.stv.commonservice.common.AppApplication;
import com.stv.commonservice.domain.DataStorageManager;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.DataPref;
import com.stv.commonservice.util.FileUtils;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;
import com.stv.commonservice.util.SystemUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DomainUtil {

    private static LogUtils sLog = LogUtils.getInstance(LogUtils.MODULE_DOMAIN, DomainUtil.class.getSimpleName());

    public static final String oldDomainList[] = {
            "logAnalysisUrl",
    };

    /**
     * 返回后台下发的域名host
     *
     * @return
     */
    public static String getHostFromInternet(Context context, DataPref mDataPref) {
        if (SystemUtils.isChangeUiType(context, mDataPref)) {
            File file = new File(getDomainSavePath());
            if (file.exists()) {
                file.delete();
            }
            sLog.i("isChangeUiType file exists : " + file.exists());
            return null;
        }

        DataStorageManager manager = DataStorageManager.getIntance(context);
        Map<String, String> maps = manager.getDomains();
        String domain = null;
        if (null != maps && maps.containsKey("dns")) {
            domain = maps.get("dns");
            sLog.i("get internet host :" + domain);
        }
        return domain;
    }

    public static String getDomainSavePath() {
        String domainSavePath;
        if (SystemUtils.isStableBuild()) {
            domainSavePath = Constants.DOMAIN_S_SAVE_PATH;
        } else {
            domainSavePath = Constants.DOMAIN_T_SAVE_PATH;
        }
        return domainSavePath;

    }

    /**
     * 返回第一次访问是用的写死域名host
     *
     * @return
     */
    public static String getFistHost() {
        if (SystemUtils.isStableBuild()) {
            if (SystemUtils.isCibn()) {
                return Constants.DOMAIN_URL_CIBN;
            } else {
                return Constants.DOMAIN_URL;
            }
        } else {
            return Constants.DOMAIN_BETA_URL;

        }
    }

    /**
     * 返回本地默认文件中的备用域名host
     *
     * @return
     */
    public static String getDefaultHost() {
        String domainRead = com.stv.library.common.util.FileUtils.getContent(
                com.stv.library.common.util.FileUtils.getAssetsFileInputStream(
                        AppApplication.getInstance(), Constants.DOMAIN_SAVE_PATH_DEFAULT));
        try {
            if (domainRead != null) {
                return new JSONObject(domainRead).optString("dns");
            } else {
                sLog.i("getDefaultHost domainRead is null ");
                return Constants.DOMAIN_BETA_URL;
            }
        } catch (JSONException e) {
            if (e != null) {
                sLog.e("getDefaultHost " + e.toString());
            }
        }
        return Constants.DOMAIN_BETA_URL;
    }

    /**
     * 将数据解析到的域名保存在map 里方便取域名
     *
     * @return
     */
    public static Map<String, String> parseDomainList(String domainJson) {
        sLog.i("start parseDomainList");
        String domainJsonDefault = readDomainJsonDefault();
        if (TextUtils.isEmpty(domainJson)) {
            sLog.i("parseDomainList domainJson is null");
            return null;
        }
        Map<String, String> maps = new HashMap<String, String>();
        try {
            JSONArray jsonArray;
            // 兼容之前（存的是全量的json）
            if (isJSONObject(domainJson)) {
                sLog.i("parse old data json");
                JSONObject jsonObject = new JSONObject(domainJson);
                JSONObject jsObjectData = jsonObject.getJSONObject("data");
                jsonArray = jsObjectData.optJSONArray("groups");
                if (jsonArray.length() < 1) {
                    jsonArray = jsObjectData.optJSONArray("regions");
                }
            } else {
                jsonArray = new JSONArray(domainJson);
            }
            JSONObject jsObjectDefault = new JSONObject(domainJsonDefault);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                String label = json.optString("label");
                String domain = json.optString("domain");
                if (TextUtils.isEmpty(domain)) {
                    sLog.i("label : " + label + " is empty from internet");
                    domain = jsObjectDefault.optString(label);
                }
                maps.put(label, domain);
            }
        } catch (JSONException e) {
            sLog.e("parseDomainList error : " + e.toString());
        }

        return maps;
    }

    private static boolean isJSONObject(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("data")) {
                return true;
            }
        } catch (JSONException e) {
            sLog.e("isJsonObject  false");
        }
        return false;
    }

    public static void saveDefaultDomain(Context context) {
        sLog.i("save default domain from boot");
        File file = new File(getDomainSavePath());
        if (file.exists()) {
            return;
        }
        try {
            String defaultDomainJson = com.stv.library.common.util.FileUtils.getContent(
                    com.stv.library.common.util.FileUtils.getAssetsFileInputStream(
                            AppApplication.getInstance(), Constants.DOMAIN_SAVE_PATH_DEFAULT));
            JSONObject jsonObject = new JSONObject(defaultDomainJson);
            Map<String, String> maps = getDefaultDomainList();
            for (int i = 0; i < DomainUtil.oldDomainList.length; i++) {
                String str = DomainUtil.oldDomainList[i];
                if (jsonObject.has(str)) {
                    if ("adUrl".equals(str)) {
                        String adUrlJson = jsonObject.optString(str);
                        saveDefaultAdUrl(context, adUrlJson);
                    } else {
                        StvHideApi.setSystemProperties(context, maps.get(str),
                                jsonObject.optString(str));
                    }
                }

            }
            sLog.i("save default domain first successful");
        } catch (Exception e) {
            sLog.i("save default domain erro : " + e.getMessage());
        }
    }

    private static Map<String, String> getDefaultDomainList() {
        Map<String, String> domainMaps = new HashMap<String, String>();
        domainMaps.put("logAnalysisUrl", "persist.letv.logAnalysisUrl");
        return domainMaps;
    }

    private static void saveDefaultAdUrl(Context context, String json) {
        try {
            sLog.i("save Default Adurl====>" + json);
            JSONObject jsonObject2 = new JSONObject(json);
            if (jsonObject2.has("letvadUrl")) {
                StvHideApi.setSystemProperties(context, "persist.letv.adUrl",
                        jsonObject2.optString("letvadUrl"));
            }
        } catch (Exception e) {
            if (null != e) {

                sLog.i("saveDefaultAdurl erro : " + e.toString());
            }
        }
    }

    public static String getDomain(Context context, String label) {
        DataStorageManager manager = DataStorageManager.getIntance(context);
        Map<String, String> maps = manager.getDomains();
        String domain = null;
        if (null != maps && maps.containsKey(label)) {
            domain = maps.get(label);
            sLog.i("internet label : " + label + " domain : " + domain);
            return domain;
        }
        // 当maps为null 则为第一次开机，后台拉取的域名列表不存在，故无法获取域名map，因此直接读取默认文件中的域名
        try {
            domain = new JSONObject(readDomainJsonDefault()).optString(label);
        } catch (JSONException e) {
            sLog.e("getDomain error : " + e.toString());
        }
        sLog.i("label : " + label + " domain : " + domain);
        return domain;

    }

    /**
     * 读取后台下发的域名json
     *
     * @return
     */
    public static String readDomainJson() {
        sLog.i("it is readDomainJson .");
        return FileUtils.readFileToString(DomainUtil.getDomainSavePath(), "UTF-8");

    }

    /**
     * 读取默认文件中的域名json
     *
     * @return
     */
    private static String readDomainJsonDefault() {
        sLog.i("it is readDomainJsonDefault: " + Constants.DOMAIN_SAVE_PATH_DEFAULT);
        return com.stv.library.common.util.FileUtils.getContent(
                com.stv.library.common.util.FileUtils.getAssetsFileInputStream(
                        AppApplication.getInstance(), Constants.DOMAIN_SAVE_PATH_DEFAULT));
    }
}
