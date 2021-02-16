package kiruto.hook;

import kiruto.registry.zk.utils.CuratorUtils;
import kiruto.transport.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Slf4j
public class CustomShutdownHook {

    public static void clearAll() {
        log.info("关闭服务后会清楚所有服务");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(
                    InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException e) {
                log.error("清除服务出现异常！");
                e.printStackTrace();
            }
        }));
    }
}
