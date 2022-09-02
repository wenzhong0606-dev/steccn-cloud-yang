package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmBillOfMaterial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【pm_bill_of_material(物料清单表)】的数据库操作Mapper
* @createDate 2022-09-02 17:01:20
* @Entity com.steccn.pms.domain.PmBillOfMaterial
*/
@Mapper
public interface PmBillOfMaterialMapper extends BaseMapper<PmBillOfMaterial> {

}




