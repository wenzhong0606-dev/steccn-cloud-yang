package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
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
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.feign.RemoteProcessBusiLinkFeignApi;
import com.steccn.platform.vo.SysMsgAdditionDTO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.vo.MessScopeVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtImproveRecordApproveDTO;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtImproveRecordMapper;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.vo.QtImproveProgressVO;
import com.steccn.qms.vo.QtImproveRecordDetailVO;
import com.steccn.qms.vo.QtImproveRecordVO;
import com.steccn.websocket.constant.WebsocketConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final QtImproveStepManService qtImproveStepManService;

    private final RemoteMessFeignApi remoteMesFeignApi;

    private final OaInfoService oaInfoService;

    private final RemoteProcessBusiLinkFeignApi remoteProcessBusiLinkFeignApi;
    @Lazy
    @Autowired
    private  QtCustomerComplainService qtCustomerComplainService;

    @Lazy
    @Autowired
    private  QtDefectProductService qtDefectProductService;


    @Override
    public Page<QtImproveRecordVO> listPage(QtImproveRecordPageDTO dto) {

        //处理时间
        if (Objects.nonNull(dto.getStartDate()) && Objects.nonNull(dto.getEndDate())) {
            dto.setStartTime(LocalDateTime.of(dto.getStartDate(), LocalTime.MIN));
            dto.setEndTime(LocalDateTime.of(dto.getEndDate(), LocalTime.MAX));
        }
        //1.定义查询条件查询数据
        LambdaQueryWrapper<QtImproveRecord> between = new LambdaQueryWrapper<QtImproveRecord>().like(Objects.nonNull(dto.getTitle()), QtImproveRecord::getTitle, dto.getTitle()).like(Objects.nonNull(dto.getImproveOrder()), QtImproveRecord::getImproveOrder, dto.getImproveOrder()).like(Objects.nonNull(dto.getDescription()), QtImproveRecord::getDescription, dto.getDescription()).like(Objects.nonNull(dto.getCustomerComplainTitle()), QtImproveRecord::getCustomerComplainTitle, dto.getCustomerComplainTitle()).like(Objects.nonNull(dto.getDefectProductTitle()), QtImproveRecord::getDefectProductTitle, dto.getDefectProductTitle()).eq(Objects.nonNull(dto.getCreateBy()), QtImproveRecord::getCreateBy, dto.getCreateBy()).eq(Objects.nonNull(dto.getStatus()), QtImproveRecord::getStatus, dto.getStatus()).eq(Objects.nonNull(dto.getStartApproverId()), QtImproveRecord::getStartApproverId, dto.getStartApproverId()).between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()), QtImproveRecord::getCreateTime, dto.getStartTime(), dto.getEndTime());
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
        //判断当前用户的角色是否是整改管理员
        MessScopeVO role = remoteMesFeignApi.getRole(SecurityUtils.getUser().getName());
        if (CollUtil.isNotEmpty(role.getStringList())) {
            if (!role.getStringList().contains(QtManagerConstant.IMPROVE_ADMINISTRATOR)) {
                //不是整改管理员
                between.in(QtImproveRecord::getId, getUserAuthData());
            }
        } else {
            Set<Long> userAuthData = getUserAuthData();
            if (CollUtil.isNotEmpty(userAuthData)) {
                between.in(QtImproveRecord::getId, userAuthData);
            }
        }
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, between);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        List<QtImproveRecordVO> qtImproveRecordVOList = BeanUtil.copyToList(resPage.getRecords(), QtImproveRecordVO.class);
        //处理、编辑按钮权限
        qtImproveRecordVOList.forEach(qtImproveRecordVO -> {
            if (qtImproveRecordVO.getCreateBy().equals(SecurityUtils.getUser().getUserId())) {
                qtImproveRecordVO.setEditFlag(QtManagerConstant.NUM_1);
            }
            if (QtManagerConstant.states.contains(qtImproveRecordVO.getStatus())) {
                //获取当前登陆人是否有处理按钮的权限
                QtImproveRecordDetail detail = qtImproveRecordDetailService.getOne(new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getRecordId, qtImproveRecordVO.getId()).eq(QtImproveRecordDetail::getApproverStep, QtManagerConstant.ImproveProgressStatus.getEnum(qtImproveRecordVO.getStatus()).getName()));
                if (detail.getApproverId().equals(SecurityUtils.getUser().getUserId())) {
                    qtImproveRecordVO.setDisposeFlag(QtManagerConstant.NUM_1);
                }
            }
        });
        return page.setRecords(qtImproveRecordVOList);
    }

    @Override
    public Long add(QtImproveRecordDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.QT_IMPROVE_RECORD);
        lock.lock();
        try {
            QtImproveRecord qtImproveRecord = new QtImproveRecord();
            BeanUtil.copyProperties(dto, qtImproveRecord);
            qtImproveRecord.setStatus(QtManagerConstant.NUM_0);
            qtImproveRecord.setImproveOrder(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_IMPROVE_RECORD, CodeGenerateUtil.NUM_BIT_8, currentMaxCode()));
            save(qtImproveRecord);
            //处理进度
            saveImproveProgresss(qtImproveRecord.getId(), QtManagerConstant.NUM_0);
            //保存d1 处理人
            QtImproveRecordDetail qtImproveRecordDetail = new QtImproveRecordDetail();
            qtImproveRecordDetail.setRecordId(qtImproveRecord.getId());
            qtImproveRecordDetail.setApproverStep(QtManagerConstant.improveProgress.D1_CREATE.getName());
            qtImproveRecordDetail.setApproverId(dto.getD1ApproverId());
            qtImproveRecordDetail.setApproverName(dto.getD1ApproverName());
            qtImproveRecordDetailService.save(qtImproveRecordDetail);
            //调用OA创建流程
            OaWorkFlowParamDTO workFlowParamDTO = new OaWorkFlowParamDTO();
            workFlowParamDTO.setUserId(SecurityUtils.getUser().getUserId());
            workFlowParamDTO.setWorkflowId("153");
            workFlowParamDTO.setRequestName(sendTitle(OaManagerConstant.OaWorkFlow.IMPROVER_APPROVE.getName(), SecurityUtils.getUser().getName()));
            //主要数据
            List<OaWorkFlowNameValueDTO> mainDataList = com.google.common.collect.Lists.newArrayList();
            //审批发起人
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_SPFQR, SecurityUtils.getUser().getUserId().toString()));
            //整改单号
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_ZGDH, qtImproveRecord.getImproveOrder()));
            //整改标题
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_ZGBT, qtImproveRecord.getTitle()));
            //描述
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_MS, qtImproveRecord.getDescription()));
            //创建人
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CJR, SecurityUtils.getUser().getName()));
            //创建时间
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CJSJ, LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss")));
            //整改启动审批人
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_ZGQDSPR, dto.getStartApproverId().toString()));
            //D1处理人
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_D1CLR, dto.getD1ApproverId().toString()));
            if (Objects.nonNull(qtImproveRecord.getCustomerComplainId())) {
                //关联客诉
                QtCustomerComplain customerComplain = qtCustomerComplainService.getById(qtImproveRecord.getCustomerComplainId());
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLKS,saveDoc(dto.getInfoUrl(), customerComplain.getTitle()).getDocid().toString()));
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLKS_MOBILE,customerComplain.getTitle() + "(请在PC端查看客诉详情)"));
            } else {
                //关联不合格品
                QtDefectProduct qtDefectProduct = qtDefectProductService.getById(qtImproveRecord.getDefectProductId());
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLBHGPCL, saveDoc(dto.getInfoUrl(),qtDefectProduct.getTitle()).getDocid().toString()));
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLBHGPCL_MOBILE,qtDefectProduct.getTitle() + "(请到PC端查看不合格品处理详情)"));
            }
            workFlowParamDTO.setMainData(mainDataList);
            //调用
            OaWorkflowStatusVO oaWorkflowStatusVO = FeignOptional.of(() -> oaInfoService.createWorkflow(workFlowParamDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            if (!oaWorkflowStatusVO.getCode().equals(PmManagerConstant.WORKFLOW_SUCCESS)) {
                throw new BusinessException("流程创建失败," + oaWorkflowStatusVO.getErrMsg());
            }
            //保存oa数据与流程关联
            ProcessBusiLinkDTO processBusiLinkDTO = new ProcessBusiLinkDTO();
            processBusiLinkDTO.setBusiId(qtImproveRecord.getId());
            processBusiLinkDTO.setInstanceId(oaWorkflowStatusVO.getRequestId());
            processBusiLinkDTO.setBusiType(QtManagerConstant.QtBusinessType.IMPROVE_START.getCode());
            Boolean aBoolean = FeignOptional.of(() -> remoteProcessBusiLinkFeignApi.add(processBusiLinkDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (!aBoolean) {
                throw new BusinessException("调用保存业务数据失败");
            }
            //发送消息
            SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
            sysMsgSendDTO.setTitle(sendTitle(OaManagerConstant.OaWorkFlow.IMPROVER_APPROVE.getName(), SecurityUtils.getUser().getName()));
            sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.SEND_BACK.getCode());
            sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.SEND_BACK.getTemplate(), SecurityUtils.getUser().getName()));
            sysMsgSendDTO.setUserList(Sets.newHashSet(dto.getStartApproverId()));
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setId(Long.valueOf(oaWorkflowStatusVO.getRequestId()));
            sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
            sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
            //发送消息
            try {
                Boolean b = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if (!b) {
                    log.info(qtImproveRecord + "：消息发送失败");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                throw new BusinessException("消息发送失败");
            }
            return qtImproveRecord.getId();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Boolean edit(QtImproveRecordDTO dto) {
        QtImproveRecord record = getById(dto.getId());
        Assert.isTrue(Objects.nonNull(record), "记录不存在");
        Assert.isTrue(QtManagerConstant.NUM_1.equals(record.getStatus()), "当前状态不可编辑");
        BeanUtil.copyProperties(dto, record);
        record.setStatus(QtManagerConstant.NUM_0);
        updateById(record);
        //保存进度
        QtImproveProgresss qtImproveProgresss = new QtImproveProgresss();
        qtImproveProgresss.setRecordId(record.getId());
        qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.REDACT.getName());
        qtImproveProgresss.setOpTime(LocalDateTime.now());
        qtImproveProgresss.setOpId(SecurityUtils.getUser().getUserId());
        qtImproveProgresss.setOpCreateName(SecurityUtils.getUser().getName());
        qtImproveProgresssService.save(qtImproveProgresss);
        //修改d1节点执行人
        QtImproveRecordDetail qtImproveRecordDetail = qtImproveRecordDetailService.getOne(new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getRecordId, record.getId()));
        qtImproveRecordDetail.setApproverStep(QtManagerConstant.improveProgress.D1_CREATE.getName());
        qtImproveRecordDetail.setApproverId(dto.getD1ApproverId());
        qtImproveRecordDetail.setApproverName(dto.getD1ApproverName());
        qtImproveRecordDetailService.updateById(qtImproveRecordDetail);
        //提交流程
        ProcessBusiLinkDTO linkDTO = FeignOptional.of(() -> remoteProcessBusiLinkFeignApi.getByBusiId(record.getId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //主要数据
        List<OaWorkFlowNameValueDTO> mainDataList = com.google.common.collect.Lists.newArrayList();
        //审批发起人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_SPFQR, SecurityUtils.getUser().getUserId().toString()));
        //整改单号
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_ZGDH, record.getImproveOrder()));
        //整改标题
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_ZGBT, record.getTitle()));
        //描述
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_MS, record.getDescription()));
        //创建人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CJR, SecurityUtils.getUser().getName()));
        //创建时间
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CJSJ, LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss").toString()));
        //整改启动审批人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_ZGQDSPR, dto.getStartApproverId().toString()));
        //D1处理人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_D1CLR, dto.getD1ApproverId().toString()));
        if (Objects.nonNull(record.getCustomerComplainId())) {
            //关联客诉
            QtCustomerComplain customerComplain = qtCustomerComplainService.getById(record.getCustomerComplainId());
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLKS,saveDoc(dto.getInfoUrl(), customerComplain.getTitle()).getDocid().toString()));
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLKS_MOBILE,customerComplain.getTitle() + "(请在PC端查看客诉详情)"));
        } else {
            //关联不合格品
            QtDefectProduct qtDefectProduct = qtDefectProductService.getById(record.getDefectProductId());
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLBHGPCL, saveDoc(dto.getInfoUrl(),qtDefectProduct.getTitle()).getDocid().toString()));
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLBHGPCL_MOBILE,qtDefectProduct.getTitle() + "(请到PC端查看不合格品处理详情)"));
        }
        OaWorkFlowSubmitParamDTO workFlowSubmitParamDTO = new OaWorkFlowSubmitParamDTO();
        workFlowSubmitParamDTO.setUserId(SecurityUtils.getUser().getUserId());
        workFlowSubmitParamDTO.setRequestId(linkDTO.getInstanceId());
        workFlowSubmitParamDTO.setWorkFlowId(OaManagerConstant.OaWorkFlow.IMPROVER_APPROVE.getId());
        workFlowSubmitParamDTO.setMainData(mainDataList);
        OaWorkflowStatusVO oaWorkflowStatusVO = FeignOptional.of(() -> oaInfoService.submitWorkflow(workFlowSubmitParamDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        if (!oaWorkflowStatusVO.getCode().equals(PmManagerConstant.WORKFLOW_SUCCESS)) {
            throw new BusinessException("流程提交失败," + oaWorkflowStatusVO.getErrMsg());
        }
        //发送消息
        SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
        sysMsgSendDTO.setTitle(sendTitle(OaManagerConstant.OaWorkFlow.IMPROVER_APPROVE.getName(), SecurityUtils.getUser().getName()));
        sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.SEND_BACK.getCode());
        sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.SEND_BACK.getTemplate(), SecurityUtils.getUser().getName()));
        sysMsgSendDTO.setUserList(Sets.newHashSet(dto.getStartApproverId()));
        SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
        sysMsgAdditionDTO.setId(Long.valueOf(linkDTO.getInstanceId()));
        sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
        sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
        //发送消息
        try {
            Boolean b = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (!b) {
                log.info(record + "：消息发送失败");
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            throw new BusinessException("消息发送失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean dispose(QtImproveRecordDisposeDTO dto) {
        QtImproveRecord record = getById(dto.getId());
        Assert.isTrue(Objects.nonNull(record), "记录不存在");
        Assert.checkBetween(record.getStatus(), QtManagerConstant.NUM_2, QtManagerConstant.NUM_7, "当前状态不可处理");
        //判断当前状态的执行人是否一致
        QtImproveRecordDetail detail = qtImproveRecordDetailService.getDetail(record.getId(), record.getStatus());
        Assert.isTrue(SecurityUtils.getUser().getUserId().equals(detail.getApproverId()), "记录审批人信息异常");
        if (QtManagerConstant.NUM_2.equals(record.getStatus())) {
            //保存d1:创建团队信息
            saveHandler(dto);
        }
        //保存进度信息
        saveImproveProgresss(record.getId(), record.getStatus());
        //修改状态
        record.setStatus(record.getStatus() + QtManagerConstant.NUM_1);
        updateById(record);
        //修改结论信息
        detail.setApproverConclusion(dto.getApproverConclusion());
        qtImproveRecordDetailService.updateById(detail);
        //获取下一节点信息
        if (record.getStatus() < QtManagerConstant.NUM_9) {
            QtImproveRecordDetail qtImproveRecordDetail = qtImproveRecordDetailService.getDetail(dto.getId(), record.getStatus());
            //发送消息
            String content = String.format(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_SEND_BACK.getTemplate(), record.getTitle(), record.getCreateName());
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setId(record.getId());
            sysMsgAdditionDTO.setCode(record.getImproveOrder());
            sendMessage(sysMsgAdditionDTO, content, Sets.newHashSet(qtImproveRecordDetail.getApproverId()));
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean sendBack(QtImproveRecordSendBackDTO dto) {
        QtImproveRecord record = getById(dto.getId());
        Assert.isTrue(Objects.nonNull(record), "记录不存在");
        Assert.checkBetween(record.getStatus(), QtManagerConstant.NUM_3, QtManagerConstant.NUM_8, "当前状态不可退回");
        //处理进度信息
        saveImproveProgresssReturn(record.getId(), record.getStatus(), dto.getReason());
        record.setStatus(record.getStatus() - QtManagerConstant.NUM_1);
        //获取上一节点信息
        QtImproveRecordDetail detail = qtImproveRecordDetailService.getDetail(dto.getId(), record.getStatus());
        //发送消息
        String content = String.format(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_SEND_BACK.getTemplate(), record.getTitle(), record.getCreateName());
        SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
        sysMsgAdditionDTO.setId(record.getId());
        sysMsgAdditionDTO.setCode(record.getImproveOrder());
        sendMessage(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_SEND_BACK.getTitle(), sysMsgAdditionDTO, content, Sets.newHashSet(detail.getApproverId()));
        return updateById(record);
    }

    @Override
    public QtImproveRecordVO get(Long id) {
        QtImproveRecord record = getById(id);
        QtImproveRecordVO qtImproveRecordVO = new QtImproveRecordVO();
        BeanUtil.copyProperties(record, qtImproveRecordVO);
        List<QtImproveRecordDetail> list = qtImproveRecordDetailService.list(new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getRecordId, record.getId()));
        List<QtImproveRecordDetailVO> detailList = BeanUtil.copyToList(list, QtImproveRecordDetailVO.class);
        qtImproveRecordVO.setDetailList(detailList);
        //获取进度信息
        List<QtImproveProgresss> progresssList = qtImproveProgresssService.list(new LambdaQueryWrapper<QtImproveProgresss>().eq(QtImproveProgresss::getRecordId, qtImproveRecordVO.getId()));
        qtImproveRecordVO.setProgresssList(BeanUtil.copyToList(progresssList, QtImproveProgressVO.class));
        //获取抄送人信息
        List<QtImproveStepMan> manList = qtImproveStepManService.list(new LambdaQueryWrapper<QtImproveStepMan>().eq(QtImproveStepMan::getRecordId, id).eq(QtImproveStepMan::getUserType, QtManagerConstant.NUM_1));
        if (CollUtil.isNotEmpty(manList)) {
            qtImproveRecordVO.setManList(BeanUtil.copyToList(manList, QtCarbonCopyManDTO.class));
        }
        //处理按钮权限
        return disposeButton(qtImproveRecordVO);
    }

    @Override
    public Boolean suspend(QtImproveRecordSuspendDTO dto) {
        QtImproveRecord record = getById(dto.getId());
        Assert.isTrue(Objects.nonNull(record), "记录不存在");
        Assert.checkBetween(record.getStatus(), QtManagerConstant.NUM_2, QtManagerConstant.NUM_8, "当前状态不可中断");
        record.setStatus(QtManagerConstant.NUM_11);
        updateById(record);
        //保存进度
        QtImproveProgresss qtImproveProgresss = new QtImproveProgresss();
        qtImproveProgresss.setRecordId(record.getId());
        qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.SUSPEND.getName());
        qtImproveProgresss.setOpTime(LocalDateTime.now());
        qtImproveProgresss.setOpId(SecurityUtils.getUser().getUserId());
        qtImproveProgresss.setOpCreateName(SecurityUtils.getUser().getName());
        qtImproveProgresss.setOpNote(dto.getReason());
        qtImproveProgresssService.save(qtImproveProgresss);
        return Boolean.TRUE;
    }

    @Override
    public Boolean delete(Long id) {
        QtImproveRecord record = getById(id);
        Assert.isTrue(Objects.nonNull(record), "记录不存在");
        Assert.isTrue(QtManagerConstant.NUM_10.equals(record.getStatus()) || QtManagerConstant.NUM_11.equals(record.getStatus()) || QtManagerConstant.NUM_1.equals(record.getStatus()), "当前状态不可删除");
        qtImproveProgresssService.remove(new LambdaQueryWrapper<QtImproveProgresss>().eq(QtImproveProgresss::getRecordId, id));
        qtImproveStepManService.remove(new LambdaQueryWrapper<QtImproveStepMan>().eq(QtImproveStepMan::getRecordId, id));
        qtImproveRecordDetailService.remove(new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getRecordId, id));
        return removeById(id);
    }

    @Override
    public Boolean approve(QtImproveRecordApproveDTO dto) {
        QtImproveRecord record = getById(dto.getId());
        Assert.isTrue(Objects.nonNull(record), "记录不存在");
        Assert.isTrue(QtManagerConstant.NUM_8.equals(record.getStatus()), "当前状态不可提交审批");
        if (CollUtil.isNotEmpty(dto.getCarbonCopyManList())) {
            //调取oa 节点
            //保存d7 抄送人信息
            qtImproveStepManService.saveMan(dto);
        }
        //保存进度信息
        saveImproveProgresss(record.getId(), record.getStatus());
        //保存结论
        QtImproveRecordDetail detail = qtImproveRecordDetailService.getDetail(record.getId(), record.getStatus());
        detail.setApproverConclusion(dto.getApproverConclusion());
        qtImproveRecordDetailService.updateById(detail);
        record.setStatus(QtManagerConstant.NUM_9);
        updateById(record);
        //调用Oa流程
        return closeout(dto, record);
    }

    /**
     * 结案提交流程
     *
     * @param dto
     * @param record
     * @return
     */
    private Boolean closeout(QtImproveRecordApproveDTO dto, QtImproveRecord record) {
        //判断是否发起过流程，如果发起过 提交
        ProcessBusiLinkDTO linkDTO = FeignOptional.of(() -> remoteProcessBusiLinkFeignApi.getByTypeAndBusiId(record.getId(), QtManagerConstant.QtBusinessType.IMPROVE_CLOSEOUT.getCode()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //调用OA创建审批流程
        OaWorkFlowParamDTO workFlowParamDTO = new OaWorkFlowParamDTO();
        workFlowParamDTO.setUserId(SecurityUtils.getUser().getUserId());
        workFlowParamDTO.setWorkflowId(OaManagerConstant.OaWorkFlow.IMPROVER_CLOSEOUT.getId());
        workFlowParamDTO.setRequestName(sendTitle(OaManagerConstant.OaWorkFlow.IMPROVER_CLOSEOUT.getName(), SecurityUtils.getUser().getName()));
        //主要数据
        List<OaWorkFlowNameValueDTO> mainDataList = com.google.common.collect.Lists.newArrayList();
        //审批发起人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_SPFQR, SecurityUtils.getUser().getUserId().toString()));
        //整改单号
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_ZGDH, record.getImproveOrder()));
        //整改标题
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_ZGBT, record.getTitle()));
        //描述
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_MS, record.getDescription()));
        //创建人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CJR, SecurityUtils.getUser().getName()));
        //创建时间
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CJSJ, LocalDateTimeUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss")));
        //整改启动审批人
        mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_ZGQDSPR, record.getStartApproverId().toString()));
        //D1处理人 - D7 处理人
        List<QtImproveRecordDetail> list = qtImproveRecordDetailService.list(new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getRecordId, record.getId()));
        for (QtImproveRecordDetail recordDetail : list) {
            if (QtManagerConstant.ImproveProgressStatus.D1_CREATE.getName().equals(recordDetail.getApproverStep())) {
                //d1
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_D1CLR, recordDetail.getApproverName()));
            } else if (QtManagerConstant.ImproveProgressStatus.D2_CLEAR_PROBLEM.getName().equals(recordDetail.getApproverStep())) {
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_D2CLR, recordDetail.getApproverName()));
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_J2, recordDetail.getApproverConclusion()));
            } else if (QtManagerConstant.ImproveProgressStatus.D3_COUNTERMEASURE.getName().equals(recordDetail.getApproverStep())) {
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_D3CLR, recordDetail.getApproverName()));
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_J3, recordDetail.getApproverConclusion()));
            } else if (QtManagerConstant.ImproveProgressStatus.D4_REASON_ANALYSIS.getName().equals(recordDetail.getApproverStep())) {
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_D4CLR, recordDetail.getApproverName()));
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_J4, recordDetail.getApproverConclusion()));
            } else if (QtManagerConstant.ImproveProgressStatus.D5_PERMANENT_CORRECTIVE_ACTION_PLAN.getName().equals(recordDetail.getApproverStep())) {
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_D5CLR, recordDetail.getApproverName()));
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_J5, recordDetail.getApproverConclusion()));
            } else if (QtManagerConstant.ImproveProgressStatus.D6_CORRECTIVE_ACTION_VERIFICATION.getName().equals(recordDetail.getApproverStep())) {
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_D6CLR, recordDetail.getApproverName()));
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_J6, recordDetail.getApproverConclusion()));
            } else if (QtManagerConstant.ImproveProgressStatus.D7_PREVENT_RECURRENCE.getName().equals(recordDetail.getApproverStep())) {
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_D7CLR, recordDetail.getApproverName()));
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_J7, recordDetail.getApproverConclusion()));
            } else if (QtManagerConstant.ImproveProgressStatus.D8_APPROVAL_END_PASS.getName().equals(recordDetail.getApproverStep())) {
                mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_D8SPR, recordDetail.getApproverId().toString()));
            }
        }
        if (Objects.nonNull(record.getCustomerComplainId())) {
            //关联客诉
            QtCustomerComplain customerComplain = qtCustomerComplainService.getById(record.getCustomerComplainId());
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLKS,saveDoc(dto.getInfoUrl(), customerComplain.getTitle()).getDocid().toString()));
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLKS_MOBILE,customerComplain.getTitle() + "(请在PC端查看客诉详情)"));
        } else {
            //关联不合格品
            QtDefectProduct qtDefectProduct = qtDefectProductService.getById(record.getDefectProductId());
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLBHGPCL, saveDoc(dto.getInfoUrl(),qtDefectProduct.getTitle()).getDocid().toString()));
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_GLBHGPCL_MOBILE,qtDefectProduct.getTitle() + "(请到PC端查看不合格品处理详情)"));
        }
        //抄送人数据
        if (CollUtil.isNotEmpty(dto.getCarbonCopyManList())) {
            List<Long> userIds = dto.getCarbonCopyManList().stream().map(QtCarbonCopyManDTO::getUserId).collect(Collectors.toList());
            String ids = userIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            mainDataList.add(new OaWorkFlowNameValueDTO(QtManagerConstant.WORKFLOW_CS, ids));
        }

        if (Objects.nonNull(linkDTO.getId())) {
            //调用OA 提交流程
            OaWorkFlowSubmitParamDTO workFlowSubmitParamDTO = new OaWorkFlowSubmitParamDTO();
            workFlowSubmitParamDTO.setUserId(SecurityUtils.getUser().getUserId());
            workFlowSubmitParamDTO.setRequestId(linkDTO.getInstanceId());
            workFlowSubmitParamDTO.setMainData(mainDataList);
            workFlowSubmitParamDTO.setWorkFlowId(OaManagerConstant.OaWorkFlow.IMPROVER_CLOSEOUT.getId());
            OaWorkflowStatusVO oaWorkflowStatusVO = FeignOptional.of(() -> oaInfoService.submitWorkflow(workFlowSubmitParamDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            if (!oaWorkflowStatusVO.getCode().equals(PmManagerConstant.WORKFLOW_SUCCESS)) {
                throw new BusinessException("流程提交失败," + oaWorkflowStatusVO.getErrMsg());
            }
        } else {
            workFlowParamDTO.setMainData(mainDataList);
            //新建
            OaWorkflowStatusVO oaWorkflowStatusVO = FeignOptional.of(() -> oaInfoService.createWorkflow(workFlowParamDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
            if (!oaWorkflowStatusVO.getCode().equals(PmManagerConstant.WORKFLOW_SUCCESS)) {
                throw new BusinessException("流程创建失败," + oaWorkflowStatusVO.getErrMsg());
            }
            //保存oa数据与流程关联
            ProcessBusiLinkDTO processBusiLinkDTO = new ProcessBusiLinkDTO();
            processBusiLinkDTO.setBusiId(record.getId());
            processBusiLinkDTO.setInstanceId(oaWorkflowStatusVO.getRequestId());
            processBusiLinkDTO.setBusiType(QtManagerConstant.QtBusinessType.IMPROVE_CLOSEOUT.getCode());
            Boolean aBoolean = FeignOptional.of(() -> remoteProcessBusiLinkFeignApi.add(processBusiLinkDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (!aBoolean) {
                throw new BusinessException("调用保存业务数据失败");
            }
        }
        //发送消息
        SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
        sysMsgSendDTO.setTitle(sendTitle(OaManagerConstant.OaWorkFlow.IMPROVER_CLOSEOUT.getName(), SecurityUtils.getUser().getName()));
        sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_APPROVE.getCode());
        sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.SEND_BACK.getTemplate(), SecurityUtils.getUser().getName()));
        //获取d8审批人
        QtImproveRecordDetail qtImproveRecordDetail = qtImproveRecordDetailService.getOne(new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getRecordId, record.getId()).eq(QtImproveRecordDetail::getApproverStep, QtManagerConstant.ImproveProgressStatus.D8_APPROVAL_END_PASS.getName()));
        sysMsgSendDTO.setUserList(Sets.newHashSet(qtImproveRecordDetail.getApproverId()));
        SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
        sysMsgAdditionDTO.setId(record.getId());
        sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
        sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
        //发送消息
        try {
            Boolean b = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (!b) {
                log.info(record + "：消息发送失败");
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            throw new BusinessException("消息发送失败");
        }

        return Boolean.TRUE;
    }

    @Override
    public Boolean approve(OaProcessNotifyDTO dto) {
        OaProcessDataDTO data = dto.getData();
        ProcessBusiLinkDTO processBusiLinkDTO = FeignOptional.of(() -> remoteProcessBusiLinkFeignApi.get(Integer.valueOf(data.getRequestId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        QtImproveRecord qtImproveRecord = getById(processBusiLinkDTO.getBusiId());
        Assert.isTrue(Objects.nonNull(qtImproveRecord), "当前业务数据不存在");
        //获取节点操作人信息
        OaWorkFlowOperatorDTO oaWorkFlowOperatorDTO = FeignOptional.of(() -> oaInfoService.getCurrentOperator(new OaOperatorParamDTO(Integer.valueOf(dto.getData().getRequestId()), Integer.valueOf(dto.getData().getNodeId()))), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        //审批通过
        if (OaDataConstant.WORKFLOW_AGREE.equals(dto.getType()) && OaDataConstant.WORKFLOW_APPROVE.equals(dto.getData().getNodeName())) {
            Assert.isTrue(QtManagerConstant.NUM_0.equals(qtImproveRecord.getStatus()), "当前状态不可审批通过");
            qtImproveRecord.setStatus(QtManagerConstant.NUM_2);
            //获取d1的处理人信息
            QtImproveRecordDetail one = qtImproveRecordDetailService.getOne(new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getRecordId, qtImproveRecord.getId()).eq(QtImproveRecordDetail::getApproverStep, QtManagerConstant.ImproveProgressStatus.D1_CREATE.getName()));
            //发送消息给处理人
            String content = String.format(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_PENDING.getTemplate(), qtImproveRecord.getTitle(), qtImproveRecord.getCreateName());
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setCode(qtImproveRecord.getImproveOrder());
            sysMsgAdditionDTO.setId(qtImproveRecord.getId());
            sendMessage(sysMsgAdditionDTO, content, Sets.newHashSet(one.getApproverId()));
            //保存进度
            QtImproveProgresss qtImproveProgresss = new QtImproveProgresss();
            qtImproveProgresss.setRecordId(qtImproveRecord.getId());
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.APPROVE_START.getName());
            qtImproveProgresss.setOpTime(LocalDateTime.now());
            qtImproveProgresss.setOpId(Long.valueOf(oaWorkFlowOperatorDTO.getId()));
            qtImproveProgresss.setOpCreateName(oaWorkFlowOperatorDTO.getName());
            qtImproveProgresssService.save(qtImproveProgresss);
        } else if (OaDataConstant.WORKFLOW_DISAGREE.equals(dto.getType()) && OaDataConstant.WORKFLOW_APPROVE.equals(dto.getData().getNodeName())) {
            //退回
            qtImproveRecord.setStatus(QtManagerConstant.NUM_1);
            //发送消息
            SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
            sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.SEND_BACK.getCode());
            sysMsgSendDTO.setTitle(data.getRequestName());
            sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.SEND_BACK.getTemplate(), qtImproveRecord.getCreateName()));
            sysMsgSendDTO.setUserList(Sets.newHashSet(qtImproveRecord.getCreateBy()));
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setId(Long.valueOf(data.getRequestId()));
            sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
            sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
            //发送消息
            try {
                Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if (!aBoolean) {
                    log.info(qtImproveRecord + "：消息发送失败");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                throw new BusinessException("消息发送失败");
            }
            //保存进度
            QtImproveProgresss qtImproveProgresss = new QtImproveProgresss();
            qtImproveProgresss.setRecordId(qtImproveRecord.getId());
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.UNAPPROVED_START.getName());
            qtImproveProgresss.setOpTime(LocalDateTime.now());
            qtImproveProgresss.setOpId(Long.valueOf(oaWorkFlowOperatorDTO.getId()));
            qtImproveProgresss.setOpCreateName(oaWorkFlowOperatorDTO.getName());
            qtImproveProgresssService.save(qtImproveProgresss);
        }
        return updateById(qtImproveRecord);
    }

    @Override
    public Boolean finishApprove(OaProcessNotifyDTO dto) {
        OaProcessDataDTO data = dto.getData();
        ProcessBusiLinkDTO processBusiLinkDTO = FeignOptional.of(() -> remoteProcessBusiLinkFeignApi.get(Integer.valueOf(data.getRequestId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        QtImproveRecord qtImproveRecord = getById(processBusiLinkDTO.getBusiId());
        Assert.isTrue(Objects.nonNull(qtImproveRecord), "当前业务数据不存在");
        //获取节点操作人信息
        OaWorkFlowOperatorDTO oaWorkFlowOperatorDTO = FeignOptional.of(() -> oaInfoService.getCurrentOperator(new OaOperatorParamDTO(Integer.valueOf(dto.getData().getRequestId()), Integer.valueOf(dto.getData().getNodeId()))), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        //审批通过
        if (OaDataConstant.WORKFLOW_AGREE.equals(dto.getType()) && OaDataConstant.WORKFLOW_APPROVE.equals(dto.getData().getNodeName())) {
            Assert.isTrue(QtManagerConstant.NUM_9.equals(qtImproveRecord.getStatus()), "当前状态不可审批通过");
            qtImproveRecord.setStatus(QtManagerConstant.NUM_10);
            //获取d7的处理人信息
            QtImproveRecordDetail one = qtImproveRecordDetailService.getOne(new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getRecordId, qtImproveRecord.getId()).eq(QtImproveRecordDetail::getApproverStep, QtManagerConstant.ImproveProgressStatus.D7_PREVENT_RECURRENCE.getName()));
            //发送消息给处理人
            SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
            sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_APPROVE.getCode());
            sysMsgSendDTO.setTitle(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_APPROVE_PASS.getTitle());
            sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_APPROVE_PASS.getTemplate(), qtImproveRecord.getTitle(), qtImproveRecord.getCreateName()));
            sysMsgSendDTO.setUserList(Sets.newHashSet(one.getApproverId()));
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setId(qtImproveRecord.getId());
            sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
            sysMsgSendDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
            //发送消息
            try {
                Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if (!aBoolean) {
                    log.info(qtImproveRecord + "：消息发送失败");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
//                throw new BusinessException("消息发送失败");
            }
            //获取d8的处理人信息
            QtImproveRecordDetail d8Detail = qtImproveRecordDetailService.getOne(new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getRecordId, qtImproveRecord.getId()).eq(QtImproveRecordDetail::getApproverStep, QtManagerConstant.ImproveProgressStatus.D8_APPROVAL_END_PASS.getName()));
            //保存D8结案
            QtImproveProgresss qtImproveProgresss = new QtImproveProgresss();
            qtImproveProgresss.setRecordId(d8Detail.getRecordId());
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D8_APPROVAL_END_PASS.getName());
            qtImproveProgresss.setOpTime(LocalDateTime.now());
            qtImproveProgresss.setOpId(d8Detail.getApproverId());
            qtImproveProgresss.setOpCreateName(d8Detail.getApproverName());
            qtImproveProgresssService.save(qtImproveProgresss);
        } else if (OaDataConstant.WORKFLOW_DISAGREE.equals(dto.getType()) && OaDataConstant.WORKFLOW_APPROVE.equals(dto.getData().getNodeName())) {
            //退回
            qtImproveRecord.setStatus(QtManagerConstant.NUM_8);
            //发送消息
            SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
            sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.SEND_BACK.getCode());
            sysMsgSendDTO.setTitle(data.getRequestName());
            sysMsgSendDTO.setContent(String.format(SysMsgConstant.MsgTypeEnum.SEND_BACK.getTemplate(), qtImproveRecord.getCreateName()));
            sysMsgSendDTO.setUserList(Sets.newHashSet(qtImproveRecord.getCreateBy()));
            SysMsgAdditionDTO sysMsgAdditionDTO = new SysMsgAdditionDTO();
            sysMsgAdditionDTO.setId(Long.valueOf(dto.getData().getRequestId()));
            sysMsgSendDTO.setAddition(sysMsgAdditionDTO);
            sysMsgSendDTO.setPushType(SysMsgConstant.MsgCategoryEnum.TO_BE_DO.getCode());
            //发送消息
            try {
                Boolean aBoolean = FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgSendDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if (!aBoolean) {
                    log.info(qtImproveRecord + "：消息发送失败");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
//                throw new BusinessException("消息发送失败");
            }
            //保存进度
            QtImproveProgresss qtImproveProgresss = new QtImproveProgresss();
            qtImproveProgresss.setRecordId(qtImproveRecord.getId());
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D7.getName());
            qtImproveProgresss.setOpTime(LocalDateTime.now());
            qtImproveProgresss.setOpId(Long.valueOf(oaWorkFlowOperatorDTO.getId()));
            qtImproveProgresss.setOpCreateName(oaWorkFlowOperatorDTO.getName());
            qtImproveProgresssService.save(qtImproveProgresss);
        }
        return updateById(qtImproveRecord);
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
        QtImproveRecord maxCodeModel = getOne(new LambdaQueryWrapper<QtImproveRecord>().orderByDesc(QtImproveRecord::getImproveOrder).last(MpConstant.STR_LIMIT_1));
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


    private Boolean sendMessage(String title, SysMsgAdditionDTO sysMsgAdditionDTO, String content, Set<Long> userIds) {
        //定义消息模板
        SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
        sysMsgSendDTO.setTitle(title);
        sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_PENDING.getCode());
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

    private Boolean sendMessage(SysMsgAdditionDTO sysMsgAdditionDTO, String content, Set<Long> userIds) {
        //定义消息模板
        SysMsgSendDTO sysMsgSendDTO = new SysMsgSendDTO();
        sysMsgSendDTO.setTitle(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_PENDING.getTitle());
        sysMsgSendDTO.setType(SysMsgConstant.MsgTypeEnum.IMPROVE_RECORD_PENDING.getCode());
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

    private Boolean saveImproveProgresss(Long id, Integer status) {
        QtImproveProgresss qtImproveProgresss = new QtImproveProgresss();
        qtImproveProgresss.setRecordId(id);
        if (QtManagerConstant.NUM_0.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.CREATE.getName());
        } else if (QtManagerConstant.NUM_1.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.UNAPPROVED_START.getName());
        } else if (QtManagerConstant.NUM_2.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D1_CREATE.getName());
        } else if (QtManagerConstant.NUM_3.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D2_CLEAR_PROBLEM.getName());
        } else if (QtManagerConstant.NUM_4.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D3_COUNTERMEASURE.getName());
        } else if (QtManagerConstant.NUM_5.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D4_REASON_ANALYSIS.getName());
        } else if (QtManagerConstant.NUM_6.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D5_PERMANENT_CORRECTIVE_ACTION_PLAN.getName());
        } else if (QtManagerConstant.NUM_7.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D6_CORRECTIVE_ACTION_VERIFICATION.getName());
        } else if (QtManagerConstant.NUM_8.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D7_PREVENT_RECURRENCE.getName());
        } else if (QtManagerConstant.NUM_9.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.D8_APPROVAL_END_PASS.getName());
        }
        qtImproveProgresss.setOpTime(LocalDateTime.now());
        qtImproveProgresss.setOpId(SecurityUtils.getUser().getUserId());
        qtImproveProgresss.setOpCreateName(SecurityUtils.getUser().getName());
        return qtImproveProgresssService.save(qtImproveProgresss);
    }

    /**
     * 保存退回节点信息
     *
     * @param id
     * @param status
     * @return
     */
    private Boolean saveImproveProgresssReturn(Long id, Integer status, String reason) {
        QtImproveProgresss qtImproveProgresss = new QtImproveProgresss();
        qtImproveProgresss.setRecordId(id);
        if (QtManagerConstant.NUM_3.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D1.getName());
        } else if (QtManagerConstant.NUM_4.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D2.getName());
        } else if (QtManagerConstant.NUM_5.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D3.getName());
        } else if (QtManagerConstant.NUM_6.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D4.getName());
        } else if (QtManagerConstant.NUM_7.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D5.getName());
        } else if (QtManagerConstant.NUM_8.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D6.getName());
        } else if (QtManagerConstant.NUM_9.equals(status)) {
            qtImproveProgresss.setOpTitle(QtManagerConstant.improveProgress.RETURN_D7.getName());
        }
        qtImproveProgresss.setOpTime(LocalDateTime.now());
        qtImproveProgresss.setOpId(SecurityUtils.getUser().getUserId());
        qtImproveProgresss.setOpCreateName(SecurityUtils.getUser().getName());
        qtImproveProgresss.setOpNote(reason);
        return qtImproveProgresssService.save(qtImproveProgresss);
    }


    /**
     * 保存处理人
     *
     * @return
     */
    private Boolean saveHandler(QtImproveRecordDisposeDTO dto) {
        List<QtImproveRecordDetail> detailList = Lists.newArrayList();
        //获取处理节点信息
        List<QtImproveRecordDetail> list = qtImproveRecordDetailService.list(new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getRecordId, dto.getId()));
        if (CollUtil.isNotEmpty(list)) {
            List<QtImproveRecordDetailDTO> qtImproveRecordDetailDTOS = BeanUtil.copyToList(list, QtImproveRecordDetailDTO.class);
            qtImproveRecordDetailDTOS.forEach(detail -> {
                QtManagerConstant.DD.execute(detail, dto);
                if (!QtManagerConstant.improveProgress.D1_CREATE.getName().equals(detail.getApproverStep())) {
                    QtImproveRecordDetail qtImproveRecordDetail = BeanUtil.copyProperties(detail, QtImproveRecordDetail.class);
                    detailList.add(qtImproveRecordDetail);
                }
            });
            if (CollUtil.isEmpty(detailList)) {
                //创建d2-d8
                QtImproveRecordDetail d2 = new QtImproveRecordDetail();
                d2.setRecordId(dto.getId());
                d2.setApproverId(dto.getD2Processor());
                d2.setApproverName(dto.getD2ProcessorName());
                d2.setApproverStep(QtManagerConstant.improveProgress.D2_CLEAR_PROBLEM.getName());
                detailList.add(d2);
                QtImproveRecordDetail d3 = new QtImproveRecordDetail();
                d3.setRecordId(dto.getId());
                d3.setApproverId(dto.getD3Processor());
                d3.setApproverName(dto.getD3ProcessorName());
                d3.setApproverStep(QtManagerConstant.improveProgress.D3_COUNTERMEASURE.getName());
                detailList.add(d3);
                QtImproveRecordDetail d4 = new QtImproveRecordDetail();
                d4.setRecordId(dto.getId());
                d4.setApproverId(dto.getD4Processor());
                d4.setApproverName(dto.getD4ProcessorName());
                d4.setApproverStep(QtManagerConstant.improveProgress.D4_REASON_ANALYSIS.getName());
                detailList.add(d4);
                QtImproveRecordDetail d5 = new QtImproveRecordDetail();
                d5.setRecordId(dto.getId());
                d5.setApproverId(dto.getD5Processor());
                d5.setApproverName(dto.getD5ProcessorName());
                d5.setApproverStep(QtManagerConstant.improveProgress.D5_PERMANENT_CORRECTIVE_ACTION_PLAN.getName());
                detailList.add(d5);
                QtImproveRecordDetail d6 = new QtImproveRecordDetail();
                d6.setRecordId(dto.getId());
                d6.setApproverId(dto.getD6Processor());
                d6.setApproverName(dto.getD6ProcessorName());
                d6.setApproverStep(QtManagerConstant.improveProgress.D6_CORRECTIVE_ACTION_VERIFICATION.getName());
                detailList.add(d6);
                QtImproveRecordDetail d7 = new QtImproveRecordDetail();
                d7.setRecordId(dto.getId());
                d7.setApproverId(dto.getD7Processor());
                d7.setApproverName(dto.getD7ProcessorName());
                d7.setApproverStep(QtManagerConstant.improveProgress.D7_PREVENT_RECURRENCE.getName());
                detailList.add(d7);
                QtImproveRecordDetail d8 = new QtImproveRecordDetail();
                d8.setRecordId(dto.getId());
                d8.setApproverId(dto.getD8Processor());
                d8.setApproverName(dto.getD8ProcessorName());
                d8.setApproverStep(QtManagerConstant.improveProgress.D8_APPROVAL_END_PASS.getName());
                detailList.add(d8);
            }
        }
        return qtImproveRecordDetailService.saveOrUpdateBatch(detailList);
    }


    /**
     * 获取当前用户拥有的数据权限
     *
     * @return
     */
    private Set<Long> getUserAuthData() {
        Set<Long> recordIds = Sets.newHashSet();
        //获取处理人为自己的记录
        List<QtImproveRecordDetail> list = qtImproveRecordDetailService.list(new LambdaQueryWrapper<QtImproveRecordDetail>().eq(QtImproveRecordDetail::getApproverId, SecurityUtils.getUser().getUserId()));
        if (CollUtil.isNotEmpty(list)) {
            recordIds.addAll(list.stream().map(QtImproveRecordDetail::getRecordId).collect(Collectors.toSet()));
        }
        //获取创建人为自己的记录 和审批人为自己的记录
        List<QtImproveRecord> recordList = list(new LambdaQueryWrapper<QtImproveRecord>().eq(QtImproveRecord::getCreateBy, SecurityUtils.getUser().getUserId()).or().eq(QtImproveRecord::getStartApproverId, SecurityUtils.getUser().getUserId()));
        if (CollUtil.isNotEmpty(recordList)) {
            recordIds.addAll(recordList.stream().map(QtImproveRecord::getId).collect(Collectors.toSet()));
        }
        //获取抄送人为自己的记录
        List<QtImproveStepMan> stepMan = qtImproveStepManService.list(new LambdaQueryWrapper<QtImproveStepMan>().eq(QtImproveStepMan::getUserType, QtManagerConstant.NUM_1).eq(QtImproveStepMan::getUserId, SecurityUtils.getUser().getUserId()));
        if (CollUtil.isNotEmpty(stepMan)) {
            recordIds.addAll(stepMan.stream().map(QtImproveStepMan::getRecordId).collect(Collectors.toSet()));
        }
        return recordIds;
    }


    /**
     * 处理按钮权限
     *
     * @return
     */
    private QtImproveRecordVO disposeButton(QtImproveRecordVO qtImproveRecordVO) {
        Long userId = SecurityUtils.getUser().getUserId();
        Long curApproval = null;
        if (CollUtil.isNotEmpty(qtImproveRecordVO.getDetailList()) && qtImproveRecordVO.getDetailList().size() >= qtImproveRecordVO.getStatus() - 1 && qtImproveRecordVO.getStatus() >= 2) {
            QtImproveRecordDetailVO qtImproveRecordDetailVO = qtImproveRecordVO.getDetailList().get(qtImproveRecordVO.getStatus() - 2);
            curApproval = qtImproveRecordDetailVO.getApproverId();
        }
        if (QtManagerConstant.NUM_2.equals(qtImproveRecordVO.getStatus()) && userId.equals(curApproval)) {
            qtImproveRecordVO.setSubmitFlag(QtManagerConstant.NUM_1);
            qtImproveRecordVO.setSuspendFlag(QtManagerConstant.NUM_1);
        } else if ((qtImproveRecordVO.getStatus() <= QtManagerConstant.NUM_7 && qtImproveRecordVO.getStatus() >= QtManagerConstant.NUM_3) && userId.equals(curApproval)) {
            qtImproveRecordVO.setSubmitFlag(QtManagerConstant.NUM_1);
            qtImproveRecordVO.setSuspendFlag(QtManagerConstant.NUM_1);
            qtImproveRecordVO.setSendBackFlag(QtManagerConstant.NUM_1);
        } else if (QtManagerConstant.NUM_8.equals(qtImproveRecordVO.getStatus()) && userId.equals(curApproval)) {
            qtImproveRecordVO.setApproveFlag(QtManagerConstant.NUM_1);
            qtImproveRecordVO.setSendBackFlag(QtManagerConstant.NUM_1);
            qtImproveRecordVO.setSuspendFlag(QtManagerConstant.NUM_1);
        }
        return qtImproveRecordVO;
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



