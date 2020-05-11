
package com.stv.commonservice.remoterupdate;

import android.content.Context;

import com.stv.commonservice.util.LogUtils;

import java.util.HashMap;

import eui.tv.ReportManager;

public class ReportLog {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_REMOTERUPDATE, ReportLog.class.getSimpleName());

    public void agnesReport(Context context, String appName, String widgetId, String eventType,
            HashMap<String, String> eventPropsMap) {
        ReportManager.getInstance(context).reportAgnes(appName,widgetId,eventType,eventPropsMap);
        mLog.i("agnesReport : appName : " + appName + " eventType: " + eventType + " eventPropsMap: " + eventPropsMap);
    }

    public void report(Context aContext, String postMsg) {
        ReportManager.getInstance(aContext).reportLog("tvaction",null,postMsg);
    }

    public String spellPostMsg(String action, String flag, String value) {
        StringBuffer buffer = new StringBuffer("action=");
        buffer.append(action);
        buffer.append("&");
        buffer.append(flag);
        buffer.append("=");
        buffer.append(value);
        return buffer.toString();
    }
}
