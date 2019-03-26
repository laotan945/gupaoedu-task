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
    public void index(HttpServletRequest request ,
                      HttpServletResponse response,
                      @MyRequestParam("nameName") String nameName){

        String result = demoService.selectOne(nameName);
        try {
            // 中文乱码处理
            request.setCharacterEncoding("UTF-8");
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @MyRequestMapping("/add")
    public void add(HttpServletRequest request, HttpServletResponse response,
                    @MyRequestParam("a") Integer a,
                    @MyRequestParam("b") Integer b){
        try {

            response.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






}
