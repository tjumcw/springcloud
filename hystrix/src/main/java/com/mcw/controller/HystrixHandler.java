package com.mcw.controller;

import com.mcw.entity.Student;
import com.mcw.feign.FeignProviderClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * @author 缪长蔚
 * @create 2024/1/2
 * @desc
 */

@RestController
@RequestMapping("/hystrix")
public class HystrixHandler {

    @Resource
    private FeignProviderClient feignProviderClient;

    @GetMapping("/findAll")
    public Collection<Student> findAll() {
        return feignProviderClient.findAll();
    }
}
