package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
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
import com.steccn.qms.dto.*;
import com.steccn.qms.service.QtImproveProgresssService;
import com.steccn.qms.service.QtImproveRecordDetailService;
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
public class QtImproveRecordServiceImpl extends ServiceImpl<QtImproveRecordMapper, QtImproveRecord> implements QtImproveRecordService {

    private final RedissonClient redissonClient;

    private final QtImproveProgresssService qtImproveProgresssService;


    private final QtImproveRecordDetailService qtImproveRecordDetailService;

    private final RemoteMsgFeignApi remoteMsgFeignApi;

    @Override
    public Page<QtImproveRecordVO> listPage(QtImproveRecordPageDTO dto) {
        //处理时间
        if (Objects.nonNull(dto.getStartDate()) && Objects.nonNull(dto.getEndDate())) {
            dto.setStartTime(LocalDateTime.of(dto.getStartDate(), LocalTime.MIN));
            dto.setEndTime(LocalDateTime.of(dto.getEndDate(), LocalTime.MAX));
        }
        //1.定义查询条件查询数据
        LambdaQueryWrapper<QtImproveRecord> between = new LambdaQueryWrapper<QtImproveRecord>().like(Objects.nonNull(dto.getImproveOrder()), QtImproveRecord::getImproveOrder, dto.getImproveOrder()).like(Objects.nonNull(dto.getDescription()), QtImproveRecord::getDescription, dto.getDescription()).like(Objects.nonNull(dto.getCustomerComplainTitle()), QtImproveRecord::getCustomerComplainTitle, dto.getCustomerComplainTitle()).like(Objects.nonNull(dto.getDefectProductTitle()), QtImproveRecord::getDefectProductTitle, dto.getDefectProductTitle()).eq(Objects.nonNull(dto.getCreateBy()), QtImproveRecord::getCreateBy, dto.getCreateBy()).eq(Objects.nonNull(dto.getStatus()), QtImproveRecord::getStatus, dto.getStatus()).eq(Objects.nonNull(dto.getStartApproverId()), QtImproveRecord::getStartApproverId, dto.getStartApproverId()).between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()), QtImproveRecord::getCreateTime, dto.getStartTime(), dto.getEndTime());
        if (Objects.nonNull(dto.getSortField())) {
            //定义排序条件
            if (QtManagerConstant.QT_IMPROVE_RECORD_ORDER.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtImproveRecord::getImproveOrder);
            } else if (QtManagerConstant.QT_IMPROVE_RECORD_DESCRIPTION.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtImproveRecord::getDescription);
            } else if (QtManagerConstant.QT_IMPROVE_RECORD_CUSTOMER_COMPLAIN_TITLE.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtImproveRecord::getCustomerComplainTitle);
            } else if (QtManagerConstant.QT_IMPROVE_RECORD_DEFECT_PRODUCT_TITLE.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtImproveRecord::getDefectProductTitle);
            } else if (QtManagerConstant.QT_IMPROVE_RECORD_CREATE_NAME.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtImproveRecord::getCreateName);
            } else if (QtManagerConstant.QT_IMPROVE_RECORD_CREATE_TIME.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtImproveRecord::getCreateTime);
            } else if (QtManagerConstant.QT_IMPROVE_RECORD_START_APPROVER_NAME.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtImproveRecord::getStartApproverName);
            } else if (QtManagerConstant.QT_IMPROVE_RECORD_STATUS.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), QtImproveRecord::getStatus);
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
            BeanUtil.copyProperties(dto, qtImproveRecord);
            qtImproveRecord.setStatus(QtManagerConstant.NUM_0);
            qtImproveRecord.setImproveOrder(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_IMPROVE_RECORD, CodeGenerateUtil.NUM_BIT_8, currentMaxCode()));
            save(qtImproveRecord);
            //处理进度
            saveImproveProgresss(dto.getId(),QtManagerConstant.NUM_0);
            //保存d1 处理人
            QtImproveRecordDetail qtImproveRecordDetail = new QtImproveRecordDetail();
            qtImproveRecordDetail.setRecordId(qtImproveRecord.getId());
            qtImproveRecordDetail.setApproverStep(QtManagerConstant.improveProgress.D1_CREATE.getName());
            qtImproveRecordDetail.setApproverId(dto.getStartApproverId());
            qtImproveRecordDetail.setApproverName(dto.getStartApproverName());
            qtImproveRecordDetailService.save(qtImproveRecordDetail);
            //获取下一节点信息
            QtImproveRecordDetail detail = qtImproveRecordDetailService.getDetail(dto.getId(), qtImproveRecord.getStatus() + QtManagerConstant.NUM_1);
            //发送消息
            String content = String.format(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_SEND_BACK.getTemplate(), qtImproveRecord.getTitle(), qtImproveRecord.getCreateName());
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setCode(qtImproveRecord.getImproveOrder());
            sendMessage(sysMsgAdditionDTO, content, Sets.newHashSet(detail.getApproverId()));
        } finally {
            lock.unlock();
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(QtImproveRecordDTO dto) {
        QtImproveRecord record = getById(dto.getId());
        Assert.isTrue(Objects.nonNull(record), "记录不存在");
        Assert.isTrue(QtManagerConstant.NUM_0.equals(record.getStatus()), "当前状态不可编辑");
        BeanUtil.copyProperties(dto, record);
        updateById(record);
        //修改进度
        QtImproveProgresss one = qtImproveProgresssService.getOne(new LambdaQueryWrapper<QtImproveProgresss>().eq(QtImproveProgresss::getRecordId, dto.getId()));
        one.setOpId(SecurityUtils.getUser().getUserId());
        one.setOpCreateName(SecurityUtils.getUser().getName());
        qtImproveProgresssService.updateById(one);
        return Boolean.TRUE;
    }

    @Override
    public Boolean dispose(QtImproveRecordDisposeDTO dto) {
        QtImproveRecord record = getById(dto.getId());
        Assert.isTrue(Objects.nonNull(record), "记录不存在");
        //判断当前状态的执行人是否一致
        QtImproveRecordDetail detail = qtImproveRecordDetailService.getDetail(dto.getId(), dto.getStatus());
        Assert.isTrue(SecurityUtils.getUser().getUserId().equals(detail.getApproverId()), "记录审批人信息异常");
        //修改状态
        record.setStatus(record.getStatus() + QtManagerConstant.NUM_1);
        BeanUtil.copyProperties(dto, record);
        updateById(record);
        saveImproveProgresss(record.getId(),record.getStatus());
        //修改结论信息
        detail.setApproverConclusion(dto.getApproverConclusion());
        qtImproveRecordDetailService.updateById(detail);
        return Boolean.TRUE;
    }

    @Override
    public Boolean sendBack(QtImproveRecordSendBackDTO dto) {
        QtImproveRecord record = getById(dto.getId());
        Assert.isTrue(Objects.nonNull(record), "记录不存在");
        Assert.checkBetween(record.getStatus(), QtManagerConstant.NUM_3, QtManagerConstant.NUM_8, "当前状态不可退回");
        //处理进度信息
        saveImproveProgresssReturn(record.getId(),record.getStatus());
        record.setStatus(record.getStatus() - QtManagerConstant.NUM_1);
        //获取上一节点信息
        QtImproveRecordDetail detail = qtImproveRecordDetailService.getDetail(dto.getId(), record.getStatus() - QtManagerConstant.NUM_1);

        //发送消息
        String content = String.format(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_SEND_BACK.getTemplate(), record.getTitle(), record.getCreateName());
        SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
        sysMsgAdditionDTO.setCode(record.getImproveOrder());
        sendMessage(sysMsgAdditionDTO, content, Sets.newHashSet(detail.getApproverId()));
        return updateById(record);
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
        QtImproveRecord maxCodeModel = getOne(new LambdaQueryWrapper<QtImproveRecord>().between(QtImproveRecord::getCreateTime, min, max).orderByDesc(QtImproveRecord::getImproveOrder).last(MpConstant.STR_LIMIT_1));
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


    private Boolean sendMessage(SysMsgAdditionDTO sysMsgAdditionDTO, String content, Set<Long> userIds) {
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

    private Boolean saveImproveProgresss(Long id,Integer status){
        QtImproveProgresss qtImproveProgresss = new QtImproveProgresss();
        qtImproveProgresss.setRecordId(id);
        if (QtManagerConstant.NUM_0.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.CREATE.getName());
        }else if (QtManagerConstant.NUM_1.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.UNAPPROVED_START.getName());
        }else if (QtManagerConstant.NUM_2.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D1_CREATE.getName());
        }else if (QtManagerConstant.NUM_3.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D2_CLEAR_PROBLEM.getName());
        }else if (QtManagerConstant.NUM_4.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D3_COUNTERMEASURE.getName());
        }else if (QtManagerConstant.NUM_5.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D4_REASON_ANALYSIS.getName());
        }else if (QtManagerConstant.NUM_6.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D5_PERMANENT_CORRECTIVE_ACTION_PLAN.getName());
        }else if (QtManagerConstant.NUM_7.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D6_CORRECTIVE_ACTION_VERIFICATION.getName());
        }else if (QtManagerConstant.NUM_8.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D7_PREVENT_RECURRENCE.getName());
        }else if (QtManagerConstant.NUM_9.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D8_APPROVAL_END_PASS.getName());
        }
        qtImproveProgresss.setOpTime(LocalDateTime.now());
        qtImproveProgresss.setOpId(SecurityUtils.getUser().getUserId());
        qtImproveProgresss.setOpCreateName(SecurityUtils.getUser().getName());
        return qtImproveProgresssService.save(qtImproveProgresss);
    }


    private Boolean saveImproveProgresssReturn(Long id,Integer status){
        QtImproveProgresss qtImproveProgresss = new QtImproveProgresss();
        qtImproveProgresss.setRecordId(id);
         if (QtManagerConstant.NUM_3.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D1.getName());
        }else if (QtManagerConstant.NUM_4.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D2.getName());
        }else if (QtManagerConstant.NUM_5.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D3.getName());
        }else if (QtManagerConstant.NUM_6.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D4.getName());
        }else if (QtManagerConstant.NUM_7.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D5.getName());
        }else if (QtManagerConstant.NUM_8.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D6.getName());
        }else if (QtManagerConstant.NUM_9.equals(status)){
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D7.getName());
        }
        qtImproveProgresss.setOpTime(LocalDateTime.now());
        qtImproveProgresss.setOpId(SecurityUtils.getUser().getUserId());
        qtImproveProgresss.setOpCreateName(SecurityUtils.getUser().getName());
        return qtImproveProgresssService.save(qtImproveProgresss);
    }


}




