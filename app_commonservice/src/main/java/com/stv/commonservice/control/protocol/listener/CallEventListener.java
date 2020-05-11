
package com.stv.commonservice.control.protocol.listener;

import com.stv.videochatsdk.api.event.SignalTimeoutEvent;

public interface CallEventListener {

    // void onWebRTCConnectionEvent(WebRTCConnectionEvent webRTCConnection);

    // void onEochEvent(CallConnectEvent cce);

    // void onResponseEvent(CallResponseEvent cre);

    void onSignalTimeoutEvent(SignalTimeoutEvent event);

}
