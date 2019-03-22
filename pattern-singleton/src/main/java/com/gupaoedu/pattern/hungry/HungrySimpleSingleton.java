package com.gupaoedu.pattern.hungry;

/**
 * @author tzw
 * @Description: 简单的 饿汉式 Demo
 * @date 2019-03-22 11:13
 */
public class HungrySimpleSingleton {

    private HungrySimpleSingleton(){}

    private static  final HungrySimpleSingleton hungrySimpleSingleton = new HungrySimpleSingleton();

    public static  HungrySimpleSingleton getInstance(){
        return  hungrySimpleSingleton;
    }
}
