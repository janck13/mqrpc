package com.erinicv1.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

/**
 * Created by Administrator on 2017/4/27 0027.
 */
public class MQClientProxyFactoryBean extends MQClientProxyFactory implements FactoryBean<Object> {

    private final static Logger logger = LoggerFactory.getLogger(MQClientProxyFactoryBean.class);

    @Override
    public void afterPropertiesSet(){
        super.afterPropertiesSet();
        if (serviceInterface == null || !serviceInterface.isInterface()){
            throw new IllegalArgumentException("'serviceInterface' is null or is not an interface");
        }
    }

    @Override
    public Object getObject() throws Exception {
        logger.debug("建立RPC客户端代理接口[{}]。" + serviceInterface.getCanonicalName());
        MQClientProxy mqClientProxy = new MQClientProxy(this);
        return Proxy.newProxyInstance(serviceInterface.getClassLoader(),new Class[]{this.serviceInterface},mqClientProxy);
    }

    @Override
    public Class<?> getObjectType() {
        return this.serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
