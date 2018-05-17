package com.lcw.model;

import java.lang.reflect.Method;
import java.util.List;

/**
 *
 * @author lancw
 */
public class MethodModel {

    private String name;
    private Method method;
    private Method interfacMethod;
    private List<ParameterModel> parameters;

    public MethodModel(String name, Method method, Method interfacMethod, List<ParameterModel> parameters) {
        this.name = name;
        this.method = method;
        this.interfacMethod = interfacMethod;
        this.parameters = parameters;
    }

    public String toJsonString() throws Exception {
        if (parameters != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < parameters.size(); i++) {
                ParameterModel parameter = parameters.get(i);
                sb.append(parameter.toParameterString());
                if (i != parameters.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            return sb.toString();
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        if (parameters != null) {
            for (int i = 0; i < parameters.size(); i++) {
                ParameterModel pm = parameters.get(i);
                sb.append(pm.getName());
                if (i != parameters.size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Method getInterfacMethod() {
        return interfacMethod;
    }

    public void setInterfacMethod(Method interfacMethod) {
        this.interfacMethod = interfacMethod;
    }

    public List<ParameterModel> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterModel> parameters) {
        this.parameters = parameters;
    }

}
