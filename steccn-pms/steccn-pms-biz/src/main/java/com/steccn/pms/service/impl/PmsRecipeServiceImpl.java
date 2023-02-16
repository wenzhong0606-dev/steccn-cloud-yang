package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmsRecipe;
import com.steccn.pms.service.PmsRecipeService;
import com.steccn.pms.mapper.PmsRecipeMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【pms_recipe(配方表)】的数据库操作Service实现
* @createDate 2023-02-16 10:58:09
*/
@Service
public class PmsRecipeServiceImpl extends ServiceImpl<PmsRecipeMapper, PmsRecipe>
    implements PmsRecipeService{

}




