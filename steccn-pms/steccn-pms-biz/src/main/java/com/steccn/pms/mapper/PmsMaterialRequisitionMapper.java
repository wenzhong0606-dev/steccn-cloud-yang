package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsMaterialRequisition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.dto.MaterialRequisitionPageDTO;
import com.steccn.pms.vo.MaterialRequisitionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author yequan
* @description 针对表【pms_material_requisition(生产领料单)】的数据库操作Mapper
* @createDate 2022-11-02 10:10:08
* @Entity com.steccn.pms.domain.PmsMaterialRequisition
*/
@Mapper
public interface PmsMaterialRequisitionMapper extends BaseMapper<PmsMaterialRequisition> {

    /**
     * 获取分页列表
     * @param page
     * @param dto
     * @return
     */
    Page<MaterialRequisitionVO> pageList(Page page, @Param("dto") MaterialRequisitionPageDTO dto);
}




