package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.CalculateDateUtil;
import com.steccn.common.vo.QtTaskClassCalculate;
import com.steccn.oa.dto.OaHrmDTO;
import com.steccn.oa.dto.OaRosterRecordDTO;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.vo.OaEmpVO;
import com.steccn.oa.vo.OaRosterRecordVO;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsRecipe;
import com.steccn.pms.domain.PmsRecipeRaw;
import com.steccn.pms.dto.PmsRecipeDTO;
import com.steccn.pms.dto.PmsRecipeRawDTO;
import com.steccn.pms.dto.PmsWetBasisWaterRateDTO;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.service.PmsRecipeRawService;
import com.steccn.pms.service.PmsRecipeService;
import com.steccn.pms.mapper.PmsRecipeMapper;
import com.steccn.pms.vo.MessScopeVO;
import com.steccn.pms.vo.PmsRecipeRawVO;
import com.steccn.pms.vo.PmsRecipeVO;
import com.steccn.pms.vo.PmsRecipeVerHisVO;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.feign.RemoteWsFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.iterators.ArrayListIterator;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_recipe(配方表)】的数据库操作Service实现
 * @createDate 2023-02-16 10:58:09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PmsRecipeServiceImpl extends ServiceImpl<PmsRecipeMapper, PmsRecipe>
        implements PmsRecipeService {

    private final PmsRecipeRawService pmsRecipeRawService;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteMessFeignApi remoteMessFeignApi;

    private final RemoteWsFeignApi remoteWsFeignApi;

    private final OaInfoService oaInfoService;

    private final RemoteMsgFeignApi remoteMsgFeignApi;

    private final RedissonClient redissonClient;

    /**
     * 获取配方单详情
     *
     * @param id
     * @return
     */
    @Override
    public PmsRecipeVO getPmsRecipeVO(Long id) {
        PmsRecipeVO pmsRecipeVO = new PmsRecipeVO();
        //1、获取最新版本号
        PmsRecipe newVersion = getOne(new LambdaQueryWrapper<PmsRecipe>().orderByDesc(PmsRecipe::getCreateTime).last("LIMIT 1"), false);
        PmsRecipe pmsRecipe = null;
        //如果传0则找到最新的版本
        if (id.equals(new Long(0))) {
            pmsRecipeVO.setIsCurrent(Integer.parseInt(PmManagerConstant.NUM_1));
            pmsRecipe = newVersion;
        } else {
            pmsRecipe = getById(id);
            Assert.notNull(pmsRecipe, "配方单不存在");
            //传过来的版本号等于新版本号
            if (newVersion.getId().equals(pmsRecipe.getId())) {
                pmsRecipeVO.setIsCurrent(Integer.parseInt(PmManagerConstant.NUM_1));
            } else {
                pmsRecipeVO.setIsCurrent(Integer.parseInt(PmManagerConstant.NUM_0));
            }
        }
        BeanUtil.copyProperties(pmsRecipe, pmsRecipeVO);
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
        hisVO.setEllipsis(0);
        hisVOList.add(hisVO);
        //如果是0000则判断
        if (PmManagerConstant.RECIPE_STR.equals(versionSplit[Integer.parseInt(PmManagerConstant.NUM_2)])) {
            //如果当前版本是系统版本则不显示左侧...
            if (!hisVO.getVersion().equals(PmManagerConstant.sysVersion)) {
                PmsRecipeVerHisVO tmp = new PmsRecipeVerHisVO();
                tmp.setEllipsis(1);
                hisVOList.add(tmp);
            }
        } else {
            //如果当前版本比2大则显示省略号
            if (Integer.parseInt(versionSplit[Integer.parseInt(PmManagerConstant.NUM_2)]) >= Integer.parseInt(PmManagerConstant.NUM_2)) {
                PmsRecipeVerHisVO tmp = new PmsRecipeVerHisVO();
                tmp.setEllipsis(1);
                hisVOList.add(tmp);
            }
        }
        //如果不是0000,则加入大版本
        if (!PmManagerConstant.RECIPE_STR.equals(versionSplit[Integer.parseInt(PmManagerConstant.NUM_2)])) {
            StringBuilder sb = new StringBuilder();
            sb.append(versionSplit[0]);
            sb.append("-");
            sb.append(versionSplit[1]);
            sb.append("-");
            sb.append(PmManagerConstant.RECIPE_STR);
            //大版本
            PmsRecipe bigVersion = getOne(new LambdaQueryWrapper<PmsRecipe>().eq(PmsRecipe::getVersion, sb.toString()), false);
            Assert.notNull(bigVersion, "大版本信息不存在");
            PmsRecipeVerHisVO hisVO2 = new PmsRecipeVerHisVO();
            hisVO2.setCreateName(bigVersion.getCreateName());
            hisVO2.setVersion(bigVersion.getVersion());
            hisVO2.setCreateTime(bigVersion.getCreateTime());
            hisVO2.setId(bigVersion.getId());
            hisVO2.setEllipsis(0);
            hisVOList.add(hisVO2);
            //如果当前版本是系统版本则不显示左侧...,否则显示
            if (!hisVO2.getVersion().equals(PmManagerConstant.sysVersion)) {
                PmsRecipeVerHisVO tmp = new PmsRecipeVerHisVO();
                tmp.setEllipsis(1);
                hisVOList.add(tmp);
            }

        }
        Collections.reverse(hisVOList);
        pmsRecipeVO.setVerHistory(hisVOList);
        //3、组成生料数据
        List<PmsRecipeRawVO> rawVOS = Lists.newArrayList();
        List<PmsRecipeRaw> pmsRecipeRawList = pmsRecipeRawService.list(new LambdaQueryWrapper<PmsRecipeRaw>().eq(PmsRecipeRaw::getRecipeId, pmsRecipe.getId()));
        BigDecimal dryBasisQuantityAll = BigDecimal.ZERO;
        for(PmsRecipeRaw tmp:pmsRecipeRawList){
            PmsRecipeRawVO rawTmp = new PmsRecipeRawVO();
            BeanUtil.copyProperties(tmp, rawTmp);
            //便于前端判断
            if (Objects.isNull(rawTmp.getWetBasisWaterRate())) {
                rawTmp.setWetBasisWaterRate(new BigDecimal(0));
            }
            //加总干基
            dryBasisQuantityAll = BigDecimalUtil.add(tmp.getDryBasisQuantity(),dryBasisQuantityAll);
            rawVOS.add(rawTmp);
        }
        pmsRecipeVO.setDryBasisQuantityAll(dryBasisQuantityAll);
        pmsRecipeVO.setRawList(rawVOS);
        //4、处理按钮权限数据
        SteccnUser user = SecurityUtils.getUser();
        MessScopeVO role = remoteMessFeignApi.getRole(user.getUsername());
        if (CollUtil.isNotEmpty(role.getStringList())) {
            if (role.getStringList().contains(PmManagerConstant.RECIPE_ROLE_ADMIN)) {
                pmsRecipeVO.setButtonRateAuth(1);
            } else {
                pmsRecipeVO.setButtonRateAuth(0);
            }

            if (role.getStringList().contains(PmManagerConstant.RECIPE_ROLE_SYSTEM)) {
                pmsRecipeVO.setButtonAmountAuth(1);
            } else {
                pmsRecipeVO.setButtonAmountAuth(0);
            }
        } else {
            pmsRecipeVO.setButtonAmountAuth(0);
            pmsRecipeVO.setButtonRateAuth(0);
        }
        //5、生料占比 历史数据刷为1 - 所属版本的熟料占比
        if(Objects.isNull(pmsRecipeVO.getClinkerRate())){
            pmsRecipeVO.setRawRate(new BigDecimal(100));
        }else {
            pmsRecipeVO.setRawRate(BigDecimalUtil.subtract(new BigDecimal(100),pmsRecipeVO.getClinkerRate()));
        }
        return pmsRecipeVO;
    }

    @Override
    public Page<PmsRecipeVerHisVO> pageList(BasePageDTO dto) {
        //根据创建时间分页
        LambdaQueryWrapper<PmsRecipe> condition = new LambdaQueryWrapper<PmsRecipe>()
                .orderByDesc(PmsRecipe::getCreateTime);
        //分页查询
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        IPage resPage = page(page, condition);
        //拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        List<PmsRecipeVerHisVO> resList = BeanUtil.copyToList(resPage.getRecords(), PmsRecipeVerHisVO.class);
        return page.setRecords(resList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishQuantity(PmsRecipeDTO dto) {
        //防止版本号出现问题
        RLock lock = redissonClient.getLock(LockConstant.TABLE_PMS_RECIPE);
        lock.lock();
        try {
            //1、发布时检查此新建版本的大版本号（YYYY-NN）是否大于最新版本的大版本号
            PmsRecipe newVersion = getOne(new LambdaUpdateWrapper<PmsRecipe>().orderByDesc(PmsRecipe::getCreateTime).last("LIMIT 1"), false);
            String[] newSpit = newVersion.getVersion().split("-");
            StringBuilder newSb = new StringBuilder();
            String[] nowSpit = dto.getVersion().split("-");
            if (nowSpit.length != Integer.parseInt(PmManagerConstant.NUM_3)) {
                throw new BusinessException("版本号输入格式错误");
            }
            StringBuilder nowSb = new StringBuilder();
            //检查大版本号
            for (int i = 0; i < nowSpit.length; i++) {
                if (i != 2) {
                    nowSb.append(nowSpit[i]);
                    newSb.append(newSpit[i]);
                }
            }
            if (Integer.parseInt(nowSb.toString()) <= Integer.parseInt(newSb.toString())) {
                throw new BusinessException("配方用量已被其他人更新，无法发布，请取消后重试");
            }
            PmsRecipe pmsRecipe = new PmsRecipe();

            //处理单位,所有单位都为kg,找到kg的id
            SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode("kg", "measure_unit"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            pmsRecipe.setBatchWaterUnit(sysDictVO.getId());
            pmsRecipe.setClinkerUnit(sysDictVO.getId());
            if (StrUtil.isNotBlank(dto.getNote())) {
                pmsRecipe.setNote(dto.getNote());
            }

            //2、处理版本号
            StringBuilder version = new StringBuilder();
            //当前年份与上一版YYYY不同时：大版本号中YYYY+1，NN置为01，小版本号置为0000，例如2023-04-0008升为2024-01-0000
            int year = LocalDate.now().getYear();
            version.append(year);
            if (LocalDate.now().getYear() != Integer.parseInt(newSpit[0])) {
                version.append("-01");
            } else {
                int bigVersion = Integer.parseInt(newSpit[1]);
                bigVersion++;
                version.append("-");
                if (bigVersion < Integer.parseInt(PmManagerConstant.NUM_10)) {
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
            for (PmsRecipeRawDTO tmp : rawList) {
                //加总生料
                dryBasisQuantityCount = BigDecimalUtil.add(dryBasisQuantityCount, tmp.getDryBasisQuantity(), 4);
                PmsRecipeRaw pmsRecipeRawModel = new PmsRecipeRaw();
                BeanUtil.copyProperties(tmp, pmsRecipeRawModel);
                //干基单位
                pmsRecipeRawModel.setDryBasisUnit(sysDictVO.getId());
                //湿基含水率
                BigDecimal wetBasisWaterRateNum = BigDecimalUtil.divide(tmp.getWetBasisWaterRate(), BigDecimal.valueOf(Long.parseLong("100")), 6);
                pmsRecipeRawModel.setWetBasisWaterRate(tmp.getWetBasisWaterRate());
                //湿基用量 湿基用量=干基用量/(1-湿基含水率)
                BigDecimal wetBasisQuantity = BigDecimalUtil.divide(tmp.getDryBasisQuantity(),
                        BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")), wetBasisWaterRateNum, 6), 4);
                //湿基用量加总
                wetBasisQuantityCount = BigDecimalUtil.add(wetBasisQuantityCount,wetBasisQuantity,4);
                pmsRecipeRawModel.setWetBasisQuantity(wetBasisQuantity);
                pmsRecipeRawModel.setWetBasisUnit(sysDictVO.getId());
                pmsRecipeRawModelList.add(pmsRecipeRawModel);
            }


            //4、处理熟料信息
            //熟料占比
            pmsRecipe.setClinkerRate(dto.getClinkerRate());
            BigDecimal clinkerRateNum = BigDecimalUtil.divide(dto.getClinkerRate(), BigDecimal.valueOf(Long.parseLong("100")), 4);
            //熟料用量=干基用量加总/(1-熟料占比)- 干基用量加总
            BigDecimal clinkerQuantityNum = BigDecimalUtil.divide(dryBasisQuantityCount, BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")), clinkerRateNum, 6), 4);
            pmsRecipe.setClinkerQuantity(BigDecimalUtil.subtract(clinkerQuantityNum,dryBasisQuantityCount));
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
                            BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")), BigDecimalUtil.divide(dto.getBatchWaterRate(), BigDecimal.valueOf(Long.parseLong("100")), 6), 6), 4),
                    wetBasisQuantityCount, 4);
            pmsRecipe.setBatchWaterQuantity(batchWaterQuantity);
            save(pmsRecipe);
            //加入版本id
            pmsRecipeRawModelList.forEach(tmp -> {
                tmp.setRecipeId(pmsRecipe.getId());
            });
            pmsRecipeRawService.saveBatch(pmsRecipeRawModelList);
            //6、发送消息
            //刷新消息,发给所有读权限的人
            sendRefreshMsg();
            //发给当班人员
            sendMessage(false, pmsRecipe.getVersion());
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publishRate(PmsWetBasisWaterRateDTO dto) {
        //防止版本号出现问题
        RLock lock = redissonClient.getLock(LockConstant.TABLE_PMS_RECIPE);
        lock.lock();
        try {
            //1、发布时检查此新建版本的大版本号（YYYY-NN）和最新版本的大版本号是否相同
            PmsRecipe newVersion = getOne(new LambdaUpdateWrapper<PmsRecipe>().orderByDesc(PmsRecipe::getCreateTime).last("LIMIT 1"), false);
            String[] newSpit = newVersion.getVersion().split("-");
            StringBuilder newSb = new StringBuilder();
            String[] nowSpit = dto.getVersion().split("-");
            if (nowSpit.length != Integer.parseInt(PmManagerConstant.NUM_3)) {
                throw new BusinessException("版本号输入格式错误");
            }
            StringBuilder nowSb = new StringBuilder();
            //检查大版本号
            for (int i = 0; i < nowSpit.length; i++) {
                if (i != 2) {
                    nowSb.append(nowSpit[i]);
                    newSb.append(newSpit[i]);
                }
            }
            //若不同，提示“配方用量已被其他人更新，无法发布，请取消后重试"
            if (Integer.parseInt(nowSb.toString()) != Integer.parseInt(newSb.toString())) {
                throw new BusinessException("配方用量已被其他人更新，无法发布，请取消后重试");
            }
            //若不大于，提示“配方含水率已被其他人更新，无法发布，请取消后重试”
            nowSb.append(nowSpit[2]);
            newSb.append(newSpit[2]);
            if (Integer.parseInt(nowSb.toString()) <= Integer.parseInt(newSb.toString())) {
                throw new BusinessException("配方含水率已被其他人更新，无法发布，请取消后重试");
            }
            SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode("kg", "measure_unit"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

            //2、处理生料信息
            BigDecimal dryBasisQuantityCount = BigDecimal.ZERO;
            BigDecimal wetBasisQuantityCount = BigDecimal.ZERO;
            List<PmsRecipeRaw> pmsRecipeRawModelList = Lists.newArrayList();

            //过滤重复id的物料
            List<PmsRecipeRawDTO> rawList = dto.getRawList();
            rawList = rawList.stream().collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(
                                    PmsRecipeRawDTO::getMaterialId
                            ))), ArrayList::new));
            for (PmsRecipeRawDTO tmp : rawList) {
                //加总生料
                dryBasisQuantityCount = BigDecimalUtil.add(dryBasisQuantityCount, tmp.getDryBasisQuantity(), 4);
                PmsRecipeRaw pmsRecipeRawModel = new PmsRecipeRaw();
                BeanUtil.copyProperties(tmp, pmsRecipeRawModel);
                //湿基含水率
                BigDecimal wetBasisWaterRateNum = BigDecimalUtil.divide(tmp.getWetBasisWaterRate(), BigDecimal.valueOf(Long.parseLong("100")), 6);
                //设置单位
                pmsRecipeRawModel.setDryBasisUnit(sysDictVO.getId());
                pmsRecipeRawModel.setWetBasisUnit(sysDictVO.getId());
                pmsRecipeRawModel.setWetBasisWaterRate(tmp.getWetBasisWaterRate());
                //湿基用量 湿基用量=干基用量/(1-湿基含水率)
                BigDecimal wetBasisQuantity = BigDecimalUtil.divide(tmp.getDryBasisQuantity(),
                        BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")), wetBasisWaterRateNum, 6), 4);
                wetBasisQuantity = BigDecimalUtil.add(wetBasisQuantityCount,wetBasisQuantity,4);
                pmsRecipeRawModel.setWetBasisQuantity(wetBasisQuantity);
                pmsRecipeRawModelList.add(pmsRecipeRawModel);
            }
            //3、处理其他信息
            PmsRecipe recipeModel = new PmsRecipe();
            BeanUtil.copyProperties(newVersion, recipeModel);
            //处理版本号
            Integer smallVersion = Integer.parseInt(newSpit[2]);
            smallVersion++;
            StringBuilder version = new StringBuilder();
            version.append(newSpit[0]);
            version.append("-");
            version.append(newSpit[1]);
            version.append("-");
            if (smallVersion < Integer.parseInt(PmManagerConstant.NUM_10)) {
                version.append("000");
                version.append(smallVersion);
            } else if (smallVersion < Integer.parseInt(PmManagerConstant.NUM_100)) {
                version.append("00");
                version.append(smallVersion);
            } else if (smallVersion < Integer.parseInt(PmManagerConstant.NUM_1000)) {
                version.append("0");
                version.append(smallVersion);
            } else {
                version.append(smallVersion);
            }
            recipeModel.setVersion(version.toString());
            //加水量=干基用量加总/(1-配合料含水率) - 湿基用量加总
            //配料含水率
            BigDecimal oneStep = BigDecimalUtil.divide(newVersion.getBatchWaterRate(), BigDecimal.valueOf(Long.parseLong("100")), 6);
            //1-配料含水率
            BigDecimal twoStep = BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")),oneStep,6);
            //干基用量加总/(1-配合料含水率)
            BigDecimal threeStep = BigDecimalUtil.divide(dryBasisQuantityCount,twoStep,4);
            //干基用量加总/(1-配合料含水率) - 湿基用量加总
            BigDecimal batchWaterQuantity = BigDecimalUtil.subtract(threeStep,wetBasisQuantityCount,4);
            recipeModel.setBatchWaterQuantity(batchWaterQuantity);
            //熟料用量=干基用量加总/(1-熟料占比)- 干基用量加总
            BigDecimal clinkerRateNum = BigDecimalUtil.divide(newVersion.getClinkerRate(), new BigDecimal(100), 6);
            BigDecimal clinkerQuantityNum = BigDecimalUtil.divide(dryBasisQuantityCount, BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")), clinkerRateNum, 6), 4);
            recipeModel.setClinkerQuantity(BigDecimalUtil.subtract(clinkerQuantityNum,dryBasisQuantityCount));
            if (Objects.nonNull(dto.getNote())) {
                recipeModel.setNote(dto.getNote());
            }
            recipeModel.setId(null);
            recipeModel.setCreateName(null);
            recipeModel.setCreateBy(null);
            recipeModel.setCreateTime(null);
            recipeModel.setUpdateBy(null);
            recipeModel.setUpdateName(null);
            recipeModel.setUpdateTime(null);
            save(recipeModel);
            //加入版本id
            pmsRecipeRawModelList.forEach(tmp -> {
                tmp.setRecipeId(recipeModel.getId());
            });
            pmsRecipeRawService.saveBatch(pmsRecipeRawModelList);
            //4、刷新消息
            sendRefreshMsg();
            //5、web端、移动端发送消息给岗位为“化验室主任”的人员、岗位为“配料工”的当前班次人员（调用OA接口获取）
            sendMessage(true, recipeModel.getVersion());

        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean refreshClerkQuantity() {
        List<PmsRecipe> pmsRecipeList = list();
        pmsRecipeList.forEach(tmp->{
            List<PmsRecipeRaw> rawList = pmsRecipeRawService.list(new LambdaQueryWrapper<PmsRecipeRaw>().eq(PmsRecipeRaw::getRecipeId, tmp.getId()));
            BigDecimal dryBasisQuantityCount = BigDecimal.ZERO;
            BigDecimal wetBasisQuantityCount = BigDecimal.ZERO;
            for(PmsRecipeRaw rawTmp:rawList){
                dryBasisQuantityCount = BigDecimalUtil.add(dryBasisQuantityCount,rawTmp.getDryBasisQuantity());
                //湿基含水率
                BigDecimal wetBasisWaterRateNum = BigDecimalUtil.divide(rawTmp.getWetBasisWaterRate(), BigDecimal.valueOf(Long.parseLong("100")), 6);
                BigDecimal wetBasisQuantity = BigDecimalUtil.divide(rawTmp.getDryBasisQuantity(),
                        BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")), wetBasisWaterRateNum, 6), 4);
                wetBasisQuantityCount =  BigDecimalUtil.add(wetBasisQuantityCount,wetBasisQuantity,4);
            }
            BigDecimal batchWaterQuantity = BigDecimalUtil.subtract(
                    BigDecimalUtil.divide(dryBasisQuantityCount,
                            BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")), BigDecimalUtil.divide(tmp.getBatchWaterRate(), BigDecimal.valueOf(Long.parseLong("100")), 6), 6), 4),
                    wetBasisQuantityCount, 4);
            PmsRecipe recipeModel = new PmsRecipe();
            BigDecimal clinkerRateNum = BigDecimalUtil.divide(tmp.getClinkerRate(), new BigDecimal(100), 6);
            BigDecimal clinkerQuantityNum = BigDecimalUtil.divide(dryBasisQuantityCount, BigDecimalUtil.subtract(BigDecimal.valueOf(Long.parseLong("1")), clinkerRateNum, 6), 4);
            recipeModel.setClinkerQuantity(BigDecimalUtil.subtract(clinkerQuantityNum,dryBasisQuantityCount));
            recipeModel.setBatchWaterQuantity(batchWaterQuantity);
            recipeModel.setId(tmp.getId());
            updateById(recipeModel);
        });
        return true;
    }

    /**
     * 发送刷新消息
     */
    protected void sendRefreshMsg() {
        MessScopeVO messScopeVO = remoteMessFeignApi.getUserNameByPermission(PmManagerConstant.RECIPE_PERMISSION_VIEW);
        if (CollUtil.isNotEmpty(messScopeVO.getStringList())) {
            MsgPushDTO msg = new MsgPushDTO();
            msg.setType(WebsocketConstant.WsPushTypeEnum.RECIPE_REFRESH.getType());
            msg.setMsgCategory(SysMsgConstant.MsgCategoryEnum.SYS_REMIND.getCode());
            msg.setMsgType(SysMsgConstant.MsgTypeEnum.RECIPE_REFRESH.getCode());
            List<Integer> ids = FeignOptional.of(() -> oaInfoService.getIdByWorkCode(messScopeVO.getStringList()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<Long> longIds = Sets.newHashSet();
            if (CollUtil.isNotEmpty(ids)) {
                ids.forEach(tmp -> {
                    longIds.add(tmp.longValue());
                });
                //不给自己发消息
                longIds.remove(SecurityUtils.getUser().getUserId());
                if (CollUtil.isNotEmpty(longIds)) {
                    msg.setUserIdList(longIds);
                    FeignOptional.of(() -> remoteWsFeignApi.pushMsg(msg), FeignConstant.STECCN_WEBSOCKET_SERVICE_NAME).get();
                }
            }
        }
    }

    /**
     * 发送配方管理消息 true表示发给副主任 false表示不发
     *
     * @param sendLab
     */
    protected void sendMessage(Boolean sendLab, String version) {
        Set<Long> longIds = Sets.newHashSet();
        if (sendLab) {
            //发送给车间副主任
            OaHrmDTO hrm = new OaHrmDTO();
            hrm.setJobtitle(PmManagerConstant.LABORATORY_DIRECTOR);
            List<OaEmpVO> oaEmpVOS = FeignOptional.of(() -> oaInfoService.getUserByIdByJobTitle(hrm), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            if (CollUtil.isNotEmpty(oaEmpVOS)) {
                oaEmpVOS.forEach(tmp -> {
                    longIds.add(tmp.getId().longValue());
                });
            }
        }
        //发送给配料工
        QtTaskClassCalculate qtTaskClassCalculate = CalculateDateUtil.calculateClassTime(LocalDateTime.now());
        OaRosterRecordDTO rosterDto = new OaRosterRecordDTO();
        rosterDto.setStartdate(qtTaskClassCalculate.getClassDate().toLocalDate().toString());
        rosterDto.setEnddate(qtTaskClassCalculate.getClassDate().toLocalDate().toString());
        rosterDto.setJobtitle(PmManagerConstant.BATCHER_ID);
        List<OaRosterRecordVO> oaRosterRecordVOS = FeignOptional.of(() -> oaInfoService.getRosterRecord(rosterDto), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        //过滤当前班次
        String classStr = "";
        if (qtTaskClassCalculate.getClassType().equals(0)) {
            classStr = "早班";
        } else {
            classStr = "晚班";
        }
        String finalClassStr = classStr;
        oaRosterRecordVOS = oaRosterRecordVOS.stream().filter(v -> v.getSerial().equals(finalClassStr)).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(oaRosterRecordVOS)) {
            List<String> workCodes = Lists.newArrayList();
            oaRosterRecordVOS.forEach(tmp -> {
                workCodes.add(tmp.getWorkcode());
            });
            //找到配料工id
            if (CollUtil.isNotEmpty(workCodes)) {
                List<OaUserIdAndName> oaUserIdAndNames = FeignOptional.of(() -> oaInfoService.getInfoByWorkCode(workCodes), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                if (CollUtil.isNotEmpty(oaUserIdAndNames)) {
                    oaUserIdAndNames.forEach(tmp -> {
                        longIds.add(tmp.getId().longValue());
                    });
                }
            }
        }
        if (CollUtil.isNotEmpty(longIds)) {
            SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
            sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.RECIPE_VERSION.getCode());
            sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.RECIPE_VERSION.getTitle());
            sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.RECIPE_VERSION.getWarnFlag());
            //不给自己发消息

            longIds.remove(SecurityUtils.getUser().getUserId());
            sysMsgDTO.setUserList(longIds);
            String content = SysMsgConstant.MsgTypeEnum.RECIPE_VERSION.getTemplate();
            sysMsgDTO.setContent(String.format(content, SecurityUtils.getUser().getName(), version));
            sysMsgDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
            FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        }
    }

}




