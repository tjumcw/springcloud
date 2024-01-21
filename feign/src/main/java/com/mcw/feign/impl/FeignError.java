package com.mcw.feign.impl;

import com.mcw.entity.Student;
import com.mcw.feign.FeignProviderClient;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author 缪长蔚
 * @create 2024/1/2
 * @desc
 */

@Component
public class FeignError implements FeignProviderClient {

    @Override
    public Collection<Student> findAll() {
        return null;
    }
}
