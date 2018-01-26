package com.erinicv1.client;

import com.erinicv1.annotation.ServiceFind;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    private String queuePrefix; //队列名前缀

    private long timeOut = -1; // 响应时间，大于0才有效

    private boolean compress = true;

    protected List<Class<?>> classList = new ArrayList<>();




    /**
     *  创建一个请求队列和设置exchange处理机制为Direct：单播-完全匹配
     * @param amqpAdmin
     * @param queueName 队列的名字
     * @param exchangeName exchange的名字
     */
    public void createRequestsQueue(AmqpAdmin amqpAdmin,String queueName,String exchangeName){
        Queue queue = new Queue(queueName,false,false,false);
        amqpAdmin.declareQueue(queue);
        DirectExchange exchange = new DirectExchange(exchangeName,false,false);
        amqpAdmin.declareExchange(exchange);
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(queueName);
        amqpAdmin.declareBinding(binding);

    }


    /**
     *  初始化
     */
    public void initilizingQueue(){
        // 使用AtomicBoolean 的CAS方法，保证只初始化一次
        if (!initializing.compareAndSet(false,true)){
            return ;
        }
        try {
            if (admin == null) {
                admin = new RabbitAdmin(connectionFactory);
            }
            Reflections reflections = new Reflections("com.erinicv1.service");
            Set<Class<?>> set = reflections.getTypesAnnotatedWith(ServiceFind.class);
            List<Class<?>> list = new ArrayList<>();
            for (Class c : set){
                if (c.isInterface()){
                    createRequestsQueue(admin,c.getSimpleName(),c.getSimpleName());
                    list.add(c);
                }
            }
            this.classList = list;
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


}
