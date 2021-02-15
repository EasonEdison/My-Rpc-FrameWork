package kiruto.impl;

import doraemon.service.HelloWorld;
import kiruto.annotation.RpcService;

@RpcService(version = "1", group = "JoJo")
public class HelloWorldImpl implements HelloWorld {
    @Override
    public String hello(String msg) {
        System.out.println("服务端输出结果: " + msg);
        return "返回给客户端：" + msg;
    }
}
