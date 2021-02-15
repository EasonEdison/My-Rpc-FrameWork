package kiruto;

import kiruto.annotation.RpcScan;
import kiruto.transport.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = {"kiruto"})
public class NettyServerTest {
    public static void main(String[] args) {
        // NettyRpcServer nettyRpcServer = new NettyRpcServer();
        // 手动注册一下
        // HelloWorldImpl helloWorld = new HelloWorldImpl();
        // RpcServiceProperties properties = RpcServiceProperties.builder()
            // .group("JoJo").version("1").build();
        // nettyRpcServer.registerService(helloWorld, properties);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
            NettyServerTest.class);

        NettyRpcServer nettyRpcServer = (NettyRpcServer) context.getBean("nettyRpcServer");
        nettyRpcServer.start();
    }
}
