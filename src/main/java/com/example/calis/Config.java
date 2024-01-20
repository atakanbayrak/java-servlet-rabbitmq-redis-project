package com.example.calis;

import redis.clients.jedis.JedisPool;

public class Config {
    public static final String AMQP_URL = "amqp://guest:guest@localhost:5672/";
    private static Config config = new Config();

    public static Config getInstance()
    {
        return config;
    }

    public static JedisPool createJedis()
    {
        JedisPool pool = new JedisPool("localhost",Integer.parseInt("6379"));
        return pool;
    }
}
