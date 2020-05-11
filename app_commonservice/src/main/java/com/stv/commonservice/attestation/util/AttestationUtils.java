
package com.stv.commonservice.attestation.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;

import com.stv.commonservice.attestation.model.Info;
import com.stv.commonservice.attestation.net.AttestTask;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StvHideApi;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

public class AttestationUtils {
    private static LogUtils sLog = LogUtils.getInstance(LogUtils.MODULE_ATTEST, AttestTask.class.getSimpleName());

    /**
     * 获取mac 实例：c8:0e:77:81:e3:df
     * @return
     */
    public static String getAddr() {
        String addr = "";
        String mac = StvHideApi.getLetvMac();
        sLog.i("getAddr mac : " + mac);
        if (null != mac && mac.length() > 0) {
            for (int i = 0; i < mac.length(); i++) {
                if (i % 2 == 1 && i < mac.length() - 1) {
                    addr += mac.charAt(i) + ":";
                } else {
                    addr += mac.charAt(i);

                }
            }
        }
        sLog.i("getAddr : " + addr);
        return addr.toLowerCase(Locale.getDefault());
    }

    public static String getRecordmode() {
        return "1";
    }

    /**
     * 解析xml
     * @param xml
     * @return
     * @throws Exception
     */
    public static Info getInfo(String xml) throws Exception {
        if (TextUtils.isEmpty(xml)) {
            sLog.i("getInfo xml is null");
            return null;
        }
        Info info = new Info();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(getStringStream(xml), "UTF-8");
        int event = parser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    if ("deviceId".equals(parser.getName())) {
                        info.deviceId = parser.nextText();
                    }
                    if ("resultCode".equals(parser.getName())) {
                        info.resultCode = parser.nextText();
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            event = parser.next();
        }
        return info;
    }

    /**
     * 将一个字符串转化为输入流
     */
    private static InputStream getStringStream(String sInputString) {
        if (sInputString != null && !sInputString.trim().equals("")) {
            try {
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(
                        sInputString.getBytes());
                return tInputStringStream;
            } catch (Exception ex) {
                sLog.e("getStringStream ex : " + ex.toString());
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 上报
     * @param context
     * @param requestCode
     * @param deviceId
     */
    public static void report(Context context, String requestCode, String deviceId) {
        sLog.i("report " + "[ requestCode : " + requestCode + " ] [ deviceId : " + deviceId + " ]");
        StvHideApi.onReportLog(context, "tvAction", "action=cibnBroadcastControl&resultCode="
                + requestCode + "&deviceId=" + deviceId);
    }
}
