
package com.stv.commonservice.common;

import android.content.Context;
import android.util.Log;

import com.stv.commonservice.util.DateUtils;
import com.stv.commonservice.util.LogUtils;
import com.stv.commonservice.util.StorageUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

public class ExceptionHandler implements UncaughtExceptionHandler {
    private LogUtils mLog = LogUtils.getInstance("Common", ExceptionHandler.class.getSimpleName());
    private Context mContext;

    public ExceptionHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        handleException(e);
    }

    /**
     * 处理异常
     * @param e
     */
    private void handleException(Throwable ex) {
        if (ex == null) {
            return;
        }

        // save to file or not
        if (!LogUtils.isSaveable() && mLog != null) {
            mLog.e(ex);
            return;
        }
        Log.e(LogUtils.sTag, "", ex);
        // write to file
        String path = StorageUtils.getCrashDir();
        if (null != path) {
            File file = new File(path
                    + DateUtils.formatDate(System.currentTimeMillis(), "yyyyMMdd"));
            File newExceptionFile = new File(path + "newException");
            BufferedWriter bw = null;
            FileWriter writer = null;

            BufferedWriter newExceptionbw = null;
            FileWriter newExceptionWriter = null;

            BufferedReader br = null;
            FileReader reader = null;

            String buff = "";
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                String string = sw.toString();

                if (!newExceptionFile.exists()) {
                    boolean newFile = newExceptionFile.createNewFile();
                    Log.i(LogUtils.sTag, "create exception file is" + newFile);
                }

                reader = new FileReader(newExceptionFile);
                br = new BufferedReader(reader);
                StringBuffer stringBuffer = new StringBuffer();

                while ((buff = br.readLine()) != null) {
                    stringBuffer.append(buff + "\n");
                }

                if (!stringBuffer.toString().equals(string)) {
                    newExceptionWriter = new FileWriter(newExceptionFile, false);
                    newExceptionbw = new BufferedWriter(newExceptionWriter);
                    newExceptionbw.write(string);

                    writer = new FileWriter(file, true);
                    bw = new BufferedWriter(writer);
                    StringBuffer sb = new StringBuffer();

                    sb.append(DateUtils.formatDate(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
                    bw.write(sb.toString() + "\n");
                    bw.write(string);
                }
            } catch (IOException e) {
                Log.e(LogUtils.sTag, "", e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LogUtils.sTag, "", e);
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        Log.e(LogUtils.sTag, "", e);
                    }
                }
                if (newExceptionWriter != null) {
                    try {
                        newExceptionWriter.close();
                    } catch (IOException e) {
                        Log.e(LogUtils.sTag, "", e);
                    }
                }
                if (null != bw) {
                    try {
                        bw.flush();
                        bw.close();
                    } catch (IOException e) {
                        Log.e(LogUtils.sTag, "", e);
                    }
                }
                if (null != newExceptionbw) {
                    try {
                        newExceptionbw.flush();
                        newExceptionbw.close();
                    } catch (IOException e) {
                        Log.e(LogUtils.sTag, "", e);
                    }
                }
                if (null != br) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        Log.e(LogUtils.sTag, "", e);
                    }
                }
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
