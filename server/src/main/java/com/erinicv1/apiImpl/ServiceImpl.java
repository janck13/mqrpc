package com.erinicv1.apiImpl;

import com.erinicv1.annotation.RpcService;
import com.erinicv1.service.Service;
import com.erinicv1.domain.Student;

import java.time.LocalDateTime;

/**
 * Created by Administrator on 2017/5/2 0002.
 */
@RpcService(Service.class)
public class ServiceImpl  {

    public String echo(String message) {
        System.out.println(LocalDateTime.now().toLocalTime() + " ===> Service.echo 被调用了");
        return message + " hello";
    }

    public Student getStudent(Student student) {
        System.out.println(LocalDateTime.now().toLocalTime() + " ===> Service.getStudent 被调用了");
        return new Student("xl",18,true);
    }
}
