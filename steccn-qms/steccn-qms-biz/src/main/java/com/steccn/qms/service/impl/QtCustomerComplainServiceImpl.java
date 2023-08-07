package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.interfaces.Join;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;

import com.steccn.oa.constant.OaManagerConstant;
import com.steccn.oa.dto.*;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.vo.OaSaveDocVO;
import com.steccn.oa.vo.OaWorkflowStatusVO;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.OaProcessDataDTO;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import com.steccn.platform.dto.ProcessBusiLinkDTO;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteProcessBusiLinkFeignApi;
import com.steccn.platform.vo.SysMsgAdditionDTO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.CustomerComplainPageDTO;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.vo.MessScopeVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.QtCarbonCopyManDTO;
import com.steccn.qms.dto.QtCustomerComplainDTO;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtCustomerComplainMapper;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.*;
import com.steccn.websocket.constant.WebsocketConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【qms_customer_complain(客诉记录表)】的数据库操作Service实现
 * @createDate 2023-03-15 10:16:54
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtCustomerComplainServiceImpl extends ServiceImpl<QtCustomerComplainMapper, QtCustomerComplain> implements QtCustomerComplainService {

    private final RedissonClient redissonClient;
    private final QtComplainDefectImproveManService qtComplainDefectImproveManService;

    private final QtCustomerComplainDetailService qtCustomerComplainDetailService;

    private final RemoteMsgFeignApi remoteMsgFeignApi;

    private final OaInfoService oaInfoService;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final QtComplainTypeService qtComplainTypeService;

    private final QtCustomerComplainMapper qtCustomerComplainMapper;

    @Autowired
    @Lazy
    private QtDealWayService qtDealWayService;

    private final QtComplainDefectProgressService qtComplainDefectProgressService;

    @Autowired
    private QtImproveRecordService qtImproveRecordService;

    private final RemoteProcessBusiLinkFeignApi remoteProcessBusiLinkFeignApi;

    private final RemoteMessFeignApi remoteMesFeignApi;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final QtCustomerComplainModuleService qtCustomerComplainModuleService;

    @Override
    public Boolean add(QtCustomerComplainDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.QT_CUSTOMER_COMPLAIN);
        lock.lock();
        try {
            // 1.基本信息
            QtCustomerComplain qtCustomerComplain = new QtCustomerComplain();
            BeanUtil.copyProperties(dto, qtCustomerComplain);
            qtCustomerComplain.setCode(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_CUSTOMER_COMPLAIN, CodeGenerateUtil.NUM_BIT_8, currentMaxCode()));
            qtCustomerComplain.setStatus(QtManagerConstant.status.PENDING.getCode());
            save(qtCustomerComplain);
            // 2.明细信息
            List<QtCustomerComplainDetail> detailList = Lists.newArrayList();
            dto.getDetails().forEach(detail -> {
                QtCustomerComplainDetail customerComplainDetail = new QtCustomerComplainDetail();
                BeanUtil.copyProperties(detail, customerComplainDetail);
                customerComplainDetail.setCustomerComplainId(qtCustomerComplain.getId());
                detailList.add(customerComplainDetail);
            });
            qtCustomerComplainDetailService.saveBatch(detailList);
            // 组件信息
            List<QtCustomerComplainModule> moduleList = Lists.newArrayList();
            dto.getModules().forEach(module -> {
                QtCustomerComplainModule qtCustomerComplainModule = new QtCustomerComplainModule();
                BeanUtil.copyProperties(module, qtCustomerComplainModule);
                qtCustomerComplainModule.setCustomerComplainId(qtCustomerComplain.getId());
                moduleList.add(qtCustomerComplainModule);
            });
            qtCustomerComplainModuleService.saveBatch(moduleList);
            //3.保存进度信息
            QtComplainDefectProgress qtComplainDefectProgress = new QtComplainDefectProgress();
            qtComplainDefectProgress.setBusiId(qtCustomerComplain.getId());
            qtComplainDefectProgress.setOpStep(QtManagerConstant.progress.CREATE.getName());
            qtComplainDefectProgress.setSort(QtManagerConstant.NUM_0);
            qtComplainDefectProgress.setOpCreateName(SecurityUtils.getUser().getName());
            qtComplainDefectProgress.setOpTime(LocalDateTime.now());
            qtComplainDefectProgress.setBusiType(QtManagerConstant.QtBusinessType.CUSTOMER_COMPLAIN.getCode());
            qtComplainDefectProgressService.save(qtComplainDefectProgress);
            Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(Sets.newHashSet(qtCustomerComplain.getComplainCustomer()))).get();
            Map<Integer, String> typeMap = qtComplainTypeService.listByIdCh(Sets.newHashSet(qtCustomerComplain.getComplainType()));
            //发送消息
            String content = String.format(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getTemplate(), officeMap.get(qtCustomerComplain.getComplainCustomer()), typeMap.get(qtCustomerComplain.getComplainType()), qtCustomerComplain.getTitle());
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setCode(qtCustomerComplain.getCode());
            sysMsgAdditionDTO.setId(qtCustomerComplain.getId());
            sendMessage(sysMsgAdditionDTO, content, Sets.newHashSet(qtCustomerComplain.getDealMan()), SysMsgConstant.MsgCategoryEnum.SYS_REMIND.getCode());
        } finally {
            lock.unlock();
        }
        return Boolean.TRUE;
    }

    @Override
    public Page<QtCustomerComplainVO> listPage(CustomerComplainPageDTO dto) {

        //处理时间
        if (Objects.nonNull(dto.getStartDate()) && Objects.nonNull(dto.getEndDate())) {
            dto.setStartTime(LocalDateTime.of(dto.getStartDate(), LocalTime.MIN));
            dto.setEndTime(LocalDateTime.of(dto.getEndDate(), LocalTime.MAX));
        }
        if (Objects.nonNull(dto.getReplyClientStartDate()) && Objects.nonNull(dto.getReplyClientEndDate())) {
            dto.setReplyClientStartTime(LocalDateTime.of(dto.getStartDate(), LocalTime.MIN));
            dto.setReplyClientEndTime(LocalDateTime.of(dto.getEndDate(), LocalTime.MAX));
        }
        //1.定义查询条件查询数据
        MPJLambdaWrapper<QtCustomerComplain> between = new MPJLambdaWrapper<QtCustomerComplain>()
                .leftJoin(QtCustomerComplainModule.class, QtCustomerComplainModule::getCustomerComplainId, QtCustomerComplain::getId)
                .leftJoin(QtCustomerComplainDetail.class, QtCustomerComplainDetail::getCustomerComplainId, QtCustomerComplain::getId)
                .like(Objects.nonNull(dto.getCode()), QtCustomerComplain::getCode, dto.getCode())
                .like(Objects.nonNull(dto.getTitle()), QtCustomerComplain::getTitle, dto.getTitle())
                .eq(Objects.nonNull(dto.getComplainCustomer()), QtCustomerComplain::getComplainCustomer, dto.getComplainCustomer())
                .eq(Objects.nonNull(dto.getComplainReceiver()), QtCustomerComplain::getComplainReceiver, dto.getComplainReceiver())
                .eq(Objects.nonNull(dto.getComplainType()), QtCustomerComplain::getComplainType, dto.getComplainType())
                .eq(Objects.nonNull(dto.getDealMan()), QtCustomerComplain::getDealMan, dto.getDealMan())
                .eq(Objects.nonNull(dto.getStatus()), QtCustomerComplain::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getDealWay()), QtCustomerComplain::getDealWay, dto.getDealWay())
