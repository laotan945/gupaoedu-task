package com.gupaoedu.action;

import com.gupaoedu.annotation.MyAutowired;
import com.gupaoedu.annotation.MyController;
import com.gupaoedu.annotation.MyRequestMapping;
import com.gupaoedu.annotation.MyRequestParam;
import com.gupaoedu.service.DemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author tzw
 * @Description: 测试调用类
 * @date 2019-03-25 17:29
 */
@MyController
@MyRequestMapping("/v2/demo")
public class DemoActionV2 {

    @MyAutowired
    private DemoService demoService;


    @MyRequestMapping("/index")
    public Object index(HttpServletRequest request ,
                      HttpServletResponse response,
                      @MyRequestParam("userName") String userName){
       return demoService.selectOne(userName);
    }


    @MyRequestMapping("/add")
    public Object add(HttpServletRequest request, HttpServletResponse response,
                    @MyRequestParam("a") double a,
                    @MyRequestParam("b") double b){
            return (a + "+" + b + "=" + (a + b));
    }


}
