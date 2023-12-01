package com.steccn.dp.mapper;

import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.AdsOpcUaAll3;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.dp.vo.DpWeightVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author Admin
* @description 针对表【ads_opc_ua_all3(tagId包含Raw_YL.ZHM.fact_weigh的数据)】的数据库操作Mapper
* @createDate 2023-11-23 09:27:09
* @Entity com.steccn.dp.domain.AdsOpcUaAll3
*/
@Mapper
public interface AdsOpcUaAll3Mapper extends BaseMapper<AdsOpcUaAll3> {
    /**
     * 电子称读数
     * @param dto f1开始时间 f2结束时间
     * @return
     */
    List<DpWeightVO> weightCount(@Param("dto") TupleDTO<LocalDateTime,LocalDateTime> dto);

    /**
     * 电子称读数按天
     * @param dto f1开始时间 f2结束时间
     * @return
     */
    List<DpValueDateVO> weightCountAsDay(@Param("dto") TupleDTO<LocalDateTime,LocalDateTime> dto);
}




