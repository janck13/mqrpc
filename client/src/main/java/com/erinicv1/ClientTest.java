package com.erinicv1;

import com.erinicv1.api.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017/5/2 0002.
 */
@Component
public class ClientTest {

    @Autowired
    Service service;

    @PostConstruct
    public void initRun(){
        System.err.println(service.echo("erinicv1"));
    }

    public static void main(String[] args){
        GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext("classpath:applicationContext-client.xml");
        Service service = (Service)applicationContext.getBean("myService");
        new ClientTest().echo(service);
    }

    public void echo(Service service){
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        for (int i = 0; i < 30; i++){
            executorService.submit(new Task(service));
        }
    }


    private class Task implements Callable{
        private Service service;

        public Task(Service service){
            this.service = service;
        }
        @Override
        public Object call() throws Exception {
            for (int i = 0; i < 100000; i++){
                System.out.println("serviceEcho当前线程："+Thread.currentThread().getName()+"| 线程任务数"+i+"| 输出："+service.echo("Hello AMQP!"));
                System.out.println("serviceStudent当前线程："+Thread.currentThread().getName()+"| 线程任务数"+i+"| 输出："+service.getStudent(null).getName());
            }
            return null;
        }
    }
}
