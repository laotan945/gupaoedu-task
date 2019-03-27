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
@MyRequestMapping("/v1/demo")
public class DemoAction {

    @MyAutowired
    private DemoService demoService;


    @MyRequestMapping("/index")
    public Object index(HttpServletRequest request ,
                      HttpServletResponse response,
                      @MyRequestParam("name") String userName){
        String result = demoService.selectOne(userName);
        return  result;

    }

    @MyRequestMapping("/add")
    public void add(HttpServletRequest request, HttpServletResponse response,
                    @MyRequestParam("a") Integer a,
                    @MyRequestParam("b") Integer b){
        System.out.println((a + "+" + b + "=" + (a + b)));
    }






}
