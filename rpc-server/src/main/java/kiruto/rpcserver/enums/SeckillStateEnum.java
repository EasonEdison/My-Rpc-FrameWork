package kiruto.rpcserver.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SeckillStateEnum {

    MUCH(2,"人太多了，请稍后再试!"),
    SUCCESS(1,"秒杀成功!"),
    END(0,"秒杀结束!"),
    REPEAT_KILL(-1,"重复秒杀!"),
    INNER_ERROR(-2,"系统异常!");

    private int state;
    private String info;
}
