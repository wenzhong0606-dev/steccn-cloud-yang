package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmBillOfMaterial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.dto.PmBillOfMaterialDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author yequan
* @description 针对表【pm_bill_of_material(物料清单表)】的数据库操作Mapper
* @createDate 2022-09-02 17:01:20
* @Entity com.steccn.pms.domain.PmBillOfMaterial
*/
@Mapper
public interface PmBillOfMaterialMapper extends BaseMapper<PmBillOfMaterial> {


    List<PmBillOfMaterial> listByCodes(@Param("codeList") List<String> codeList);

}