//                .eq(QtCustomerComplainDetail::getDelFlag,QtManagerConstant.NUM_0)
                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime())
                        , QtCustomerComplain::getComplainDate, dto.getStartTime(), dto.getEndTime())
                .between(Objects.nonNull(dto.getReplyClientStartTime()) && Objects.nonNull(dto.getReplyClientEndTime())
                        , QtCustomerComplain::getReplyClientDate, dto.getReplyClientStartTime(), dto.getReplyClientEndTime())
                .like(Objects.nonNull(dto.getClientCaseNo()), QtCustomerComplainDetail::getClientCaseNo, dto.getClientCaseNo())
                .like(Objects.nonNull(dto.getModuleNumber()), QtCustomerComplainModule::getModuleNumber, dto.getModuleNumber())
                .selectAll(QtCustomerComplain.class);

        if (Objects.nonNull(dto.getSortField())) {
            //定义排序条件
            if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_CODE.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtCustomerComplain::getCode);
            } else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_title.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtCustomerComplain::getTitle);
            } else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_CUSTOMER.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtCustomerComplain::getComplainCustomerName);
            } else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_TYPE.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtCustomerComplain::getComplainTypeName);
            } else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_DATE.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtCustomerComplain::getComplainDate);
            } else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_DEAL_MAN.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtCustomerComplain::getDealManName);
            } else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_RECEIVER.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtCustomerComplain::getComplainReceiverName);
            } else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_DEAL_WAY.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtCustomerComplain::getDealWayName);
            } else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_REPLY_CLIENT_DATE.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtCustomerComplain::getReplyClientDate);
            }
        }
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //判断当前用户的角色是否是客诉管理员
        MessScopeVO role = remoteMesFeignApi.getRole(SecurityUtils.getUser().getName());
        if (CollUtil.isNotEmpty(role.getStringList())) {
            if (!role.getStringList().contains(QtManagerConstant.COMPLAINT_MANAGER)) {
                //不是整改管理员
                between.in(QtCustomerComplain::getId, getUserAuthData());
            }
        } else {
            Set<Long> userAuthData = getUserAuthData();
            if (CollUtil.isNotEmpty(userAuthData)) {
                between.in(QtCustomerComplain::getId, userAuthData);
            } else {
                return page;
            }
        }

        //3.查询分页数据
        IPage<QtCustomerComplain> resPage = qtCustomerComplainMapper.selectJoinPage(page, QtCustomerComplain.class, between);
