package com.lcw.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lancw
 */
public class DataAccessUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataAccessUtil.class.getName());

    public static Map<String, Object> selectNoSQLDBStatusForRedis() throws Exception {
        List<Map<String, Object>> list = new ArrayList();
        String info = RedisUtil.getInfo();
        Properties properties = new Properties();
        InputStream inStream = new ByteArrayInputStream(info.getBytes());
        properties.load(inStream);

        Iterator<Map.Entry<Object, Object>> it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Map<String, Object> map = new HashMap();
            Map.Entry<Object, Object> entry = (Map.Entry) it.next();
            String parameter = (String) entry.getKey();
            map.put("parameter", parameter);
            switch (parameter) {
                case "redis_version":
                    map.put("value", entry.getValue());
                    map.put("content", "redis版本号");
                    break;
                case "last_save_time":
                    map.put("value", entry.getValue());
                    map.put("content", "最后保存RDB文件的时间");
                    break;
                default:
                    map.put("value", entry.getValue());
                    map.put("content", "");
                    break;
            }
            list.add(map);
        }
        int rowCount = list.size();
        Map<String, Object> page = new HashMap<>();
        page.put("totalCount", rowCount);
        page.put("result", list);
        return page;
    }

    public static List<String> getAllDataBaseForReids() throws Exception {
        List<String> listAll = new ArrayList();
        try {
            int dbAmount = RedisUtil.getDbAmountForRedis();
            for (int y = 0; y < dbAmount; y++) {
                listAll.add("DB" + y);
            }
        } catch (Exception e) {
            LOGGER.error("获取redis数据库异常", e);
            return null;
        }
        return listAll;
    }
}
