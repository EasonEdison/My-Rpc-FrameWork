package kiruto.registry.zk;

import kiruto.loadbalance.LoadBalance;
import kiruto.loadbalance.loadstrategy.RandomLoadBalance;
import kiruto.registry.ServiceDiscovery;
import kiruto.registry.zk.utils.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import naruto.enums.RpcErrorMessageEnum;
import naruto.exception.RpcException;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZKServiceDiscovery implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public ZKServiceDiscovery() {
        this.loadBalance = new RandomLoadBalance();
    }

    @Override
    public InetSocketAddress lookupService(String rpcServiceName) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (serviceList == null || serviceList.size() == 0) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        String serviceAddress = loadBalance.selectServiceAddress(serviceList, rpcServiceName);
        // log.info("选中的服务器地址为: {}", serviceAddress);
        String[] address = serviceAddress.split(":");
        String host = address[0];
        int port = Integer.parseInt(address[1]);
        return new InetSocketAddress(host,port);
    }
}
