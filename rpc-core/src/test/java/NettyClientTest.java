import doraemon.service.HelloWorld;
import kiruto.entity.RpcServiceProperties;
import kiruto.proxy.RpcClientProxy;
import kiruto.transport.client.NettyRpcClient;

public class NettyClientTest {
    public static void main(String[] args) {
        NettyRpcClient nettyRpcClient = new NettyRpcClient();
        RpcServiceProperties properties = RpcServiceProperties.builder()
            .group("JoJo").version("1").build();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(nettyRpcClient, properties);
        HelloWorld helloWorld = rpcClientProxy.getProxyInstance(HelloWorld.class);
        System.out.println("调用前");
        String hello = helloWorld.hello("ko no dio da !");
        System.out.println("调用后：" + hello);

    }
}
