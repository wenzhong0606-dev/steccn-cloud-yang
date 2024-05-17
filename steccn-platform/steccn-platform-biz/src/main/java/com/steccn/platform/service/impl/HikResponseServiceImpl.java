package com.steccn.platform.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.hikvision.feign.RemoteHikFeignApi;
import com.steccn.hikvision.vo.HikCompanyVO;
import com.steccn.platform.domain.HikResponse;
import com.steccn.platform.dto.HikDbDTO;
import com.steccn.platform.dto.HikDbWeighCommitDTO;
import com.steccn.platform.dto.HikDbWeighRegDTO;
import com.steccn.platform.service.HikResponseService;
import com.steccn.platform.mapper.HikResponseMapper;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.wms.dto.WmsWeighbridgeInfoDTO;
import com.steccn.wms.feign.RemoteWmsStorageReceiptFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
* @author Admin
* @description 针对表【hik_response】的数据库操作Service实现
* @createDate 2024-05-16 09:39:14
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class HikResponseServiceImpl extends ServiceImpl<HikResponseMapper, HikResponse>
    implements HikResponseService{
    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(3, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000), Executors.defaultThreadFactory());
    private final RemoteWmsStorageReceiptFeignApi remoteWmsStorageReceiptFeignApi;
    private final RemoteHikFeignApi remoteHikFeignApi;
    private final SysEqpSpareService eqpSpareService;

    @Override
    public Boolean saveResponse(HikDbDTO dto) {
        HikResponse response = new HikResponse();
        //称重确认时间的id
        response.setType("79174070273");
        // 解析为 OffsetDateTime
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dto.getSendTime());
        response.setHappenTime(offsetDateTime.toLocalDateTime());
        JSONObject jsonObject = new JSONObject(dto);
        response.setContentJson(jsonObject.toString());
        save(response);
        //保存到仓储页面
        if(dto.getEvents().get(0).getData().getModelData().getWeighReg().getWeigh_status().equals(200)){
            poolExecutor.execute(()->{
                WmsWeighbridgeInfoDTO weighbridgeDTO = new WmsWeighbridgeInfoDTO();
                List<HikDbWeighCommitDTO> weighCommits = dto.getEvents().get(0).getData().getModelData().getWeighCommits();
                HikDbWeighRegDTO weighReg = dto.getEvents().get(0).getData().getModelData().getWeighReg();
                String materialName = weighCommits.get(1).getTrans_materialName();
                EqpSpareVO eqpSpareVO = eqpSpareService.getByName(materialName);
                if(Objects.isNull(eqpSpareVO)){
                    weighbridgeDTO.setMaterialName(materialName);
                }else {
                    weighbridgeDTO.setMaterialId(eqpSpareVO.getId());
                    weighbridgeDTO.setMaterialName(materialName);
                    weighbridgeDTO.setMaterialCode(eqpSpareVO.getCode());
                }
                weighbridgeDTO.setFlowCode(weighReg.getWeigh_no());
                weighbridgeDTO.setLicensePlate(weighReg.getPlate_no());
                weighbridgeDTO.setDriverName(weighReg.getDriver_name());
                weighbridgeDTO.setDriverPhone(weighReg.getDriver_phone());
                List<HikCompanyVO> companyVOS = FeignOptional.of(() -> remoteHikFeignApi.hikCompanyInfo(new TupleDTO<String, String>(weighCommits.get(1).getCompany_id(), null)), FeignConstant.STECCN_HIK_SERVICE_NAME).get();
                weighbridgeDTO.setCompanyId(companyVOS.get(0).getCompanyId());
                weighbridgeDTO.setCompanyName(companyVOS.get(0).getCompanyName());
                weighbridgeDTO.setGrossWeight(weighCommits.get(1).getRaw_net_weight());
                weighbridgeDTO.setTareWeight(weighCommits.get(1).getDeduct_weight());
                weighbridgeDTO.setNetWeight(weighCommits.get(1).getResult());
                weighbridgeDTO.setFinishTime(OffsetDateTime.parse(weighReg.getEndTime()).toLocalDateTime());
                FeignOptional.of(()->remoteWmsStorageReceiptFeignApi.weighbridgeSave(weighbridgeDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            });

        }
        return true;
    }
}




