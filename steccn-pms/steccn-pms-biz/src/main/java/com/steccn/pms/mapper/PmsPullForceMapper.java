package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsPullForce;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
* @author yequan
* @description 针对表【pms_pull_force(拉引力表)】的数据库操作Mapper
* @createDate 2022-11-02 10:10:08
* @Entity com.steccn.pms.domain.PmsPullForce
*/
@Mapper
public interface PmsPullForceMapper extends BaseMapper<PmsPullForce> {
    /**
     * 聚合一天的拉引量
     * @param start 开始时间
     * @param end 结束时间
     * @param idSet id列表
     * @return
     */
    List<PmsPullForce> selectPullForceByDay(@Param("start")LocalDate start, @Param("end")LocalDate end, @Param("idSet")Set<Integer> idSet);
}




