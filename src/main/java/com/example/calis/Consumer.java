package com.example.calis;

import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer {

    private ConnectionFactory fac;
    private Connection connection;
    private Channel channel;
    public String pushTckn;
    private static Consumer subscriber = new Consumer();

    public static Consumer getInstance()
    {
        return subscriber;
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
                        setTckn("TCKN From Cache: " + answer);
                    }
                    else
                    {
                        Database dbprocess = Database.getInstance();
                        String getTckn = dbprocess.getFromDatabase(tckn);
                        System.out.println("Veritabanından okundu: " + getTckn);
                        setTckn("Veritabanından okundu: " + getTckn);
                        //Burada hash kullanılabilir, eşsiz bir değer üretebilmek için işaretleme adına.
                        jedis.set(tckn,getTckn);
                        //Expire edebilmek için jedis.expire kullanılabilir, bir araştır.
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