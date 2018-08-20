package com.erinicv1.client;

import com.erinicv1.domain.Person;
import com.erinicv1.domain.RpcRequest;
import com.erinicv1.domain.RpcResponse;
import com.erinicv1.util.HessianSerializerUtil;
import com.erinicv1.utils.SerializationUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        //判断
        System.out.println("start");
        System.out.println("end");
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
            return "[protostuff " + proxy.getClass() + "]";
        }
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setServiceName(method.getDeclaringClass().getName());
        rpcRequest.setParamTypes(method.getParameterTypes());
        rpcRequest.setParams(args);
        byte[] playOut = SerializationUtil.serialize(rpcRequest);
        RabbitTemplate rabbitTemplate = factory.getRabbitTemplate();
//
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType("x-application/protostuff");

        Message message = new Message(playOut,messageProperties);
        String name = method.getDeclaringClass().getSimpleName();
        String ex =  factory.getClass().getName();
        Message response = rabbitTemplate.sendAndReceive(
                name,
               name,
                message);

        if (response == null){
            throw new TimeoutException("RPC服务响应超时");
        }

        RpcResponse rpcResponse = SerializationUtil.deserialize(response.getBody(),RpcResponse.class);

        if (rpcResponse.getError() != null && !"".equals(rpcResponse.getError())){
            throw new RuntimeException(rpcResponse.getError());
        }

        return rpcResponse.getResult();
    }


    public static void main(String[] args) {
        int[] array = {1,2,3,4};
        change(array);
        System.out.println(Arrays.toString(array));
        Person person = new Person();
        person.setAge(2);
        change2(person);
        System.out.println(person.getAge());
        List<Person>  list = new ArrayList<>();
        Person person2 = new Person();
        list.add(person2);
        person2.setAge(3);
        change(list);
        System.out.println(list.get(0).getAge());
    }

    private static void change(int[] array){
        int[] nums = {1,2,1};
        array = nums;
        System.out.println(Arrays.toString(array) + ">>>>");
    }

    private static void change2(Person person){
        Person person1 = new Person();
        person1.setAge(12);
        person = person1;
        System.out.println(person.getAge() + ">>>");
    }

    private static void change(List<Person> list){
        Person person1 = new Person();
        person1.setAge(12);
        Person person = list.get(0);
        person = person1;
        System.out.println(person.getAge() + ">>>>");
    }
}
