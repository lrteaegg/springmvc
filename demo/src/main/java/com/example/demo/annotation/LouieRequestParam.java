package com.example.demo.annotation;

import java.lang.annotation.*;

/**
 * @创建人 幕风
 * @创建时间 2018/12/2
 * @描述
 */
@Target({ElementType.PARAMETER}) //类的成员对象上用了注解 TYPE是用在类上
@Retention(RetentionPolicy.RUNTIME) // 运行时实例化
@Documented // 加载到javadoc中，注解还是能继承
public @interface LouieRequestParam {
    String value() default  ""; /// 注解后能跟值
}
