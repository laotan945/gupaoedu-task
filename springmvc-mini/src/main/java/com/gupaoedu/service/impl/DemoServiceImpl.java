package com.gupaoedu.service.impl;

import com.gupaoedu.annotation.MyService;
import com.gupaoedu.service.DemoService;

/**
 * @author tzw
 * @Description: Service 测试类
 * @date 2019-03-25 20:30
 */
@MyService
public class DemoServiceImpl implements DemoService {


    @Override
    public String selectOne(String nameName) {
        return "*******测试查询某" + nameName+ "对象*********";
    }


}
