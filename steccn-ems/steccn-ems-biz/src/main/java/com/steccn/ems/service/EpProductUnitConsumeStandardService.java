package com.steccn.ems.service;

import com.steccn.ems.domain.EpProductUnitConsumeStandard;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpEnergyTypeDTO;
import com.steccn.ems.dto.EpProductUnitStandardDTO;
import com.steccn.ems.dto.EpProductUnitStandardPageDTO;
import com.steccn.ems.vo.EpProductUnitConsumeStandardVO;

import java.util.List;

/**
* @author yequan
* @description 针对表【ep_product_unit_consume_standard(产品单耗标准)】的数据库操作Service
* @createDate 2022-11-29 19:27:28
*/
public interface EpProductUnitConsumeStandardService extends IService<EpProductUnitConsumeStandard> {

    /**
     *分页查询产品单耗标准
     * @return
     */
    List<EpProductUnitConsumeStandardVO> getEpProductUnitConsumeStandardList(EpProductUnitStandardPageDTO dto);

    /**
     * 新建或编辑产品单耗标准
     * @param dto
     * @return
     */
    Boolean addOrEdit(EpProductUnitStandardDTO dto);
}
