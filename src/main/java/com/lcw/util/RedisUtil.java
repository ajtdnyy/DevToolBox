package com.lcw.util;

import com.lcw.eum.RedisDataType;
import com.lcw.model.NotSqlEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Tuple;

/**
 *
 * @author lancw
 */
public class RedisUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class.getName());
    private static final int TIMEOUT = 10000;
    public static JedisPool pool = null;
    public static String ip;
    public static String port;
    public static String pwd;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static JedisPool initPool(String ip, String port, String pwd) {
        if (pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(5);
            config.setTestOnBorrow(true);
            if (StringUtils.isBlank(pwd)) {
                pool = new JedisPool(config, ip, Integer.parseInt(port));
            } else {
                pool = new JedisPool(config, ip, Integer.parseInt(port), TIMEOUT, pwd);
            }
        }
        return pool;
    }

    public static JedisPool getPool() {
        if (pool == null) {
            initPool(ip, port, pwd);
        }
        return pool;
    }

    public static void returnResource(Jedis redis) {
        if (redis != null) {
            redis.close();
        }
    }

    public static int getDbAmountForRedis() {
        Jedis jedis = null;
        int dbAmount = 1;
        try {
            jedis = getPool().getResource();
            List dbs = jedis.configGet("databases");
            if (dbs.size() > 0) {
                dbAmount = Integer.parseInt((String) dbs.get(1));
            } else {
                dbAmount = 15;
            }
        } catch (Exception e) {
            LOGGER.error("获取redis数据库出错", e);
            return 1;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return dbAmount;
    }

    public static String get(String key) {
        String value = null;
        try (Jedis jedis = getPool().getResource()) {
            value = jedis.get(key);
        } catch (Exception e) {
            LOGGER.error("获取数据异常", e);
        }
        return value;
    }

    public static String getInfo() {
        String value = null;
        try (Jedis jedis = getPool().getResource()) {
            value = jedis.info();
            value += "\ntotalKeys:" + jedis.dbSize();
        } catch (Exception e) {
            LOGGER.error("获取信息异常", e);
        }
        return value;
    }

    public static String getConfig(String configKey) {
        String value = "";
        try (Jedis jedis = getPool().getResource()) {
            List<String> list = jedis.configGet(configKey);
            for (int i = 0; i < list.size(); i++) {
                value = (String) list.get(i);
            }
        } catch (Exception e) {
            LOGGER.error("获取配置异常", e);
        }
        return value;
    }

    public static Map<String, Object> get2(String key, String NoSQLDbName, String databaseName) {
        String currentDBindex = NoSQLDbName.substring(NoSQLDbName.length() - 1, NoSQLDbName.length());
        Map<String, Object> map = new HashMap();
        try (Jedis jedis = getPool().getResource()) {
            jedis.select(Integer.parseInt(currentDBindex));
            String type = jedis.type(key);
            Long exTime = jedis.ttl(key);
            map.put("key", key);
            map.put("type", type);
            map.put("exTime", exTime);
            if (type.equals("string")) {
                map.put("value", jedis.get(key));
            }
            if (type.equals("list")) {
                Long lon = jedis.llen(key);
                map.put("value", jedis.lrange(key, 0L, lon));
            }
            if (type.equals("set")) {
                map.put("value", jedis.smembers(key).toString());
            }
            if (type.equals("zset")) {
                Set<Tuple> set = jedis.zrangeWithScores(key, 0L, -1L);
                map.put("value", set);
            }
            if (type.equals("hash")) {
                map.put("value", jedis.hgetAll(key));
            }

        } catch (Exception e) {
            LOGGER.error("获取数据异常", e);
        }
        return map;
    }

    public static boolean bgsave(String databaseName) {
        try (Jedis jedis = getPool().getResource()) {
            jedis.bgsave();
            return true;
        } catch (Exception e) {
            LOGGER.error("保存数据异常", e);
            return false;
        }
    }

    public static boolean set(NotSqlEntity notSqlEntity) {
        Jedis jedis = null;
        try {
            String key = notSqlEntity.getKey();
            String value = notSqlEntity.getValue();
//            value = StringEscapeUtils.unescapeHtml4(value);
            RedisDataType type = RedisDataType.valueOf(notSqlEntity.getType());
            int o1 = -1;
            if (("".equals(notSqlEntity.getExTime())) || ("0".equals(notSqlEntity.getExTime()))) {
                o1 = -1;
            } else {
                o1 = Integer.parseInt(notSqlEntity.getExTime());
            }

            jedis = getPool().getResource();
            switch (type) {
                case string:
                    jedis.set(key, value);
                    break;
                case list: {
                    String[] valuek = notSqlEntity.getValuek();
                    jedis.del(new String[]{key});
                    for (int i = valuek.length; i > 0; i--) {
                        if (i == valuek.length) {
                            jedis.lpush(key, new String[]{valuek[(i - 1)]});
                        } else {
                            jedis.lpushx(key, valuek[(i - 1)]);
                        }
                    }
                    break;
                }
                case set: {
                    String[] valuek = notSqlEntity.getValuek();
                    jedis.del(new String[]{key});
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < valuek.length; i++) {
                        sb.append(i == 0 ? "" : ",").append(valuek[i]);
                    }
                    jedis.sadd(key, new String[]{sb.toString()});
                    break;
                }
                case zset: {
                    String[] valuek = notSqlEntity.getValuek();
                    String[] valueV = notSqlEntity.getValuev();
                    jedis.del(new String[]{key});
                    Map<String, Double> scoreMembers = new HashMap();
                    for (int i = valuek.length; i > 0; i--) {
                        Double valuekkk = Double.parseDouble(valuek[(i - 1)].trim());
                        String valuevvv = valueV[(i - 1)].trim();
                        if (valuevvv == null) {
                            valuevvv = "";
                        }
                        scoreMembers.put(valuevvv, valuekkk);
                    }
                    jedis.zadd(key, scoreMembers);
                    break;
                }
                case hash: {
                    String[] valuek = notSqlEntity.getValuek();
                    String[] valueV = notSqlEntity.getValuev();
                    jedis.del(new String[]{key});
                    Map<String, String> hashmm = new HashMap();
                    for (int i = valuek.length; i > 0; i--) {
                        String valuekkk = valuek[(i - 1)].trim();
                        String valuevvv = valueV[(i - 1)].trim();
                        if (valuevvv == null) {
                            valuevvv = "";
                        }
                        hashmm.put(valuekkk, valuevvv);
                    }
                    jedis.hmset(key, hashmm);
                    break;
                }
                default:
                    break;
            }

            if (o1 != -1) {
                jedis.expire(key, o1);
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("保存数据异常", e);
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public static boolean deleteKeys(String NoSQLDbName, String[] ids) {
        String currentDBindex = NoSQLDbName.substring(NoSQLDbName.length() - 1, NoSQLDbName.length());
        try (Jedis jedis = getPool().getResource()) {
            jedis.select(Integer.parseInt(currentDBindex));
            for (String id : ids) {
                jedis.del(new String[]{id});
            }
        } catch (Exception e) {
            LOGGER.error("删除数据异常", e);
            return false;
        }
        return true;
    }

    public static Map<String, Object> getNoSQLDBForRedis(int pageSize, int limitFrom, String NoSQLDbName, String selectKey, String selectValue) {
        String currentDBindex = NoSQLDbName.substring(2, NoSQLDbName.length());
        Map<String, Object> tempMap = new HashMap();
        Jedis jedis = null;
        List<Map<String, Object>> list = new ArrayList();
        try {
            jedis = getPool().getResource();
            jedis.select(Integer.parseInt(currentDBindex));
            Long dbSize = jedis.dbSize();
            Set nodekeys = new HashSet();
            if (selectKey.equals("nokey")) {
                if (dbSize > 10000L) {
                    limitFrom = 0;
                    for (int z = 0; z < pageSize; z++) {
                        nodekeys.add(jedis.randomKey());
                    }
                } else {
                    nodekeys = jedis.keys("*");
                }
            } else {
                nodekeys = jedis.keys("*" + selectKey + "*");
            }
            Iterator it = nodekeys.iterator();
            int i = 0;
            String value = "";
            while (it.hasNext()) {
                if ((i >= limitFrom) && (i <= limitFrom + pageSize)) {
                    Map<String, Object> map = new HashMap();
                    String key = (String) it.next();
                    RedisDataType type = RedisDataType.valueOf(jedis.type(key));
                    map.put("key", key);
                    map.put("type", type);
                    Long lon = 0L;
                    switch (type) {
                        case hash:
                            map.put("value", jedis.hgetAll(key).toString());
                            break;
                        case list:
                            lon = jedis.llen(key);
                            map.put("value", jedis.lrange(key, 0L, lon));
                            break;
                        case set:
                            map.put("value", jedis.smembers(key).toString());
                            break;
                        case string:
                            value = jedis.get(key);
                            map.put("value", value);
                            break;
                        case zset:
                            lon = jedis.zcard(key);
                            Set<Tuple> set = jedis.zrangeWithScores(key, 0L, lon);
                            Iterator<Tuple> itt = set.iterator();
                            String ss = "";
                            while (itt.hasNext()) {
                                Tuple str = (Tuple) itt.next();
                                ss = ss + "[" + str.getScore() + "," + str.getElement() + "],";
                            }
                            ss = ss.substring(0, ss.length() - 1);
                            map.put("value", "[" + ss + "]");
                            break;
                    }
                    Long expire = jedis.ttl(key);
                    if (expire > 0) {
                        expire = expire * 1000 + System.currentTimeMillis();
                        map.put("expire", dateFormat.format(new Date(expire)));
                    } else {
                        map.put("expire", "");
                    }
                    list.add(map);
                } else {
                    it.next();
                }
                i++;
            }

            if (selectKey.equals("nokey")) {
                tempMap.put("rowCount", Integer.parseInt(dbSize.toString()));
            } else {
                tempMap.put("rowCount", i);
            }
            tempMap.put("dataList", list);
        } catch (Exception e) {
            LOGGER.error("获取数据异常", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return tempMap;
    }

}
