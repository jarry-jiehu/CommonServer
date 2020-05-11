
package com.stv.commonservice.library.base;

import android.content.Context;
import android.os.Handler;

import com.stv.library.common.util.LogUtils;

public class BaseHelper {
    private static volatile Context context;
    private static Handler handler;

    public static Context getContext() {
        return context;
    }

    public static Handler getHandler() {
        return handler;
    }

    public static void init(Context con, Handler han) {
        context = con;
        handler = han;
        LogUtils.setRootTag("CommonServiceNew");
    }
}
