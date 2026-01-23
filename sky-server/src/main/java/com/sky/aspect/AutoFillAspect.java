package com.sky.aspect;

/*
    *自定义切面,实现公共字段自动填充逻辑
 */

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.mapping.model.InternalEntityInstantiatorFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect     //告诉java这是个切面
@Component   //自动注入有关,会交给spring管理
@Slf4j          //lombok有关,方便日志操作的,比如我们的log.info,大多数类都能标这个注解

//一个点,log.info和system.out.println都是输出,一个用来输出专业的日志,一个只是用来调试的,懂了吧.所以我们现在输出一般都是用log.info,之前学习用的system.out.println调试


public class AutoFillAspect {

    /*
    切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) &&  @annotation(com.sky.annotation.AutoFill)")//锁定mapper包所有方法中加了autofill注解的方法
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){         //joinpoint用来获取目标方法的参数
        log.info("开始进行公共字段填充");

        //1.获取当前拦截mapper方法的数据库操作类型

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();     //奇怪的很,这个getsignature方法的代码不在我们的源码里面,是aop运行的时候动态生成的
        //这玩意不需要具体掌握,知道能用这玩意拿到方法签名,然后拿到方法名方法对象就行
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //2.获取当前被拦截方法的参数--实体对象
        Object[] args = joinPoint.getArgs(); //获得多个参数,我们人为将employee放到第一位就好

        if(args == null || args.length == 0){
            return;

        }//保险的判断

        Object entity = args[0];//别用employee接受,因为以后不一定都是employee
        //3.准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //4.根据不同操作类型进行赋值,通过反射进行赋值
        if(operationType == OperationType.INSERT){
            //为4个公共字段赋值
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //上面几个方法的名字给定义为了常量,可以防止敲错.
                //通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);//动态为entity调用setCreateTime方法,并传入now参数.
                //前面的entity是给谁调用,后面的是传什么参数
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }else if (operationType == OperationType.UPDATE){
            //为两个公共字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }




    }


}
