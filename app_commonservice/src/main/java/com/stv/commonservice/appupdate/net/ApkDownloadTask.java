
package com.stv.commonservice.appupdate.net;

import android.content.Context;

import com.stv.commonservice.appupdate.manager.UpdateInfoManager;
import com.stv.commonservice.appupdate.manager.UpdateManager;
import com.stv.commonservice.appupdate.model.UpdateInfo;
import com.stv.commonservice.appupdate.net.UpdateReporter.OperatingResults;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StorageUtils;
import com.stv.commonservice.util.StvHideApi;

import eui.lighthttp.FileDownload.Callback;
import eui.lighthttp.Helper;

/**
 * 下载APK
 */
public class ApkDownloadTask implements Runnable {
    private LogUtils mLog = LogUtils.getInstance(LogUtils.MODULE_APPUPDATE, ApkDownloadTask.class.getSimpleName());
    private UpdateInfo mInfo;
    private UpdateManager mUpdateManager;
    private Helper mHelper;
    private Context mContext;
    private String REPORT_STATUS_SUCCESS = "1";
    private String REPORT_STATUS_ERROR = "0";

    public ApkDownloadTask(Context context, UpdateInfo info, UpdateManager updateManager) {
        mContext = context;
        mInfo = info;
        mUpdateManager = updateManager;
        mHelper = new Helper();
    }

    @Override
    public void run() {

        if (null == mInfo) {
            mLog.i("download file info is null!");
            return;
        }
        String url = mInfo.getUrl();
        final String filePath = StorageUtils.getApkDir() + mInfo.getPackageName() + Constants.Y_APK_SUFFIX;

        final String reportPackageName = mInfo.getPackageName();
        final String reportVersionCode = mInfo.getApkVersion();

        try {
            mHelper.download(url, filePath, new Callback() {

                @Override
                public void onSuccess() {
                    String name = mInfo.getPackageName();
                    mLog.i("Download " + name + " apk success ." + " Thread:" + Thread.currentThread().getId());
                    mInfo.setPath(filePath);
                    mUpdateManager.install(name);
                    String needReportStr = "action=silentUpdate&packageName=" + reportPackageName
                            + "&versionCode=" + reportVersionCode
                            + "&status=" + REPORT_STATUS_SUCCESS
                            + "&otherdata=" +mInfo.getOtherdata();
                    StvHideApi.onReportLog(mContext, "tvAction", needReportStr);
                    UpdateReporter.getInstance(mContext).reportDownlowd(mInfo.getOtherdata(), OperatingResults.SUCCESS);
                }

                @Override
                public void onProgressUpdate(long arg0, long arg1) {

                }

                @Override
                public void onFailure(Exception exception) {
                    String packName = mInfo.getPackageName();
                    mLog.i("Download apk failure . packName： " + packName + " ,exception :" + exception + " ,Thread:" + Thread.currentThread().getId());
                    if (UpdateInfoManager.isExistInfo(packName)) {
                        UpdateInfoManager.remove(packName);
                    }
                    String needReportStr = "action=silentUpdate&packageName=" + reportPackageName
                            + "&versionCode=" + reportVersionCode
                            + "&status=" + REPORT_STATUS_ERROR
                            + "&otherdata=" +mInfo.getOtherdata();
                    StvHideApi.onReportLog(mContext, "tvAction", needReportStr);
                }
            });
        } catch (Exception e) {
            mLog.i(" DownloadApk error :" + e.getMessage());
        }
    }
}
