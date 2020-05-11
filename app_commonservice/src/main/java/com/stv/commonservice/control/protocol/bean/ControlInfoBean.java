
package com.stv.commonservice.control.protocol.bean;

/**
 * 应用定制消息
 */
public class ControlInfoBean {
    public static final String ACTION_KEY_EVENT = "action_key_event";
    public static final String ACTION_TOUCH_EVENT = "action_touch_event";
    public static final String ACTION_BRUSH_EVENT = "action_brush_event";
    public static final String ACTION_BRUSH_SWITCH = "action_brush_switch";
    public static final String ACTION_PC_MODE = "action_pc_mode";
    public static final String ACTION_SWITCH_EVENT = "action_switch_event";
    /** 控制动作：按键事件 touch事件 画笔绘图 */
    private String type;
    /** touch事件透传MotionEvent 按键传键值 */
    private String eventStr;
    /** 控制的画布宽高 */
    private int width;
    private int height;
    /** 画笔模式开关 */
    private boolean isBrush;

    public ControlInfoBean(String type, String eventStr, int width, int height, boolean isBrush) {
        this.type = type;
        this.eventStr = eventStr;
        this.width = width;
        this.height = height;
        this.isBrush = isBrush;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEventStr() {
        return eventStr;
    }

    public void setEventStr(String eventStr) {
        this.eventStr = eventStr;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isBrush() {
        return isBrush;
    }

    public void setBrush(boolean brush) {
        isBrush = brush;
    }

    @Override
    public String toString() {
        return "ControlInfoBean{" +
                "type='" + type + '\'' +
                ", eventStr='" + eventStr + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", isBrush=" + isBrush +
                '}';
    }
}
