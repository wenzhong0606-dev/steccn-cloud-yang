package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsRecipe;
import com.steccn.pms.domain.PmsRecipeRaw;
import com.steccn.pms.service.PmsRecipeRawService;
import com.steccn.pms.service.PmsRecipeService;
import com.steccn.pms.mapper.PmsRecipeMapper;
import com.steccn.pms.vo.PmsRecipeRawVO;
import com.steccn.pms.vo.PmsRecipeVO;
import com.steccn.pms.vo.PmsRecipeVerHisVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
* @author yequan
* @description 针对表【pms_recipe(配方表)】的数据库操作Service实现
* @createDate 2023-02-16 10:58:09
*/
@Service
@RequiredArgsConstructor
public class PmsRecipeServiceImpl extends ServiceImpl<PmsRecipeMapper, PmsRecipe>
    implements PmsRecipeService{

    private final PmsRecipeRawService pmsRecipeRawService;

    /**
     * 获取配方单详情
     * @param id
     * @return
     */
    @Override
    public PmsRecipeVO getPmsRecipeVO(Long id) {
        PmsRecipeVO pmsRecipeVO = new PmsRecipeVO();
        //1、获取最新版本号
        PmsRecipe newVersion = getOne(new LambdaQueryWrapper<PmsRecipe>().orderByDesc(PmsRecipe::getCreateTime),false);
        PmsRecipe pmsRecipe = null;
        //如果传0则找到最新的版本
        if(id.equals(new Long(0))){
            pmsRecipeVO.setIsCurrent(Integer.parseInt(PmManagerConstant.NUM_1));
            pmsRecipe = newVersion;
        }else {
            pmsRecipe = getById(id);
            Assert.notNull(pmsRecipe,"配方单不存在");
            //传过来的版本号等于新版本号
            if(newVersion.getId().equals(pmsRecipe.getId())){
                pmsRecipeVO.setIsCurrent(Integer.parseInt(PmManagerConstant.NUM_1));
            }else {
                pmsRecipeVO.setIsCurrent(Integer.parseInt(PmManagerConstant.NUM_0));
            }
        }
        BeanUtil.copyProperties(pmsRecipe,pmsRecipeVO);
        //2、组成版本历史
        List<PmsRecipeVerHisVO> hisVOList = Lists.newArrayList();
        String version = pmsRecipe.getVersion();
        //版本历史list
        String[] versionSplit = version.split("-");
        PmsRecipeVerHisVO hisVO = new PmsRecipeVerHisVO();
        hisVO.setVersion(pmsRecipe.getVersion());
        hisVO.setCreateName(pmsRecipe.getCreateName());
        hisVO.setCreateTime(pmsRecipe.getCreateTime());
        hisVOList.add(hisVO);
        //如果不是0000,则加入大版本
        if(!PmManagerConstant.RECIPE_STR.equals(versionSplit[Integer.parseInt(PmManagerConstant.NUM_2)])){
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < versionSplit.length; i++) {
                if(i==2){
                    sb.append(PmManagerConstant.RECIPE_STR);
                }else{
                    sb.append(versionSplit[i]);
                }
            }
            //大版本
            PmsRecipe bigVersion = getOne(new LambdaQueryWrapper<PmsRecipe>().eq(PmsRecipe::getVersion, sb.toString()), false);
            Assert.notNull(bigVersion, "大版本信息不存在");
            PmsRecipeVerHisVO hisVO2 = new PmsRecipeVerHisVO();
            hisVO2.setCreateName(bigVersion.getCreateName());
            hisVO2.setVersion(bigVersion.getVersion());
            hisVO2.setCreateTime(bigVersion.getCreateTime());
            hisVOList.add(hisVO2);
        }
        Collections.reverse(hisVOList);
        pmsRecipeVO.setVerHistory(hisVOList);
        //3、组成生料数据
        List<PmsRecipeRawVO> rawVOS = Lists.newArrayList();
        List<PmsRecipeRaw> pmsRecipeRawList = pmsRecipeRawService.list(new LambdaQueryWrapper<PmsRecipeRaw>().eq(PmsRecipeRaw::getRecipeId, pmsRecipe.getId()));
        pmsRecipeRawList.forEach(tmp -> {
            PmsRecipeRawVO rawTmp = new PmsRecipeRawVO();
            BeanUtil.copyProperties(tmp,rawTmp);
            rawVOS.add(rawTmp);
        });
        pmsRecipeVO.setRawList(rawVOS);
        return pmsRecipeVO;
    }
}




