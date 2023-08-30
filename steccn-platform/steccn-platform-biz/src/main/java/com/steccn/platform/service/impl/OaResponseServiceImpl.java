package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.domain.OaResponse;
import com.steccn.platform.domain.SysEqpSpare;
import com.steccn.platform.dto.*;
import com.steccn.platform.feign.RemoteMesFeignApi;
import com.steccn.platform.feign.RemoteSrmFeignApi;
import com.steccn.platform.mapper.OaResponseMapper;
import com.steccn.platform.service.OaProcessHandler;
import com.steccn.platform.service.OaResponseService;
import com.steccn.platform.service.SrmTokenService;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.vo.OaMaterialVO;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.feign.RemoteWsFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
    private final OaProcessHandler oaProcessHandler;
    private final SrmTokenService srmTokenService;
    private final RemoteSrmFeignApi remoteSrmFeignApi;
    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(3, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000), Executors.defaultThreadFactory());

    @Override
    public Boolean addProcessPush(OaProcessPushDTO dto) {
        OaResponse model = new OaResponse();
        model.setUniqueValue(dto.getUniqueValue());
        model.setType(OaDataConstant.Oa_Data_Type.PROCESS.getCode());
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
        //发送消息给三方系统
        //推给srm
        List<Integer> srmWorkFlowId = Lists.newArrayList(161,163,158,159,160);
        if(srmWorkFlowId.contains(dto.getData().getWorkflowId())){
            poolExecutor.execute(()->{
                try {
                    String srmOauthToken = srmTokenService.getSrmOauthToken();
                    MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
                    headers.add("Authorization","Bearer "+srmOauthToken);
                    SrmOaWorkFlowDTO oaMsg = new SrmOaWorkFlowDTO();
                    oaMsg.setPayload(jsonObject.toString());
                    String sendOaWorkFlowMsg = remoteSrmFeignApi.sendOaWorkFlowMsg(headers,oaMsg);
                    String status = com.alibaba.fastjson.JSONObject.parseObject(sendOaWorkFlowMsg).getString("status");
                    if(status==null
                            ||!status.equals(MpConstant.STATUS_200)){
                        log.error(sendOaWorkFlowMsg);
                    }

                }catch (Exception e) {
                    log.error("SrmSend",e.getLocalizedMessage());
                }
            });
        }
        model.setContentJson(jsonObject.toString());
        //2、根据工作流id调用对应的业务
        oaProcessHandler.handler(dto);
        try {
            MsgPushDTO msgPushDTO = new MsgPushDTO();
            msgPushDTO.setType(WebsocketConstant.WsPushTypeEnum.OA.getType());
            R<MsgPushDTO> wsRes = remoteWsFeignApi.pushRefreshMsg(msgPushDTO);
            log.debug("{}", wsRes.getData());
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
        }
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




