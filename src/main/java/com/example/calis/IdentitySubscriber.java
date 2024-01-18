package com.example.calis;

import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

public class IdentitySubscriber {

    private ConnectionFactory fac;
    private Connection connection;
    private Channel channel;
    public String pushTckn;
    private static IdentitySubscriber subscriber = new IdentitySubscriber();
    RedisConfig redis = RedisConfig.getInstance();
    private IdentitySubscriber()
    {
        System.out.println("Consumer singleton oluşturuldu.");
    }

    public static IdentitySubscriber getInstance()
    {
        return subscriber;
    }
    public IdentitySubscriber(ConnectionFactory factory, Connection connection, Channel channel)
    {
        this.connection = connection;
        this.channel = channel;
    }

    public void setFactory(ConnectionFactory factory)
    {
        this.fac = factory;
    }

    public void setTckn(String tckn)
    {
        pushTckn = tckn;
    }
    public String getTckn()
    {
        return pushTckn;
    }

    public void useProcess() throws TimeoutException, IOException {
        JedisPool pool = new JedisPool("localhost", Integer.parseInt("6379"));


        Connection connection;
        ConnectionFactory factory = new ConnectionFactory();
        //Burada db veya redis kontrolü mü yapılacak ?
        try
        {
            connection = factory.newConnection(RabbitConfig.AMQP_URL);
            channel = connection.createChannel();

            DeliverCallback deliverCallback = (s, message) ->
            {
                String tckn = new String(message.getBody());
                System.out.println("TCKN Consume Edildi: " + tckn);
                String createUniqueCache = tckn;
                //Redis içerisine baktı ve bulamadı durumu


                try(Jedis jedis = pool.getResource()) {
                    if(jedis.get(createUniqueCache) != null)
                    {
                        System.out.println("TCKN From Cache: " + jedis.get(tckn));
                        String answer = jedis.get(tckn);
                        setTckn("TCKN From Cache: " + answer);
                    }
                    else
                    {
                        //dbden çek
                        DatabaseProcess dbprocess = DatabaseProcess.getInstance();
                        String getTckn = dbprocess.getFromDatabase(tckn);
                        System.out.println("Veritabanından okundu: " + getTckn);
                        String answer = getTckn;
                        setTckn("Veritabanından okundu: " + answer);
                        jedis.set(tckn,getTckn);
                        //System.out.println("TCKN DB After Cache: " + jedis.get(tckn));
                    }

                }catch (RuntimeException e)
                {
                    System.out.println(e);
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
}

/*
                if(redis.readFromCache(tckn) != null)
                {
                    System.out.println("BURADA MISIN");
                    System.out.println(redis.readFromCache(tckn));
                    System.out.println("Cache Okuması");
                }
                else
                {
                    System.out.println("Burada");
                    try {
                        java.sql.Connection sqlconnection = DatabaseConnection.connect();
                        PreparedStatement select = sqlconnection.prepareStatement("select * from users");
                        ResultSet set = select.executeQuery();
                        redis.processRequest(set.getString(2));

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
*/
