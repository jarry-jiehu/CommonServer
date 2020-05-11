
package com.stv.commonservice.util;

import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class NetUtils {
    private static LogUtils mLog = LogUtils.getInstance("AppUpdate", NetUtils.class.getSimpleName());

    /**
     * 生成url
     * @param host
     * @param param
     * @return
     */
    public static String getUrl(String host, Map<String, String> param) {
        try {
            StringBuilder stringBuilder = new StringBuilder("");
            if (param != null) {
                Iterator<String> iterator = param.keySet().iterator();
                if (iterator.hasNext()) {
                    if (host.contains("?") == false) {
                        stringBuilder.append("?");
                    }
                }
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    Object value = param.get(key);
                    stringBuilder.append(key);
                    stringBuilder.append("=");
                    stringBuilder.append(URLEncoder.encode(value.toString(),
                            "utf-8"));
                    if (iterator.hasNext()) {
                        stringBuilder.append("&");
                    }
                }
            }
            return host + stringBuilder.toString();
        } catch (Exception e) {
            mLog.e("REquestTask get Url e : " + e != null ? e.toString() : "e == null");
        }
        return null;
    }
}
