package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmsMaterialRequisitionDemander;
import com.steccn.pms.service.PmsMaterialRequisitionDemanderService;
import com.steccn.pms.mapper.PmsMaterialRequisitionDemanderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author 34872
* @description 针对表【pms_material_requisition_demander(领用单明细需求人)】的数据库操作Service实现
* @createDate 2023-08-24 13:57:37
*/
@Service
@RequiredArgsConstructor
public class PmsMaterialRequisitionDemanderServiceImpl extends ServiceImpl<PmsMaterialRequisitionDemanderMapper, PmsMaterialRequisitionDemander>
    implements PmsMaterialRequisitionDemanderService{

}




