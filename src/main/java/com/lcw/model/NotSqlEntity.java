package com.lcw.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author lancw
 */
public class NotSqlEntity implements Serializable {

    private String key;
    private String value;
    private String type;
    private List<String> list;
    private Set<String> set;
    private List<Map<String, Object>> listMap;
    private String[] valuek;
    private String[] valuev;
    private String exTime;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public Set<String> getSet() {
        return set;
    }

    public void setSet(Set<String> set) {
        this.set = set;
    }

    public List<Map<String, Object>> getListMap() {
        return listMap;
    }

    public void setListMap(List<Map<String, Object>> listMap) {
        this.listMap = listMap;
    }

    public String[] getValuek() {
        return valuek;
    }

    public void setValuek(String[] valuek) {
        this.valuek = valuek;
    }

    public String[] getValuev() {
        return valuev;
    }

    public void setValuev(String[] valuev) {
        this.valuev = valuev;
    }

    public String getExTime() {
        return exTime;
    }

    public void setExTime(String exTime) {
        this.exTime = exTime;
    }
}
