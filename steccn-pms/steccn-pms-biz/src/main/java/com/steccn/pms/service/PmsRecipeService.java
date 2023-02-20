package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.pms.domain.PmsRecipe;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsRecipeDTO;
import com.steccn.pms.dto.PmsRecipeRawDTO;
import com.steccn.pms.vo.PmsRecipeVO;
import com.steccn.pms.vo.PmsRecipeVerHisVO;

/**
* @author yequan
* @description 针对表【pms_recipe(配方表)】的数据库操作Service
* @createDate 2023-02-16 10:58:09
*/
public interface PmsRecipeService extends IService<PmsRecipe> {
    /**
     * 获取配方单详情
     * @param id
     * @return
     */
    PmsRecipeVO getPmsRecipeVO(Long id);

    /**
     * 获取版本分页
     * @return
     */
    Page<PmsRecipeVerHisVO> pageList(BasePageDTO dto);

    /**
     * 更新用量
     * @param dto
     * @return
     */
    Boolean publishQuantity(PmsRecipeDTO dto);
}
