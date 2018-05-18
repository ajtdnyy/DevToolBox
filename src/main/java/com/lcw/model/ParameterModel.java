package com.lcw.model;

import com.lcw.util.ReflectUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 文件名称：ParameterModel.java </p>
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
 * @since 2017-6-27 10:16:46
 */
public class ParameterModel {

    private String name;
    private String desc;
    private Method method;
    private int index;

    public ParameterModel(String name, String instance, Method method, int index) {
        this.name = name;
        this.desc = instance;
        this.method = method;
        this.index = index;
    }

    public String toParameterString() throws Exception {
        StringBuilder sb = new StringBuilder();
        if (desc.startsWith("L")) {
            Class c = null;
            String cln = desc.substring(1).replaceAll("/", ".").replace(";", "");
            if (desc.contains("java.")) {
                c = getClass().getClassLoader().loadClass(name);
            } else {
                c = getClass().getClassLoader().loadClass(cln);
            }
            String msg = "解析参数异常";
            if (c.isInterface()) {
                if (c.equals(List.class) || c.equals(Set.class)) {
                    ParameterizedType pt = getParameterizedType();
                    if (pt == null) {
                        return msg;
                    }
                    String t = pt.getActualTypeArguments()[0].getTypeName();
                    if (t.contains("java.lang")) {
                        sb.append(name).append("[]");
                    } else {
                        Class cc = getClass().getClassLoader().loadClass(t);
                        sb.append("[").append(ReflectUtil.classToJson(cc, 1)).append("]");
                    }
                } else if (c.equals(Map.class)) {
                    ParameterizedType pt = getParameterizedType();
                    if (pt == null) {
                        return msg;
                    }
                    String t = pt.getActualTypeArguments()[1].getTypeName();
                    if (t.contains("java.lang")) {
                        sb.append("{}");
                    } else {
                        Class cc = getClass().getClassLoader().loadClass(t);
                        sb.append("{").append(ReflectUtil.classToJson(cc, 1)).append("}");
                    }
                }
            } else if (c.isArray()) {
                sb.append(name).append("[]");
            } else if (c.isPrimitive() || c.isEnum() || ReflectUtil.isNotAnalysisClass(c.getName())) {
                sb.append(name);
            } else {
                Field[] fs = c.getDeclaredFields();
                sb.append("{");
                for (int i = 0; i < fs.length; i++) {
                    Field f = fs[i];
                    Class t = f.getType();
                    if (t.isPrimitive() || t.isEnum() || ReflectUtil.isNotAnalysisClass(t.getTypeName())) {
                        sb.append(f.getName()).append(":''");
                    } else if (t.isArray()) {
                        sb.append(f.getName()).append(":[]");
                    } else if (t.isInterface()) {
                        if (t.isAssignableFrom(List.class) || t.isAssignableFrom(Set.class)) {
                            Class gc = ReflectUtil.getParameterizedTypes(f.getGenericType(), 0);
                            sb.append(f.getName()).append(":[").append(ReflectUtil.classToJson(gc, 1)).append("]");
                        } else if (t.isAssignableFrom(Map.class)) {
                            Class gc = ReflectUtil.getParameterizedTypes(f.getGenericType(), 1);
                            sb.append(f.getName()).append(":{key:").append(ReflectUtil.classToJson(gc, 1)).append("}");
                        } else {
                            sb.append(f.getName()).append(":").append(ReflectUtil.classToJson(t, 1));
                        }
                    } else {
                        sb.append(f.getName()).append(":").append(ReflectUtil.classToJson(t, 1));
                    }
                    if (i != fs.length - 1) {
                        sb.append(",");
                    }
                }
                sb.append("}");
            }
        } else {
            sb.append(name);
        }
        return sb.toString();
    }

    public ParameterizedType getParameterizedType() {
        for (Parameter p : method.getParameters()) {
            if (p.getName().equals(name) || String.format("arg%d", index).equals(p.getName())) {
                if (p.getParameterizedType() instanceof ParameterizedType) {
                    return (ParameterizedType) p.getParameterizedType();
                }
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
