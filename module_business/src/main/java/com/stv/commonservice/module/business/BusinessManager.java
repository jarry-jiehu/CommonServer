
package com.stv.commonservice.module.business;

import android.content.Context;
import android.text.TextUtils;

import com.stv.commonservice.library.base.BaseHelper;

import com.stv.commonservice.module.business.processor.BusinessProcessor;
import com.stv.library.common.util.LogUtils;
import com.stv.library.common.util.SyspropProxy;
import com.stv.library.letv.tv.LetvShutdownUtils;

public class BusinessManager {
    private static BusinessManager instance;
    private final String TAG = "BusinessManager";
    private Context context;
    private BusinessProcessor processor;

    // http://wiki.letv.cn/pages/viewpage.action?pageId=77958811
    private final String KEY_BUSINESS_TYPE = "ro.build.business.type";

    public static BusinessManager getInstance() {
        if (null == instance) {
            synchronized (BusinessManager.class) {
                if (null == instance) {
                    instance = new BusinessManager();
                }
            }
        }
        return instance;
    }

    private BusinessManager() {
        context = BaseHelper.getContext();
    }

    public boolean isBusinessDevice() {
        String value = SyspropProxy.get(context, KEY_BUSINESS_TYPE);
        LogUtils.i(TAG, "type: " + value);
        if (!TextUtils.isEmpty(value) && value.startsWith("business_")) {
            return true;
        }
        return false;
    }

    public void start(String path) {
        start(path, false);
    }

    public void start(String path, boolean skipDialog) {
        start(path, skipDialog, false);
    }

    public void start(String path, boolean skipDialog, boolean skipActivity) {
        if (null == processor) {
            if (null == path) {
                processor = new BusinessProcessor();
            } else {
                processor = new BusinessProcessor(path);
            }
        } else if (processor.isRunning()) {
            LogUtils.d(TAG, "Already running...");
            return;
        }
        processor.setDiskPath(path);
        processor.setSkipDialog(skipDialog);
        processor.setSkipActivity(skipActivity);
        processor.processReceive();
    }

    public BusinessProcessor getProcessor() {
        if (null == processor) {
            processor = new BusinessProcessor();
        }
        return processor;
    }

    public void shutdown(boolean reboot) {
        getProcessor().dismiss();
        LetvShutdownUtils.shutdown(context, reboot);
    }
}
