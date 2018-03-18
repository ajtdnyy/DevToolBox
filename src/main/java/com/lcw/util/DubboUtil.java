package com.lcw.util;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboInvoker;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import java.lang.reflect.Method;

/**
 *
 * @author lancw
 */
public class DubboUtil {

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
        DubboInvoker<?> invoker = (DubboInvoker<?>) PROTOCOL.refer(c, URL.valueOf(fullUrl));
        if (invoker.isAvailable()) {
            Invocation inv = new RpcInvocation(method, args);
            Result ret = invoker.invoke(inv);
            PROTOCOL.destroy();
            return JSON.json(ret.getValue());
        }
        return null;
    }
}
