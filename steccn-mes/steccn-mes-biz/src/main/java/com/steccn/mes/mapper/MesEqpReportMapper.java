package com.steccn.mes.mapper;

import com.github.yulichang.base.mapper.MPJJoinMapper;
import com.steccn.mes.domain.MesEqpReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【mes_eqp_report(设备运行报表)】的数据库操作Mapper
* @createDate 2024-05-06 16:36:23
* @Entity com.steccn.mes.domain.MesEqpReport
*/
@Mapper
public interface MesEqpReportMapper extends MPJJoinMapper<MesEqpReport> {

}




