
package com.stv.commonservice.common.provider;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.stv.commonservice.common.AppApplication;
import com.stv.commonservice.domain.DataStorageManager;
import com.stv.commonservice.domain.util.DomainUtil;
import com.stv.commonservice.util.Constants;
import com.stv.commonservice.util.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 该类目前仅为测试工具使用，测试工具需要通过该类读取目录下保存的文件内容
 */
public class CustomProvider extends ContentProvider {
    private final String DOMAIN_SAVE_PATH = DomainUtil.getDomainSavePath();
    private LogUtils mLog = LogUtils.getInstance("Common", CustomProvider.class.getSimpleName());
    public static final String AUTHORITY = "com.stv.activation.provider.customprovider";
    public static final String TABLE_NAME_DOMAIN = "domain";
    public static final String TABLE_NAME_ATTEST = "attestation";
    private static final int DOMAIN_LIST = 1;
    private static final int ATTESTAT_LIST = 2;
    private static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME_DOMAIN, DOMAIN_LIST);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME_ATTEST, ATTESTAT_LIST);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor cursor = null;
        int code = uriMatcher.match(uri);
        mLog.i("query code : " + code);
        switch (code) {
            case DOMAIN_LIST:
                String domain = DomainUtil.getDomain(AppApplication.getInstance(), selectionArgs[0]);
                String[] columns = new String[]{
                        "_id", "_value"
                };
                cursor = new MatrixCursor(columns);
                String[] strs = new String[]{
                        "0", domain
                };
                ((MatrixCursor) cursor).addRow(strs);
                break;
            case ATTESTAT_LIST:
                String requestCode = DataStorageManager.getIntance(AppApplication.getInstance())
                        .getAttestRequestCode();
                String[] columns_attest = new String[]{
                        "_id", "_value"
                };
                cursor = new MatrixCursor(columns_attest);
                String[] strs_attest = new String[]{
                        "0", requestCode
                };
                ((MatrixCursor) cursor).addRow(strs_attest);
                break;
            default:
                break;
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @SuppressLint("NewApi")
    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        int code = uriMatcher.match(uri);
        switch (code) {
            case DOMAIN_LIST:
                File file = new File(DOMAIN_SAVE_PATH);
                mLog.i("DomainProvider domainSavePath : " + DOMAIN_SAVE_PATH);
                if (file.exists()) {
                    return ParcelFileDescriptor.open(file,
                            ParcelFileDescriptor.MODE_READ_ONLY);
                }
                break;
            case ATTESTAT_LIST:
                File file_attest = new File(Constants.REQUEST_CODE_SAVE_PATH);
                mLog.i("AttestContent requestCode : " + Constants.REQUEST_CODE_SAVE_PATH);
                if (file_attest.exists()) {
                    return ParcelFileDescriptor.open(file_attest,
                            ParcelFileDescriptor.MODE_READ_ONLY);
                } else {
                    mLog.i("Attest file do not exists");
                }
            default:
                break;
        }
        throw new FileNotFoundException(uri.getPath());
    }
}
