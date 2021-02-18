package kiruto.rpcserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import kiruto.rpcserver.entity.SqlVo;
import kiruto.rpcserver.entity.SuccessKilled;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface SuccesskilledMapper extends BaseMapper<SuccessKilled> {

    @Select("${sql}")
    Object selectSql(SqlVo sql);

    @Update("${sql}")
    void updateSql(SqlVo sqlVo);

    @Insert("${sql}")
    void insertSql(SqlVo sqlVo);

    @Delete("${sql}")
    void deleteSql(SqlVo sqlVo);
}
