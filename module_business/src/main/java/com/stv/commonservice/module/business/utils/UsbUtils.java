
package com.stv.commonservice.module.business.utils;

import android.app.Activity;
import android.content.Context;
import android.os.storage.StorageManager;

import com.stv.library.common.util.LogUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import eui.tv.TvManager;

public class UsbUtils {
    private static final String TAG = "UsbUtils";
    private static final String MOUNTS_FILE = "/proc/mounts";
    private static final String DEV_VOLD_PATH = "/dev/block/vold/";
    private static final String DEV_U4_NTFS_PATH = "/dev/block/sd";

    public static String[] getPaths() {
        LogUtils.d(TAG, "getPaths()");
        String[] paths = null;

        boolean isU4 = false;
        String model = TvManager.getModel();
        LogUtils.d(TAG, "model: " + model);
        if ("u4".equalsIgnoreCase(model)) {
            isU4 = true;
        }

        File ptfile = new File(MOUNTS_FILE);
        FileReader fileReader = null;
        BufferedReader bufferReader = null;
        try {
            ArrayList<String> list = new ArrayList<>();
            fileReader = new FileReader(ptfile);
            bufferReader = new BufferedReader(fileReader);
            String buf = bufferReader.readLine();
            while (buf != null) {
                String stringarray[] = buf.split(" ");
                if (stringarray.length < 3)
                    continue;
                String tmp = stringarray[0];
                if (tmp != null && (tmp.contains(DEV_VOLD_PATH)
                        || (isU4 && tmp.contains(DEV_U4_NTFS_PATH)))) {
                    File path = new File(stringarray[1]);
                    if (path != null && path.isDirectory()) {
                        LogUtils.d(TAG, "search U Disk. path: " + path.getAbsolutePath());
                        list.add(path.getAbsolutePath());
                    } else {
                        LogUtils.d(TAG, "Error!!!");
                    }
                }
                buf = bufferReader.readLine();
            }
            if (list.size() > 0) {
                paths = new String[list.size()];
                for (int i = 0; i < paths.length; i++) {
                    paths[i] = list.get(i);
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e.getMessage());
        } finally {
            if (null != bufferReader) {
                try {
                    bufferReader.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, e.getMessage());
                }
                bufferReader = null;
            }
            if (null != fileReader) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    LogUtils.e(TAG, e.getMessage());
                }
                fileReader = null;
            }
        }

        return paths;
    }

    /**
     * 判断是否有U盘插入,当U盘开机之前插入使用该方法.
     *
     * @param path
     * @return
     */
    public static boolean isMounted(String path) {
        boolean blnRet = false;
        String strLine = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(MOUNTS_FILE));
            while ((strLine = reader.readLine()) != null) {
                if (strLine.contains(path)) {
                    blnRet = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reader = null;
            }
        }
        return blnRet;
    }

    /**
     * 获取U盘的路径和名称
     */
    public static void getUName(Context context) {
        Class<?> volumeInfoClazz = null;
        Method getDescriptionComparator = null;
        Method getBestVolumeDescription = null;
        Method getVolumes = null;
        Method isMountedReadable = null;
        Method getType = null;
        Method getPath = null;
        List<?> volumes = null;
        try {
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            getDescriptionComparator = volumeInfoClazz.getMethod("getDescriptionComparator");
            getBestVolumeDescription = StorageManager.class.getMethod("getBestVolumeDescription", volumeInfoClazz);
            getVolumes = StorageManager.class.getMethod("getVolumes");
            isMountedReadable = volumeInfoClazz.getMethod("isMountedReadable");
            getType = volumeInfoClazz.getMethod("getType");
            getPath = volumeInfoClazz.getMethod("getPath");
            StorageManager storageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
            volumes = (List<?>) getVolumes.invoke(storageManager);
            for (Object vol : volumes) {
                if (vol != null && (boolean) isMountedReadable.invoke(vol) && (int) getType.invoke(vol) == 0) {
                    File path2 = (File) getPath.invoke(vol);
                    String p1 = (String) getBestVolumeDescription.invoke(storageManager, vol);
                    String p2 = path2.getPath();
                    LogUtils.d(TAG, "U Disk Name: " + p1); // 打印U盘卷标名称
                    LogUtils.d(TAG, "U Disk Path: " + p2); // 打印U盘路径
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
