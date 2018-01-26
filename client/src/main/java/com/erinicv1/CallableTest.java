package com.erinicv1;

import com.erinicv1.service.Service;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallableTest {

    private static final Integer THREAD_NUM = 10;

    private static final Integer REQUEST_NUM = 100;

    private List<Callable<String>> callables = new ArrayList<>(THREAD_NUM);

    public static void main(String[] args) {
        GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext("classpath:applicationContext-client.xml");
        Service service = (Service)applicationContext.getBean("myService");
        new CallableTest().echo(service);
    }


    public void echo(Service service){
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < THREAD_NUM; i++){
           callables.add(new Callable<String>() {
               @Override
               public String call() throws Exception {
                   for (int var2 = 0; var2 < REQUEST_NUM; var2++){
                       String result = service.echo(String.valueOf(var2));
                       if (!result.equals(var2 + " hello")){
                           System.out.println("error : " + result);
                       }
                   }
                   return "done";
               }
           });
        }

        try {
            executorService.invokeAll(callables);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        long timeCost = (System.currentTimeMillis() - startTime);
        System.out.printf("Sync call total-time-cost:%sms, req/s = %s \n",timeCost,((double)(THREAD_NUM * REQUEST_NUM)) / timeCost * 1000);
        executorService.shutdown();
    }
}
