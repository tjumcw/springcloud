package com.mcw.repository;

import com.mcw.entity.Student;

import java.util.Collection;

/**
 * @author 缪长蔚
 * @create 2024/1/1
 * @desc
 */

public interface StudentRepository {

    Collection<Student> findALl();

    Student findById(long id);

    void saveOrUpdate(Student student);

    void deleteById(long id);
}
