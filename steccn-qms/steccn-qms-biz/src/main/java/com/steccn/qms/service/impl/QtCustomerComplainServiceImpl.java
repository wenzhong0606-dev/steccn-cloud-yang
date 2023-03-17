package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.RedisConfig;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysMsgAdditionDTO;
import com.steccn.pms.dto.CustomerComplainPageDTO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.QtCustomerComplainDTO;
import com.steccn.qms.dto.QtCustomerComplainDetailDTO;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtCustomerComplainMapper;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.QtCustomerComplainDetailVO;
import com.steccn.qms.vo.QtCustomerComplainVO;
import com.steccn.websocket.constant.WebsocketConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private  QtDealWayService qtDealWayService;

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
            Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(Sets.newHashSet(qtCustomerComplain.getComplainCustomer()))).get();
            Map<Integer, String> typeMap = qtComplainTypeService.listByIdCh(Sets.newHashSet(qtCustomerComplain.getComplainType()));
            //发送消息
            sendMessage(officeMap.get(qtCustomerComplain.getComplainCustomer()), typeMap.get(qtCustomerComplain.getComplainType()), qtCustomerComplain.getTitle(), Sets.newHashSet(qtCustomerComplain.getDealMan()), qtCustomerComplain.getCode());
            // 流程相关
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
        //1.定义查询条件查询数据
        LambdaQueryWrapper<QtCustomerComplain> between = new LambdaQueryWrapper<QtCustomerComplain>().like(Objects.nonNull(dto.getCode()), QtCustomerComplain::getCode, dto.getCode()).like(Objects.nonNull(dto.getTitle()), QtCustomerComplain::getTitle, dto.getTitle()).eq(Objects.nonNull(dto.getComplainCustomer()), QtCustomerComplain::getComplainCustomer, dto.getComplainCustomer()).eq(Objects.nonNull(dto.getComplainReceiver()), QtCustomerComplain::getComplainReceiver, dto.getComplainReceiver()).eq(Objects.nonNull(dto.getComplainType()), QtCustomerComplain::getComplainType, dto.getComplainType()).eq(Objects.nonNull(dto.getDealMan()), QtCustomerComplain::getDealMan, dto.getDealMan()).eq(Objects.nonNull(dto.getStatus()), QtCustomerComplain::getStatus, dto.getStatus()).eq(Objects.nonNull(dto.getDealWay()), QtCustomerComplain::getDealWay, dto.getDealWay()).between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()), QtCustomerComplain::getComplainDate, dto.getStartTime(), dto.getEndTime());
        if (Objects.nonNull(dto.getSortField())){
            //定义排序条件
            if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_CODE.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtCustomerComplain::getCode);
            }else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_title.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtCustomerComplain::getTitle);
            }else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_CUSTOMER.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtCustomerComplain::getComplainCustomerName);
            }else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_TYPE.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtCustomerComplain::getComplainTypeName);
            }else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_DATE.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtCustomerComplain::getComplainDate);
            }else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_DEAL_MAN.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtCustomerComplain::getDealManName);
            }else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_RECEIVER.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtCustomerComplain::getComplainReceiverName);
            }else if (QtManagerConstant.QT_CUSTOMER_COMPLAIN_DEAL_WAY.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtCustomerComplain::getDealWayName);
            }
        }
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, between);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        List<QtCustomerComplainVO> qtCustomerComplainVOList = BeanUtil.copyToList(resPage.getRecords(), QtCustomerComplainVO.class);
        //5.处理数据
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
            if (Objects.nonNull(qtCustomerComplainVO.getDealWay())){
                qtCustomerComplainVO.setDealWayName(wayMap.get(qtCustomerComplainVO.getDealWay()));
            }
        });
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
            List<Long> idList = Lists.newArrayList();
            idList.add(qtCustomerComplainVO.getComplainReceiver());
            idList.add(qtCustomerComplainVO.getDealMan());
            //获取抄送人 审批人信息
            List<QtComplainDefectImproveMan> manList = qtComplainDefectImproveManService.list(new LambdaQueryWrapper<QtComplainDefectImproveMan>().eq(QtComplainDefectImproveMan::getBusiId, id));
            if (CollUtil.isNotEmpty(manList)) {
                manList.forEach(man -> {
                    idList.add(man.getUserId());
                    if (QtManagerConstant.userType.APPROVE_MAN.getCode().equals(man.getUserType())) {
                        qtCustomerComplainVO.setApproveMan(man.getUserId());
                    } else {
                        qtCustomerComplainVO.setCarbonCopyMan(man.getUserId());
                    }
                });
            }
            Map<Long, String> oaUserMap = FeignOptional.of(() -> oaInfoService.listByIdCh(idList), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(qtCustomerComplainVO.getComplainCustomer())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Integer, String> typeMap = qtComplainTypeService.listByIdCh(Sets.newHashSet(customerComplain.getComplainType()));
            if (Objects.nonNull(qtCustomerComplainVO.getDealWay())){
                Map<Integer, String> integerStringMap = qtDealWayService.listByIdCh(Sets.newHashSet(qtCustomerComplainVO.getDealWay()));
                qtCustomerComplainVO.setDealWayName(integerStringMap.get(qtCustomerComplainVO.getDealWay()));
            }
            qtCustomerComplainVO.setComplainTypeName(typeMap.get(qtCustomerComplainVO.getComplainType()));
            qtCustomerComplainVO.setComplainCustomerName(officeMap.get(qtCustomerComplainVO.getComplainCustomer()));
            qtCustomerComplainVO.setComplainReceiverName(oaUserMap.get(qtCustomerComplainVO.getComplainReceiver()));
            qtCustomerComplainVO.setDealManName(oaUserMap.get(qtCustomerComplainVO.getDealMan()));
            if (Objects.nonNull(qtCustomerComplainVO.getApproveMan())){
                qtCustomerComplainVO.setApproveManName(oaUserMap.get(qtCustomerComplainVO.getApproveMan()));
            }
            if (Objects.nonNull(qtCustomerComplainVO.getCarbonCopyMan())){
                qtCustomerComplainVO.setCarbonCopyManName(oaUserMap.get(qtCustomerComplainVO.getCarbonCopyMan()));
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
        if (Objects.nonNull(qtCustomerComplainDTO.getDetails())) {
            qtCustomerComplainDetailService.edit(qtCustomerComplainDTO);
        }
        List<Long> idList = Lists.newArrayList();
        idList.add(qtCustomerComplainDTO.getComplainReceiver());
        idList.add(qtCustomerComplainDTO.getDealMan());
        Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(qtCustomerComplainDTO.getComplainCustomer())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Integer, String> typeMap = qtComplainTypeService.listByIdCh(Sets.newHashSet(qtCustomerComplainDTO.getComplainType()));
        //发送消息
        sendMessage(officeMap.get(qtCustomerComplainDTO.getComplainCustomer()), typeMap.get(qtCustomerComplainDTO.getComplainType()), qtCustomerComplainDTO.getTitle(), Sets.newHashSet(qtCustomerComplainDTO.getDealMan()), customerComplain.getCode());
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
        if (Objects.nonNull(qtCustomerComplainDTO.getCarbonCopyMan())) {
            QtComplainDefectImproveMan man = new QtComplainDefectImproveMan();
            man.setBusiId(customerComplain.getId());
            man.setUserId(qtCustomerComplainDTO.getCarbonCopyMan());
            man.setUserType(QtManagerConstant.userType.CARBON_COPY_MAN.getCode());
            man.setBusiType(QtManagerConstant.QtBusinessType.CUSTOMER_COMPLAIN.getCode());
            manList.add(man);
        }
        qtComplainDefectImproveManService.saveBatch(manList);
        //编辑明细信息
        if (Objects.nonNull(qtCustomerComplainDTO.getDetails())) {
            qtCustomerComplainDetailService.edit(qtCustomerComplainDTO);
        }
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


    /**
     * 发送消息
     *
     * @param complainCustomerName 客户名称
     * @param complainTypeName     投诉类型
     * @param title                标签
     * @param userIds              消息接收人
     * @param orderCode            单号
     * @return
     */
    private Boolean sendMessage(String complainCustomerName, String complainTypeName, String title, Set<Long> userIds, String orderCode) {
        //定义消息模板
        SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
        sysMsgSendDTO.setTitle(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getTitle());
        sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getCode());
        sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getTemplate(), complainCustomerName, complainTypeName, title));
        sysMsgSendDTO.setUserList(userIds);
        SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
        sysMsgAdditionDTO.setCode(orderCode);
        sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
        sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
        //发送消息
        try {
            Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (!aBoolean) {
                log.info(complainCustomerName + "：消息发送失败");
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
        LocalDateTime max = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        QtCustomerComplain maxCodeModel = getOne(new LambdaQueryWrapper<QtCustomerComplain>().between(QtCustomerComplain::getCreateTime, min, max).orderByDesc(QtCustomerComplain::getCode).last(MpConstant.STR_LIMIT_1));
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
}




