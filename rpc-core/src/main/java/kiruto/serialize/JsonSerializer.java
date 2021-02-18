package kiruto.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kiruto.entity.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import naruto.enums.SerializationTypeEnum;
import naruto.exception.SerializeException;

import java.io.IOException;

@Slf4j
public class JsonSerializer implements Serializer {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serializer(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化时有错误发生:", e);
            throw new SerializeException("序列化发生错误");
        }
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) {
        try {
            Object obj = objectMapper.readValue(bytes, clazz);
            if (obj instanceof RpcRequest) {
                obj = handleRequest(obj);
            }
            return clazz.cast(obj);
        } catch (IOException e) {
            log.error("序列化时有错误发生:", e);
            throw new SerializeException("序列化发生错误");
        }
    }

    @Override
    public int getCode() {
        return SerializationTypeEnum.JSON.getCode();
    }

    private Object handleRequest(Object obj) throws IOException {
        RpcRequest rpcRequest = (RpcRequest) obj;
        for (int i = 0; i < rpcRequest.getParamTypes().length; i++) {
            Class<?> clazz = rpcRequest.getParamTypes()[i];
            if (!clazz.isAssignableFrom(rpcRequest.getParameters()[i].getClass())) {
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, clazz);
            }
        }
        return rpcRequest;
    }
}
