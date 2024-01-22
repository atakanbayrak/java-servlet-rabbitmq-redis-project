package com.example.calis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Publisher {

    private final static Publisher publisher = new Publisher();
    public static Publisher getInstance()
    {
        return publisher;
    }

    public void createProcess(String tckn) throws TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection;
        Channel channel;
        try
        {
            connection = factory.newConnection(Config.AMQP_URL);
            channel = connection.createChannel();

            channel.queueDeclare("kuyruk", true, false, false, null);
            channel.exchangeDeclare("my-direct", BuiltinExchangeType.DIRECT, true);
            channel.queueBind("kuyruk", "my-direct", "tckn");

            channel.basicPublish("my-direct", "tckn", null, tckn.getBytes());
            channel.close();
            connection.close();

        }catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void createPost(User user) throws TimeoutException, JsonProcessingException {
        ConnectionFactory factory = new ConnectionFactory();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(user);
        Connection connection;
        Channel channel;
        try
        {
            connection = factory.newConnection(Config.AMQP_URL);
            channel = connection.createChannel();

            channel.queueDeclare("post", true, false, false, null);
            channel.exchangeDeclare("my-direct", BuiltinExchangeType.DIRECT, true);
            channel.queueBind("post", "my-direct", "db");

            channel.basicPublish("my-direct", "db", null, json.getBytes());
            channel.close();
            connection.close();

        }catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
