package com.lcw.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * 文件名称：SystemSetting.java </p>
 * <p>
 * 文件描述：</p>
 * <p>
 * 版权所有： 版权所有(C)2017-2099 </p>
 * <p>
 * 内容摘要： </p>
 * <p>
 * 其他说明： </p>
 *
 * @version 1.0
 * @author lancw
 * @since 2017-6-26 15:37:04
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "com.lcw.model.SystemSetting")
@XmlRootElement(name = "Setting")
public class SystemSetting {

    private String zookeeperClientString;
    private String servicePomLocation;
    private String serviceFilter;
    private String timeout;
    private String redisAddress;
    private String redisPort;
    private String redisPassword;
    private String filterType;
    private String fileType;
    private Integer dubboVersion = 1;
    private Boolean dubboWrapText = Boolean.TRUE;
    private Boolean noImplBox = Boolean.TRUE;
    private Boolean httpWrapText = Boolean.TRUE;
    private Boolean formatWrapText = Boolean.TRUE;
    private Boolean toJavaWrapText = Boolean.TRUE;

    public Integer getDubboVersion() {
        return dubboVersion;
    }

    public void setDubboVersion(Integer dubboVersion) {
        this.dubboVersion = dubboVersion;
    }

    public String getRedisAddress() {
        return redisAddress;
    }

    public String getFileType() {
        return fileType;
    }

    public Boolean getNoImplBox() {
        return noImplBox;
    }

    public void setNoImplBox(Boolean noImplBox) {
        this.noImplBox = noImplBox;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setRedisAddress(String redisAddress) {
        this.redisAddress = redisAddress;
    }

    public String getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(String redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public Boolean getFormatWrapText() {
        return formatWrapText;
    }

    public void setFormatWrapText(Boolean formatWrapText) {
        this.formatWrapText = formatWrapText;
    }

    public Boolean getToJavaWrapText() {
        return toJavaWrapText;
    }

    public void setToJavaWrapText(Boolean toJavaWrapText) {
        this.toJavaWrapText = toJavaWrapText;
    }

    public Boolean getHttpWrapText() {
        return httpWrapText;
    }

    public void setHttpWrapText(Boolean httpWrapText) {
        this.httpWrapText = httpWrapText;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getServiceFilter() {
        return serviceFilter;
    }

    public void setServiceFilter(String serviceFilter) {
        this.serviceFilter = serviceFilter;
    }

    public String getZookeeperClientString() {
        return zookeeperClientString;
    }

    public void setZookeeperClientString(String zookeeperClientString) {
        this.zookeeperClientString = zookeeperClientString;
    }

    public String getServicePomLocation() {
        return servicePomLocation;
    }

    public void setServicePomLocation(String servicePomLocation) {
        this.servicePomLocation = servicePomLocation;
    }

    public Boolean getDubboWrapText() {
        return dubboWrapText;
    }

    public void setDubboWrapText(Boolean dubboWrapText) {
        this.dubboWrapText = dubboWrapText;
    }

}
