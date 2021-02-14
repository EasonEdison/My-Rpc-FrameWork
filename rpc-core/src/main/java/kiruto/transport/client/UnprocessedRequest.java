package kiruto.transport.client;

import kiruto.entity.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存放还没有被服务器端处理的请求.
 */
public class UnprocessedRequest {

    // 有为何是呢嘛规范吗
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_FUTURE =
        new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_FUTURE.put(requestId, future);
    }


    public void complete(RpcResponse<Object> response) {
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_FUTURE
            .get(response.getRequestId());
        if (future != null) {
            future.complete(response);
        } else {
            throw new IllegalStateException("没有对应的future");
        }
    }

    public void remove(String requestId) {
        UNPROCESSED_FUTURE.remove(requestId);
    }

}
