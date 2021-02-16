package kiruto.web;

import doraemon.service.HelloWorld;
import kiruto.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckill")
// @Component
public class HelloController {

    @RpcReference(version = "1", group = "JoJo")
    private HelloWorld helloWorld;

    @GetMapping("/test")
    public void test() {
        String hello = helloWorld.hello("ko no dio da !");
        System.out.println(hello);
    }
}
