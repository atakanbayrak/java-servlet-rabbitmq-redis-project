package com.example.calis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class Consumer {

    Database dbprocess = Database.getInstance();
    private Channel channel;
    private final static Consumer subscriber = new Consumer();

    public static Consumer getInstance()
    {
        return subscriber;
    }

    public void useProcess() throws TimeoutException {

        try(JedisPool pool = Config.createJedis())
        {
            ConnectionFactory factory = new ConnectionFactory();
            try
            {
                try(Connection connection = factory.newConnection(Config.AMQP_URL);)
                {
                    channel = connection.createChannel();

                    DeliverCallback deliverCallback = (s, message) ->
                    {
                        String tckn = new String(message.getBody());
                        String hashValue;

                        try {
                            hashValue = Hashing.generateHash(tckn);
                        } catch (NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }

                        try(Jedis jedis = pool.getResource()) {
                            if(jedis.get(hashValue) != null)
                            {
                                System.out.println("TCKN From Cache: " + jedis.get(hashValue));
                            }
                            else
                            {
                                Database dbprocess = Database.getInstance();
                                String tckndb = dbprocess.getFromDatabase(tckn);

                                System.out.println("Veritabanından okundu: " + tckndb);

                                jedis.set(hashValue,tckndb);
                                jedis.expire(hashValue, 10);
                                jedis.close();
                            }
                        }catch (RuntimeException e)
                        {
                            throw new RuntimeException(e);
                        }
                    };

                    CancelCallback cancelCallback = s -> {
                        System.out.println("No message"+s);
                    };

                    channel.basicConsume("kuyruk", true, deliverCallback, cancelCallback);
                }
            }catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public void useCreation() throws TimeoutException
    {

        ConnectionFactory factory = new ConnectionFactory();
        try
        {
            try(Connection connection =factory.newConnection(Config.AMQP_URL) )
            {
                channel = connection.createChannel();

                DeliverCallback deliverCallback = (s, message) ->
                {
                    ObjectMapper objectMapper = new ObjectMapper();
                    User user = objectMapper.readValue(message.getBody(), User.class);

                    try {
                        Database.connect();
                        dbprocess.saveToDatabase(user.getFullname(),user.getTckn());
                        Database.connect().close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }

                };

                CancelCallback cancelCallback = s -> {
                    System.out.println("No message"+s);
                };

                channel.basicConsume("post", true, deliverCallback, cancelCallback);
            }
        }catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}