package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsRecipe;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【pms_recipe(配方表)】的数据库操作Mapper
* @createDate 2023-02-16 10:58:09
* @Entity com.steccn.pms.domain.PmsRecipe
*/
@Mapper
public interface PmsRecipeMapper extends BaseMapper<PmsRecipe> {

}




