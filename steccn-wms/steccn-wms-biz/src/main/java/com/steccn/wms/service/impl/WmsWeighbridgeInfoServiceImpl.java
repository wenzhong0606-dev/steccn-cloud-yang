package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.common.constant.MpConstant;
import com.steccn.hikvision.feign.RemoteHikFeignApi;
import com.steccn.oa.constant.OaConstant;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.vo.QtCheckPlanVO;
import com.steccn.wms.domain.WmsWeighbridgeInfo;
import com.steccn.wms.dto.WmsStorageReceiptDTO;
import com.steccn.wms.dto.WmsStorageReceiptInfoDTO;
import com.steccn.wms.dto.WmsWeighbridgeGerDTO;
import com.steccn.wms.dto.WmsWeighbridgeInfoDTO;
import com.steccn.wms.feign.RemoteWmsStorageReceiptFeignApi;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.service.WmsWeighbridgeInfoService;
import com.steccn.wms.mapper.WmsWeighbridgeInfoMapper;
import com.steccn.wms.vo.WmsWeighbridgeInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
* @author Admin
* @description 针对表【wms_weighbridge_info(地磅信息)】的数据库操作Service实现
* @createDate 2024-05-16 14:30:55
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class WmsWeighbridgeInfoServiceImpl extends ServiceImpl<WmsWeighbridgeInfoMapper, WmsWeighbridgeInfo>
    implements WmsWeighbridgeInfoService{
    private final WmsStorageReceiptService wmsStorageReceiptService;

    @Override
    public Boolean saveInfo(WmsWeighbridgeInfoDTO dto) {
        WmsWeighbridgeInfo model = new WmsWeighbridgeInfo();
        BeanUtil.copyProperties(dto,model);
        save(model);
        return true;
    }

    @Override
    public Page<WmsWeighbridgeInfoVO> pageList(WmsWeighbridgeInfoDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<WmsWeighbridgeInfo> condition = new LambdaQueryWrapper<WmsWeighbridgeInfo>()
                .like(StrUtil.isNotBlank(dto.getLicensePlate()),WmsWeighbridgeInfo::getLicensePlate,dto.getLicensePlate())
                .like(StrUtil.isNotBlank(dto.getDriverName()),WmsWeighbridgeInfo::getDriverName,dto.getDriverName())
                .like(StrUtil.isNotBlank(dto.getMaterialName()),WmsWeighbridgeInfo::getMaterialName,dto.getMaterialName())
                .ge(Objects.nonNull(dto.getFinishTimeStart()),WmsWeighbridgeInfo::getFinishTime,dto.getFinishTimeStart())
                .le(Objects.nonNull(dto.getFinishTimeEnd()),WmsWeighbridgeInfo::getFinishTime,dto.getFinishTimeEnd())
                .eq(Objects.nonNull(dto.getIsGenerateList()),WmsWeighbridgeInfo::getIsGenerateList, dto.getIsGenerateList());
        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.查询分页数据
        IPage<WmsWeighbridgeInfo> resPage = page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的信息
        List<WmsWeighbridgeInfoVO> resRecords = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resPage.getRecords())) {
            for (WmsWeighbridgeInfo wmsWeighbridgeInfo : resPage.getRecords()) {
                WmsWeighbridgeInfoVO vo = new WmsWeighbridgeInfoVO();
                BeanUtils.copyProperties(wmsWeighbridgeInfo, vo);
                resRecords.add(vo);
            }
        }
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean generateList(WmsWeighbridgeGerDTO dto) {
        List<WmsWeighbridgeInfo> result = list(new LambdaQueryWrapper<WmsWeighbridgeInfo>().in(WmsWeighbridgeInfo::getId,dto.getIds()));
        //生成采购入库单
        WmsStorageReceiptDTO receiptDTO = new WmsStorageReceiptDTO();
        receiptDTO.setWarehouseId(dto.getWarehouseId());
        receiptDTO.setType(1600405739121573888L);
        List<WmsStorageReceiptInfoDTO> receiptDTOList = Lists.newArrayList();
        if(CollUtil.isNotEmpty(result)){
            result.forEach(tmp->{
                WmsStorageReceiptInfoDTO wmsStorageReceiptInfoDTO = new WmsStorageReceiptInfoDTO();
                wmsStorageReceiptInfoDTO.setMaterialCode(tmp.getMaterialCode());
                wmsStorageReceiptInfoDTO.setMaterialId(tmp.getMaterialId());
                wmsStorageReceiptInfoDTO.setMaterialName(tmp.getMaterialName());
                wmsStorageReceiptInfoDTO.setQuantity(tmp.getNetWeight());
                wmsStorageReceiptInfoDTO.setShelfId(dto.getShelfId());
                wmsStorageReceiptInfoDTO.setShelfName(dto.getShelfName());
                wmsStorageReceiptInfoDTO.setShelfCode(dto.getShelfCode());
                wmsStorageReceiptInfoDTO.setUnit(1551473812960997376L);
                receiptDTOList.add(wmsStorageReceiptInfoDTO);
            });
        }
        receiptDTO.setList(receiptDTOList);
        String code = wmsStorageReceiptService.addReturnCode(receiptDTO);
        WmsWeighbridgeInfo model = new WmsWeighbridgeInfo();
        model.setIsGenerateList(OaConstant.NUM_1);
        model.setReceiptCode(code);
        update(model,new LambdaQueryWrapper<WmsWeighbridgeInfo>().in(WmsWeighbridgeInfo::getId,dto.getIds()));
        return true;
    }
}




