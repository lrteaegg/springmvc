package com.example.demo.controller;

import com.example.demo.annotation.LouieAutowired;
import com.example.demo.annotation.LouieController;
import com.example.demo.annotation.LouieRequestParam;
import com.example.demo.annotation.LouieRequestMapping;
import com.example.demo.service.DemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @创建人 幕风
 * @创建时间 2018/12/2
 * @描述
 */
@LouieController
@LouieRequestMapping("/louie")
public class DemoController {
    @LouieAutowired("DemoServiceImpl") //map.get("key")
    private DemoService demoService; // map.get(key) --> object

    @LouieRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @LouieRequestParam("name") String name,
                      @LouieRequestParam("age") String age) throws IOException {
        PrintWriter pw = response.getWriter();
       String result = demoService.query(name, age);
       pw.write(result);
    }
}
