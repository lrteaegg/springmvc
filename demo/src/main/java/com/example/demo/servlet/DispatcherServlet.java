package com.example.demo.servlet;


import com.example.demo.annotation.*;
import com.example.demo.controller.DemoController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @创建人 幕风
 * @创建时间 2018/12/2
 * @描述
 */
public class DispatcherServlet extends HttpServlet {

    private List<String> classNames = new ArrayList<String>();
    Map<String, Object> beans = new HashMap<String, Object>();
    Map<String, Object> handlerMap = new HashMap<String, Object>();

    public void init(ServletConfig config){
        System.out.println("*****************");
        // IOC
        // 把所有的bean加载---扫描所有的class文件
        scanPackage("com.example");
        try {
            doInstance(); // 根据全类名创建bean
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        doIoc(); // 根据bewan进行依赖注入

        buildUrlMapping(); // /demo/query --> 建立关系
    }

    private void buildUrlMapping() {
        if(beans.entrySet().size() <= 0){
            System.out.println("没有类的实例化。。。");
            return;
        }
        for (Map.Entry<String, Object> entry : beans.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(LouieController.class)){
                LouieRequestMapping requestMapping = clazz.getAnnotation(LouieRequestMapping.class);
                String classPath = requestMapping.value();
                Method[] methods = clazz.getMethods();
                for (Method method: methods){
                    if (method.isAnnotationPresent(LouieRequestMapping.class)){
                        LouieRequestMapping methodMapping =
                                method.getAnnotation(LouieRequestMapping.class);
                        String methodPath = methodMapping.value();
                        handlerMap.put(classPath+methodPath, method);

                    }else{
                        continue;
                    }
                }
            }else {
                continue;
            }
        }

    }

    // 把service注入到控制层
    private void doIoc() {
        if (beans.entrySet().size() <= 0){
            System.out.println("没有一个实例化的类");
        }
        // 把Map里所有的实例化遍历出来
        for (Map.Entry<String, Object> entry : beans.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(LouieController.class)){
                Field[] fields = clazz.getDeclaredFields();
                for (Field field: fields){
                    if (field.isAnnotationPresent(LouieAutowired.class)){
                        LouieAutowired auto = field.getAnnotation(LouieAutowired.class);
                        String key = auto.value();  // demoService
                        field.setAccessible(true);
                        try {
                            field.set(instance, beans.get(key));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }else {
                        continue;
                    }
                }
            }else {
                continue;
            }

        }
    }

    private void scanPackage(String basePackage){
        URL url = this.getClass().getClassLoader()
                .getResource("/"+basePackage.replaceAll("\\.","/"));
        String fileStr = url.getFile();
        File file = new File(fileStr);

        String[] filesStr = file.list(); // demo

        for (String path: filesStr){
            File filePath = new File(fileStr+path); // com.example.demo...
            if (filePath.isDirectory()){
                scanPackage(basePackage+"."+path);
            } else {
                // 加入LIST中
                classNames.add(basePackage+"."+ filePath.getName()); // 拿到文件的目录
            }
        }
    }
    // 根据扫描的类实例化
    private void doInstance() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (classNames.size()<=0){
            System.out.println("扫描失败");
            return;
        }else {
            for (String className:classNames){
                String cn = className.replace(".class","");
                Class<?> clazz = Class.forName(cn); // 实例化对象
                if (clazz.isAnnotationPresent(LouieController.class)){
                    Object instance = clazz.newInstance(); // 控制实例化对象
                    LouieRequestMapping requestMapping =
                            clazz.getAnnotation(LouieRequestMapping.class);
                    String rmvalue = requestMapping.value();
                    beans.put(rmvalue, instance);
                }else if(clazz.isAnnotationPresent(LouieService.class)){
                    LouieService service = clazz.getAnnotation(LouieService.class);
                    Object instance = clazz.newInstance();
                    beans.put(service.value(), instance);
                }else {
                    continue;
                }
            }
        }
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 获取请求路径 /demo/query
        String uri = req.getRequestURI();
        String context = req.getContextPath();
        String path = uri.replace(context, "");

        Method method = (Method) handlerMap.get(path);

        // 根据key = /demo到map去拿
        DemoController instance = (DemoController) beans.get("/"+path.split("/")[1]); //

        Object arg[] = hand(req, resp, method);
        try {
            method.invoke(instance, arg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Object[] hand(HttpServletRequest req, HttpServletResponse resp, Method method) {
        Class<?>[] paramClazzs = method.getParameterTypes();

        Object[] args = new Object[paramClazzs.length];

        int args_i = 0;
        int index = 0;

        for (Class<?> paramClazz : paramClazzs){
            if(ServletRequest.class.isAssignableFrom(paramClazz)){
                args[args_i++] = req;
            }
            if (ServletResponse.class.isAssignableFrom(paramClazz)){
                args[args_i++] = resp;
            }
            Annotation[] paramAns = method.getParameterAnnotations()[index];
            if (paramAns.length > 0){
                for (Annotation paramAn : paramAns){
                    if (LouieRequestParam.class.isAssignableFrom(paramAn.getClass())){
                        LouieRequestParam rp = (LouieRequestParam) paramAn;

                        args[args_i++] = req.getParameter(rp.value());
                    }
                }
            }
            index ++;
        }
        return args;
    }
}








