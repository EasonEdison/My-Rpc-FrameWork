package doraemon.impl;

import doraemon.service.HelloWorld;

public class HelloWorldImpl implements HelloWorld {
    @Override
    public String hello(String msg) {
        System.out.println("服务端输出结果: " + msg);
        return "返回给客户端：" + msg;
    }
}
