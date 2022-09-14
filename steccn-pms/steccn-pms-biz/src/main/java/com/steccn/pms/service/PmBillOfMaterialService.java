package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmBillOfMaterial;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmBillOfMaterialDTO;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.vo.PmBillOfMaterialVO;

import java.io.IOException;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【pm_bill_of_material(物料清单表)】的数据库操作Service
 * @createDate 2022-09-02 17:01:20
 */
public interface PmBillOfMaterialService extends IService<PmBillOfMaterial> {

    R<Boolean> add(PmBillOfMaterialDTO dto);

    Page<PmBillOfMaterialVO> pageList(PmBillOfMaterialDTO dto);

    R<Boolean> edit(PmBillOfMaterialDTO dto);

    Boolean delete(Integer id);

    Boolean excelImportData(PmExcelDTO dto) throws IOException;
}
