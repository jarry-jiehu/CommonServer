
package com.stv.commonservice.control.protocol.listener;


import com.stv.videochatsdk.api.event.CallSignalStatus;

/**
 * 来电接听的Listener
 */
public interface CallRequestEventListener {

    void onRequestEvent(CallSignalStatus cre);

}
