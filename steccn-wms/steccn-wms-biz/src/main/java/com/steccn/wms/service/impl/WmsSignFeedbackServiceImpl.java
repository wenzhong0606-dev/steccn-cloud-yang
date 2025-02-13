package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(WmsSignFeedbackDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.SIGN_FEED_BACK);
        lock.lock();
        try{
            WmsSignFeedback one = getOne(new LambdaQueryWrapper<WmsSignFeedback>()
                    .eq(WmsSignFeedback::getSalesOrder, dto.getSalesOrder())
                    .last("LIMIT 1"));
            if(Objects.isNull(one)){
                WmsSignFeedback wmsSignFeedback = BeanUtil.copyProperties(dto, WmsSignFeedback.class);
                wmsSignFeedback.setState(0);
                Set<String> orders  = Sets.newHashSet();
                orders.add(wmsSignFeedback.getSalesOrder());
                KsLogisticsOrderDTO ksLogisticsOrderDTO = new KsLogisticsOrderDTO();
                ksLogisticsOrderDTO.setSalesOrders(orders);
                Map<String, String> carCodeMap = FeignOptional.of(() -> remoteSaleFeignApi.salesOrderCarCode(ksLogisticsOrderDTO), FeignConstant.STECCN_SALES_NAME).get();
                wmsSignFeedback.setCarCode(carCodeMap.get(wmsSignFeedback.getSalesOrder()));
                save(wmsSignFeedback);
                FeignOptional.of(() -> remoteSaleFeignApi.setInWmsColumn(ksLogisticsOrderDTO), FeignConstant.STECCN_SALES_NAME).get();
            }
        }finally {
            lock.unlock();
        }
        return true;
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
        Page page;
        //如果是待反馈状态则num为3
        if(dto.getState().equals(OaConstant.NUM_3)){
            if(Objects.nonNull(dto.getIsMobile())&&dto.getIsMobile()){
                dto.setUserName(SecurityUtils.getUser().getName());
            }
            page = FeignOptional.of(() -> remoteSaleFeignApi.signFeedback(dto), FeignConstant.STECCN_SALES_NAME).get();
        }else {
            LambdaQueryWrapper<WmsSignFeedback> between = new LambdaQueryWrapper<WmsSignFeedback>()
                    .like(Objects.nonNull(dto.getSalesOrder()), WmsSignFeedback::getSalesOrder, dto.getSalesOrder())
                    .like(Objects.nonNull(dto.getProduct()), WmsSignFeedback::getProduct, dto.getProduct())
                    .like(Objects.nonNull(dto.getLogisticsCompany()), WmsSignFeedback::getLogisticsCompany, dto.getLogisticsCompany())
                    .like(Objects.nonNull(dto.getCarCode()), WmsSignFeedback::getCarCode, dto.getCarCode())
                    .like(Objects.nonNull(dto.getClient()), WmsSignFeedback::getClient, dto.getClient())
                    .eq(Objects.nonNull(dto.getSignedQuantity()), WmsSignFeedback::getSignedQuantity, dto.getSignedQuantity())
                    .eq(Objects.nonNull(dto.getSignedPriceQuantity()), WmsSignFeedback::getSignedPriceQuantity, dto.getSignedPriceQuantity())
                    .eq(Objects.nonNull(dto.getSignatureType()), WmsSignFeedback::getSignatureType, Objects.nonNull(dto.getSignatureType())&&dto.getSignatureType().equals("0") ? "正常签收" : "异常签收")
                    .eq(Objects.nonNull(dto.getSendQuantity()), WmsSignFeedback::getSendQuantity, dto.getSendQuantity())
                    .eq(Objects.nonNull(dto.getPriceQuantity()), WmsSignFeedback::getPriceQuantity, dto.getPriceQuantity())
                    .eq(Objects.nonNull(dto.getIsMobile())&&dto.getIsMobile().equals(true),WmsSignFeedback::getCreateBy,SecurityUtils.getUser().getUserId())
                    .between(Objects.nonNull(dto.getStartDate()) && Objects.nonNull(dto.getEndDate()), WmsSignFeedback::getSignedDate, dto.getStartDate(), dto.getEndDate())
                    .between(Objects.nonNull(dto.getSendStartDate()) && Objects.nonNull(dto.getSendEndDate()),WmsSignFeedback::getSendDate,dto.getSendStartDate(),dto.getSendEndDate())
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
            page = page(new Page(dto.getCurrent(), dto.getSize()), between);
        }
        Page<WmsSignFeedbackVO> resPage = new Page<>();
        if (CollUtil.isNotEmpty(page.getRecords())){
            List<Long> fileIds = Lists.newArrayList();

            BeanUtil.copyProperties(page,resPage);
            List<WmsSignFeedbackVO> wmsSignFeedbackVOList = BeanUtil.copyToList(page.getRecords(), WmsSignFeedbackVO.class);
            if(Objects.nonNull(dto.getState())&&!dto.getState().equals(OaConstant.NUM_3)){
                Set<String> orders = Sets.newHashSet();
                for (WmsSignFeedbackVO wmsSignFeedbackVO : wmsSignFeedbackVOList) {
                    orders.add(wmsSignFeedbackVO.getSalesOrder());
                    fileIds.add(wmsSignFeedbackVO.getFileId());
                }
                KsLogisticsOrderDTO ksLogisticsOrderDTO = new KsLogisticsOrderDTO();
                ksLogisticsOrderDTO.setSalesOrders(orders);
                Map<String, String> carCodeMap = FeignOptional.of(() -> remoteSaleFeignApi.salesOrderCarCode(ksLogisticsOrderDTO), FeignConstant.STECCN_SALES_NAME).get();
                Map<Long, FileInfoVO> fileMap = FeignOptional.of(() -> remoteFileFeignApi.listById(fileIds), FeignConstant.STECCN_MINIO_SERVICE_NAME).get();
                for (WmsSignFeedbackVO wmsSignFeedbackVO : wmsSignFeedbackVOList) {
                    wmsSignFeedbackVO.setCarCode(carCodeMap.get(wmsSignFeedbackVO.getSalesOrder()));
                    wmsSignFeedbackVO.setFileUrl(fileMap.get(wmsSignFeedbackVO.getFileId()).getFileUrl());
                }
            }

            if (Objects.isNull(dto.getSortField())&&Objects.nonNull(dto.getState())&&!dto.getState().equals(OaConstant.NUM_3)) {
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
    public void downloadImages(HttpServletResponse response, WmsSignFeedbackDTO dto) throws IOException {
        LambdaQueryWrapper<WmsSignFeedback> between = new LambdaQueryWrapper<WmsSignFeedback>()
                .select(WmsSignFeedback::getFileId,WmsSignFeedback::getSalesOrder)
                .like(Objects.nonNull(dto.getSalesOrder()), WmsSignFeedback::getSalesOrder, dto.getSalesOrder())
                .like(Objects.nonNull(dto.getProduct()), WmsSignFeedback::getProduct, dto.getProduct())
                .like(Objects.nonNull(dto.getLogisticsCompany()), WmsSignFeedback::getLogisticsCompany, dto.getLogisticsCompany())
                .like(Objects.nonNull(dto.getCarCode()), WmsSignFeedback::getCarCode, dto.getCarCode())
                .like(Objects.nonNull(dto.getClient()), WmsSignFeedback::getClient, dto.getClient())
                .eq(Objects.nonNull(dto.getSignedQuantity()), WmsSignFeedback::getSignedQuantity, dto.getSignedQuantity())
                .eq(Objects.nonNull(dto.getSignedPriceQuantity()), WmsSignFeedback::getSignedPriceQuantity, dto.getSignedPriceQuantity())
                .eq(Objects.nonNull(dto.getSignatureType()), WmsSignFeedback::getSignatureType, Objects.nonNull(dto.getSignatureType())&&dto.getSignatureType().equals("0") ? "正常签收" : "异常签收")
                .eq(Objects.nonNull(dto.getSendQuantity()), WmsSignFeedback::getSendQuantity, dto.getSendQuantity())
                .eq(Objects.nonNull(dto.getPriceQuantity()), WmsSignFeedback::getPriceQuantity, dto.getPriceQuantity())
                .between(Objects.nonNull(dto.getStartDate()) && Objects.nonNull(dto.getEndDate()), WmsSignFeedback::getSignedDate, dto.getStartDate(), dto.getEndDate())
                .between(Objects.nonNull(dto.getSendStartDate()) && Objects.nonNull(dto.getSendEndDate()),WmsSignFeedback::getSendDate,dto.getSendStartDate(),dto.getSendEndDate())
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
        List<WmsSignFeedback> result = list(between);
        if(CollUtil.isEmpty(result)){
            throw new BusinessException("没有符合标准的图片");
        }
        if(result.size()>500){
            throw new BusinessException("下载图片数量不能超过500张");
        }
        List<Long> fileIds = result.stream().filter(v->Objects.nonNull(v.getFileId())).map(WmsSignFeedback::getFileId).collect(Collectors.toList());
        Map<Long, FileInfoVO> fileInfoVOMap = FeignOptional.of(() -> remoteFileFeignApi.listById(fileIds), FeignConstant.STECCN_MINIO_SERVICE_NAME).get();
        // 设置响应头
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=images.zip");
        Set<String> fileNameRepeat = Sets.newHashSet();
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (WmsSignFeedback tmp:result) {
                FileInfoVO fileInfoTmp = fileInfoVOMap.get(tmp.getFileId());
                if(Objects.isNull(fileInfoTmp)){
                    continue;
                }
                String urlStr = fileInfoTmp.getFileUrl();
                try (InputStream inputStream = new URL(urlStr).openStream()) {
                    // 给每个文件命名并添加到 ZIP

                    //避免文件名重复
                    String fileName;
                    if(fileNameRepeat.add(tmp.getSalesOrder())){
                        fileName = tmp.getSalesOrder() + getFileExtension(urlStr);
                    }else {
                        fileName = tmp.getSalesOrder()+ "_"+RandomUtil.randomString(2) + getFileExtension(urlStr);
                    }

                    zos.putNextEntry(new ZipEntry(fileName));

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        zos.write(buffer, 0, bytesRead);
                    }

                    zos.closeEntry();
                }
//                catch (IOException e) {
//                    log.error("Failed to download or add: " + urlStr);
//                }
            }
        }
//        catch (IOException e) {
//            log.error("Error creating ZIP: " + e.getMessage());
//        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
        ksLogisticsOrderDTO.setPriceQty(order.getPriceQuantity());
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

    private String getFileExtension(String urlStr) {
        int lastIndex = urlStr.lastIndexOf('.');
        if (lastIndex > 0) {
            return urlStr.substring(lastIndex);
        }
        return ""; // 默认无扩展名
    }

}




