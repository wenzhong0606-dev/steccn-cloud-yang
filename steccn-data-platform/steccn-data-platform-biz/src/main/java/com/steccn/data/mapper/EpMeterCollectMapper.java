package com.steccn.data.mapper;

import com.steccn.data.domain.EpMeterCollect;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.data.dto.DwMeterReadDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【ep_meter_collect(能源绩效-远程仪表采样表)】的数据库操作Mapper
 * @createDate 2022-12-16 13:23:55
 * @Entity com.steccn.data.domain.EpMeterCollect
 */
@Mapper
public interface EpMeterCollectMapper extends BaseMapper<EpMeterCollect> {

    /**
     * 动态建表
     *
     * @param tableName 表明
     * @return int
     */
    int createTable(@Param("tableName") String tableName, @Param("idIndex") String idIndex, @Param("indexA") String indexA, @Param("indexB") String indexB);

    /**
     * 获取所有的表名
     * @return 表名称集合
     */
    Set<String> getTableNames(@Param("logicTableName") String logicTableName);

}




