package com.gupaoedu.annotation;

import java.lang.annotation.*;

/**
 * @author tzw
 * @Description: service 注解
 * @date 2019-03-25 19:04
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyService {

    String value() default "";

}
