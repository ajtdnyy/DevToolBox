package com.lcw.util;

import com.lcw.eum.RequestMethod;
import static com.lcw.eum.RequestMethod.GET;
import static com.lcw.eum.RequestMethod.POST;
import com.lcw.model.HttpRequestData;
import com.sun.webkit.network.CookieManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lancw
 */
public class HttpClientUtil {

    public static final String COOKIE = "Cookie";
    public static final String REQUEST_HEADER_PREFIX = "request_";
    public static CloseableHttpClient CLIENT;
    private static final CookieStore COOKIE_STORE = new BasicCookieStore();
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class.getName());

    /**
     * 下载文件
     *
     * @param destUrl
     * @param fileName
     * @throws IOException
     */
    public static void downloadFile(String destUrl, String fileName) throws IOException {
        byte[] buf = new byte[1024];
        int size = 0;
        URL url = new URL(destUrl);
        HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
        httpUrl.connect();
        try (BufferedInputStream bis = new BufferedInputStream(httpUrl.getInputStream()); FileOutputStream fos = new FileOutputStream(fileName)) {
            while ((size = bis.read(buf)) != -1) {
                fos.write(buf, 0, size);
            }
        }
        httpUrl.disconnect();
    }

    public static HttpResponse sendRequest(HttpRequestData.Request req) throws Exception {
        Integer timeout = Integer.valueOf(req.getTimeout());
        SSLContext sslc = SSLContexts.custom().loadTrustMaterial((X509Certificate[] chain, String authType) -> true).build();
        CLIENT = HttpClients.custom().setSSLContext(sslc).setDefaultCookieStore(COOKIE_STORE).setUserAgent(req.getUseagent()).build();
        RequestConfig rc = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).setRedirectsEnabled(true).build();
        switch (RequestMethod.valueOf(req.getMethod())) {
            case GET:
                return doGet(req, rc);
            case POST:
                return doPost(req, rc);
        }
        return null;
    }

    private static HttpResponse doGet(HttpRequestData.Request req, RequestConfig rc) throws Exception {
        String url = req.getUrlWithData();
        LOGGER.info("Get request url:" + url);
        HttpGet get = new HttpGet(url);
        get.setConfig(rc);
        get.setHeader("Content-Type", req.getContentType() + ";charset=" + req.getCharset());
        initHeader(get, req.getHeader());
        HttpClientContext hc = new HttpClientContext();
        HttpResponse resp = CLIENT.execute(get, hc);
        addResquestHeaderToResponseForShow(resp, hc.getRequest());
        return resp;
    }

    private static HttpResponse doPost(HttpRequestData.Request req, RequestConfig rc) throws Exception {
        String url = req.getUrl();
        List<NameValuePair> parameters = new ArrayList<>();
        String bd = req.encodeBody();
        HttpEntity formEntiry = null;
        if (bd != null) {
            String ct = req.getContentType();
            ContentType type = ContentType.create(ct, req.getCharset());
            if (ct.contains("xml") || ct.contains("json") || ContentType.TEXT_PLAIN.getMimeType().equals(ct)) {
                StringEntity se = new StringEntity(bd, req.getCharset());
                se.setContentType(ct);
                formEntiry = se;
            } else if (com.lcw.eum.ContentType.CONTENT_TYPE_MULTIPART_FORM_DATA.getCode().equals(ct)) {
                String[] tmp = bd.split("&");
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                for (String str : tmp) {
                    int idx = str.indexOf("=");
                    if (idx > 0) {
                        String t0 = str.substring(0, idx);
                        String t1 = str.substring(idx + 1);
                        if (t1.startsWith("/") || t1.substring(1).startsWith(":")) {
                            File file = new File(t1);
                            if (file.exists()) {
                                builder.addBinaryBody(t0, file);
                                LOGGER.info("文件字段提交:" + str);
                            } else {
                                builder.addTextBody(t0, t1);
                                LOGGER.info("文件不存在或误判断为文件，转为普通字段提交:" + str);
                            }
                        } else {
                            builder.addTextBody(t0, t1);
                        }
                    } else {
                        throw new Exception("错误参数：" + str);
                    }
                }
                formEntiry = builder.build();
            } else {
                String[] tmp = bd.split("&");
                for (String str : tmp) {
                    int idx = str.indexOf("=");
                    if (idx > 0) {
                        parameters.add(new BasicNameValuePair(str.substring(0, idx), str.substring(idx + 1)));
                    } else {
                        throw new Exception("错误参数：" + str);
                    }
                }
                formEntiry = EntityBuilder.create().setParameters(parameters).setContentType(type).build();
            }
        }
        return sendPostHttp(url, formEntiry, req.getHeader(), rc);
    }

    private static HttpResponse sendPostHttp(String url, HttpEntity formEntiry, String header, RequestConfig rc) throws Exception {
        HttpPost postmethod = new HttpPost(url);
        postmethod.setConfig(rc);
        initHeader(postmethod, header);
        postmethod.setEntity(formEntiry);
        HttpClientContext hc = new HttpClientContext();
        HttpResponse resp = CLIENT.execute(postmethod, hc);
        addResquestHeaderToResponseForShow(resp, hc.getRequest());
        return resp;
    }

    private static void initHeader(HttpRequestBase base, String header) throws Exception {
        if (header != null && !header.trim().isEmpty()) {
            String[] tmp = header.split("&");
            for (String str : tmp) {
                if (str.startsWith(COOKIE)) {
                    base.addHeader(COOKIE, str.replace(COOKIE + "=", ""));
                } else {
                    int idx = str.indexOf("=");
                    if (idx > 0) {
                        base.addHeader(str.substring(0, idx), str.substring(idx + 1));
                    }
                }
            }
        }
        CookieManager cm = (CookieManager) CookieHandler.getDefault();
        Field fd = cm.getClass().getDeclaredField("store");
        fd.setAccessible(true);
        Object cs = fd.get(cm);//cookieStore
        fd = cs.getClass().getDeclaredField("buckets");
        fd.setAccessible(true);
        cs = fd.get(cs);//Map<String,Map<Cookie,Cookie>>
        Method md = cs.getClass().getDeclaredMethod("get", Object.class);
        String domain = base.getURI().getHost();
        Map map = new HashMap();
        while (domain.length() > 0) {
            Object bucket = md.invoke(cs, domain);
            if (bucket != null) {
                map.putAll((Map) bucket);
            }
            int nextPoint = domain.indexOf('.');
            if (nextPoint != -1) {
                domain = domain.substring(nextPoint + 1);
            } else {
                break;
            }
        }
        if (!map.isEmpty()) {
            Iterator it = map.keySet().iterator();
            StringBuilder sb = new StringBuilder();
            while (it.hasNext()) {
                Object next = it.next();
                fd = next.getClass().getDeclaredField("name");
                fd.setAccessible(true);
                sb.append(fd.get(next));
                fd = next.getClass().getDeclaredField("value");
                fd.setAccessible(true);
                sb.append("=").append(fd.get(next)).append(";");
            }
            if (!sb.toString().isEmpty()) {
                Header h = base.getFirstHeader(COOKIE);
                String ck = "";
                if (h != null) {
                    ck = h.getValue();
                }
                base.addHeader(COOKIE, sb.toString() + ck);
            }
        }
    }

    private static void addResquestHeaderToResponseForShow(HttpResponse resp, HttpRequest base) throws Exception {
        Header[] hs = base.getAllHeaders();
        if (hs != null) {
            for (Header h : hs) {
                resp.addHeader(REQUEST_HEADER_PREFIX + h.getName(), h.getValue());
            }
        }
    }

}
