package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsProducePlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【pms_produce_plan(生产月计划)】的数据库操作Mapper
* @createDate 2022-11-02 10:10:08
* @Entity com.steccn.pms.domain.PmsProducePlan
*/
@Mapper
public interface PmsProducePlanMapper extends BaseMapper<PmsProducePlan> {

}




