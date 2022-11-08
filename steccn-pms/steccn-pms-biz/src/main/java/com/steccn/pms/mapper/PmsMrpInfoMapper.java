package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsMrpInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.dto.PmsProducePlanOrderDTO;
import com.steccn.pms.vo.PmsMrpInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author yequan
* @description 针对表【pms_mrp_info(物料需求计划详情)】的数据库操作Mapper
* @createDate 2022-11-02 10:10:08
* @Entity com.steccn.pms.domain.PmsMrpInfo
*/
@Mapper
public interface PmsMrpInfoMapper extends BaseMapper<PmsMrpInfo> {

    /**
     * 查询需要下达的物料的信息
     * @param dto
     * @return
     */
    List<PmsMrpInfo>  list(@Param("dto") PmsProducePlanOrderDTO dto);
}




