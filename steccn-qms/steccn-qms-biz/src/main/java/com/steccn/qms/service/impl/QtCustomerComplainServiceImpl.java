package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import com.steccn.qms.domain.QtComplainDefectImproveMan;
import com.steccn.qms.domain.QtCustomerComplain;
import com.steccn.qms.domain.QtCustomerComplainDetail;
import com.steccn.qms.dto.QtCustomerComplainDTO;
import com.steccn.qms.dto.QtCustomerComplainDetailDTO;
import com.steccn.qms.service.QtComplainDefectImproveManService;
import com.steccn.qms.service.QtCustomerComplainDetailService;
import com.steccn.qms.service.QtCustomerComplainService;
import com.steccn.qms.mapper.QtCustomerComplainMapper;
import com.steccn.qms.vo.QtCustomerComplainVO;
import com.steccn.websocket.constant.WebsocketConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    @Override
    public Boolean add(QtCustomerComplainDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.QT_CUSTOMER_COMPLAIN);
        lock.lock();
        try {
            // 1.基本信息
            QtCustomerComplain qtCustomerComplain = new QtCustomerComplain();
            BeanUtil.copyProperties(dto, qtCustomerComplain);
            qtCustomerComplain.setCode(QtManagerConstant.PREFIX_CUSTOMER_COMPLAIN + currentMaxCode());
            qtCustomerComplain.setStatus(QtManagerConstant.status.PENDING.getCode());
            save(qtCustomerComplain);
//            List<QtComplainDefectImproveMan> manList = Lists.newArrayList();
//            QtComplainDefectImproveMan qtComplainDefectImproveMan = new QtComplainDefectImproveMan();
//            qtComplainDefectImproveMan.setBusiId(qtCustomerComplain.getId());
//            qtComplainDefectImproveMan.setUserType(QtManagerConstant.userType.APPROVE_MAN.getCode());
//            qtComplainDefectImproveMan.setBusiType(QtManagerConstant.QtBusinessType.CUSTOMER_COMPLAIN.getCode());
//            qtComplainDefectImproveMan.setUserId(Long.valueOf(dto.getApproveMan()));
//            manList.add(qtComplainDefectImproveMan);
//            if (Objects.nonNull(dto.getCarbonCopyMan())) {
//                QtComplainDefectImproveMan man = new QtComplainDefectImproveMan();
//                man.setBusiId(qtCustomerComplain.getId());
//                man.setUserType(QtManagerConstant.userType.CARBON_COPY_MAN.getCode());
//                man.setBusiType(QtManagerConstant.QtBusinessType.CUSTOMER_COMPLAIN.getCode());
//                manList.add(man);
//            }
//            qtComplainDefectImproveManService.saveBatch(manList);
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
            //发送消息
//            sendMessage(officeMap.get(qtCustomerComplain.getComplainCustomer()),)
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
        qtCustomerComplainVOList.forEach(qtCustomerComplainVO -> {
            idList.add(Long.valueOf(qtCustomerComplainVO.getComplainReceiver()));
            idList.add(Long.valueOf(qtCustomerComplainVO.getDealMan()));
            officeIds.add(qtCustomerComplainVO.getComplainCustomer());
        });
        Map<Long, String> oaUserMap = FeignOptional.of(() -> oaInfoService.listByIdCh(idList), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        qtCustomerComplainVOList.forEach(qtCustomerComplainVO -> {
            qtCustomerComplainVO.setComplainCustomerName(officeMap.get(qtCustomerComplainVO.getComplainCustomer()));
            qtCustomerComplainVO.setComplainReceiverName(oaUserMap.get(Long.valueOf(qtCustomerComplainVO.getComplainReceiver())));
            qtCustomerComplainVO.setDealManName(oaUserMap.get(Long.valueOf(qtCustomerComplainVO.getDealMan())));
        });
        return page.setRecords(qtCustomerComplainVOList);
    }


    /**
     * 发送消息
     *
     * @param complainCustomerName 客户名称
     * @param complainTypeName     投诉类型
     * @param title                标签
     * @param userIds              消息接收人
     * @param orderId              单号
     * @return
     */
    private Boolean sendMessage(String complainCustomerName, String complainTypeName, String title, Set<Long> userIds, Long orderId) {
        //定义消息模板
        SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
        sysMsgSendDTO.setTitle(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getTitle());
        sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getCode());
        sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.CUSTOMER_COMPLAIN.getTemplate(), complainCustomerName, complainTypeName, title));
        sysMsgSendDTO.setUserList(userIds);
        SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
        sysMsgAdditionDTO.setId(orderId);
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
                String substringVal = maxCodeModel.getCode().substring(QtManagerConstant.PREFIX_CUSTOMER_COMPLAIN.length());
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = ++l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }
}




