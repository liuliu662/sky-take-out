package com.sky.annotation;


import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识公共字段需要自动填充处理
 */
@Target(ElementType.METHOD)       //用来限定这个注解只能用于方法,当然也可以限定为用于别的
@Retention(RetentionPolicy.RUNTIME)   //用来表示该代码运行阶段也有效,方便aop
public @interface AutoFill {
    //表明这个自定义注解接受的参数类型
    OperationType value();
}
