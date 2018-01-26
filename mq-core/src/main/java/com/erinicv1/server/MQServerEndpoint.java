package com.erinicv1.server;

import com.erinicv1.annotation.RpcService;
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/27 0027.
 */
public class MQServerEndpoint implements InitializingBean,DisposableBean,ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(MQServerEndpoint.class);

    private static final Integer DEFAULT_CONSUMERS = 50;
    private ConnectionFactory connectionFactory;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    private Map<String,Object> handlerMap = new HashMap<>();

    private List<String> queueNames;

    private AmqpAdmin admin;

    private int concurrentConsumers;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String,Object> map = applicationContext.getBeansWithAnnotation(RpcService.class);
        Map<String,Object> handlerMap = new HashMap<>();
        List<String> queueNames = new ArrayList<>();
        for (Object bean : map.values()){
            String name = (bean.getClass().getAnnotation(RpcService.class)).value().getName();
            handlerMap.put(name,bean);
            queueNames.add(name.substring(name.lastIndexOf(".") + 1));
        }
        this.handlerMap = handlerMap;
        this.queueNames = queueNames;
    }


    public MQServerEndpoint(){
        concurrentConsumers = DEFAULT_CONSUMERS;
    }


    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }




    private void createQueue(AmqpAdmin amqpAdmin,String name){
        Queue queue = new Queue(name,false,false,false);
        amqpAdmin.declareQueue(queue);
    }

    public void run(){
        logger.debug(" start create queues of size : " + queueNames.size() );
        //添加监听
        for (String queueName : queueNames){
            connectionFactory.addConnectionListener(new ConnectionListener() {
                @Override
                public void onCreate(Connection connection) {
                    createQueue(admin,queueName);
                }

                @Override
                public void onClose(Connection connection) {

                }
            });
        }

        MessageListenerAdapter adapter = new MessageListenerAdapter(new ListenerMethod(handlerMap));
        adapter.setMessageConverter(null);
        adapter.setMandatoryPublish(false);

        simpleMessageListenerContainer = new SimpleMessageListenerContainer();

        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
        simpleMessageListenerContainer.setQueueNames(queueNames.toArray(new String[queueNames.size()]));
        simpleMessageListenerContainer.setMessageListener(adapter);

        if (this.concurrentConsumers > 0){
            simpleMessageListenerContainer.setConcurrentConsumers(concurrentConsumers);
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


    public int getConcurrentConsumers() {
        return concurrentConsumers;
    }

    public void setConcurrentConsumers(int concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
    }
}
