package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsReportWorkConsume;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.vo.EnergyGroupListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author yequan
* @description 针对表【pms_report_work_consume(报工能耗表)】的数据库操作Mapper
* @createDate 2022-11-02 15:32:08
* @Entity com.steccn.pms.domain.PmsReportWorkConsume
*/
@Mapper
public interface PmsReportWorkConsumeMapper extends BaseMapper<PmsReportWorkConsume> {

    /**
     * 根据产线分组水电油气能耗
     * @param productLineIdList 产线id集合
     * @return list
     */
    List<EnergyGroupListVO> energyGroup(@Param("productLineIdList") List<Integer> productLineIdList);
}




