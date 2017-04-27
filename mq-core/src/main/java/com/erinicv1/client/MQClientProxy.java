package com.erinicv1.client;

import com.erinicv1.util.HessianSerializerUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeoutException;

/**
 * Created by Administrator on 2017/4/27 0027.
 */
public class MQClientProxy implements InvocationHandler {

    private MQClientProxyFactory factory;

    public MQClientProxy(MQClientProxyFactory factory){
        this.factory = factory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (ReflectionUtils.isEqualsMethod(method)){
            Object value = args[0];
            if (value == null || !Proxy.isProxyClass(value.getClass())){
                return Boolean.FALSE;
            }
            MQClientProxy handler = (MQClientProxy)Proxy.getInvocationHandler(value);
            return this.factory.equals(handler.factory);
        }else if (ReflectionUtils.isHashCodeMethod(method)){
            factory.hashCode();
        }else if (ReflectionUtils.isToStringMethod(method)){
            return "[HessianProxy " + proxy.getClass() + "]";
        }

        RabbitTemplate rabbitTemplate = factory.getRabbitTemplate();
        byte[] playOut = HessianSerializerUtil.clientRequestBody(args,method,factory.isCompress());

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentEncoding("deflate");
        messageProperties.setContentType("x-application/hessian");

        Message message = new Message(playOut,messageProperties);
        Message response = rabbitTemplate.sendAndReceive(
                factory.getRequestQueueName(),
                factory.getRequestExchangeName(),
                message);

        if (response == null){
            throw new TimeoutException("RPC服务响应超时");
        }

        MessageProperties properties = message.getMessageProperties();
        boolean isCompress = "deflate".equals(properties.getContentEncoding());

        return HessianSerializerUtil.clienResponseBody(message.getBody(),method,isCompress);
    }


}
