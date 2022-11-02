package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsMaterialRequisition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【pms_material_requisition(生产领料单)】的数据库操作Mapper
* @createDate 2022-11-02 10:10:08
* @Entity com.steccn.pms.domain.PmsMaterialRequisition
*/
@Mapper
public interface PmsMaterialRequisitionMapper extends BaseMapper<PmsMaterialRequisition> {

}




