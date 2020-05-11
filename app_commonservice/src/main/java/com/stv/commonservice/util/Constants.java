
package com.stv.commonservice.util;

import android.text.TextUtils;

import eui.tv.TvManager;

public class Constants {

    public static final String STR_PACKAGENAME = "com.stv.commonservice";
    public static final String TV_ACTION_ON = "com.letv.android.str.TV_ACTION_ON";
    public static final String STV_ACTION_CONNECTIVITY_CHANGE = "stv.net.conn.CONNECTIVITY_CHANGE";

    /******************** AppUpdate start ***************************/

    /**
     * 请求升级列表成功
     */
    public static final int GET_UPDATE_LIST_SUCCESS = 1001;
    /**
     * 请求升级列表失败
     */
    public static final int GET_UPDATE_LIST_ERROR = GET_UPDATE_LIST_SUCCESS + 1;
    /**
     * 请求下载apk成功
     */
    public static final int GET_DOWNLOAD_APK_SUCCESS = GET_UPDATE_LIST_ERROR + 1;
    /**
     * 请求下载apk失败
     */
    public static final int GET_DOWNLOAD_APK_ERROR = GET_DOWNLOAD_APK_SUCCESS + 1;
    /**
     * 请求一个升级成功
     */
    public static final int GET_UPDATE_ONE_SUCCESS = GET_DOWNLOAD_APK_ERROR + 1;
    /**
     * 请求一个升级成功
     */
    public static final int GET_UPDATE_ONE_ERROR = GET_UPDATE_ONE_SUCCESS + 1;

    public static final String Y_OTA = "ota";
    public static final String Y_UPDATE_BASE_HOST_CONST = "persist.letv.apiUrl";
    public static final String Y_UPDATE_BASE_HOST_DEFAULT = "ota.scloud.letv.com";

    public static final String REQ_AK = "ak_lMHsCi32Wgyaqg23g9YL";
    public static final String REQ_SK = "sk_xVU5x7uvHXse38f8axKN";
    public static final String REQ_DATA = "data";

    public static final String Y_APK_PATH = "/data/";
    public static final String Y_APK_SUFFIX = ".apk";
    public static final String Y_APK_DIC = ".autoupdate";
    public static final String Y_SERVICE_RECEIVER_ACTION = "com.android.letv.update";
    public static final String Y_INSTALL_RECEIVER_ACTION = "com.android.letv.install";

    public static final String Y_PREFERENCES_NAME = "PACKAGE";
    public static final String Y_PREFERENCES_KEY = "init";

    public static final String ACTION_CHECK_UPDATE = "com.stv.stvappupdate.CheckUpdate";
    public static final String ACTION_INSTALL = "com.stv.stvappupdate.Install";
    public static final String ACTION_INSTALL_SUCCESS = "com.android.packageinstaller.action.APP_INSTALL_SUCCESS";
    public static final String ACTION_INSTALL_FAILED = "com.android.packageinstaller.action.APP_INSTALL_FAILED";

    public static final String ACTION_UNINSTALL_SUCCESS = "com.android.packageinstaller.action.APP_UNINSTALL_SUCCESS";
    public static final String ACTION_UNINSTALL_FAILED = "com.android.packageinstaller.action.APP_UNINSTALL_FAILED";


    public static final String EXTRA_PACKAGE = "com.android.packageinstaller.action.package";
    public static final String EXTRA_INSTALL_ERROR_COUNT = "install_error_count";
    public static final String EXTRA_UPDATE = "extra_update";
    public static final String EXTRA_STORAGE_ERROR_REASON = "storage_error_reason";

    /******************** AppUpdate end ***************************/

    /******************** Domain start ***************************/

    public static final String DOMAIN_BETA_URL = "test.tvapi.letv.com";
    public static final String DOMAIN_URL = "dnm.scloud.letv.com";
    public static final String DOMAIN_URL_CIBN = "dnm-scloud.cp21.ott.cibntv.net";
    private static final String UI_TYPE = TvManager.getUiType().toLowerCase();
    public static String DOMAIN_SAVE_PATH_DEFAULT;

