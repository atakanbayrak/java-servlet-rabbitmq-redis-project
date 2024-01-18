package com.example.calis;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class RabbitConfig {
    public static final String DEFAULT_QUEUE = "Queue-1";
    public static final String AMQP_URL = "amqp://guest:guest@localhost:5672/";


    public Connection createInstance(ConnectionFactory fac)
    {
        ConnectionFactory factory = fac;
        try {
            factory.setUri(AMQP_URL);
            Connection connection = factory.newConnection();
            return connection;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
    public RabbitConfig()
    {

    }



}
