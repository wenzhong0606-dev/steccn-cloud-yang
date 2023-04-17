package com.steccn.platform.strategy;

import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.dto.OaProcessDataDTO;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import com.steccn.pms.dto.MaterialRequisitionApprovalDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.feign.RemoteWsFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author yequan
 * @desc 抽象策略
 * @date 2023/4/12
 */
@Service
@RequiredArgsConstructor
public class RequisitionStrategy extends AbstractOaProcessStrategy {

    private final RemoteWsFeignApi remoteWsFeignApi;
    private final RemotePmsFeignApi remotePmsFeignApi;

    @Override
    public void execute(OaProcessNotifyDTO dto) {
        if (support(dto)) {
            requisition(dto);
        }
    }

    @Override
    public String getWorkFlow() {
        return OaDataConstant.Oa_WorkFlow_Type.REQUISITION.getWorkflowId().toString();
    }

    protected void requisition(OaProcessNotifyDTO dto) {
        OaProcessDataDTO data = dto.getData();
        //通过,只有当归档时不是拒绝状态才算通过
        if (dto.getType().equals(OaDataConstant.WORKFLOW_AGREE) && data.getNodeName().equals(OaDataConstant.WORKFLOW_FINISHED)) {
            //获取业务主键id
            Long id = FeignOptional.of(() -> remotePmsFeignApi.getIdByRequestId(Long.parseLong(data.getRequestId())), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            //根据id查询状态
            Long status = FeignOptional.of(() -> remotePmsFeignApi.getStatusById(id), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            if (Objects.nonNull(status) && !status.equals(Long.parseLong(OaDataConstant.NUM_3))) {
                //修改主键状态
                MaterialRequisitionApprovalDTO materialRequisitionApprovalDTO = new MaterialRequisitionApprovalDTO();
                materialRequisitionApprovalDTO.setId(id);
                materialRequisitionApprovalDTO.setApprovalFlag(1);
                materialRequisitionApprovalDTO.setNodeId(data.getNodeId());
                FeignOptional.of(() -> remotePmsFeignApi.approval(materialRequisitionApprovalDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
                //推送刷新消息
                MsgPushDTO msgPushDTO = new MsgPushDTO();
                msgPushDTO.setMsgType(WebsocketConstant.WsPushTypeEnum.REQUISITION.getType());
                FeignOptional.of(() -> remoteWsFeignApi.pushRefreshMsg(msgPushDTO), FeignConstant.STECCN_WEBSOCKET_SERVICE_NAME).get();
            }
        }

        //不通过
        if (dto.getType().equals(OaDataConstant.WORKFLOW_DISAGREE)) {
            //获取业务主键id
            Long id = FeignOptional.of(() -> remotePmsFeignApi.getIdByRequestId(Long.parseLong(data.getRequestId())), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            //修改主键状态
            MaterialRequisitionApprovalDTO materialRequisitionApprovalDTO = new MaterialRequisitionApprovalDTO();
            materialRequisitionApprovalDTO.setId(id);
            materialRequisitionApprovalDTO.setApprovalFlag(2);
            materialRequisitionApprovalDTO.setNodeId(data.getNodeId());
            FeignOptional.of(() -> remotePmsFeignApi.approval(materialRequisitionApprovalDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            //推送刷新消息
            MsgPushDTO msgPushDTO = new MsgPushDTO();
            msgPushDTO.setMsgType(WebsocketConstant.WsPushTypeEnum.REQUISITION.getType());
            FeignOptional.of(() -> remoteWsFeignApi.pushRefreshMsg(msgPushDTO), FeignConstant.STECCN_WEBSOCKET_SERVICE_NAME).get();
        }
    }
}
