package kiruto.rpcserver;

import kiruto.annotation.RpcScan;
import kiruto.transport.server.NettyRpcServer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("kiruto.rpcserver.mapper")
@RpcScan(basePackage = {"kiruto"})
public class RpcServerApplication {

    public static void main(String[] args) {
        // AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
        //     RpcServerApplication.class);
        //
        // NettyRpcServer nettyRpcServer = (NettyRpcServer) context.getBean("nettyRpcServer");
        // nettyRpcServer.start();
        SpringApplication.run(RpcServerApplication.class, args);
        NettyRpcServer nettyRpcServer = new NettyRpcServer();
        nettyRpcServer.start();
    }

}
