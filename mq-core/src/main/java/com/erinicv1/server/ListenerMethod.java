package com.erinicv1.server;

import com.erinicv1.util.HessianSerializerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

/**
 * Created by Administrator on 2017/4/26 0026.
 */
public class ListenerMethod {

    private Logger logger = LoggerFactory.getLogger(ListenerMethod.class);

    private final static String SPRING_CORRELATION_ID = "spring_reply_correlation";

    private Class serviceApi;

    private Object serviceImpl;

    private ListenerMethod(Class serviceApi,Object serviceImpl){
        this.serviceApi = serviceApi;
        this.serviceImpl = serviceImpl;
    }

    public static ListenerMethod getNewInstance(Class serviceApi,Object serviceImpl){
        return new ListenerMethod(serviceApi,serviceImpl);
    }


    /**
     *  handleMessage 是 MessageListenerAdapter的默认消息处理器
     * @param message
     * @return
     */
    public Message handleMessage(Message message){
        System.out.println("调用-服务名："+serviceImpl.getClass().getName());
        logger.debug("message receive : " + message);


        MessageProperties messageProperties = message.getMessageProperties();
        boolean compressed = "deflate".equals(messageProperties.getContentEncoding());
        byte[] response;
        try{
            response = HessianSerializerUtil.serverResponseBody(message.getBody(),compressed,serviceImpl,serviceApi);

        }catch (Throwable e){
            logger.error(" handel message fail : ",e);
            compressed = false;
            response = HessianSerializerUtil.serverFautl(message.getBody(),e);
        }

        MessageProperties properties = new MessageProperties();
        properties.setContentType("x-application/hessian");
        if (compressed){
            properties.setContentEncoding("deflate");
        }
        properties.setHeader(SPRING_CORRELATION_ID,messageProperties.getHeaders().get(SPRING_CORRELATION_ID));

        return new Message(response,properties);
    }
}
