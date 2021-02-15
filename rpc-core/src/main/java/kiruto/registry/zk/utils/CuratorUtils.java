package kiruto.registry.zk.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CuratorUtils {

    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc-sec-kill";

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "192.168.153.128:2181,192.168.153.128:2182,192.168.153.128:2183";

    private CuratorUtils() {
    }


    /**
     * 创建客户端.
     */
    public static CuratorFramework getZkClient() {
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // 设置重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
            .connectString(DEFAULT_ZOOKEEPER_ADDRESS)
            .retryPolicy(retryPolicy)
            .build();
        zkClient.start();
        return zkClient;
    }


    /**
     * 创建持久化节点，创建前检查是否已经存在.
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("节点 {} 已经存在！", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("节点 {} 创建成功！", path);
            }
            // 可能原本没有、也可能存在路径但是集合中没有
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("节点 {} 创建失败！", path);
            e.printStackTrace();
        }
    }

    /**
     * 获取路径的子节点.
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            // 万一没找到呢，直接存null？
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            // 监视器
            registerWatcher(servicePath, zkClient);
        } catch (Exception e) {
            log.error("查找 {} 的子节点失败！", rpcServiceName);
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 监听子节点的变化，如果发生变化就更新内存中的值.
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework zkClient, PathChildrenCacheEvent pathChildrenCacheEvent)
                throws Exception {
                // 发生变化，就更新
                List<String> serviceAddresses = zkClient.getChildren().forPath(servicePath);
                SERVICE_ADDRESS_MAP.put(servicePath, serviceAddresses);
            }
        });
        pathChildrenCache.start();
    }

    /**
     * 清空某服务器的所有服务.
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        REGISTERED_PATH_SET.stream().parallel().forEach(path -> {
            try {
                // 通过查后缀的方法来过滤删除
                if (path.endsWith(inetSocketAddress.toString())) {
                    zkClient.delete().forPath(path);
                }
            } catch (Exception e) {
                log.error("删除路径 {} 失败！", path);
                e.printStackTrace();
            }
        });
        log.info("指定服务器 {} 的路径删除完成！", inetSocketAddress.toString());
    }
}
