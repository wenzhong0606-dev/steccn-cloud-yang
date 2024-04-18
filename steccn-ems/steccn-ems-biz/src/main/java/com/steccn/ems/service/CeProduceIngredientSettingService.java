package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.ems.domain.CeProduceIngredientSetting;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.CeProduceIngredientSettingDTO;
import com.steccn.ems.dto.CeProduceIngredientSettingPageDTO;
import com.steccn.ems.vo.CeProduceIngredientSettingVO;

/**
 * @author yequan
 * @description 针对表【ce_produce_ingredient_setting(生产原料设置表)】的数据库操作Service
 * @createDate 2024-04-18 09:27:24
 */
public interface CeProduceIngredientSettingService extends IService<CeProduceIngredientSetting> {

    /**
     * 查询生产原料设置分页列表
     *
     * @param dto
     * @return
     */
    Page<CeProduceIngredientSettingVO> getPage(CeProduceIngredientSettingPageDTO dto);

    /**
     * 根据id查询生产原料设置
     *
     * @param id
     * @return
     */
    CeProduceIngredientSettingVO info(Integer id);

    /**
     * 新增生产原料设置
     *
     * @param dto
     * @return
     */
    Boolean add(CeProduceIngredientSettingDTO dto);

    /**
     * 编辑生产原料设置
     *
     * @param dto
     * @return
     */
    Boolean edit(CeProduceIngredientSettingDTO dto);
}
