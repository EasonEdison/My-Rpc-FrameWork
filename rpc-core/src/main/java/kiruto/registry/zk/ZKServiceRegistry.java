package kiruto.registry.zk;

import kiruto.registry.ServiceRegistry;
import kiruto.registry.zk.utils.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

@Slf4j
public class ZKServiceRegistry implements ServiceRegistry {

    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName
            + inetSocketAddress.toString();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
