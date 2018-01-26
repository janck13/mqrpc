package com.erinicv1.service;


import com.erinicv1.annotation.ServiceFind;
import com.erinicv1.domain.Student;

/**
 * Created by Administrator on 2017/4/28 0028.
 */
@ServiceFind
public interface Service {

    String echo(String message);

    Student getStudent(Student student);
}
