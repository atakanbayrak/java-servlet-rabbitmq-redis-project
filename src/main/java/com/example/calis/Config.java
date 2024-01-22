package com.example.calis;

import redis.clients.jedis.JedisPool;

public class Config {
    public static final String AMQP_URL = "amqp://guest:guest@localhost:5672/";

    public static JedisPool createJedis()
    {
        return new JedisPool("localhost",Integer.parseInt("6379"));
    }
}
