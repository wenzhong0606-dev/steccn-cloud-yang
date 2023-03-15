package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.common.RedisConfig;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
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
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【qms_customer_complain(客诉记录表)】的数据库操作Service实现
 * @createDate 2023-03-15 10:16:54
 */
@Service
@RequiredArgsConstructor
public class QtCustomerComplainServiceImpl extends ServiceImpl<QtCustomerComplainMapper, QtCustomerComplain> implements QtCustomerComplainService {

    private final RedissonClient redissonClient;
    private final QtComplainDefectImproveManService qtComplainDefectImproveManService;

    private final QtCustomerComplainDetailService qtCustomerComplainDetailService;

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
            // 流程相关
        } finally {
            lock.unlock();
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
        QtCustomerComplain maxCodeModel = getOne(new LambdaQueryWrapper<QtCustomerComplain>()
                .between(QtCustomerComplain::getCreateTime, min, max)
                .orderByDesc(QtCustomerComplain::getCode)
                .last(MpConstant.STR_LIMIT_1));
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




