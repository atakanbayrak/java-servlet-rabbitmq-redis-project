package com.example.calis;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Publisher {

    private Channel channel;
    private Connection connection;

    private static Publisher publisher = new Publisher();
    private Publisher()
    {
        System.out.println("Publisher Singleton oluşturuldu.");
    }

    public static Publisher getInstance()
    {
        return publisher;
    }

    public void createProcess(String fullname,String tckn) throws TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection;
        Channel channel;
        try
        {
            connection = factory.newConnection(Config.AMQP_URL);
            System.out.println("Connection Yaratıldı");
            channel = connection.createChannel();
            System.out.println("Channel Oluşturuldu");

            channel.queueDeclare("kuyruk", true, false, false, null);
            channel.exchangeDeclare("my-direct", BuiltinExchangeType.DIRECT, true);
            channel.queueBind("kuyruk", "my-direct", "tckn");

            channel.basicPublish("my-direct", "tckn", null, tckn.getBytes());
            System.out.println("TCKN Publish Edildi: " + tckn);
            channel.close();
            connection.close();

        }catch (IOException e)
        {
            throw new RuntimeException(e + "Burada");
        }

    }

//    public static void main(String[] args) throws Exception{
//        ConnectionFactory factory = new ConnectionFactory();
//        Connection connection;
//
//        try
//        {
//            connection = (Channel) factory.newConnection(RabbitConfig.AMQP_URL);
//            channel = connection.getConnection().createChannel();
//            String message = "39260999725";
//            channel.basicPublish("", RabbitConfig.DEFAULT_QUEUE, null, message.getBytes());
//            channel.close();
//            connection.close();
//        }catch (IOException e)
//        {
//            throw new RuntimeException(e);
//        }catch (TimeoutException e)
//        {
//            throw new RuntimeException(e);
//        }
//    }

}
