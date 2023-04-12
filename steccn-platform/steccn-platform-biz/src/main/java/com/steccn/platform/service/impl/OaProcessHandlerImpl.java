package com.steccn.platform.service.impl;

import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.dto.OaProcessDataDTO;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import com.steccn.platform.feign.RemoteMesFeignApi;
import com.steccn.platform.service.OaProcessHandler;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.strategy.AbstractOaProcessStrategy;
import com.steccn.pms.dto.MaterialRequisitionApprovalDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.qms.feign.RemoteCustomerComplainFeignApi;
import com.steccn.qms.feign.RemoteDefectProductFeignApi;
import com.steccn.qms.feign.RemoteImproveProgressFeignApi;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.feign.RemoteWsFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @desc OA流程处理
 * @date 2023/4/12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OaProcessHandlerImpl implements OaProcessHandler {

    private final List<AbstractOaProcessStrategy> strategyList;

    @Override
    public void handler(OaProcessNotifyDTO dto) {
        strategyList.forEach(ele -> {
            ele.execute(dto);
        });
    }
}
