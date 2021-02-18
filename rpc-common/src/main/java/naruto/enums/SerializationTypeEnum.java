package naruto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {

    KYRO((byte) 0x01, "kyro"),
    PROTOSTUFF((byte) 0x02, "protostuff"),
    JSON((byte) 0x03, "json");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (SerializationTypeEnum s : SerializationTypeEnum.values()) {
            if (s.getCode() == code) {
                return s.getName();
            }
        }
        return null;
    }

    public static byte getCode(String name) {
        for (SerializationTypeEnum s : SerializationTypeEnum.values()) {
            if (s.getName().equals(name)) {
                return s.getCode();
            }
        }
        return 0;
    }

}
