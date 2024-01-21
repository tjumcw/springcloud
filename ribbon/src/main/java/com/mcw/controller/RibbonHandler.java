package com.mcw.controller;

import com.mcw.entity.Student;
import javafx.print.Collation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * @author 缪长蔚
 * @create 2024/1/1
 * @desc
 */

@RestController
@RequestMapping("/ribbon")
public class RibbonHandler {

    @Resource
    private RestTemplate restTemplate;

    public Collection<Student> findAll() {
        return restTemplate.getForObject("http://provider/student/findAll", Collection.class);
    }
}
