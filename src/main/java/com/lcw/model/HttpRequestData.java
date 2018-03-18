package com.lcw.model;

import com.lcw.eum.RequestMethod;
import java.util.HashSet;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author lancw
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "com.lcw.model.HttpRequestData")
@XmlRootElement(name = "request")
public class HttpRequestData {

    private final HashSet<Request> request = new HashSet<>();

    public HashSet<Request> getRequest() {
        return request;
    }

    public boolean add(Request request) {
        return this.request.add(request);
    }

    public static class Request {

        private String url;
        private String method;
        private String contentType;
        private String header;
        private String body;
        private String alias = "";
        private String charset = "UTF-8";
        private String useagent;
        private String timeout;
        private Boolean replaceVar = Boolean.FALSE;

        public Boolean getReplaceVar() {
            return replaceVar;
        }

        public void setReplaceVar(Boolean replaceVar) {
            this.replaceVar = replaceVar;
        }

        /**
         * 如果方式是GET返回url中将带上body参数
         *
         * @return
         */
        public String getUrl() {
            return url;
        }

        public String getUrlWithData() {
            if (url != null) {
                if (RequestMethod.GET.toString().equals(method) && body != null && !body.trim().isEmpty()) {
                    if (url.endsWith("?")) {
                        return url + encodeBody();
                    } else if (url.contains("?")) {
                        return url + "&" + encodeBody();
                    } else {
                        return url + "?" + encodeBody();
                    }
                }
            }
            return url;
        }

        public String encodeBody() {
            if (body != null) {
                String tmpBody = body.replaceAll("==", "##");
                String[] tmp = tmpBody.split("&");
                for (String str : tmp) {
                    String[] tt = str.split("=");
                    if (tt.length == 2) {
                        String t = tt[1].replaceAll("##", "==");
                        tmpBody = tmpBody.replace(tt[1], t);
                    }
                }
                return tmpBody;
            }
            return body;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getHeader() {
            return header;
        }

        public void setHeader(String header) {
            this.header = header;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getCharset() {
            return charset;
        }

        public void setCharset(String charset) {
            this.charset = charset;
        }

        public String getUseagent() {
            return useagent;
        }

        public void setUseagent(String useagent) {
            this.useagent = useagent;
        }

        public String getTimeout() {
            return timeout;
        }

        public void setTimeout(String timeout) {
            this.timeout = timeout;
        }

        @Override
        public String toString() {
            return String.format("[%s]%s", method, StringUtils.isBlank(alias) ? url : alias + " " + url);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + Objects.hashCode(this.url);
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
            final Request other = (Request) obj;
            return Objects.equals(this.url, other.url);
        }

    }
}
