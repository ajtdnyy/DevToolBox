package com.lcw.util;

import java.util.List;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lancw
 */
public class ZookeeperUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperUtil.class.getName());
    static ZkClient zk;
    static final String DUBBO_PREFIX = "/dubbo/";
    static final String DUBBO_PROVIDERS = "/providers";
    private static String zkAddress;

    /**
     * 连接zookeeper
     *
     * @param address
     */
    public static void connectZK(String address) {
        if (address == null || !address.contains(":")) {
            LOGGER.error("zookeeper连接地址错误");
            return;
        }
        boolean change = zk == null || zkAddress == null || !zkAddress.equals(address);
        zkAddress = address;
        if (change) {
            zk = new ZkClient(address, 10000);
        }
    }

    /**
     * 获取zookeeper上注册的dubbo服务
     *
     * @param path
     * @return
     */
    public static List<String> getDubboService(String path) {
        if (zk == null) {
            connectZK(zkAddress);
        }
        if (path != null) {
            path = DUBBO_PREFIX + path + DUBBO_PROVIDERS;
        }
        path = path == null || path.trim().isEmpty() ? "/dubbo" : path;
        List<String> cs = zk.getChildren(path);
        return cs;
    }

    public static void close() {
        if (zk != null) {
            zk.close();
            zk = null;
            System.gc();
        }
    }
}
