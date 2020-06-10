package com.lcw.util;

import java.lang.reflect.Method;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.json.JSON;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.protocol.AsyncToSyncInvoker;
import org.apache.dubbo.rpc.protocol.dubbo.DubboProtocol;

/**
 *
 * @author lancw
 */
public class ApacheDubboUtil {

    private static final DubboProtocol PROTOCOL = DubboProtocol.getDubboProtocol();

    /**
     * 调用方法
     *
     * @param c
     * @param method
     * @param fullUrl
     * @param args
     * @return
     * @throws java.lang.Exception
     */
    public static Object invoke(Class c, Method method, String fullUrl, Object... args) throws Exception {
        URL url = URL.valueOf(fullUrl);
        AsyncToSyncInvoker<?> invoker = (AsyncToSyncInvoker<?>) PROTOCOL.refer(c, url);
        if (invoker.isAvailable()) {
            Invocation inv = new RpcInvocation(method, url.getParameter("interface"), args);
            Result ret = invoker.invoke(inv);
            PROTOCOL.destroy();
            return JSON.json(ret.getValue());
        }
        return null;
    }
}
