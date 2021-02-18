package kiruto.rpcserver;

import kiruto.rpcserver.entity.SqlVo;
import kiruto.rpcserver.mapper.SuccesskilledMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RpcServerApplicationTests {

    @Autowired
    private SuccesskilledMapper successkilledMapper;

    // @Value("${test.value}")
    // private String value;

    @Test
    void contextLoads() {
        System.out.println("111");
        String sql = "select count(*) from seckill";
        Object integer = successkilledMapper.selectSql(new SqlVo(sql));
        System.out.println(integer);
    }

}
