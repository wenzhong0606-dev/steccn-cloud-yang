package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsRecipe;
import com.steccn.pms.domain.PmsRecipeRaw;
import com.steccn.pms.dto.PmsRecipeDTO;
import com.steccn.pms.dto.PmsRecipeRawDTO;
import com.steccn.pms.service.PmsRecipeRawService;
import com.steccn.pms.service.PmsRecipeService;
import com.steccn.pms.mapper.PmsRecipeMapper;
import com.steccn.pms.vo.PmsRecipeRawVO;
import com.steccn.pms.vo.PmsRecipeVO;
import com.steccn.pms.vo.PmsRecipeVerHisVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.iterators.ArrayListIterator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

    private final RemoteDictFeignApi remoteDictFeignApi;

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
        hisVO.setId(pmsRecipe.getId());
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
            hisVO2.setId(bigVersion.getId());
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

    @Override
    public Page<PmsRecipeVerHisVO> pageList(BasePageDTO dto) {
        //根据创建时间分页
        LambdaQueryWrapper<PmsRecipe> condition = new LambdaQueryWrapper<PmsRecipe>()
                .orderByDesc(PmsRecipe::getCreateTime);
        //分页查询
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        IPage resPage = page(page,condition);
        //拷贝page信息
        BeanUtil.copyProperties(resPage,page);
        List<PmsRecipeVerHisVO> resList = BeanUtil.copyToList(resPage.getRecords(),PmsRecipeVerHisVO.class);
        return page.setRecords(resList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishQuantity(PmsRecipeDTO dto) {
        //1、发布时检查此新建版本的大版本号（YYYY-NN）是否大于最新版本的大版本号
        PmsRecipe newVersion = getOne(new LambdaUpdateWrapper<PmsRecipe>().orderByDesc(PmsRecipe::getCreateTime), false);
        String[] newSpit = newVersion.getVersion().split("-");
        StringBuilder newSb = new StringBuilder();
        String[] nowSpit = dto.getVersion().split("-");
        StringBuilder nowSb = new StringBuilder();
        //检查大版本号
        for(int i = 0; i < nowSpit.length; i++){
            if(i!=2){
                nowSb.append(nowSpit[i]);
                newSb.append(newSpit[i]);
            }
        }
        if(Integer.parseInt(nowSb.toString()) < Integer.parseInt(newSb.toString())) {
            throw new RuntimeException("配方用量已被其他人更新，无法发布，请取消后重试");
        }
        PmsRecipe pmsRecipe = new PmsRecipe();

        //处理单位,所有单位都为kg,找到kg的id
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode("kg", "measure_unit"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        pmsRecipe.setBatchWaterUnit(sysDictVO.getId());
        pmsRecipe.setClinkerUnit(sysDictVO.getId());

        //2、处理版本号
        StringBuilder version = new StringBuilder();
        //当前年份与上一版YYYY不同时：大版本号中YYYY+1，NN置为01，小版本号置为0000，例如2023-04-0008升为2024-01-0000
        int year = LocalDate.now().getYear();
        if(LocalDate.now().getYear() != Integer.parseInt(newSpit[0])){
            version.append(year);
            version.append("-01");
        }else {
            int bigVersion = Integer.parseInt(newSpit[1]);
            bigVersion++;
            version.append("-");
            if(bigVersion < Integer.parseInt(PmManagerConstant.NUM_10)){
                version.append(0);
            }
            version.append(bigVersion);
        }
        version.append("-0000");
        pmsRecipe.setVersion(version.toString());

        //3、处理生料信息
        BigDecimal dryBasisQuantityCount = BigDecimal.ZERO;
        BigDecimal wetBasisQuantityCount = BigDecimal.ZERO;
        //拿到最新版本的所有生料信息
        List<PmsRecipeRaw> pmsRecipeRawModelList = Lists.newArrayList();

        //过滤重复id的物料
        List<PmsRecipeRawDTO> rawList = dto.getRawList();
        rawList = rawList.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(
                                PmsRecipeRawDTO::getMaterialId
                        ))), ArrayList::new));
        for(PmsRecipeRawDTO tmp:rawList){
            //加总生料
            dryBasisQuantityCount = BigDecimalUtil.add(dryBasisQuantityCount,tmp.getDryBasisQuantity(),4);
            PmsRecipeRaw pmsRecipeRawModel = new PmsRecipeRaw();
            BeanUtil.copyProperties(tmp,pmsRecipeRawModel);
            //干基单位
            pmsRecipeRawModel.setDryBasisUnit(sysDictVO.getId());
            //湿基含水率
            BigDecimal wetBasisWaterRateNum = BigDecimalUtil.divide(tmp.getWetBasisWaterRate(),BigDecimal.valueOf(Long.parseLong("100"),6));
            pmsRecipeRawModel.setWetBasisWaterRate(tmp.getWetBasisWaterRate());
            //湿基用量 湿基用量=干基用量/(1-湿基含水率)
            BigDecimal wetBasisQuantity = BigDecimalUtil.divide(tmp.getDryBasisQuantity(),
                    BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")),wetBasisWaterRateNum,6),4);
            pmsRecipeRawModel.setWetBasisQuantity(wetBasisQuantity);
            pmsRecipeRawModel.setWetBasisUnit(sysDictVO.getId());
            pmsRecipeRawModelList.add(pmsRecipeRawModel);
        }


        //4、处理熟料信息
        //熟料占比
        pmsRecipe.setClinkerRate(dto.getClinkerRate());
        BigDecimal clinkerRateNum = BigDecimalUtil.divide(dto.getClinkerRate(),BigDecimal.valueOf(Long.parseLong("100")),4);
        //熟料用量=干基用量加总/(1-熟料占比)
        BigDecimal clinkerQuantityNum = BigDecimalUtil.divide(dryBasisQuantityCount,BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")),clinkerRateNum,6),4);
        pmsRecipe.setClinkerQuantity(clinkerQuantityNum);
        //5、处理水份
        //配料含水率
        pmsRecipe.setBatchWaterRate(dto.getBatchWaterRate());
        //上偏差
        pmsRecipe.setBatchWaterUpper(dto.getBatchWaterUpper());
        //下偏差
        pmsRecipe.setBatchWaterLower(dto.getBatchWaterLower());
        //加水量=干基用量加总/(1-配合料含水率) - 湿基用量加总
        BigDecimal batchWaterQuantity = BigDecimalUtil.subtract(
                BigDecimalUtil.divide(dryBasisQuantityCount,
                        BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")),dto.getBatchWaterRate(),6),4),
                wetBasisQuantityCount,4);
        pmsRecipe.setBatchWaterQuantity(batchWaterQuantity);
        save(pmsRecipe);
        //加入版本id
        pmsRecipeRawModelList.forEach(tmp -> {
            tmp.setRecipeId(pmsRecipe.getId());
        });
        pmsRecipeRawService.saveBatch(pmsRecipeRawModelList);
        return true;
    }
}




