package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.ResultCode;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.vo.SysMsgAdditionDTO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.QtCarbonCopyManDTO;
import com.steccn.qms.dto.QtDefectProductDTO;
import com.steccn.qms.dto.QtDefectProductPageDTO;
import com.steccn.qms.service.QtComplainDefectImproveManService;
import com.steccn.qms.service.QtComplainDefectProgressService;
import com.steccn.qms.service.QtCustomerComplainService;
import com.steccn.qms.service.QtDefectProductService;
import com.steccn.qms.mapper.QtDefectProductMapper;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.QtComplainDefectProgressVO;
import com.steccn.qms.vo.QtCustomerComplainVO;
import com.steccn.qms.vo.QtDefectProductVO;
import com.steccn.websocket.constant.WebsocketConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【qms_defect_product(不合格品处理表)】的数据库操作Service实现
 * @createDate 2023-03-15 10:16:54
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtDefectProductServiceImpl extends ServiceImpl<QtDefectProductMapper, QtDefectProduct>
        implements QtDefectProductService {

    private final RedissonClient redissonClient;

    private final RemoteMsgFeignApi remoteMsgFeignApi;

    private final QtComplainDefectImproveManService qtComplainDefectImproveManService;

    private final QtComplainDefectProgressService qtComplainDefectProgressService;

    private final OaInfoService oaInfoService;


    @Override
    public Boolean add(QtDefectProductDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.QT_DEFECT_PRODUCT);
        lock.lock();
        try {
            QtDefectProduct qtDefectProduct = new QtDefectProduct();
            BeanUtil.copyProperties(dto, qtDefectProduct);
            qtDefectProduct.setCode(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_DEFECT_PRODUCT, CodeGenerateUtil.NUM_BIT_8, currentMaxCode()));
            qtDefectProduct.setStatus(QtManagerConstant.status.PENDING.getCode());
            save(qtDefectProduct);
            //保存进度信息
            QtComplainDefectProgress qtComplainDefectProgress = new QtComplainDefectProgress();
            qtComplainDefectProgress.setBusiId(qtDefectProduct.getId());
            qtComplainDefectProgress.setOpStep(QtManagerConstant.progress.CREATE.getName());
            qtComplainDefectProgress.setSort(QtManagerConstant.NUM_0);
            qtComplainDefectProgress.setOpCreateName(SecurityUtils.getUser().getName());
            qtComplainDefectProgress.setOpTime(LocalDateTime.now());
            qtComplainDefectProgress.setBusiType(QtManagerConstant.QtBusinessType.UNACCEPTED_PRODUCT.getCode());
            qtComplainDefectProgressService.save(qtComplainDefectProgress);
            //发送消息
            SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
            sysMsgSendDTO.setTitle(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getTitle());
            sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getCode());
            sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.DEFECT_PRODUCT.getTemplate(),dto.getTitle(),dto.getDescription(),dto.getTaskTypeName()));
            sysMsgSendDTO.setUserList(Sets.newHashSet(qtDefectProduct.getDealMan()));
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setCode(qtDefectProduct.getCode());
            sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
            sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
            try {
                Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if (!aBoolean) {
                    log.info("消息发送失败");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                throw new BusinessException("消息发送失败");
            }
            return Boolean.TRUE;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Page<QtDefectProductVO> listPage(QtDefectProductPageDTO dto) {
        //处理时间
        if (Objects.nonNull(dto.getStartDate()) && Objects.nonNull(dto.getEndDate())) {
            dto.setStartTime(LocalDateTime.of(dto.getStartDate(), LocalTime.MIN));
            dto.setEndTime(LocalDateTime.of(dto.getEndDate(), LocalTime.MAX));
        }
        //1.定义查询条件查询数据
        LambdaQueryWrapper<QtDefectProduct> between = new LambdaQueryWrapper<QtDefectProduct>()
                .like(Objects.nonNull(dto.getCode()), QtDefectProduct::getCode, dto.getCode())
                .like(Objects.nonNull(dto.getTitle()), QtDefectProduct::getTitle, dto.getTitle())
                .eq(Objects.nonNull(dto.getTaskCode()), QtDefectProduct::getTaskCode, dto.getTaskCode())
                .eq(Objects.nonNull(dto.getTaskType()), QtDefectProduct::getTaskType, dto.getTaskType())
                .eq(Objects.nonNull(dto.getMaterialName()), QtDefectProduct::getMaterialName, dto.getMaterialName())
                .eq(Objects.nonNull(dto.getDealMan()), QtDefectProduct::getDealMan, dto.getDealMan())
                .eq(Objects.nonNull(dto.getStatus()), QtDefectProduct::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getDealWay()), QtDefectProduct::getDealWay, dto.getDealWay())
                .eq(Objects.nonNull(dto.getTaskFinishMan()),QtDefectProduct::getTaskFinishMan,dto.getTaskFinishMan())
                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()),
                        QtDefectProduct::getTaskFinishTime, dto.getStartTime(), dto.getEndTime());
        if (Objects.nonNull(dto.getSortField())){
            //定义排序条件
            if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_CODE.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtDefectProduct::getCode);
            }else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_title.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtDefectProduct::getTitle);
            }else if (QtManagerConstant.QT_DEFECT_PRODUCT_TASK_CODE.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtDefectProduct::getTaskCode);
            }else if (QtManagerConstant.QT_DEFECT_PRODUCT_TASK_TYPE.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtDefectProduct::getTaskTypeName);
            }else if (QtManagerConstant.QT_DEFECT_PRODUCT_MATERIAL_NAME.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtDefectProduct::getMaterialName);
            }else if (QtManagerConstant.QT_DEFECT_PRODUCT_DEAL_MAN.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtDefectProduct::getDealManName);
            }else if (QtManagerConstant.QT_DEFECT_PRODUCT_TASK_FINISH_MAN.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtDefectProduct::getTaskFinishManName);
            }else if (QtManagerConstant.QT_DEFECT_PRODUCT_DEAL_WAY.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtDefectProduct::getDealWayName);
            }else if (QtManagerConstant.QT_DEFECT_PRODUCT_TASK_FINISH_TIME.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtDefectProduct::getTaskFinishTime);
            }else if (QtManagerConstant.QT_DEFECT_PRODUCT_STATUS.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtDefectProduct::getStatus);
            }
        }
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, between);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        List<QtDefectProductVO> qtCustomerComplainVOList = BeanUtil.copyToList(resPage.getRecords(), QtDefectProductVO.class);
        return page.setRecords(qtCustomerComplainVOList);
    }

    @Override
    public Boolean edit(QtDefectProductDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.QT_DEFECT_PRODUCT);
        lock.lock();
        try {
            QtDefectProduct qtDefectProduct = getById(dto.getId());
            Assert.isTrue(Objects.nonNull(qtDefectProduct.getId()), "记录不存在");
            BeanUtil.copyProperties(dto, qtDefectProduct);
            //发送消息
            SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
            sysMsgSendDTO.setTitle(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getTitle());
            sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getCode());
            sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.DEFECT_PRODUCT.getTemplate(), dto.getTitle(), dto.getDescription(), dto.getTaskTypeName()));
            sysMsgSendDTO.setUserList(Sets.newHashSet(qtDefectProduct.getDealMan()));
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setCode(qtDefectProduct.getCode());
            sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
            sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
            //发送消息
            try {
                Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if (!aBoolean) {
                    log.info("消息发送失败");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                throw new BusinessException("消息发送失败");
            }
            return updateById(qtDefectProduct);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public Boolean delete(Long id) {
        QtDefectProduct qtDefectProduct = getById(id);
        Assert.isTrue(Objects.nonNull(qtDefectProduct.getId()), "记录不存在");
        return removeById(id);
    }

    @Override
    public Boolean dispose(QtDefectProductDTO dto) {
        QtDefectProduct qtDefectProduct = getById(dto.getId());
        Assert.isTrue(Objects.nonNull(qtDefectProduct.getId()), "记录不存在");
        BeanUtil.copyProperties(dto, qtDefectProduct);
        qtDefectProduct.setStatus(QtManagerConstant.status.APPROVE.getCode());
        List<QtComplainDefectImproveMan> manList = Lists.newArrayList();
        //保存抄送人审批人信息
        QtComplainDefectImproveMan qtComplainDefectImproveMan = new QtComplainDefectImproveMan();
        qtComplainDefectImproveMan.setBusiId(qtDefectProduct.getId());
        qtComplainDefectImproveMan.setUserType(QtManagerConstant.userType.APPROVE_MAN.getCode());
        qtComplainDefectImproveMan.setBusiType(QtManagerConstant.QtBusinessType.CUSTOMER_COMPLAIN.getCode());
        qtComplainDefectImproveMan.setUserId(dto.getApproveMan());
        manList.add(qtComplainDefectImproveMan);
        //保存进度信息
        QtComplainDefectProgress qtComplainDefectProgress = new QtComplainDefectProgress();
        qtComplainDefectProgress.setBusiId(qtDefectProduct.getId());
        qtComplainDefectProgress.setOpStep(QtManagerConstant.progress.DISPOSE.getName());
        qtComplainDefectProgress.setSort(QtManagerConstant.NUM_1);
        qtComplainDefectProgress.setOpCreateName(SecurityUtils.getUser().getName());
        qtComplainDefectProgress.setOpTime(LocalDateTime.now());
        qtComplainDefectProgress.setBusiType(QtManagerConstant.QtBusinessType.UNACCEPTED_PRODUCT.getCode());
        qtComplainDefectProgressService.save(qtComplainDefectProgress);
        if (CollUtil.isNotEmpty(dto.getManList())) {
            for (QtCarbonCopyManDTO qtCarbonCopyManDTO : dto.getManList()) {
                QtComplainDefectImproveMan man = new QtComplainDefectImproveMan();
                man.setBusiId(dto.getId());
                man.setUserId(qtCarbonCopyManDTO.getUserId());
                man.setUserType(QtManagerConstant.userType.CARBON_COPY_MAN.getCode());
                man.setBusiType(QtManagerConstant.QtBusinessType.UNACCEPTED_PRODUCT.getCode());
                manList.add(man);
            }
        }
        qtComplainDefectImproveManService.remove(new LambdaQueryWrapper<QtComplainDefectImproveMan>().eq(QtComplainDefectImproveMan::getBusiId,dto.getId()));
        qtComplainDefectImproveManService.saveBatch(manList);
        return  updateById(qtDefectProduct);
    }

    @Override
    public QtDefectProductVO get(Long id) {
        QtDefectProduct qtDefectProduct = getById(id);
        Assert.isTrue(Objects.nonNull(qtDefectProduct), "记录不存在");
        QtDefectProductVO qtDefectProductVO = new QtDefectProductVO();
        BeanUtil.copyProperties(qtDefectProduct,qtDefectProductVO);
        //获取进度信息
        List<QtComplainDefectProgress> progresses = qtComplainDefectProgressService.list(new LambdaQueryWrapper<QtComplainDefectProgress>()
                .eq(QtComplainDefectProgress::getBusiId, id).orderByAsc(QtComplainDefectProgress::getCreateTime));
        if (CollUtil.isNotEmpty(progresses)){
            qtDefectProductVO.setProgressList(BeanUtil.copyToList(progresses, QtComplainDefectProgressVO.class));
        }
        //获取抄送人审批人信息
        List<QtComplainDefectImproveMan> list = qtComplainDefectImproveManService
                .list(new LambdaQueryWrapper<QtComplainDefectImproveMan>()
                .eq(QtComplainDefectImproveMan::getBusiId, id));
        if (CollUtil.isNotEmpty(list)){
            List<Long> userIds = list.stream().map(QtComplainDefectImproveMan::getUserId).collect(Collectors.toList());
            Map<Long, String> map = FeignOptional.of(() -> oaInfoService.listByIdCh(userIds), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            List<QtCarbonCopyManDTO> manList = Lists.newArrayList();
            list.forEach(man ->{
                if (QtManagerConstant.NUM_0.equals(man.getUserType())){
                    qtDefectProductVO.setApproveMan(man.getUserId());
                    qtDefectProductVO.setApproveManName(map.get(man.getUserId()));
                }else {
                    QtCarbonCopyManDTO qtCarbonCopyManDTO = new QtCarbonCopyManDTO();
                    qtCarbonCopyManDTO.setUserId(man.getUserId());
                    qtCarbonCopyManDTO.setUserName(map.get(man.getUserId()));
                    manList.add(qtCarbonCopyManDTO);
                }
            });
            qtDefectProductVO.setManList(manList);
        }
        return qtDefectProductVO;
    }


    /**
     * 当前最大的code值
     *
     * @return 数值类型，可能为null
     */
    private Long currentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        LocalDateTime max = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        QtDefectProduct maxCodeModel = getOne(new LambdaQueryWrapper<QtDefectProduct>().between(QtDefectProduct::getCreateTime, min, max).orderByDesc(QtDefectProduct::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(CodeGenerateUtil.PREFIX_DEFECT_PRODUCT.length());
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }
}




