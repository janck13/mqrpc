package com.erinicv1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by Administrator on 2017/5/2 0002.
 */
@SpringBootApplication
@ImportResource("applicationContext-server.xml")
public class MQPRCStart1 {

    public static void main(String[] args){
        SpringApplication.run(MQPRCStart1.class,args);
    }
}
