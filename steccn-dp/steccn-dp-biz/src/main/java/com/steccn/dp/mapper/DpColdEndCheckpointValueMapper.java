package com.steccn.dp.mapper;

import com.steccn.dp.domain.DpColdEndCheckpointValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author Admin
* @description 针对表【dp_cold_end_checkpoint_value(冷端各线测点数据)】的数据库操作Mapper
* @createDate 2024-03-01 16:21:20
* @Entity com.steccn.dp.domain.DpColdEndCheckpointValue
*/
@Mapper
public interface DpColdEndCheckpointValueMapper extends BaseMapper<DpColdEndCheckpointValue> {
    /**
     * 获取各测点的最新时间
     * @return
     */
    List<DpColdEndCheckpointValue> selectLatestTime();

    /**
     * 删除小于这个时间的数据
     * @param date
     * @return
     */
    Boolean deleteData(@Param("date")LocalDateTime date);
}




