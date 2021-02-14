package kiruto.serialize;

public interface Serializer {

    byte[] serializer(Object obj);

    <T> T deserializer(byte[] bytes, Class<T> clazz);

    // 通过code获取实例
    public static Serializer getByCode(int code) {
        switch (code) {
            case 1:
                return new KryoSerializer();
            default:
                return new KryoSerializer();
        }
    }

    int getCode();
}
