package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.StandardVO;
import com.steccn.ems.domain.EpProductUnitConsumeStandard;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpProductUnitStandardDTO;
import com.steccn.ems.dto.EpProductUnitStandardPageDTO;
import com.steccn.ems.vo.EpProductUnitConsumeStandardVO;

import java.time.LocalDate;
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
    Page<EpProductUnitConsumeStandardVO> getEpProductUnitConsumeStandardList(EpProductUnitStandardPageDTO dto);

    /**
     * 新建或编辑产品单耗标准
     * @param dto
     * @return
     */
    Boolean addOrEdit(EpProductUnitStandardDTO dto);

    /**
     * 根据id查询产品单耗标准
     * @param id
     * @return
     */
    EpProductUnitConsumeStandardVO getProductUnitStandardVO(Integer id);

    /**
     * 删除产品单耗标准
     * @param id
     * @return
     */
    Boolean delete(Integer id);

    /**
     * 有效的单耗标准列表
     *
     * @param energyType
     * @param start 有效期-开始时间
     * @param end 有效期-结束时间
     * @return list
     */
    List<StandardVO> validStandardList(Integer energyType, LocalDate start, LocalDate end);
}
