package com.stv.commonservice.module.business.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by zhaoyiming on 18-10-8.
 */
public class ZipUtils {
    private static final int BUFFER_SIZE = 2 * 1024;

    /**
     * @param srcDir 压缩文件夹路径
     * @param KeepDirStructure 是否保留原来的目录结构, true:保留目录结构; false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */
    public static void toZip(String[] srcDir, String outDir,
            boolean KeepDirStructure) throws RuntimeException, Exception {

        OutputStream out = new FileOutputStream(new File(outDir));

        long start = System.currentTimeMillis();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            List<File> sourceFileList = new ArrayList<File>();
            for (String dir : srcDir) {
                File sourceFile = new File(dir);
                sourceFileList.add(sourceFile);
            }
            compress(sourceFileList, zos, KeepDirStructure);
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 递归压缩方法
     *
     * @param sourceFile 源文件
     * @param zos zip输出流
     * @param name 压缩后的名称
     * @param KeepDirStructure 是否保留原来的目录结构, true:保留目录结构; false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos,
            String name, boolean KeepDirStructure) throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            ZipEntry entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(sourceFile.length());
            entry.setCompressedSize(sourceFile.length());
            long crc = 0;
            crc = calFileCRC32(sourceFile);
            entry.setCrc(crc);
            zos.putNextEntry(entry);
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            // Complete the entry
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            for (File file : listFiles) {
                if (KeepDirStructure) {
                    compress(file, zos, name + "/" + file.getName(),
                            KeepDirStructure);
                } else {
                    compress(file, zos, file.getName(), KeepDirStructure);
                }

            }
        }
    }

    private static void compress(List<File> sourceFileList,
            ZipOutputStream zos, boolean KeepDirStructure) throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        for (File sourceFile : sourceFileList) {
            String name = sourceFile.getName();
            if (sourceFile.isFile()) {
                ZipEntry entry = new ZipEntry(name);
                entry.setMethod(ZipEntry.STORED);
                entry.setSize(sourceFile.length());
                entry.setCompressedSize(sourceFile.length());
                long crc = 0;
                crc = calFileCRC32(sourceFile);
                entry.setCrc(crc);
                zos.putNextEntry(entry);
                int len;
                FileInputStream in = new FileInputStream(sourceFile);
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                zos.closeEntry();
                in.close();
            } else {
                File[] listFiles = sourceFile.listFiles();
                for (File file : listFiles) {
                    if (KeepDirStructure) {
                        compress(file, zos, name + "/" + file.getName(),
                                KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(),
                                KeepDirStructure);
                    }

                }
            }
        }
    }

    public static long calFileCRC32(File file) throws IOException {
        FileInputStream fi = new FileInputStream(file);
        CheckedInputStream checksum = new CheckedInputStream(fi, new CRC32());
        while (checksum.read() != -1) {
        }
        long temp = checksum.getChecksum().getValue();
        fi.close();
        checksum.close();
        return temp;
    }

}