    static {
        if (!TextUtils.isEmpty(UI_TYPE) && !"null".equalsIgnoreCase(UI_TYPE)) {
            DOMAIN_SAVE_PATH_DEFAULT = "domain/" + UI_TYPE + "/domain_default";
        } else {
            DOMAIN_SAVE_PATH_DEFAULT = "domain/cibn/domain_default";
        }
    }

    public static final String SUB_DOMAIN_URL = "/api/v1/domain/list";
    public static final String DOMAIN_AK = "ak_7jRazztjeNjew7e";
    public static final String DOMAIN_SK = "sk_epxIp7dhqnjqWu7mfztj";
    public static final String ALARM_BROADCAST_FOR_DOMAIN = "com.stv.domain.alarmbraodcast";
    public static final int SALE_DOMAIN_ID = 1503;
    public static final int GET_DOMAIN_DELAY = 1000 * 60 * 60 * 24;

    public static final String DOMAIN_T_SAVE_PATH = "/data/data/" + STR_PACKAGENAME + "/domain_T";
    public static final String DOMAIN_S_SAVE_PATH = "/data/data/" + STR_PACKAGENAME + "/domain_S";

    /******************** Domain end ***************************/

    /******************** Attestation start ***************************/
    public static final String REQUEST_CODE_SAVE_PATH = "/data/data/" + STR_PACKAGENAME + "/requestCode";
    public static final String ALARM_BROADCAST_FOR_ATTESTATION = "com.stv.attest.alarmbraodcast";

    public static final int ALARM_ID_ATTEST = 001;
    public static final int ATTEST_REQUEST_DELAY = 1000 * 60 * 60;

    public static final String CARRIER_STATE_DENIED = "0";// 未通过（接口返回0或2）
    public static final String CARRIER_STATE_ALLOWED = "1";// 通过（接口返回1）
    public static final String CARRIER_STATE_DEFAULT = "2";// 通过（接口返回-1或超时）

    /******************** Attestation end ***************************/

    /******************** Activation start ***************************/

    public static final String URL_interface = "/manager/api/v1/tvactivation";
    public static final String ALARM_BROADCAST_FOR_ACTIVATION = "com.stv.activation.alarmbraodcast";
    public static final long ALARM_TIME = 30 * 60 * 1000;
    /******************** Activation end ***************************/

    /******************** RemoterUpdate start ***************************/

    public static final int Y_MAX_DELAY_TIME = 1000;
    public static final int ALARM_ID_REMOTER = 001;
    public final static String ACTION_REMOTE_UPDATE_ALARM = "com.stv.remoter.remote.update_alarm"; // 定时器
    public static final int REMOTER_CHECK_UPDATE_DELAY = 1000 * 60 * 1;
    public final static String ACTION_REMOTE_UPDATE_SETTING = "com.stv.globalsetting.remote.update_controler_manually"; // 设置
    public final static String ACTION_REMOTE_UPDATE_WAKE_UP = "com.letv.input_nanosic"; // 唤醒
    public static final String PROP_TEST_KEY = "sys.stv.remoterupdate.test";
    public final static String ACTION_PAIRING_END = "letv.remote.remote_pairing_end"; // 配对成功
    public static final String ACTION_RESTART_SETTING = "com.stv.remoterupdate.upgradefinish";

    /******************** RemoterUpdate end ***************************/

    /******************** push start ***************************/

    public static final String ACTION_RECEIVE_PUSH_UPDATE = "com.stv.appupdate.ACTION_RECEIVE_PUSH_UPDATE";
    public static final String RECEIVE_PERMISSION = ".permission.PUSH_UPDATE";
    public static final String EXTRA_VALUE_MESSAGE = "value_message";
    public static final String EXTRA_APP_ID = "id_9450af0ceabd42f59d52ea18c886d480";
    public static final String EXTRA_APP_KEY = "ak_wxaiNZWpxBbDicYJGVQy";

    /******************** push end ***************************/

    //老平台801,918,928,8064 Platform值
    public static final int PLATFORM_6A801 = 0;
    public static final int PLATFORM_6A918 = 1;
    public static final int PLATFORM_6A928 = 2;
    public static final int PLATFORM_APQ8064 = 4;


}
