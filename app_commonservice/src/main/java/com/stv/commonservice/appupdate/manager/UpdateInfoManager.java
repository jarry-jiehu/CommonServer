
package com.stv.commonservice.appupdate.manager;

import android.text.TextUtils;

import com.stv.commonservice.appupdate.model.UpdateInfo;

import java.util.Iterator;
import java.util.List;

public class UpdateInfoManager {
    private static List<UpdateInfo> sUpdateInfos;

    public static void setUpdateInfos(List<UpdateInfo> infos) {
        if (null != sUpdateInfos && sUpdateInfos.size() > 0) {
            sUpdateInfos.clear();
        }
        sUpdateInfos = infos;
    }

    public static List<UpdateInfo> getUpdateInfos() {
        return sUpdateInfos;
    }

    /**
     * 通过包名查询是否存在
     * @param pkg
     * @return
     */
    public static boolean isExistInfo(String pkg) {
        if (null == sUpdateInfos || TextUtils.isEmpty(pkg)) {
            return false;
        }
        for (UpdateInfo info : sUpdateInfos) {
            if (null != info && pkg.equals(info.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过包名删除更新信息
     * @param pkg
     */
    public static void remove(String pkg) {
        if (null == sUpdateInfos || sUpdateInfos.size() <= 0 || TextUtils.isEmpty(pkg)) {
            return;
        }
        Iterator<UpdateInfo> iterator = sUpdateInfos.iterator();
        while (iterator.hasNext()) {
            UpdateInfo uInfo = iterator.next();
            if (uInfo.getPackageName().equals(pkg)) {
                iterator.remove();
                break;
            }
        }
    }

    public static UpdateInfo getUpdateInfo(String pkg) {
        if (null == sUpdateInfos || sUpdateInfos.size() <= 0 || TextUtils.isEmpty(pkg)) {
            return null;
        } else {
            for (UpdateInfo info : sUpdateInfos) {
                if (null != info && pkg.equals(info.getPackageName())) {
                    return info;
                }
            }
            return null;
        }
    }
}
