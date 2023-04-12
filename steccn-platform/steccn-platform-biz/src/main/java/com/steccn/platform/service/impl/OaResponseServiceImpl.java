package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.domain.OaResponse;
import com.steccn.platform.domain.SysEqpSpare;
import com.steccn.platform.dto.*;
import com.steccn.platform.feign.RemoteMesFeignApi;
import com.steccn.platform.service.OaProcessHandler;
import com.steccn.platform.service.OaResponseService;
import com.steccn.platform.mapper.OaResponseMapper;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.MesClassificationVO;
import com.steccn.platform.vo.OaMaterialVO;
import com.steccn.pms.dto.MaterialRequisitionApprovalDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.qms.feign.RemoteCustomerComplainFeignApi;
import com.steccn.qms.feign.RemoteDefectProductFeignApi;
import com.steccn.qms.feign.RemoteImproveProgressFeignApi;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.feign.RemoteWsFeignApi;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
* @author Admin
* @description 针对表【oa_response】的数据库操作Service实现
* @createDate 2022-12-26 13:10:08
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class OaResponseServiceImpl extends ServiceImpl<OaResponseMapper, OaResponse>
    implements OaResponseService{

    private final SysEqpSpareService sysEqpSpareService;
    private final RemoteMesFeignApi remoteMesFeignApi;
    private final RemoteWsFeignApi remoteWsFeignApi;
    private final RemotePmsFeignApi remotePmsFeignApi;

    private final RemoteCustomerComplainFeignApi remoteCustomerComplainFeignApi;

    private final RemoteDefectProductFeignApi remoteDefectProductFeignApi;

    private final RemoteImproveProgressFeignApi remoteImproveProgressFeignApi;
    private final OaProcessHandler oaProcessHandler;

    @Override
    public Boolean addProcessPush(OaProcessPushDTO dto) {
        OaResponse model = new OaResponse();
        model.setUniqueValue(dto.getUniqueValue());
        model.setType(OaDataConstant.Oa_Data_Type.PROCESS.getCode());
        JSONObject jsonObject = new JSONObject(dto);
        model.setContentJson(jsonObject.toString());
        return save(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addMessagePush(OaMessagePushDTO dto) {
        OaResponse model = new OaResponse();
        model.setUniqueValue(dto.getUniqueValue());
        model.setType(OaDataConstant.Oa_Data_Type.MESSAGE.getCode());
        JSONObject jsonObject = new JSONObject(dto);
        model.setContentJson(jsonObject.toString());
        try {
            R<String> wsRes = remoteWsFeignApi.pushOa(Long.parseLong(dto.getUsername()));
            log.debug("{}", wsRes.getData());
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
        }
        return save(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addProcessNotify(OaProcessNotifyDTO dto) {
        //1、保存oa流程的值
        OaResponse model = new OaResponse();
        model.setUniqueValue(dto.getUniqueValue());
        model.setType(OaDataConstant.Oa_Data_Type.NOTIFY.getCode());
        if(dto.getCreationTime()!=null){
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dto.setCreationTimeStr(dto.getCreationTime().format(fmt));
        }
        JSONObject jsonObject = new JSONObject(dto);
        model.setContentJson(jsonObject.toString());
        //2、根据工作流id调用对应的业务
//        OaProcessDataDTO data = dto.getData();
        oaProcessHandler.handler(dto);
        //2.1生产领料单业务
//        if(data.getWorkflowId().equals(OaDataConstant.Oa_WorkFlow_Type.REQUISITION.getWorkflowId().toString())){
//            requisition(dto);
//        }
//        //2.2客诉业务
//        if (data.getWorkflowId().equals(OaDataConstant.Oa_WorkFlow_Type.CUSTOMER_COMPLAIN.getWorkflowId().toString())){
//           FeignOptional.of(() -> remoteCustomerComplainFeignApi.approve(dto), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
//        }
//        //2.3不合格品业务
//        if (data.getWorkflowId().equals(OaDataConstant.Oa_WorkFlow_Type.DEFECT_PRODUCT.getWorkflowId().toString())){
//            FeignOptional.of(() -> remoteDefectProductFeignApi.approve(dto), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
//        }
//        //2.4整改启动审批回调
//        if (data.getWorkflowId().equals(OaDataConstant.Oa_WorkFlow_Type.IMPROVE_PROGRESS_START.getWorkflowId().toString())){
//            FeignOptional.of(() ->remoteImproveProgressFeignApi.approve(dto),FeignConstant.STECCN_QMS_SERVICE_NAME).get();
//        }
//        //2.5整改结束审批回调
//        if (data.getWorkflowId().equals(OaDataConstant.Oa_WorkFlow_Type.IMPROVE_PROGRESS_END.getWorkflowId().toString())){
//            FeignOptional.of(() ->remoteImproveProgressFeignApi.finishApprove(dto),FeignConstant.STECCN_QMS_SERVICE_NAME).get();
//        }
        return save(model);
    }

    @Override
    public Page<OaMaterialVO> listPage(OaMaterialDTO dto) {
        //1.自定义查询条件
        LambdaQueryWrapper<SysEqpSpare> condition = new LambdaQueryWrapper<SysEqpSpare>()
                .like(StrUtil.isNotBlank(dto.getKey()), SysEqpSpare::getName, dto.getKey())
                //如果传了值则是该时间之后的值
                .ge(dto.getUpdateTime()!=null,SysEqpSpare::getUpdateDate,dto.getUpdateTime());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage<SysEqpSpare> resPage = sysEqpSpareService.page(page, condition);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        List<SysEqpSpare> records = resPage.getRecords();
        //5.处理list的数据
        List<OaMaterialVO> eqpSpareVOList = Lists.newArrayList();
        if(CollUtil.isNotEmpty(records)){
            //搜集分类id的数据
            Set<String> classIds = new HashSet<>();
            records.forEach(tmp -> {
                classIds.add(tmp.getType());
            });
            Map<String, String> classInfoById = remoteMesFeignApi.getClassInfoById(classIds);
            //再次遍历加入数据
            records.forEach(tmp -> {
                OaMaterialVO resultVO = new OaMaterialVO();
                resultVO.setMaterialCode(tmp.getCode());
                resultVO.setMaterialName(tmp.getName());
                resultVO.setUpdateTime(tmp.getUpdateDate());
                if(classInfoById.containsKey(tmp.getType())){
                    resultVO.setCategoryName(classInfoById.get(tmp.getType()));
                }else {
                    resultVO.setCategoryName("");
                }
                eqpSpareVOList.add(resultVO);
            });
        }
        return page.setRecords(eqpSpareVOList);
    }

//    protected void requisition(OaProcessNotifyDTO dto){
//        OaProcessDataDTO data = dto.getData();
//        //通过,只有当归档时不是拒绝状态才算通过
//        if(dto.getType().equals(OaDataConstant.WORKFLOW_AGREE)&&data.getNodeName().equals(OaDataConstant.WORKFLOW_FINISHED)){
//            //获取业务主键id
//            Long id = FeignOptional.of(() -> remotePmsFeignApi.getIdByRequestId(Long.parseLong(data.getRequestId())), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
//            //根据id查询状态
//            Long status = FeignOptional.of(() -> remotePmsFeignApi.getStatusById(id), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
//            if(Objects.nonNull(status) && !status.equals(Long.parseLong(OaDataConstant.NUM_3))){
//                //修改主键状态
//                MaterialRequisitionApprovalDTO materialRequisitionApprovalDTO = new MaterialRequisitionApprovalDTO();
//                materialRequisitionApprovalDTO.setId(id);
//                materialRequisitionApprovalDTO.setApprovalFlag(1);
//                materialRequisitionApprovalDTO.setNodeId(data.getNodeId());
//                FeignOptional.of(()->remotePmsFeignApi.approval(materialRequisitionApprovalDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
//                //推送刷新消息
//                MsgPushDTO msgPushDTO = new MsgPushDTO();
//                msgPushDTO.setMsgType(WebsocketConstant.WsPushTypeEnum.REQUISITION.getType());
//                FeignOptional.of(()->remoteWsFeignApi.pushRefreshMsg(msgPushDTO),FeignConstant.STECCN_WEBSOCKET_SERVICE_NAME).get();
//            }
//        }
//
//        //不通过
//        if(dto.getType().equals(OaDataConstant.WORKFLOW_DISAGREE)){
//            //获取业务主键id
//            Long id = FeignOptional.of(() -> remotePmsFeignApi.getIdByRequestId(Long.parseLong(data.getRequestId())), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
//            //修改主键状态
//            MaterialRequisitionApprovalDTO materialRequisitionApprovalDTO = new MaterialRequisitionApprovalDTO();
//            materialRequisitionApprovalDTO.setId(id);
//            materialRequisitionApprovalDTO.setApprovalFlag(2);
//            materialRequisitionApprovalDTO.setNodeId(data.getNodeId());
//            FeignOptional.of(()->remotePmsFeignApi.approval(materialRequisitionApprovalDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
//            //推送刷新消息
//            MsgPushDTO msgPushDTO = new MsgPushDTO();
//            msgPushDTO.setMsgType(WebsocketConstant.WsPushTypeEnum.REQUISITION.getType());
//            FeignOptional.of(()->remoteWsFeignApi.pushRefreshMsg(msgPushDTO),FeignConstant.STECCN_WEBSOCKET_SERVICE_NAME).get();
//        }
//    }
}




