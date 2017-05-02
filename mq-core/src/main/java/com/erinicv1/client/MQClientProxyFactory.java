package com.erinicv1.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2017/4/27 0027.
 */
public class MQClientProxyFactory implements InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(MQClientProxyFactory.class);

    private ConnectionFactory connectionFactory;

    private RabbitAdmin admin;

    private RabbitTemplate rabbitTemplate;

    private AtomicBoolean initializing = new AtomicBoolean(false);

    private String queuePrefix;

    private long timeOut = -1;

    private boolean compress = true;

    protected Class<?> serviceInterface;

    public MQClientProxyFactory(){

    }


    public void createRequestsQueue(AmqpAdmin amqpAdmin,String queueName,String exchangeName){
        Queue queue = new Queue(queueName,false,false,false);
        amqpAdmin.declareQueue(queue);
        DirectExchange exchange = new DirectExchange(exchangeName,false,false);
        amqpAdmin.declareExchange(exchange);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(queueName);
        amqpAdmin.declareBinding(binding);

    }

    public String getRequestQueueName(){
        String queueName = serviceInterface.getSimpleName();
        if (queuePrefix != null){
            queueName = queuePrefix + "." + queueName;
        }

        return queueName;
    }

    public String getRequestExchangeName(){
        String queueName = serviceInterface.getSimpleName();
        if (queuePrefix != null){
            queueName = queuePrefix + "." + queueName;
        }

        return queueName;
    }

    public void initilizingQueue(){
        if (!initializing.compareAndSet(false,true)){
            return ;
        }
        try {
            if (admin == null) {
                admin = new RabbitAdmin(connectionFactory);
            }
            createRequestsQueue(admin, getRequestQueueName(), getRequestExchangeName());
        }finally {
            initializing.compareAndSet(true,false);
        }
    }

    public void afterPropertiesSet(){
        if (connectionFactory == null){
            throw new IllegalArgumentException(" Property connection is need ");
        }
        if (rabbitTemplate == null){
            rabbitTemplate = new RabbitTemplate(connectionFactory);
            if (timeOut > 0){
                rabbitTemplate.setReplyTimeout(timeOut);
                logger.debug("配置RPC消息的响应超时时间为{}", timeOut);
            }
        }
        admin = new RabbitAdmin(connectionFactory);
        initilizingQueue();
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public String getQueuePrefix() {
        return queuePrefix;
    }

    public void setQueuePrefix(String queuePrefix) {
        this.queuePrefix = queuePrefix;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public RabbitTemplate getRabbitTemplate() {
        return rabbitTemplate;
    }



    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        if (serviceInterface == null || !serviceInterface.isInterface()){
            throw new IllegalArgumentException("'serviceInterface' is null or is not an interface");
        }
        this.serviceInterface = serviceInterface;
    }
}
