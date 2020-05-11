
package com.stv.commonservice.control.protocol.listener;

import com.stv.videochatsdk.api.event.CallInternalErrorEvent;

/**
 * 内部错误
 */
public interface CallInternalErrorListen {
    void onCallInternalErrorEvent(CallInternalErrorEvent event);
}
