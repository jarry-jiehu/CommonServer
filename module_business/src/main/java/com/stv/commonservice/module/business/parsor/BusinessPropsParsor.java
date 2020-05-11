
package com.stv.commonservice.module.business.parsor;

import android.text.TextUtils;

import com.stv.commonservice.library.base.BaseHelper;
import com.stv.commonservice.module.business.callback.BusinessListener;
import com.stv.commonservice.module.business.helper.BusinessPropsHelper;
import com.stv.library.common.util.LogUtils;
import com.stv.library.common.util.SyspropProxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class BusinessPropsParsor {
    private static final String TAG = "BusinessPropsParsor";

    public static void readProps(String path, BusinessListener listener) {
        String propsPath = path + "/business.prop";
        File propsFile = new File(propsPath);
        if (!propsFile.exists() || !propsFile.isFile() || !propsFile.canRead()) {
            LogUtils.d(TAG, "writeSystemProps() business.prop is not exists or is not a file.");
            listener.onWriteProps(0, 0, true, null);
            return;
        }
        FileInputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = new FileInputStream(propsFile);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer buffer = new StringBuffer();
            String line = null;
            boolean hasEnter = false;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
                buffer.append("\n");
                hasEnter = true;
            }
            if (hasEnter) {
                buffer.deleteCharAt(buffer.lastIndexOf("\n"));
                String[] props = buffer.toString().split("\n");
                parseAndWirte(props, listener);
            } else {
                listener.onWriteProps(0, 0, true, null);
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "writeSystemProps() Exception: ", e);
            listener.onWriteProps(0, 0, false, null);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, "writeSystemProps() finally: ", e);
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, "writeSystemProps() finally: ", e);
            }
        }
    }

    private static void parseAndWirte(String[] props, BusinessListener listener) {
        if (null == props || 0 == props.length) {
            listener.onWriteProps(0, 0, true, null);
            return;
        }
        ArrayList<String> propList = new ArrayList<>();
        for (String prop : props) {
            if (TextUtils.isEmpty(prop)) {
                continue;
            }
            if (prop.startsWith("#")) {
                continue;
            }
            if (!prop.contains("=")) {
                continue;
            }
            String[] tmpArr = prop.split("=");
            if (null == tmpArr || 2 != tmpArr.length) {
                continue;
            }
            tmpArr[0] = tmpArr[0].replaceAll("\\s*", "");
            tmpArr[1] = tmpArr[1].trim();
            prop = tmpArr[0] + "=" + tmpArr[1];
            propList.add(prop);
        }

        if (0 == propList.size()) {
            listener.onWriteProps(0, 0, true, null);
            return;
        }

        int index = 0;
        for (String prop : propList) {
            index++;
            String[] tmpArr = prop.split("=");
            SyspropProxy.set(BaseHelper.getContext(), tmpArr[0], tmpArr[1]);
            if (tmpArr[1].equals(SyspropProxy.get(BaseHelper.getContext(), tmpArr[0]))) {
                LogUtils.d(TAG, "Success：" + prop);
                if (BusinessPropsHelper.contains(tmpArr[0])) {
                    BusinessPropsHelper.process(tmpArr[0]);
                }
                listener.onWriteProps(propList.size(), index, true, prop);
            } else {
                LogUtils.d(TAG, "Failure：" + prop);
                listener.onWriteProps(propList.size(), index, false, prop);
            }
        }
    }
}
