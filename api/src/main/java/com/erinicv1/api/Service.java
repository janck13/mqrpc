package com.erinicv1.api;

import com.erinicv1.pojo.Student;

/**
 * Created by Administrator on 2017/4/28 0028.
 */
public interface Service {

    String echo(String message);

    Student getStudent(Student student);
}
