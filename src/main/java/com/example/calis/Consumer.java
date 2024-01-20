package com.example.calis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class Consumer {

    Database dbprocess = Database.getInstance();
    private Channel channel;
    public String pushTckn;
    private static Consumer subscriber = new Consumer();

    public static Consumer getInstance()
    {
        return subscriber;
    }

    public void useProcess() throws TimeoutException, IOException {

        JedisPool pool = Config.createJedis();
        Connection connection;
        ConnectionFactory factory = new ConnectionFactory();
        try
        {
            connection = factory.newConnection(Config.AMQP_URL);
            channel = connection.createChannel();

            DeliverCallback deliverCallback = (s, message) ->
            {
                String tckn = new String(message.getBody());
                try(Jedis jedis = pool.getResource()) {
                    if(jedis.get(tckn) != null)
                    {
                        System.out.println("TCKN From Cache: " + jedis.get(tckn));
                        String answer = jedis.get(tckn);
                        setAnswer("TCKN From Cache: " + answer);
                    }
                    else
                    {
                        Database dbprocess = Database.getInstance();
                        String tckndb = dbprocess.getFromDatabase(tckn);
                        System.out.println("Veritabanından okundu: " + tckndb);
                        setAnswer("Veritabanından okundu: " + tckndb);

                        String hashValue = Hashing.generateHash(tckndb);
                        jedis.set(hashValue,tckndb);
                        jedis.expire(hashValue, 10);
                        jedis.close();
                    }
                }catch (RuntimeException e)
                {
                    System.out.println(e);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            };

            CancelCallback cancelCallback = s -> {
                System.out.println("No message"+s);
            };

            channel.basicConsume("kuyruk", true, deliverCallback, cancelCallback);
        }catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void useCreation() throws TimeoutException, IOException
    {
        Connection connection;
        ConnectionFactory factory = new ConnectionFactory();
        try
        {
            connection = factory.newConnection(Config.AMQP_URL);
            channel = connection.createChannel();

            DeliverCallback deliverCallback = (s, message) ->
            {

                String answer = Arrays.toString(message.getBody());
                ObjectMapper objectMapper = new ObjectMapper();

                ArrayList<String> users = objectMapper.readValue(answer, new TypeReference<ArrayList<String>>(){});
                System.out.println(users.get(0));
                for(String user:users)
                {
                    System.out.println(user);
                }
                try {
                    Database.connect();
                    //dbprocess.saveToDatabase();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            };

            CancelCallback cancelCallback = s -> {
                System.out.println("No message"+s);
            };

            channel.basicConsume("post", true, deliverCallback, cancelCallback);
        }catch (IOException e)
        {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAnswer(String tckn)
    {
        pushTckn = tckn;
    }
    public String getAnswer()
    {
        return pushTckn;
    }
}