//        IPage resPage = page(page, between);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        List<QtCustomerComplainVO> qtCustomerComplainVOList = BeanUtil.copyToList(resPage.getRecords(), QtCustomerComplainVO.class);
        //5.处理数据
        if (CollUtil.isNotEmpty(qtCustomerComplainVOList)) {
            List<Long> idList = Lists.newArrayList();
            Set<Integer> officeIds = Sets.newHashSet();
            Set<Integer> typeIds = Sets.newHashSet();
            Set<Integer> wayIds = Sets.newHashSet();
            qtCustomerComplainVOList.forEach(qtCustomerComplainVO -> {
                idList.add(Long.valueOf(qtCustomerComplainVO.getComplainReceiver()));
                idList.add(Long.valueOf(qtCustomerComplainVO.getDealMan()));
                officeIds.add(qtCustomerComplainVO.getComplainCustomer());
                typeIds.add(qtCustomerComplainVO.getComplainType());
                wayIds.add(qtCustomerComplainVO.getDealWay());
                //处理按钮权限
                if (qtCustomerComplainVO.getCreateBy().equals(SecurityUtils.getUser().getUserId())) {
                    qtCustomerComplainVO.setEditFlag(QtManagerConstant.NUM_1);
                }
                if (QtManagerConstant.NUM_0.equals(qtCustomerComplainVO.getStatus())) {
                    //获取当前登陆人是否有处理按钮的权限
                    if (qtCustomerComplainVO.getDealMan().equals(SecurityUtils.getUser().getUserId())) {
                        qtCustomerComplainVO.setDisposeFlag(QtManagerConstant.NUM_1);
                    }
                }
            });
            Map<Long, String> oaUserMap = FeignOptional.of(() -> oaInfoService.listByIdCh(idList), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Integer, String> typeMap = qtComplainTypeService.listByIdCh(typeIds);
            Map<Integer, String> wayMap = qtDealWayService.listByIdCh(wayIds);
            qtCustomerComplainVOList.forEach(qtCustomerComplainVO -> {
                qtCustomerComplainVO.setComplainCustomerName(officeMap.get(qtCustomerComplainVO.getComplainCustomer()));
                qtCustomerComplainVO.setComplainReceiverName(oaUserMap.get(Long.valueOf(qtCustomerComplainVO.getComplainReceiver())));
                qtCustomerComplainVO.setDealManName(oaUserMap.get(Long.valueOf(qtCustomerComplainVO.getDealMan())));
                qtCustomerComplainVO.setComplainTypeName(typeMap.get(qtCustomerComplainVO.getComplainType()));
                if (Objects.nonNull(qtCustomerComplainVO.getDealWay())) {
                    qtCustomerComplainVO.setDealWayName(wayMap.get(qtCustomerComplainVO.getDealWay()));
                }
            });
        }
        return page.setRecords(qtCustomerComplainVOList);
    }

    @Override
    public QtCustomerComplainVO getInfo(Long id) {
        QtCustomerComplainVO qtCustomerComplainVO = new QtCustomerComplainVO();
        QtCustomerComplain customerComplain = getById(id);
        if (Objects.nonNull(customerComplain)) {
            List<QtCustomerComplainDetail> list = qtCustomerComplainDetailService.list(new LambdaQueryWrapper<QtCustomerComplainDetail>().in(QtCustomerComplainDetail::getCustomerComplainId, customerComplain.getId()));
            BeanUtil.copyProperties(customerComplain, qtCustomerComplainVO);
            qtCustomerComplainVO.setDetails(BeanUtil.copyToList(list, QtCustomerComplainDetailVO.class));
            //获取组件信息
            List<QtCustomerComplainModule> modules = qtCustomerComplainModuleService.list(new LambdaQueryWrapper<QtCustomerComplainModule>().in(QtCustomerComplainModule::getCustomerComplainId, customerComplain.getId()));
            qtCustomerComplainVO.setModules(BeanUtil.copyToList(modules, QtCustomerComplainModelVO.class));
            List<Long> idList = Lists.newArrayList();
            idList.add(qtCustomerComplainVO.getComplainReceiver());
            idList.add(qtCustomerComplainVO.getDealMan());
            //获取抄送人 审批人信息
            List<QtComplainDefectImproveMan> manList = qtComplainDefectImproveManService.list(new LambdaQueryWrapper<QtComplainDefectImproveMan>().eq(QtComplainDefectImproveMan::getBusiId, id));
            if (CollUtil.isNotEmpty(manList)) {
                List<QtCarbonCopyManDTO> manVOList = Lists.newArrayList();
                List<Long> userIds = manList.stream().map(QtComplainDefectImproveMan::getUserId).collect(Collectors.toList());
                Map<Long, String> oaUsers = FeignOptional.of(() -> oaInfoService.listByIdCh(userIds), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                manList.forEach(man -> {
                    if (QtManagerConstant.userType.APPROVE_MAN.getCode().equals(man.getUserType())) {
                        qtCustomerComplainVO.setApproveMan(man.getUserId());
                    } else {
                        manVOList.add(new QtCarbonCopyManDTO(man.getUserId(), oaUsers.get(man.getUserId())));
                    }
                });
                qtCustomerComplainVO.setManList(manVOList);
            }

            Map<Long, String> oaUserMap = FeignOptional.of(() -> oaInfoService.listByIdCh(idList), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(qtCustomerComplainVO.getComplainCustomer())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Integer, String> typeMap = qtComplainTypeService.listByIdCh(Sets.newHashSet(customerComplain.getComplainType()));
            if (Objects.nonNull(qtCustomerComplainVO.getDealWay())) {
                Map<Integer, String> integerStringMap = qtDealWayService.listByIdCh(Sets.newHashSet(qtCustomerComplainVO.getDealWay()));
                qtCustomerComplainVO.setDealWayName(integerStringMap.get(qtCustomerComplainVO.getDealWay()));
            }
            qtCustomerComplainVO.setComplainTypeName(typeMap.get(qtCustomerComplainVO.getComplainType()));
            qtCustomerComplainVO.setComplainCustomerName(officeMap.get(qtCustomerComplainVO.getComplainCustomer()));
            qtCustomerComplainVO.setComplainReceiverName(oaUserMap.get(qtCustomerComplainVO.getComplainReceiver()));
            qtCustomerComplainVO.setDealManName(oaUserMap.get(qtCustomerComplainVO.getDealMan()));
            if (Objects.nonNull(qtCustomerComplainVO.getApproveMan())) {
                qtCustomerComplainVO.setApproveManName(oaUserMap.get(qtCustomerComplainVO.getApproveMan()));
            }
            //获取客诉进度信息
            //获取进度信息
            List<QtComplainDefectProgress> progresses = qtComplainDefectProgressService.list(new LambdaQueryWrapper<QtComplainDefectProgress>()
                    .eq(QtComplainDefectProgress::getBusiId, id).orderByAsc(QtComplainDefectProgress::getCreateTime));
            if (CollUtil.isNotEmpty(progresses)) {
                qtCustomerComplainVO.setProgressList(BeanUtil.copyToList(progresses, QtComplainDefectProgressVO.class));
            }
            //获取关联整改内容
            List<QtImproveRecord> recordList = qtImproveRecordService.list(new LambdaQueryWrapper<QtImproveRecord>().eq(QtImproveRecord::getCustomerComplainId, customerComplain.getId()));
            if (CollUtil.isNotEmpty(recordList)) {
                qtCustomerComplainVO.setImproveRecordList(BeanUtil.copyToList(recordList, QtImproveRecordVO.class));
            }
        }
        return qtCustomerComplainVO;
    }

    @Override
    public Boolean edit(QtCustomerComplainDTO qtCustomerComplainDTO) {
        QtCustomerComplain customerComplain = getById(qtCustomerComplainDTO.getId());
        Assert.isTrue(Objects.nonNull(customerComplain), "客诉单不存在");
        BeanUtil.copyProperties(qtCustomerComplainDTO, customerComplain);
        updateById(customerComplain);
        //编辑明细信息
        qtCustomerComplainDetailService.edit(qtCustomerComplainDTO);
        //编辑组件信息
        qtCustomerComplainModuleService.edit(qtCustomerComplainDTO);
        List<Long> idList = Lists.newArrayList();
        idList.add(qtCustomerComplainDTO.getComplainReceiver());
        idList.add(qtCustomerComplainDTO.getDealMan());
        Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(qtCustomerComplainDTO.getComplainCustomer())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Integer, String> typeMap = qtComplainTypeService.listByIdCh(Sets.newHashSet(qtCustomerComplainDTO.getComplainType()));

        return Boolean.TRUE;
    }

    @Override
    public Boolean dispose(QtCustomerComplainDTO qtCustomerComplainDTO) {
        QtCustomerComplain customerComplain = getById(qtCustomerComplainDTO.getId());
        Assert.isTrue(Objects.nonNull(customerComplain), "客诉单不存在");
        BeanUtil.copyProperties(qtCustomerComplainDTO, customerComplain);
        customerComplain.setStatus(QtManagerConstant.status.APPROVE.getCode());
        updateById(customerComplain);
        //客诉处理相关内容
        List<QtComplainDefectImproveMan> manList = Lists.newArrayList();
        QtComplainDefectImproveMan qtComplainDefectImproveMan = new QtComplainDefectImproveMan();
        qtComplainDefectImproveMan.setBusiId(customerComplain.getId());
        qtComplainDefectImproveMan.setUserType(QtManagerConstant.userType.APPROVE_MAN.getCode());
        qtComplainDefectImproveMan.setBusiType(QtManagerConstant.QtBusinessType.CUSTOMER_COMPLAIN.getCode());
        qtComplainDefectImproveMan.setUserId(qtCustomerComplainDTO.getApproveMan());
        manList.add(qtComplainDefectImproveMan);
        if (CollUtil.isNotEmpty(qtCustomerComplainDTO.getManList())) {
            for (QtCarbonCopyManDTO qtCarbonCopyManDTO : qtCustomerComplainDTO.getManList()) {
                QtComplainDefectImproveMan man = new QtComplainDefectImproveMan();
                man.setBusiId(qtCustomerComplainDTO.getId());
                man.setUserId(qtCarbonCopyManDTO.getUserId());
                man.setUserType(QtManagerConstant.userType.CARBON_COPY_MAN.getCode());
                man.setBusiType(QtManagerConstant.QtBusinessType.CUSTOMER_COMPLAIN.getCode());
                manList.add(man);
            }
        }
        qtComplainDefectImproveManService.remove(new LambdaQueryWrapper<QtComplainDefectImproveMan>().eq(QtComplainDefectImproveMan::getBusiId, customerComplain.getId()));
        qtComplainDefectImproveManService.saveBatch(manList);
        //处理进度信息
        QtComplainDefectProgress qtComplainDefectProgress = new QtComplainDefectProgress();
        qtComplainDefectProgress.setBusiId(customerComplain.getId());
        qtComplainDefectProgress.setOpStep(QtManagerConstant.progress.DISPOSE.getName());
        qtComplainDefectProgress.setSort(QtManagerConstant.NUM_1);
        qtComplainDefectProgress.setOpCreateName(SecurityUtils.getUser().getName());
        qtComplainDefectProgress.setOpTime(LocalDateTime.now());
        qtComplainDefectProgress.setBusiType(QtManagerConstant.QtBusinessType.UNACCEPTED_PRODUCT.getCode());
        qtComplainDefectProgressService.save(qtComplainDefectProgress);
        //编辑明细信息
        if (Objects.nonNull(qtCustomerComplainDTO.getDetails())) {
            qtCustomerComplainDetailService.edit(qtCustomerComplainDTO);
        }
        //调用oa流程
        startOaFlow(qtCustomerComplainDTO, customerComplain);

        return Boolean.TRUE;
    }

    @Override
    public Boolean delete(Long id) {
        QtCustomerComplain customerComplain = getById(id);
        Assert.isTrue(Objects.nonNull(customerComplain), "客诉单不存在");
        //删除明细
        qtCustomerComplainDetailService.remove(new LambdaQueryWrapper<QtCustomerComplainDetail>().eq(QtCustomerComplainDetail::getCustomerComplainId, id));
        //删除抄送人 审批人
        qtComplainDefectImproveManService.remove(new LambdaQueryWrapper<QtComplainDefectImproveMan>().eq(QtComplainDefectImproveMan::getBusiId, id));
        return removeById(id);
    }

    @Override
    public Boolean approve(OaProcessNotifyDTO dto) {
        OaProcessDataDTO data = dto.getData();
        ProcessBusiLinkDTO processBusiLinkDTO = FeignOptional.of(() -> remoteProcessBusiLinkFeignApi.get(Integer.valueOf(data.getRequestId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        QtCustomerComplain customerComplain = getById(processBusiLinkDTO.getBusiId());
        Assert.isTrue(Objects.nonNull(customerComplain), "当前业务数据不存在");
        //获取节点操作人信息
        OaWorkFlowOperatorDTO oaWorkFlowOperatorDTO = FeignOptional.of(() -> oaInfoService.getCurrentOperator(new OaOperatorParamDTO(Integer.valueOf(dto.getData().getRequestId()),
                Integer.valueOf(dto.getData().getNodeId()))), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        //审批通过
        if (OaDataConstant.WORKFLOW_AGREE.equals(dto.getType()) && OaDataConstant.WORKFLOW_APPROVE.equals(dto.getData().getNodeName())) {
            Assert.isTrue(QtManagerConstant.NUM_1.equals(customerComplain.getStatus()), "当前状态不可审批通过");
            customerComplain.setStatus(QtManagerConstant.NUM_2);
            //发送消息
            SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
            sysMsgSendDTO.setTitle(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN_APPROVE.getTitle());
            sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN_APPROVE.getCode());
            sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN_APPROVE.getTemplate(), customerComplain.getComplainCustomerName(), customerComplain.getComplainTypeName(), customerComplain.getTitle()));
            sysMsgSendDTO.setUserList(Sets.newHashSet(customerComplain.getCreateBy()));
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setId(customerComplain.getId());
            sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
            sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
            //发送消息
            try {
                Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if (!aBoolean) {
                    log.info(customerComplain + "：消息发送失败");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                throw new BusinessException("消息发送失败");
            }
            //保存进度信息
            QtComplainDefectProgress qtComplainDefectProgress = new QtComplainDefectProgress();
            qtComplainDefectProgress.setBusiId(customerComplain.getId());
            qtComplainDefectProgress.setOpStep(QtManagerConstant.progress.APPROVE.getName());
            qtComplainDefectProgress.setSort(QtManagerConstant.NUM_0);
            qtComplainDefectProgress.setOpCreateName(oaWorkFlowOperatorDTO.getName());
            qtComplainDefectProgress.setOpTime(LocalDateTime.now());
            qtComplainDefectProgress.setBusiType(QtManagerConstant.QtBusinessType.CUSTOMER_COMPLAIN.getCode());
            qtComplainDefectProgressService.save(qtComplainDefectProgress);
        } else if (OaDataConstant.WORKFLOW_DISAGREE.equals(dto.getType()) && OaDataConstant.WORKFLOW_APPROVE.equals(dto.getData().getNodeName())) {
            //退回
            customerComplain.setStatus(QtManagerConstant.NUM_0);
            //发送消息
            SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
            sysMsgSendDTO.setTitle(data.getRequestName());
            sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.SEND_BACK.getCode());
            sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.SEND_BACK.getTemplate(), customerComplain.getCreateName()));
            sysMsgSendDTO.setUserList(Sets.newHashSet(customerComplain.getCreateBy()));
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setId(Long.valueOf(dto.getData().getRequestId()));
            sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
            sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
            //发送消息
            try {
                Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if (!aBoolean) {
                    log.info(customerComplain + "：消息发送失败");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                throw new BusinessException("消息发送失败");
            }
            //保存进度信息
            QtComplainDefectProgress qtComplainDefectProgress = new QtComplainDefectProgress();
            qtComplainDefectProgress.setBusiId(customerComplain.getId());
            qtComplainDefectProgress.setOpStep(QtManagerConstant.progress.APPROVAL_RETURN.getName());
            qtComplainDefectProgress.setSort(QtManagerConstant.NUM_0);
            qtComplainDefectProgress.setOpCreateName(oaWorkFlowOperatorDTO.getName());
            qtComplainDefectProgress.setOpTime(LocalDateTime.now());
            qtComplainDefectProgress.setBusiType(QtManagerConstant.QtBusinessType.CUSTOMER_COMPLAIN.getCode());
            qtComplainDefectProgressService.save(qtComplainDefectProgress);
        }
        return updateById(customerComplain);
    }

    @Override
    public Boolean modelNoFlag(String modelNo) {
        QtCustomerComplainModule one = qtCustomerComplainModuleService.getOne(new LambdaQueryWrapper<QtCustomerComplainModule>().in(QtCustomerComplainModule::getModuleNumber, modelNo).last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(one)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    /**
     * 发送消息
     *
     * @param sysMsgAdditionDTO
     * @param content
     * @param userIds
     * @param pushType
     * @return
     */
    private Boolean sendMessage(SysMsgAdditionDTO sysMsgAdditionDTO, String content, Set<Long> userIds, Integer pushType) {
        //定义消息模板
        SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
        sysMsgSendDTO.setTitle(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getTitle());
        sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getCode());
        sysMsgSendDTO.setContent(content);
        sysMsgSendDTO.setUserList(userIds);
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
        return Boolean.TRUE;
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
        QtCustomerComplain maxCodeModel = getOne(new LambdaQueryWrapper<QtCustomerComplain>().orderByDesc(QtCustomerComplain::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(CodeGenerateUtil.PREFIX_CUSTOMER_COMPLAIN.length());
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }

    private void startOaFlow(QtCustomerComplainDTO dto, QtCustomerComplain qtCustomerComplain) {
        //判断是否发起过流程，如果发起过 提交
        ProcessBusiLinkDTO linkDTO = FeignOptional.of(() -> remoteProcessBusiLinkFeignApi.getByBusiId(qtCustomerComplain.getId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<QtImproveRecord> qtImproveRecords = qtImproveRecordService.list(new LambdaQueryWrapper<QtImproveRecord>().eq(QtImproveRecord::getCustomerComplainId, qtCustomerComplain.getId()));
        //主要数据
        List<OaWorkFlowNameValueDTO> mainDataList = Lists.newArrayList();
        //审批发起人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_SPFQR, SecurityUtils.getUser().getUserId().toString()));
        //客诉单号
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_KSDH, qtCustomerComplain.getCode()));
        //标题
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_KSBT, dto.getTitle()));
        //问题描述
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_MS, saveDoc(dto.getDescUrl()+qtCustomerComplain.getId(),"描述").getDocid().toString()));
        //投诉客户
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_TSKH, dto.getComplainCustomerName()));
        //投诉日期
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_TSRQ, dto.getComplainDate().toString()));
        //投诉接收人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_TSJSR, dto.getComplainReceiverName()));
        //投诉创建人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_TSCJR, qtCustomerComplain.getCreateName()));
        //投诉创建时间
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_TSCJSJ, LocalDateTimeUtil.format(qtCustomerComplain.getCreateTime(), "yyyy-MM-dd HH:mm:ss")));
        //投诉类型
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_TSLX, qtCustomerComplain.getComplainTypeName()));
        //处理人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CLR, qtCustomerComplain.getDealManName()));
        //处理方式
        QtDealWay dealWay = qtDealWayService.getById(qtCustomerComplain.getDealWay());
        if (Objects.nonNull(dealWay)) {
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CLFS, dealWay.getCode().toString()));
        }
        //备注
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_BZ, dto.getNote()));
        //审批人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_SPR, dto.getApproveMan().toString()));
        //抄送人
        if (CollUtil.isNotEmpty(dto.getManList())) {
            List<Long> userIds = dto.getManList().stream().map(QtCarbonCopyManDTO::getUserId).collect(Collectors.toList());
            String ids = userIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CS, ids));
        }
        //详细数据
        List<OaWorkFlowDetailObjDTO> detailDataList = Lists.newArrayList();
        //明细表一
        OaWorkFlowDetailObjDTO oaWorkFlowDetailObjDTO = new OaWorkFlowDetailObjDTO();
        oaWorkFlowDetailObjDTO.setTableDbName(QtManagerConstant.WORKFLOW_TABLE_CUSTOMER_COMPLAIN_DETAIL1);

        List<OaWorkFlowDetailDataListDTO> workflowRequestTableRecords = Lists.newArrayList();
        //获取客诉明细数据
        List<QtCustomerComplainDetail> list = qtCustomerComplainDetailService.list(new LambdaQueryWrapper<QtCustomerComplainDetail>()
                .eq(QtCustomerComplainDetail::getCustomerComplainId, qtCustomerComplain.getId()));
        if (CollUtil.isNotEmpty(list)) {
            Set<Long> dictIds = list.stream().map(QtCustomerComplainDetail::getBadJudgeBasis).collect(Collectors.toSet());
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            list.forEach(detail -> {
                OaWorkFlowDetailDataListDTO oaWorkFlowDetailDataListDTO = new OaWorkFlowDetailDataListDTO();
                List<OaWorkFlowNameValueDTO> valueDTOS = Lists.newArrayList();
                //箱号
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_XH, detail.getCaseNo()));
                //生产批次号
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_SCPCH, detail.getBatchNo()));
                //物料名称
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_WLMC, detail.getMaterialName()));
                //物料编码
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_WLBM, detail.getMaterialCode()));
                //发货数量
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_FHSL, transition(detail.getDeliverQuantity())));
                //投诉数量
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_TSSL, transition(detail.getComplainQuantity())));
                //上线数量
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_SXSL, transition(detail.getOnlineQuantity())));
                //不良率
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_BLL, transition(detail.getDefectRate())));
                //抽检数量
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CJSL, transition(detail.getSpotQuantity())));
                //抽检率
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CJL, transition(detail.getSpotRate())));
                //勘察确认时间
                if (Objects.nonNull(detail.getSurveyDate())) {
                    valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_KCQRRQ, detail.getSurveyDate().toString()));
                }
                //不良判断依据
                if (Objects.nonNull(detail.getBadJudgeBasis())) {
                    valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_BLPDYJ, dictMap.get(detail.getBadJudgeBasis())));
                }
                //勘察确认详细结果
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_KCQRXXJG, detail.getSurveyResult()));
                //勘察确认初步结论
                valueDTOS.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_KCQRCBJ, detail.getSurveyConclusion()));
                oaWorkFlowDetailDataListDTO.setWorkflowRequestTableFields(valueDTOS);
                workflowRequestTableRecords.add(oaWorkFlowDetailDataListDTO);
            });
        }
        oaWorkFlowDetailObjDTO.setWorkflowRequestTableRecords(workflowRequestTableRecords);
        detailDataList.add(oaWorkFlowDetailObjDTO);
        //明细表2
        OaWorkFlowDetailObjDTO oaWorkFlowDetail2 = new OaWorkFlowDetailObjDTO();
        //表名
        oaWorkFlowDetail2.setTableDbName(QtManagerConstant.WORKFLOW_TABLE_CUSTOMER_COMPLAIN_DETAIL2);
        //表中详情数据
        List<OaWorkFlowDetailDataListDTO> workflowRequestTableRecords2 = Lists.newArrayList();
        //数据详情
        //保存url
        if (CollUtil.isNotEmpty(qtImproveRecords)) {
            OaWorkFlowDetailDataListDTO oaWorkFlowDetailDataListDTO = new OaWorkFlowDetailDataListDTO();
            List<OaWorkFlowNameValueDTO> urlTableData = Lists.newArrayList();
            for (QtImproveRecord qtImproveRecord : qtImproveRecords) {
                OaSaveDocVO oaSaveDocVO = FeignOptional.of(() -> oaInfoService.saveDoc(dto.getInfoUrl() + qtImproveRecord.getId(), qtImproveRecord.getTitle()), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                urlTableData.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLZG2, oaSaveDocVO.getDocid().toString()));
                urlTableData.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLZG_MOBILE, qtImproveRecord.getTitle() + "(请在PC端查看整改详情)"));
            }
            oaWorkFlowDetailDataListDTO.setWorkflowRequestTableFields(urlTableData);
            workflowRequestTableRecords2.add(oaWorkFlowDetailDataListDTO);
            oaWorkFlowDetail2.setWorkflowRequestTableRecords(workflowRequestTableRecords2);
            detailDataList.add(oaWorkFlowDetail2);
        }
        if (Objects.nonNull(linkDTO.getId())) {
            //调用OA 提交流程
            OaWorkFlowSubmitParamDTO workFlowSubmitParamDTO = new OaWorkFlowSubmitParamDTO();
            workFlowSubmitParamDTO.setUserId(SecurityUtils.getUser().getUserId());
            workFlowSubmitParamDTO.setRequestId(linkDTO.getInstanceId());
            workFlowSubmitParamDTO.setMainData(mainDataList);
            workFlowSubmitParamDTO.setDetailData(detailDataList);
            workFlowSubmitParamDTO.setWorkFlowId(OaManagerConstant.OaWorkFlow.CUSTOMER_COMPLAIN.getId());
            OaWorkflowStatusVO oaWorkflowStatusVO = FeignOptional.of(() -> oaInfoService.submitWorkflow(workFlowSubmitParamDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            if (!oaWorkflowStatusVO.getCode().equals(PmManagerConstant.WORKFLOW_SUCCESS)) {
                throw new BusinessException("流程提交失败," + oaWorkflowStatusVO.getErrMsg());
            }
            //发送消息
            SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
            sysMsgSendDTO.setTitle(sendTitle(OaManagerConstant.OaWorkFlow.CUSTOMER_COMPLAIN.getName(), SecurityUtils.getUser().getName()));
            sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.SEND_BACK.getCode());
            String content = String.format(SysMsgConstant.MsgTypeEnum.SEND_BACK.getTemplate(), SecurityUtils.getUser().getName());
            sysMsgSendDTO.setContent(content);
            sysMsgSendDTO.setUserList(Sets.newHashSet(dto.getApproveMan()));
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setId(Long.valueOf(oaWorkflowStatusVO.getRequestId()));
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
        } else {
            //调用oa创建流程
            OaWorkFlowParamDTO workflowDTO = new OaWorkFlowParamDTO();
            workflowDTO.setUserId(SecurityUtils.getUser().getUserId());
            workflowDTO.setWorkflowId(OaManagerConstant.OaWorkFlow.CUSTOMER_COMPLAIN.getId());
            workflowDTO.setRequestName(sendTitle(OaManagerConstant.OaWorkFlow.CUSTOMER_COMPLAIN.getName(), SecurityUtils.getUser().getName()));
            workflowDTO.setMainData(mainDataList);
            workflowDTO.setDetailData(detailDataList);
            //调用
            OaWorkflowStatusVO oaWorkflowStatusVO = FeignOptional.of(() -> oaInfoService.createWorkflow(workflowDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            if (!oaWorkflowStatusVO.getCode().equals(PmManagerConstant.WORKFLOW_SUCCESS)) {
                throw new BusinessException("流程创建失败," + oaWorkflowStatusVO.getErrMsg());
            }
            //保存oa数据与流程关联
            ProcessBusiLinkDTO process = new ProcessBusiLinkDTO();
            process.setBusiId(qtCustomerComplain.getId());
            process.setInstanceId(oaWorkflowStatusVO.getRequestId());
            process.setBusiType(QtManagerConstant.QtBusinessType.CUSTOMER_COMPLAIN.getCode());
            Boolean aBoolean = FeignOptional.of(() -> remoteProcessBusiLinkFeignApi.add(process), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (!aBoolean) {
                throw new BusinessException("调用保存业务数据失败");
            }
            //发送消息
            SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
            sysMsgSendDTO.setTitle(sendTitle(OaManagerConstant.OaWorkFlow.CUSTOMER_COMPLAIN.getName(), SecurityUtils.getUser().getName()));
            sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.SEND_BACK.getCode());
            String content = String.format(SysMsgConstant.MsgTypeEnum.SEND_BACK.getTemplate(), SecurityUtils.getUser().getName());
            sysMsgSendDTO.setContent(content);
            sysMsgSendDTO.setUserList(Sets.newHashSet(dto.getApproveMan()));
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setId(Long.valueOf(oaWorkflowStatusVO.getRequestId()));
            sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
            sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
            try {
                Boolean a = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if (!a) {
                    log.info("消息发送失败");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                throw new BusinessException("消息发送失败");
            }
        }

    }

    private String transition(BigDecimal value) {
        if (Objects.nonNull(value)) {
            return value.toString();
        }
        return "";
    }


    /**
     * 获取当前用户拥有的数据权限
     *
     * @return
     */
    private Set<Long> getUserAuthData() {
        Set<Long> recordIds = Sets.newHashSet();
        //获取处理人为自己的记录
        List<QtCustomerComplain> list = list(new LambdaQueryWrapper<QtCustomerComplain>().eq(QtCustomerComplain::getDealMan, SecurityUtils.getUser().getUserId()));
        if (CollUtil.isNotEmpty(list)) {
            recordIds.addAll(list.stream().map(QtCustomerComplain::getId).collect(Collectors.toSet()));
        }
        //获取创建人为自己的记录
        List<QtCustomerComplain> customerComplainList = list(new LambdaQueryWrapper<QtCustomerComplain>().eq(QtCustomerComplain::getCreateBy, SecurityUtils.getUser().getUserId()));
        if (CollUtil.isNotEmpty(customerComplainList)) {
            recordIds.addAll(customerComplainList.stream().map(QtCustomerComplain::getId).collect(Collectors.toSet()));
        }
        //获取审批人为自己的记录或者抄送人为自己
        List<QtComplainDefectImproveMan> qtComplainDefectImproveManList = qtComplainDefectImproveManService.list(new LambdaQueryWrapper<QtComplainDefectImproveMan>()
                .eq(QtComplainDefectImproveMan::getUserId, SecurityUtils.getUser())
                .eq(QtComplainDefectImproveMan::getBusiType, QtManagerConstant.QtBusinessType.CUSTOMER_COMPLAIN));
        if (CollUtil.isNotEmpty(qtComplainDefectImproveManList)) {
            recordIds.addAll(qtComplainDefectImproveManList.stream().map(QtComplainDefectImproveMan::getBusiId).collect(Collectors.toSet()));
        }
        return recordIds;
    }


    /**
     * 生成流程实例标题  流程实例名称-流程发起人名称-日期
     *
     * @param titleName
     * @param createName
     * @return
     */
    private String sendTitle(String titleName, String createName) {
        return titleName + QtManagerConstant.WHIPPTREE + createName + QtManagerConstant.WHIPPTREE + LocalDate.now();
    }


    /**
     * 保存infourl的详情内容
     * @param infoUrl
     * @param docName
     * @return
     */
    private OaSaveDocVO saveDoc(String infoUrl, String docName) {
        return FeignOptional.of(() -> oaInfoService.saveDoc(infoUrl, docName), FeignConstant.STECCN_OA_SERVICE_NAME).get();
    }
}




