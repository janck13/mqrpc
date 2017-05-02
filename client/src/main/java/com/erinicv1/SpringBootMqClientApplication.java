package com.erinicv1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by Administrator on 2017/4/28 0028.
 */
@SpringBootApplication
@ImportResource("applicationContext-client.xml")
public class SpringBootMqClientApplication {
    public static void main(String[] args){
        SpringApplication.run(SpringBootMqClientApplication.class,args);
    }
}
