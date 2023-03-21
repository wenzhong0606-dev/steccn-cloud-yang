package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.vo.SysMsgAdditionDTO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.QtImproveRecordDTO;
import com.steccn.qms.dto.QtImproveRecordPageDTO;
import com.steccn.qms.service.QtImproveProgresssService;
import com.steccn.qms.service.QtImproveRecordService;
import com.steccn.qms.mapper.QtImproveRecordMapper;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.PassRateDateVo;
import com.steccn.qms.vo.QtCustomerComplainVO;
import com.steccn.qms.vo.QtImproveRecordVO;
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
import java.util.Objects;
import java.util.Set;

/**
* @author yequan
* @description 针对表【qms_improve_record(整改记录)】的数据库操作Service实现
* @createDate 2023-03-15 10:16:54
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class QtImproveRecordServiceImpl extends ServiceImpl<QtImproveRecordMapper, QtImproveRecord>
    implements QtImproveRecordService {

    private final RedissonClient redissonClient;

    private final QtImproveProgresssService qtImproveProgresssService;

    private final RemoteMsgFeignApi remoteMsgFeignApi;
    @Override
    public Page<QtImproveRecordVO> listPage(QtImproveRecordPageDTO dto) {
        //处理时间
        if (Objects.nonNull(dto.getStartDate()) && Objects.nonNull(dto.getEndDate())) {
            dto.setStartTime(LocalDateTime.of(dto.getStartDate(), LocalTime.MIN));
            dto.setEndTime(LocalDateTime.of(dto.getEndDate(), LocalTime.MAX));
        }
        //1.定义查询条件查询数据
        LambdaQueryWrapper<QtImproveRecord> between = new LambdaQueryWrapper<QtImproveRecord>()
                .like(Objects.nonNull(dto.getImproveOrder()), QtImproveRecord::getImproveOrder, dto.getImproveOrder())
                .like(Objects.nonNull(dto.getDescription()), QtImproveRecord::getDescription, dto.getDescription())
                .like(Objects.nonNull(dto.getCustomerComplainTitle()), QtImproveRecord::getCustomerComplainTitle, dto.getCustomerComplainTitle())
                .like(Objects.nonNull(dto.getDefectProductTitle()), QtImproveRecord::getDefectProductTitle, dto.getDefectProductTitle())
                .eq(Objects.nonNull(dto.getCreateBy()), QtImproveRecord::getCreateBy, dto.getCreateBy())
                .eq(Objects.nonNull(dto.getStatus()), QtImproveRecord::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getStartApproverId()), QtImproveRecord::getStartApproverId, dto.getStartApproverId())
                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()), QtImproveRecord::getCreateTime, dto.getStartTime(), dto.getEndTime());
        if (Objects.nonNull(dto.getSortField())){
            //定义排序条件
            if (QtManagerConstant.QT_IMPROVE_RECORD_ORDER.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtImproveRecord::getImproveOrder);
            }else if (QtManagerConstant.QT_IMPROVE_RECORD_DESCRIPTION.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtImproveRecord::getDescription);
            }else if (QtManagerConstant.QT_IMPROVE_RECORD_CUSTOMER_COMPLAIN_TITLE.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtImproveRecord::getCustomerComplainTitle);
            }else if (QtManagerConstant.QT_IMPROVE_RECORD_DEFECT_PRODUCT_TITLE.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtImproveRecord::getDefectProductTitle);
            }else if (QtManagerConstant.QT_IMPROVE_RECORD_CREATE_NAME.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtImproveRecord::getCreateName);
            }else if (QtManagerConstant.QT_IMPROVE_RECORD_CREATE_TIME.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtImproveRecord::getCreateTime);
            }else if (QtManagerConstant.QT_IMPROVE_RECORD_START_APPROVER_NAME.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtImproveRecord::getStartApproverName);
            }else if (QtManagerConstant.QT_IMPROVE_RECORD_STATUS.equals(dto.getSortField())){
                between.orderBy(Boolean.TRUE,dto.getAscFlag(),QtImproveRecord::getStatus);
            }
        }
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, between);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        List<QtImproveRecordVO> qtImproveRecordVOList = BeanUtil.copyToList(resPage.getRecords(), QtImproveRecordVO.class);
        return page.setRecords(qtImproveRecordVOList);
    }

    @Override
    public Boolean add(QtImproveRecordDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.QT_IMPROVE_RECORD);
        lock.lock();
        try {
            QtImproveRecord qtImproveRecord = new QtImproveRecord();
            BeanUtil.copyProperties(dto,qtImproveRecord);
            qtImproveRecord.setStatus(QtManagerConstant.NUM_0);
            qtImproveRecord.setImproveOrder(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_IMPROVE_RECORD,CodeGenerateUtil.NUM_BIT_8,currentMaxCode()));
            save(qtImproveRecord);
            //处理进度
            QtImproveProgresss qtImproveProgresss = new QtImproveProgresss();
            qtImproveProgresss.setRecordId(qtImproveRecord.getId());
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.CREATE.getName());
            qtImproveProgresss.setOpTime(LocalDateTime.now());
            qtImproveProgresss.setOpId(SecurityUtils.getUser().getUserId());
            qtImproveProgresss.setOpCreateName(SecurityUtils.getUser().getName());
            qtImproveProgresssService.save(qtImproveProgresss);
        }finally {
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
        QtImproveRecord maxCodeModel = getOne(new LambdaQueryWrapper<QtImproveRecord>().between(QtImproveRecord::getCreateTime, min, max)
                .orderByDesc(QtImproveRecord::getImproveOrder).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getImproveOrder().substring(CodeGenerateUtil.PREFIX_CUSTOMER_COMPLAIN.length());
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




