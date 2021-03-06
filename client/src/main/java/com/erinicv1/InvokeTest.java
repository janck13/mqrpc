package com.erinicv1;

import com.erinicv1.service.FabResult;
import com.erinicv1.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.util.Assert;

public class InvokeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvokeTest.class);

    public static void main(String[] args){
        GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext("classpath:applicationContext-client.xml");
        Service service = (Service)applicationContext.getBean("myService");
        String result = service.echo("star ");
        Assert.isTrue(result.equals("star  hello"));
        FabResult fabResult = (FabResult)applicationContext.getBean("myService");
        Integer re = fabResult.get(3);
        Assert.isTrue(re == 2);
        System.out.println("done");
    }
}
