package com.steccn.pms.service;

import com.steccn.pms.domain.PmsRecipe;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.vo.PmsRecipeVO;

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
}
