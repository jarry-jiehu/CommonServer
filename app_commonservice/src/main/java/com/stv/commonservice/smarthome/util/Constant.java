
package com.stv.commonservice.smarthome.util;

import android.content.Context;
import android.text.TextUtils;

import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;

public class Constant {
    private static LogUtils slogUtils = LogUtils.getInstance(LogUtils.MODULE_SMART_HOME, "Constant");

    /** EIOT服务器 eiotcloud.beta-ext.scloud.letv.com, eiotcloud.qa.scloud.letv.cn **/
    //private static final String EIOT_URL = "eiotcloud.beta-ext.scloud.letv.com";// 测试服务器
     private static final String EIOT_URL = "api-elink.cp21.ott.cibntv.net";// 正式服务器
    public static final String ROUTE_LOGIN = "/api/account/login";
    public static final String ROUTE_QUERY_HOUSE = "/api/house/query";
    public static final String ROUTE_BIND_DEVICE = "/api/platform/appbind";
    public static final String ROUTE_UNBIND_DEVICE = "/api/platform/appunbind";
    public static final String ROUTE_REGISTER_DEVICE = "/api/familydevice/register";
    /** mqtt服务器地址 格式例如：tcp://10.0.261.159:1883 **/
    private static final String MQTT_URL = "tcp://10.112.36.194:1883";// 测试服务器
    // private static final String MQTT_URL = "tcp://emq-elink.cp21.ott.cibntv.net:1883";// 正式服务器
    /** APP ID && KEY **/
    public static final String APP_ID = "10000";
    public static final String APP_KEY = "f8e17ce81e69470aaa540df84a877e5a";
    public static final String PRODUCT_KEY = "pk.34e26a8ba85";
    private static final String KEY_EIOT_URL = "eioturl";
    private static final String KEY_MQTT_URL = "eiotemqurl";
    /** Preferences相关 **/
    public static final String KEY_ELINK_ID = "elinkId";
    public static final String KEY_DEVICE_SECRET = "deviceSecret";
    public static final String KEY_LE_TOKEN = "letoken";
    public static final String KEY_HOUSE_ID = "houseId";
    /** 账号中心相关 **/
    public static final String ACCOUNT_TYPE = "com.letv";
    public static final String AUTHORITY = "com.letv.account.userinfo";
    public static final String COLUMN_LOGIN_NAME = "login_name";
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_BEAN = "bean";
    public static final String COLUMN_TOKEN = "token";
    public static final String COLUMN_NICK_NAME = "nick_name";
    public static final String COLUMN_DEAD_LINE = "dead_line";
    public static final String COLUMN_MOBILE = "mobile";
    public static final String COLUMN_AREA = "area";

    /**
     * 获取EIOT服务器地址
     * @param context
     * @return
     */
    private static String getEiotUrl(Context context) {
        String eiotUrl = StvHideApi.getDomain(context, KEY_EIOT_URL);
        slogUtils.d("getDomain for EIOT Url: " + eiotUrl);
        if (TextUtils.isEmpty(eiotUrl)) {
            eiotUrl = EIOT_URL;
        }
        if (!eiotUrl.contains("http")) {
            eiotUrl = "http://" + eiotUrl;
        }
        return "http://" + EIOT_URL;
    }

    /**
     * 获取MQTT URL
     * @param context
     * @return
     */
    public static String getMQTTUrl(Context context) {
        String mqttUrl = StvHideApi.getDomain(context, KEY_MQTT_URL);
        slogUtils.d("getDomain for MQTT Url: " + mqttUrl);
        if (TextUtils.isEmpty(mqttUrl)) {
            mqttUrl = MQTT_URL;
        }
        return MQTT_URL;
    }

    public static String getLoginUrl(Context context) {
        return getEiotUrl(context) + ROUTE_LOGIN;
    }

    /**
     * 获取房屋信息查询的url
     * @return
     */
    public static String getHouseQueryUrl(Context context) {
        return getEiotUrl(context) + ROUTE_QUERY_HOUSE;
    }

    /**
     * 获取设备绑定的url
     * @return
     */
    public static String getDeviceBindUrl(Context context) {
        return getEiotUrl(context) + ROUTE_BIND_DEVICE;
    }

    /**
     * 获取设备解绑的url
     * @return
     */
    public static String getDeviceUnbindUrl(Context context) {
        return getEiotUrl(context) + ROUTE_UNBIND_DEVICE;
    }

    /**
     * 获取设备注册的url
     * @return
     */
    public static String getRegisterUrl(Context context) {
        return getEiotUrl(context) + ROUTE_REGISTER_DEVICE;
    }

}
