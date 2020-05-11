
package com.stv.commonservice.module.business.helper;

import android.provider.Settings;

import com.stv.commonservice.library.base.BaseHelper;
import com.stv.library.common.util.LogUtils;
import com.stv.library.common.util.SyspropProxy;

public class BusinessPropsHelper {
    private static final String TAG = "BusinessPropsHelper";

    private static final String PROP_KEY_SCREENSAVER = "persist.lbc.screensavertime";

    public static final String[] props = {
            PROP_KEY_SCREENSAVER
    };

    /**
     * 屏保开关：默认关闭-http://jira.letv.cn/browse/UIPE-3635?filter=-3
     */
    private static long[] times = {
            -2, 5 * 60 * 1000, 10 * 60 * 1000, 15 * 60 * 1000, 30 * 60 * 1000
    };

    public static boolean contains(final String prop) {
        for (String tmp : props) {
            if (tmp.equals(prop)) {
                return true;
            }
        }
        return false;
    }

    public static void process(final String prop) {
        if (PROP_KEY_SCREENSAVER.equals(prop)) {
            int value = SyspropProxy.getInt(BaseHelper.getContext(), PROP_KEY_SCREENSAVER, -1);
            if (0 > value || 4 < value) {
                LogUtils.e(TAG, "value is error.");
                return;
            }
            Settings.System.putLong(BaseHelper.getContext().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, times[value]);
            LogUtils.i(TAG, "Write settings of screensaver: " + value);
            try {
                long writed = Settings.System.getLong(BaseHelper.getContext().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
                LogUtils.d(TAG, "Writed value: " + writed);
            } catch (Settings.SettingNotFoundException e) {
                LogUtils.e(TAG, "Writed value read error", e);
            }
        }
    }
}
