package com.lcw.eum;

/**
 *
 * @author lancw
 */
public enum FilterType {
    SERVICE_NAME("SERVICE_NAME", "服务名"), APP_NAME("APP_NAME", "应用名"), SERVER_IP("SERVER_IP", "机器IP");
    private final String code;
    private final String name;

    private FilterType(String code, String name) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }
}
