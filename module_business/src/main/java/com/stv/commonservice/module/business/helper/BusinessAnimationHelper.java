
package com.stv.commonservice.module.business.helper;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.stv.commonservice.library.base.BaseHelper;
import com.stv.commonservice.module.business.utils.ZipUtils;
import com.stv.library.common.util.BitmapUtils;
import com.stv.library.common.util.FileUtils;
import com.stv.library.common.util.LogUtils;

public class BusinessAnimationHelper {
    private static final String TAG = "BusinessAnimationHelper";
    private static final String FILE_NAME = "bootanimation.png";

    public static boolean isOwnFile(String path) {
        if (TextUtils.isEmpty(path) || !path.endsWith(BusinessAnimationHelper.FILE_NAME)) {
            return false;
        }
        return true;
    }

    public static boolean customAnimation(final String path) {
        LogUtils.i(TAG, "customAnimation() path: " + path);
        Bitmap bitmap = BitmapUtils.readBitMap(BaseHelper.getContext(), path);
        if (null == bitmap) {
            return false;
        }
        String workPath = BaseHelper.getContext().getFilesDir().getAbsolutePath() + "/business/animation";
        FileUtils.checkDirExists(workPath);
        FileUtils.write(workPath + "/desc.txt", "1920 1080 1\r\np 0 0 part0\r\n", false);
        FileUtils.checkDirExists(workPath + "/part0");
        // if ( BitmapUtils.saveBitmapToPNG(bitmap, workPath + "/part0/boot_motion_00001.png")) {
        if (FileUtils.copyFile(path, workPath + "/part0/boot_motion_00001.png")) {
            try {
                String[] srcs = {
                        workPath + "/desc.txt", workPath + "/part0"
                };
                String outDir = "/cache/bootanimation.zip";
                ZipUtils.toZip(srcs, outDir, true);
                FileUtils.chmod(outDir, "644");
                FileUtils.deleteDirOrFile(workPath);
                LogUtils.i(TAG, "customAnimation() success.");
                return true;
            } catch (Exception e) {
                LogUtils.e(TAG, "customAnimation() Exception.", e);
                return false;
            }
        } else {
            LogUtils.i(TAG, "customAnimation() failure.");
            return false;
        }
    }
}
