package com.gupaoedu.pattern.lazy;


/**
 * @author tzw
 * @Description: 简单的 懒汉式 Demo
 * @date 2019-03-19 23:39
 */
public class LazySimpleSingleton {

    private LazySimpleSingleton(){}

    private static LazySimpleSingleton  lazySimpleSingleton ;


    public static  LazySimpleSingleton getInstance(){
        if(null==lazySimpleSingleton){
            return lazySimpleSingleton = new LazySimpleSingleton();
        }
        return lazySimpleSingleton;
    }

}
