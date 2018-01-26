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


    /**
     * 返回代理类
     * @return
     * @throws Exception
     */
    @Override
    public Object getObject() throws Exception {
//        logger.debug("建立RPC客户端代理接口[{}]。" + serviceInterface.getCanonicalName());
        MQClientProxy mqClientProxy = new MQClientProxy(this);
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),classList.toArray(new Class[classList.size()]),mqClientProxy);
    }

    @Override
    public Class<?> getObjectType() {
        return Object.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
