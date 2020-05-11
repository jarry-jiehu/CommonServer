
package com.stv.commonservice.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FileUtils {
    private static LogUtils sLog = LogUtils.getInstance("Common", FileUtils.class.getSimpleName());

    public static boolean deleteFile(String aDic, String aFileName) {
        File mFile = new File(aDic + aFileName);
        if (mFile.exists()) {
            mFile.delete();
            return true;
        }
        return false;
    }

    public static boolean createFile(String aDic, String aFileName) {
        createDic(aDic);
        try {
            File mFile = new File(aDic + aFileName);
            if (!mFile.exists()) {
                mFile.createNewFile();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static boolean isExist(String aDic, String aFileName) {
        File mFile = new File(aDic + aFileName);
        if (mFile.exists()) {
            return true;
        }
        return false;
    }

    public static boolean createDic(String aDic) {
        File mFile = new File(aDic);
        if (!mFile.exists()) {
            mFile.mkdirs();
            return true;
        }
        return false;
    }

    public static void saveFile(String str, File file) {
        FileWriter writer = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
                handleLog();
            }
            writer = new FileWriter(file, true);
            writer.write(DateUtils.formatDate(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")
                    + "  " + str + "\n");
        } catch (IOException e) {
            Log.e(LogUtils.sTag, "", e);
        } finally {
            if (null != writer) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    Log.e(LogUtils.sTag, "", e);
                }
            }
        }
    }

    /**
     * 处理保存的日志
     */
    private static void handleLog() {
        String logDir = StorageUtils.getLogDir();
        if (null == logDir) {
            return;
        }
        File f = new File(logDir);
        if (f.exists() && f.isDirectory()) {
            File[] files = f.listFiles();
            for (File file : files) {
                String name = file.getName();
                if (null == name || 8 != name.length()) {
                    file.delete();
                    continue;
                }
                long time = DateUtils.parseDate(name, "yyyyMMdd");
                if (0 == time || System.currentTimeMillis() - time >= 7 * 24 * 3600 * 1000) {
                    file.delete();
                }
            }
        }
    }

    public static String readFileToString(String fileNamePath, String encoding) {
        InputStreamReader inputStreamReader = null;
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        try {
            fileInputStream = new FileInputStream(fileNamePath);
            inputStreamReader = new InputStreamReader(fileInputStream, encoding);
            bufferedReader = new BufferedReader(inputStreamReader);
            char[] buf = new char[1024];
            StringBuffer buffer = new StringBuffer();
            int size = 0;
            while ((size = bufferedReader.read(buf)) != -1) {
                buffer.append(buf, 0, size);
            }
            return buffer.toString();
        } catch (Exception e) {
            if (null != e) {
                sLog.i("readFileToString erro : " + e.toString());
            }
        } finally {
            closeFileInputStream(fileInputStream);
            closeInputStreamReader(inputStreamReader);
            closeBufferedReader(bufferedReader);
        }
        return null;
    }

    /**
     * @方法描述：如果父文件夹不存在，则级联新建文件夹
     * @param fileNamePath 文件夹名或者文件名，绝对路径
     */
    public static void makeFatherDir(String fileNamePath) {
        File file = new File(fileNamePath);
        // 判断是否存在,不存在则创建
        if (file != null) {
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
        }
    }

    // 全新写入，覆盖旧的文件
    public static boolean write(String fileNamePath, String content, String encoding) {
        FileOutputStream fileOutputStream = null;
        Writer writer = null;
        try {
            makeFatherDir(fileNamePath);
            File file = new File(fileNamePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            // 写数据
            fileOutputStream = new FileOutputStream(file);
            writer = new OutputStreamWriter(fileOutputStream, encoding);// 利用这个可以设置写入文件Stream的encoding
            writer.write(content);
            // Process p = Runtime.getRuntime().exec("chmod 777 " + fileNamePath);
            // log.e(p.waitFor() + "<===============");
            return true;
        } catch (Exception e) {
            sLog.e("save erro ==>" + e.getMessage());
            return false;
        } finally {
            closeWriter(writer);
            closeFileOutputStream(fileOutputStream);
        }
    }

    public static void closeFileOutputStream(FileOutputStream fos) {
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                sLog.e("closeFileOutputStream erro");
            }
        }
    }

    public static void closeFileInputStream(FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                sLog.e("closeFileInputStream erro");
            }
        }
    }

    public static void closeInputStreamReader(InputStreamReader isr) {
        if (isr != null) {
            try {
                isr.close();
            } catch (IOException e) {
                sLog.e("closeInputStreamReader erro");
            }
        }
    }

    public static void closeBufferedReader(BufferedReader br) {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {
                sLog.e("closeBufferedReader erro");
            }
        }
    }

    public static void closeWriter(Writer w) {
        if (w != null) {
            try {
                w.close();
            } catch (IOException e) {
                sLog.e("closeWriter erro");
            }
        }
    }
}
