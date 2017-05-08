package com.erinicv1.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.aop.SpringProxy;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Created by Administrator on 2017/4/27 0027.
 */
public class MQServerEndpoint implements InitializingBean,DisposableBean {

    private final static Logger logger = LoggerFactory.getLogger(MQServerEndpoint.class);

    private ConnectionFactory connectionFactory;

    private Class<?> serviceAPI;

    private Object serviceImpl;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    private AmqpAdmin admin;

    private int concurrnetCusomer;

    private String queuePrefix;

    public MQServerEndpoint(){
        setServiceAPI(findRemote(getClass()));
        setServiceImpl(this);
    }

    public MQServerEndpoint(Object serviceImpl){
        setServiceAPI(findRemote(serviceImpl.getClass()));
        setServiceImpl(serviceImpl);
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }


    public int getConcurrnetCusomer() {
        return concurrnetCusomer;
    }

    public void setConcurrnetCusomer(int concurrnetCusomer) {
        this.concurrnetCusomer = concurrnetCusomer;
    }

    public String getQueuePrefix() {
        return queuePrefix;
    }

    public void setQueuePrefix(String queuePrefix) {
        this.queuePrefix = queuePrefix;
    }

    public void setServiceImpl(Object serviceImpl){
        this.serviceImpl = serviceImpl;
        setServiceAPI( findRemote(serviceImpl.getClass()));
    }

    public void setServiceAPI(Class<?> serviceAPI){
        this.serviceAPI = serviceAPI;
    }

    private Class<?> findRemote(Class<?> clz){
        if (null == clz){
            return null;
        }

        Class[] classes = clz.getInterfaces();
        if (classes.length > 0 && !classes[0].equals(SpringProxy.class)){
            return classes[0];
        }else {
            return findRemote(clz.getSuperclass());
        }
    }

    private String getRequestQueueName(Class<?> clz){
        String requestQueueName = clz.getSimpleName();
        if (queuePrefix != null){
            requestQueueName = queuePrefix +"." + requestQueueName;
        }
        return requestQueueName;
    }

    private void createQueue(AmqpAdmin amqpAdmin,String name){
        Queue queue = new Queue(name,false,false,false);
        amqpAdmin.declareQueue(queue);
    }

    public void run(){
        logger.debug("Launching endpoint for service : " + serviceAPI.getSimpleName() );

        connectionFactory.addConnectionListener(new ConnectionListener() {
            @Override
            public void onCreate(Connection connection) {
                createQueue(admin,getRequestQueueName(serviceAPI));
            }

            @Override
            public void onClose(Connection connection) {

            }
        });

        MessageListenerAdapter adapter = new MessageListenerAdapter(ListenerMethod.getNewInstance(serviceAPI,serviceImpl));
        adapter.setMessageConverter(null);
        adapter.setMandatoryPublish(false);

        simpleMessageListenerContainer = new SimpleMessageListenerContainer();

        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
        simpleMessageListenerContainer.setQueueNames(getRequestQueueName(serviceAPI));
        simpleMessageListenerContainer.setMessageListener(adapter);

        if (this.concurrnetCusomer > 0){
            simpleMessageListenerContainer.setConcurrentConsumers(concurrnetCusomer);
        }

        simpleMessageListenerContainer.start();
    }

    public void afterPropertiesSet()throws Exception{
        if (connectionFactory == null){
            throw new IllegalArgumentException(" Property connection is need ");
        }
        this.admin = new RabbitAdmin(connectionFactory);
        this.run();
    }

    @Override
    public void destroy() throws Exception {
        simpleMessageListenerContainer.destroy();
    }
}
