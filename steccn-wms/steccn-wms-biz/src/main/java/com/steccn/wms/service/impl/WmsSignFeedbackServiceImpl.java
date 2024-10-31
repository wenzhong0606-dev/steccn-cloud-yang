package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.minio.fegin.RemoteFileFeignApi;
import com.steccn.minio.vo.FileInfoVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSalesFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.sales.dto.KsLogisticsOrderDTO;
import com.steccn.sales.feign.RemoteSaleFeignApi;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsSignFeedback;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.WmsSignFeedbackDTO;
import com.steccn.wms.service.WmsSignFeedbackService;
import com.steccn.wms.mapper.WmsSignFeedbackMapper;
import com.steccn.wms.vo.WmsOutInWarehouseInfoVO;
import com.steccn.wms.vo.WmsSignFeedbackVO;
import com.steccn.wms.vo.WmsStorageReceiptInfoVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author 34872
* @description 针对表【wms_sign_feedback】的数据库操作Service实现
* @createDate 2024-08-06 16:49:20
*/
@Service
@RequiredArgsConstructor
public class WmsSignFeedbackServiceImpl extends ServiceImpl<WmsSignFeedbackMapper, WmsSignFeedback>
    implements WmsSignFeedbackService{

    private final RemoteFileFeignApi remoteFileFeignApi;
    private final RemoteSaleFeignApi remoteSaleFeignApi;

    @Override
    public Boolean add(WmsSignFeedbackDTO dto) {
        WmsSignFeedback wmsSignFeedback = BeanUtil.copyProperties(dto, WmsSignFeedback.class);
        wmsSignFeedback.setState(0);
        Set<String> orders  = Sets.newHashSet();
        orders.add(wmsSignFeedback.getSalesOrder());
        KsLogisticsOrderDTO ksLogisticsOrderDTO = new KsLogisticsOrderDTO();
        ksLogisticsOrderDTO.setSalesOrders(orders);
        Map<String, String> carCodeMap = FeignOptional.of(() -> remoteSaleFeignApi.salesOrderCarCode(ksLogisticsOrderDTO), FeignConstant.STECCN_SALES_NAME).get();
        wmsSignFeedback.setCarCode(carCodeMap.get(wmsSignFeedback.getSalesOrder()));
        return save(wmsSignFeedback);
    }

    @Override
    public Boolean updateSignFeedback(WmsSignFeedbackDTO dto) {
        WmsSignFeedback wmsSignFeedback = BeanUtil.copyProperties(dto, WmsSignFeedback.class);
        wmsSignFeedback.setState(0);
        return updateById(wmsSignFeedback);
    }

    @Override
    public WmsSignFeedbackVO get(Long id) {
        WmsSignFeedback wmsSignFeedback = getById(id);
        WmsSignFeedbackVO wmsSignFeedbackVO = BeanUtil.copyProperties(wmsSignFeedback, WmsSignFeedbackVO.class);
        if (Objects.nonNull(wmsSignFeedbackVO.getFileId())){
            List<Long> fileIds = Lists.newArrayList();
            fileIds.add(wmsSignFeedbackVO.getFileId());
            Map<Long, FileInfoVO> longFileInfoVOMap = FeignOptional.of(() -> remoteFileFeignApi.listById(fileIds), FeignConstant.STECCN_MINIO_SERVICE_NAME).get();
            wmsSignFeedbackVO.setFileUrl(longFileInfoVOMap.get(wmsSignFeedbackVO.getFileId()).getFileUrl());
        }
        return wmsSignFeedbackVO;
    }

    @Override
    public Page<WmsSignFeedbackVO> pageList(WmsSignFeedbackDTO dto) {
        LambdaQueryWrapper<WmsSignFeedback> between = new LambdaQueryWrapper<WmsSignFeedback>()
                .like(Objects.nonNull(dto.getSalesOrder()), WmsSignFeedback::getSalesOrder, dto.getSalesOrder())
                .like(Objects.nonNull(dto.getProduct()), WmsSignFeedback::getProduct, dto.getProduct())
                .like(Objects.nonNull(dto.getLogisticsCompany()), WmsSignFeedback::getLogisticsCompany, dto.getLogisticsCompany())
                .like(Objects.nonNull(dto.getCarCode()), WmsSignFeedback::getCarCode, dto.getCarCode())
                .eq(Objects.nonNull(dto.getSignedQuantity()), WmsSignFeedback::getSignedQuantity, dto.getSignedQuantity())
                .eq(Objects.nonNull(dto.getSignedPriceQuantity()), WmsSignFeedback::getSignedPriceQuantity, dto.getSignedPriceQuantity())
                .eq(Objects.nonNull(dto.getSignatureType()), WmsSignFeedback::getSignatureType, dto.getSignatureType())
                .eq(Objects.nonNull(dto.getSendQuantity()), WmsSignFeedback::getSendQuantity, dto.getSendQuantity())
                .eq(Objects.nonNull(dto.getPriceQuantity()), WmsSignFeedback::getPriceQuantity, dto.getPriceQuantity())
                .between(Objects.nonNull(dto.getStartDate()) && Objects.nonNull(dto.getEndDate()), WmsSignFeedback::getSignedDate, dto.getStartDate(), dto.getEndDate())
                .eq(Objects.nonNull(dto.getState()), WmsSignFeedback::getState, dto.getState());

        if (Objects.nonNull(dto.getSortField())) {
            //定义排序条件
            if (WmsManagerConstant.SALES_ORDER.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getSalesOrder);
            } else if (WmsManagerConstant.CLIENT.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getClient);
            } else if (WmsManagerConstant.PRODUCT.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getProduct);
            } else if (WmsManagerConstant.SEND_DATE.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getSendDate);
            } else if (WmsManagerConstant.LOGISTICS_COMPANY.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getLogisticsCompany);
            } else if (WmsManagerConstant.CAR_CODE.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getCarCode);
            } else if (WmsManagerConstant.SEND_QUANTITY.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getSendQuantity);
            } else if (WmsManagerConstant.PRICE_QUANTITY.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getPriceQuantity);
            } else if (WmsManagerConstant.SIGNATURE_TYPE.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getSignatureType);
            }else if (WmsManagerConstant.SIGNED_QUANTITY.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getSignedQuantity);
            }else if (WmsManagerConstant.SIGNED_PRICE_QUANTITY.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getSignedPriceQuantity);
            }else if (WmsManagerConstant.SIGNED_DATE.equals(dto.getSortField())) {
                between.orderBy(Boolean.TRUE, dto.getAscFlag(), WmsSignFeedback::getSignedDate);
            }
        } else {
            between.orderByDesc(WmsSignFeedback::getCreateTime);
        }
        Page page = page(new Page(dto.getCurrent(), dto.getSize()), between);
        Page<WmsSignFeedbackVO> resPage = new Page<>();
        if (CollUtil.isNotEmpty(page.getRecords())){
            BeanUtil.copyProperties(page,resPage);
            List<WmsSignFeedbackVO> wmsSignFeedbackVOList = BeanUtil.copyToList(page.getRecords(), WmsSignFeedbackVO.class);
            Set<String> orders = Sets.newHashSet();
            for (WmsSignFeedbackVO wmsSignFeedbackVO : wmsSignFeedbackVOList) {
                orders.add(wmsSignFeedbackVO.getSalesOrder());
            }
            KsLogisticsOrderDTO ksLogisticsOrderDTO = new KsLogisticsOrderDTO();
            ksLogisticsOrderDTO.setSalesOrders(orders);
            Map<String, String> carCodeMap = FeignOptional.of(() -> remoteSaleFeignApi.salesOrderCarCode(ksLogisticsOrderDTO), FeignConstant.STECCN_SALES_NAME).get();
            for (WmsSignFeedbackVO wmsSignFeedbackVO : wmsSignFeedbackVOList) {
                wmsSignFeedbackVO.setCarCode(carCodeMap.get(wmsSignFeedbackVO.getSalesOrder()));
            }

            if (Objects.isNull(dto.getSortField())) {
                            wmsSignFeedbackVOList.sort(Comparator.comparing(WmsSignFeedbackVO::getSignedDate)
            );
                List<WmsSignFeedbackVO> collect = wmsSignFeedbackVOList.stream()
                        .sorted(Comparator.comparing(WmsSignFeedbackVO::getSignedDate, Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(WmsSignFeedbackVO::getLogisticsCompany, Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(WmsSignFeedbackVO::getClient, Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(WmsSignFeedbackVO::getProduct, Comparator.nullsLast(Comparator.naturalOrder()))
                                .thenComparing(WmsSignFeedbackVO::getSendDate, Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(WmsSignFeedbackVO::getSalesOrder, Comparator.nullsLast(Comparator.reverseOrder()))).collect(Collectors.toList());
                resPage.setRecords(collect);
            }else {
                resPage.setRecords(wmsSignFeedbackVOList);
            }
        }
        return resPage;
    }

    @Override
    public Boolean affirm(WmsSignFeedbackDTO dto) {
        Assert.notNull(dto.getId(),"反馈id不能为空");
        WmsSignFeedback order = getById(dto.getId());
        KsLogisticsOrderDTO ksLogisticsOrderDTO = new KsLogisticsOrderDTO();
        ksLogisticsOrderDTO.setName(order.getSalesOrder());
        if (order.getSignatureType().equals("正常签收")){
            ksLogisticsOrderDTO.setSignType("normal");
        }else {
            ksLogisticsOrderDTO.setSignType("abnormal");
        }
        ksLogisticsOrderDTO.setSignQty(order.getSignedQuantity());
        ksLogisticsOrderDTO.setPieceQty(order.getPriceQuantity());
        ksLogisticsOrderDTO.setSignDate(order.getSignedDate());
        ksLogisticsOrderDTO.setAbnormalCause(order.getAbnormalCause());
        ksLogisticsOrderDTO.setState("feedback");
        FeignOptional.of(() -> remoteSaleFeignApi.update(ksLogisticsOrderDTO), FeignConstant.STECCN_SALES_NAME).get();
        order.setState(1);
        return updateById(order);
    }

    @Override
    public Boolean sendBack(WmsSignFeedbackDTO dto) {
        Assert.notNull(dto.getId(),"反馈id不能为空");
        WmsSignFeedback order = getById(dto.getId());
        Assert.notNull(order,"反馈的物流单不存在");
        Assert.notBlank(dto.getSendBackReason(),"退回理由不能为空");
        if(!order.getState().equals(OaConstant.NO)){
            throw new BusinessException("只有待确认反馈状态的物流单才可退回");
        }
        WmsSignFeedback model = new WmsSignFeedback();
        model.setId(order.getId());
        model.setState(OaConstant.NUM_2);
        model.setSendBackReason(dto.getSendBackReason());
        updateById(model);
        return true;
    }

    @Override
    public Long countSendBack() {
        long count = count(new LambdaQueryWrapper<WmsSignFeedback>()
                .eq(WmsSignFeedback::getCreateBy, SecurityUtils.getUser().getUserId())
                .eq(WmsSignFeedback::getState, OaConstant.NUM_2));
        return count;
    }

    @Override
    public Page<WmsSignFeedbackVO> sendBackList(WmsSignFeedbackDTO dto) {
        LambdaQueryWrapper<WmsSignFeedback> between = new LambdaQueryWrapper<WmsSignFeedback>()
                .eq(WmsSignFeedback::getCreateBy, SecurityUtils.getUser().getUserId())
                .eq(WmsSignFeedback::getState, OaConstant.NUM_2)
                .orderByDesc(WmsSignFeedback::getUpdateTime);
        Page page = page(new Page(dto.getCurrent(), dto.getSize()), between);
        Page<WmsSignFeedbackVO> resPage = new Page<>();
        if (CollUtil.isNotEmpty(page.getRecords())){
            BeanUtil.copyProperties(page,resPage);
            List<WmsSignFeedbackVO> wmsSignFeedbackVOList = BeanUtil.copyToList(page.getRecords(), WmsSignFeedbackVO.class);
            List<Long> fileId = wmsSignFeedbackVOList.stream().filter(v -> Objects.nonNull(v.getFileId())).map(WmsSignFeedbackVO::getFileId).collect(Collectors.toList());
            Map<Long, FileInfoVO> longFileInfoVOMap = FeignOptional.of(() -> remoteFileFeignApi.listById(fileId), FeignConstant.STECCN_MINIO_SERVICE_NAME).get();
            wmsSignFeedbackVOList.forEach(tmp->{
                tmp.setFileUrl(longFileInfoVOMap.get(tmp.getFileId()).getFileUrl());
            });
            resPage.setRecords(wmsSignFeedbackVOList);
        }
        return resPage;
    }
}




