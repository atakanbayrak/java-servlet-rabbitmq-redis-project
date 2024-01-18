package com.example.calis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


public class RedisConfig {
    JedisPool pool = new JedisPool("localhost", 6379);

    String createKey = null;
    private static RedisConfig redisConfig = new RedisConfig();

    private RedisConfig()
    {
        System.out.println("Redis Config Singleton Oluşturuldu");
    }

    public static RedisConfig getInstance()
    {
        return redisConfig;
    }
    public void processRequest(String tckn)
    {
        Jedis jedis = pool.getResource();
        createKey = tckn;
        System.out.println("Connection to server sucessfully");
        System.out.println("Server is running" + jedis.ping());

        //Redis içerisine atma
        jedis.set(createKey,tckn);
    }

    public String readFromCache(String key)
    {
        Jedis jedis = pool.getResource();
        //Burayı get içerisinde çağıracağız.
        System.out.println("Stored tckn in redis:: " + jedis.get(createKey));
        return jedis.get(createKey);
    }
}
