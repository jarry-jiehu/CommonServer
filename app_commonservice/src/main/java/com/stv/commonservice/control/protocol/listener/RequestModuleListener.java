
package com.stv.commonservice.control.protocol.listener;

public interface RequestModuleListener {
    void onRequestControl(RequestResultListener listener, boolean isCancel);
}
