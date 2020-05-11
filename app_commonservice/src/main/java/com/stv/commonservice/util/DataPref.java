
package com.stv.commonservice.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * SharedPreference工具类
 * @version 1.0
 */
public class DataPref {
    // =============================== Constants Start===========================================
    private static final String FILE_NAME = "COMMON_SERVICE";
    // 检测更新时间
    private static String CHECK_TIMESTAMP = "CHECK_TIMESTAMP";
    // 储存UiType
    public static final String Key_UITYPE = "KEY_UITYPE";
    // 激活
    public static final String SHAREPRE_ACTIVE = "SHAREPRE_ACTIVE";
    // 遥控器升级
    public static final String PREFERENCE_REMOTER_UPDATE_LATER = "PREFERENCE_REMOTER_UPDATE_LATER";
    // 已经做过检查标记
    public static final String PREFERENCE_REMOTER_UPDATE_DONE = "PREFERENCE_REMOTER_UPDATE_DONE";
    // 保存上一次国广后台返回的状态码
    private static final String KEY_LAST_RESULT_CODE = "KEY_LAST_RESULT_CODE";
    // 保存乐视规则的状态码 未通过（接口返回0或2） 通过（接口返回1） 通过（接口返回-1或超时）
    private static final String KEY_STV_RESULT_CODE = "KEY_STV_RESULT_CODE";
    // ================================ Constants End ==========================================

    // =============================== Fields Start===========================================
    private Context mContext;
    private static volatile DataPref sInstance;
    private SharedPreferences mPref;
    private Editor mEditor;

    // ================================ Fields End ==========================================

    // =============================== Constructors Start===========================================

    private DataPref(Context context) {
        mContext = context;
    }

    public synchronized static DataPref getInstance(Context context) {
        if (null == sInstance) {
            synchronized (DataPref.class) {
                if (null == sInstance) {
                    sInstance = new DataPref(context);
                }
            }
        }
        return sInstance;
    }

    // ================================ Constructors End ==========================================

    // =============================== Getters Start===========================================

    private SharedPreferences getSharedPreferences() {
        if (null == mPref) {
            mPref = mContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        }
        return mPref;
    }

    private Editor getEditor() {
        if (null == mEditor) {
            mEditor = getSharedPreferences().edit();
        }
        return mEditor;
    }

    public long getCheckTimeStamp() {
        return getSharedPreferences().getLong(CHECK_TIMESTAMP, 0);
    }

    public String getKeyUiType() {
        return getSharedPreferences().getString(Key_UITYPE, "");
    }

    public int getIntPreferences(String key, int defaultValue) {
        return getSharedPreferences().getInt(key, 0);
    }

    public String getStringPreferences(String key, String defaultValue) {
        return getSharedPreferences().getString(key, defaultValue);
    }

    public boolean getACTIVE() {
        return getSharedPreferences().getBoolean(SHAREPRE_ACTIVE, false);
    }

    public boolean getRemoterLater() {
        return getSharedPreferences().getBoolean(PREFERENCE_REMOTER_UPDATE_LATER, false);
    }

    public boolean getRemoterDone() {
        return getSharedPreferences().getBoolean(PREFERENCE_REMOTER_UPDATE_DONE, false);
    }

    public String getLastAttestCode() {
        return getStringPreferences(KEY_LAST_RESULT_CODE, "-10");
    }

    public String getStvAttestResuleCode() {
        return getStringPreferences(KEY_STV_RESULT_CODE, Constants.CARRIER_STATE_DEFAULT);
    }

    // ================================ Getters End ==========================================

    // =============================== Setters Start===========================================
    public boolean setACTIVE(boolean active) {
        Editor editor = getEditor();
        editor.putBoolean(SHAREPRE_ACTIVE, active);
        return editor.commit();
    }

    public boolean setCheckTimeStamp(long timestamp) {
        Editor editor = getEditor();
        editor.putLong(CHECK_TIMESTAMP, timestamp);
        return editor.commit();
    }

    public boolean setKeyUiType(String uiType) {
        Editor editor = getEditor();
        editor.putString(Key_UITYPE, uiType);
        return editor.commit();
    }

    public boolean setIntPreferences(String key, int value) {
        Editor editor = getEditor();
        editor.putInt(key, value);
        return editor.commit();
    }

    public boolean setStringPreferences(String key, String value) {
        Editor editor = getEditor();
        editor.putString(key, value);
        return editor.commit();
    }

    public boolean setRemoterLater(boolean later) {
        Editor editor = getEditor();
        editor.putBoolean(PREFERENCE_REMOTER_UPDATE_LATER, later);
        return editor.commit();
    }

    public boolean setRemoterDone(boolean done) {
        Editor editor = getEditor();
        editor.putBoolean(PREFERENCE_REMOTER_UPDATE_DONE, done);
        return editor.commit();
    }

    public void setLastAttestCode(String lastAttestCode) {
        setStringPreferences(KEY_LAST_RESULT_CODE, lastAttestCode);
    }

    public void setStvAttestResultCode(String stvAttestResultCode) {
        setStringPreferences(KEY_STV_RESULT_CODE, stvAttestResultCode);
    }
    // ================================ Setters End ==========================================
}
