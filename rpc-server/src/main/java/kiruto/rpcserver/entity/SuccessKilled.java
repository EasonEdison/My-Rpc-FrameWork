package kiruto.rpcserver.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("success_killed")
public class SuccessKilled implements Serializable {
    private long seckillId;
    private long userId;

    /**
     * 表示下单的时候还有多少个.
     */
    private short state;
    private Timestamp createTime;
}
