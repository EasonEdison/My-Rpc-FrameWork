package kiruto.transport.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ClientChannelProvider {

    private final Map<String, Channel> channelMap;

    public ClientChannelProvider() {
        this.channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        Channel channel = channelMap.get(key);
        if (channel != null && channel.isActive()) {
            return channel;
        } else {
            log.info("获取channel失败");
            channelMap.remove(key);
            return null;
        }
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }
}
