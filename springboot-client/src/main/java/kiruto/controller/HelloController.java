package kiruto.controller;

import doraemon.service.HelloWorld;
import kiruto.annotation.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class HelloController {

    @RpcReference(version = "1", group = "JoJo")
    private HelloWorld helloWorld;

    public void test() {
        String hello = helloWorld.hello("ko no dio da !");
        System.out.println(hello);
    }
}
