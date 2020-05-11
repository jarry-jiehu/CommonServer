
package com.stv.commonservice.smarthome.manager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.smarthome.util.Constant;

public class AccountInfoManager {
    private LogUtils logUtils = LogUtils.getInstance(LogUtils.MODULE_SMART_HOME, "AccountInfoManager");
    public static final Uri CONTENT_URI = Uri.parse("content://" +
            Constant.AUTHORITY + "/" + Constant.ACCOUNT_TYPE);

    public static final String[] sProjection = new String[] {
            Constant.COLUMN_LOGIN_NAME, Constant.COLUMN_UID, Constant.COLUMN_PASSWORD, Constant.COLUMN_BEAN,
            Constant.COLUMN_TOKEN, Constant.COLUMN_NICK_NAME, Constant.COLUMN_DEAD_LINE, Constant.COLUMN_AREA
    };

    private static AccountInfoManager sInstance = null;

    public static synchronized AccountInfoManager getInstance() {
        if (sInstance == null)
            sInstance = new AccountInfoManager();
        return sInstance;
    }

    public AccountInfoManager() {
    }

    public String getAccountInfo(Context context, String info) {
        logUtils.d("info=" + info);
        String result = "";
        Cursor cursor = null;
        ContentResolver mContentResolver;

        try {
            mContentResolver = context.getContentResolver();
            cursor = mContentResolver.query(CONTENT_URI,
                    sProjection, null, null, null);

            if (null != cursor && cursor.moveToFirst() && cursor.getColumnIndex(info) != -1) {
                result = cursor.getString(cursor.getColumnIndex(info));
                logUtils.d("result = " + result);
            }
        } catch (Exception e) {
            logUtils.e(e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return result;
    }
}
