
package com.stv.commonservice.control.protocol.bean;

import java.io.Serializable;

public class CallBundle implements Serializable {
    public static final String KEY = "CallBundle";
    public int call_from;
    public String callId;
    public String callerPhone;
    public boolean isVideoCall = false;
    public String callerDevId;
    public boolean isPhoneCall = false;
    public String callPicture;
    public String callerName;
    // calloutType 1代表的是设备呼出２代表的是联系人呼出
    // public int calloutType;
    // public String calloutPhoneNumber;
    // public String calloutDevid;
    // public boolean hasCameraRemote;
    // 未拨通默认为手机
    // public CallDevType callDevType = CallDevType.PHONE;
    public boolean isControl;
    public boolean isController;
}
