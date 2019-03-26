package com.gupaoedu.annotation;

import java.lang.annotation.*;

/**
 * @author tzw
 * @Description: Controller 注解
 * @date 2019-03-25 19:04
 */
@Target({ElementType.TYPE}) // 类，接口（包括注释类型）或枚举声明
@Retention(RetentionPolicy.RUNTIME) // 保留策略：运行时保留
@Documented
public @interface MyController {

}
