package com.mcw.feign;

import com.mcw.entity.Student;
import com.mcw.feign.impl.FeignError;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collection;

/**
 * @author 缪长蔚
 * @create 2024/1/2
 * @desc
 */

@FeignClient(value = "provider", fallback = FeignError.class)
public interface FeignProviderClient {

    @GetMapping("/student/findAll")
    Collection<Student> findAll();
}
