package com.lcw.model;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dubbo://192.168.145.131:20880/com.alibaba.dubbo.demo.DemoService
 * ?anyhost=true&application=demo-provider&dubbo=2.5.3&
 * interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin
 * &methods=sayHello&pid=4268&revision=2.5.3&side=provider&timestamp=1497993263112
 *
 * @author lancw
 */
public class DubboServiceModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboServiceModel.class.getName());
    private static final String DUBBO = "dubbo://";
    private static final String APPLICATION = "application=";
    private static final String INTERFACE = "interface=";
    private static final String METHODS = "methods=";
    private static final String TIMEOUT = "timeout=";
    private Class cls;
    private String url;
    private String clientStr = "";
    private String application = "";
    private String interfaceName;
    private String interfaceFullName;
    private final HashMap<String, String[]> methods = new HashMap<>();
    private final HashMap<String, String> urlMap = new HashMap<>();
    private final HashMap<String, List<MethodModel>> fullMethods = new HashMap<>();

    public DubboServiceModel(String url) {
        init(url, "1000");
    }

    public void init(String url, String timeout) {
        try {
            url = URLDecoder.decode(url, "utf8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
        try {
            Integer.valueOf(timeout);
        } catch (Exception e) {
            timeout = "1000";
        }
        if (url.startsWith(DUBBO)) {
            this.url = url;
            clientStr = url.replace(DUBBO, "");
            clientStr = clientStr.substring(0, clientStr.indexOf("/"));
            urlMap.put(clientStr, url);
            String[] params = url.substring(url.indexOf("?") + 1).split("&");
            for (String param : params) {
                if (param.startsWith(APPLICATION)) {
                    application = param.replace(APPLICATION, "");
                } else if (param.startsWith(INTERFACE)) {
                    String tmp = param.replace(INTERFACE, "");
                    interfaceFullName = tmp;
                    interfaceName = tmp.substring(tmp.lastIndexOf(".") + 1);
                } else if (param.startsWith(METHODS)) {
                    methods.put(clientStr, param.replace(METHODS, "").split(","));
                } else if (param.startsWith(TIMEOUT)) {
                    this.url = url.replace(param, TIMEOUT + timeout);
                }
            }
            if (!this.url.contains(TIMEOUT)) {
                this.url += "&" + TIMEOUT + timeout;
            }
        } else {
            interfaceFullName = url;
            interfaceName = url.substring(url.lastIndexOf(".") + 1);
        }
    }

    @Override
    public String toString() {
        return interfaceName;
    }

    public HashMap<String, String> getUrlMap() {
        return urlMap;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.interfaceFullName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DubboServiceModel other = (DubboServiceModel) obj;
        return Objects.equals(this.interfaceFullName, other.interfaceFullName);
    }

    public Class getCls() {
        return cls;
    }

    public void setCls(Class cls) {
        this.cls = cls;
    }

    public String getClientStr() {
        return clientStr;
    }

    public String getUrl() {
        return url;
    }

    public String getUrl(String timeout, String ipAddress) {
        String tmpURL = urlMap.get(ipAddress);
        if (StringUtils.isBlank(timeout) || !StringUtils.isNumeric(timeout)) {
            return tmpURL;
        }
        String[] params = tmpURL.substring(tmpURL.indexOf("?") + 1).split("&");
        for (String param : params) {
            if (param.startsWith(TIMEOUT)) {
                tmpURL = tmpURL.replace(param, TIMEOUT + timeout);
            }
        }
        if (!tmpURL.contains(TIMEOUT)) {
            tmpURL += "&" + TIMEOUT + timeout;
        }
        return tmpURL;
    }

    public String getApplication() {
        return application;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getInterfaceFullName() {
        return interfaceFullName;
    }

    public String[] getMethods(String ipAddress) {
        return methods.get(ipAddress);
    }

    public List<MethodModel> getFullMethods(String ipAddress) {
        return fullMethods.get(ipAddress);
    }

    public void putFullMethods(String ipAddress, List<MethodModel> value) {
        fullMethods.put(ipAddress, value);
    }

}
