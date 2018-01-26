package com.erinicv1.server;

import com.erinicv1.domain.RpcRequest;
import com.erinicv1.domain.RpcResponse;
import com.erinicv1.util.HessianSerializerUtil;
import com.erinicv1.utils.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/26 0026.
 */
public class ListenerMethod {

    private Logger logger = LoggerFactory.getLogger(ListenerMethod.class);


    private Map<String,Object> handlerMap = new HashMap<>();

    public ListenerMethod(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    /**
     *  handleMessage 是 MessageListenerAdapter的默认消息处理器
     * @param message
     * @return
     */
    public Message handleMessage(Message message){
        RpcRequest rpcRequest = SerializationUtil.deserialize(message.getBody(), RpcRequest.class);
        String requestName = rpcRequest.getServiceName();
        Object o = handlerMap.get(requestName);
        RpcResponse rpcResponse = new RpcResponse();
        try {
            Method method = o.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamTypes());
            method.setAccessible(true);
            Object result = method.invoke(o,rpcRequest.getParams());
            rpcResponse.setResult(result);
        }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
            rpcResponse.setError(e.getMessage());
            logger.error(" process request has an error : {}",e.getMessage());
        }


        byte[] array = SerializationUtil.serialize(rpcResponse);

        logger.debug("message receive : " + message);

        MessageProperties properties = new MessageProperties();
        properties.setContentType("x-application/protostuff");

        return new Message(array,properties);
    }
}
