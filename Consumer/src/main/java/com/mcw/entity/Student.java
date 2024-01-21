package com.mcw.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 缪长蔚
 * @create 2024/1/1
 * @desc
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {

    private long id;

    private String name;

    private  int age;
}
