package com.steccn.pms.service;

import com.steccn.pms.domain.PmMaterial;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmBillOfMaterialDTO;

/**
* @author yequan
* @description 针对表【pm_material(物料清单-物料表)】的数据库操作Service
* @createDate 2022-09-02 17:01:20
*/
public interface PmMaterialService extends IService<PmMaterial> {

    Boolean saveMaterial(PmBillOfMaterialDTO dto);

    void deleteByMaterialId(Long id);
}
