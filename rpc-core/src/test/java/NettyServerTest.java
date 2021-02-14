import doraemon.impl.HelloWorldImpl;
import kiruto.entity.RpcServiceProperties;
import kiruto.transport.server.NettyRpcServer;

public class NettyServerTest {
    public static void main(String[] args) {
        NettyRpcServer nettyRpcServer = new NettyRpcServer();
        // 手动注册一下
        HelloWorldImpl helloWorld = new HelloWorldImpl();
        RpcServiceProperties properties = RpcServiceProperties.builder()
            .group("JoJo").version("1").build();
        nettyRpcServer.registerService(helloWorld, properties);
        nettyRpcServer.start();
    }
}
