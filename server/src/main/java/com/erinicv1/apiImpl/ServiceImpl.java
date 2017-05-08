package com.erinicv1.apiImpl;

import com.erinicv1.api.Service;
import com.erinicv1.pojo.Student;

import java.time.LocalDateTime;

/**
 * Created by Administrator on 2017/5/2 0002.
 */
public class ServiceImpl implements Service {

    @Override
    public String echo(String message) {
        System.out.println(LocalDateTime.now().toLocalTime() + " ===> Service.echo 被调用了");
        return message + " hello";
    }

    @Override
    public Student getStudent(Student student) {
        System.out.println(LocalDateTime.now().toLocalTime() + " ===> Service.getStudent 被调用了");
        return new Student("xl",18,true);
    }
}
