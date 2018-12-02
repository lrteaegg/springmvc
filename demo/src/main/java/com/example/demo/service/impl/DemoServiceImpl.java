package com.example.demo.service.impl;

import com.example.demo.annotation.LouieService;
import com.example.demo.service.DemoService;

/**
 * @创建人 幕风
 * @创建时间 2018/12/2
 * @描述
 */
@LouieService("DemoServiceImpl")
public class DemoServiceImpl implements DemoService {
    @Override
    public String query(String name, String age) {
        return "name=="+name+"; age=="+age;
    }
}
