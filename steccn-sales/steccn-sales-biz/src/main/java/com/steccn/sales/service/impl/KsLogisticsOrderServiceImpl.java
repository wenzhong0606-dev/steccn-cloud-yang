package com.steccn.sales.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.sales.constant.SalesConstant;
import com.steccn.sales.domain.KsLogisticsOrder;
import com.steccn.sales.domain.KsSendNotice;
import com.steccn.sales.domain.ProductProduct;
import com.steccn.sales.domain.ResPartner;
import com.steccn.sales.dto.KsLogisticsDTO;
import com.steccn.sales.dto.KsSaleIdAndNameDTO;
import com.steccn.sales.mapper.KsSendNoticeMapper;
import com.steccn.sales.mapper.ProductProductMapper;
import com.steccn.sales.mapper.ResPartnerMapper;
import com.steccn.sales.service.KsLogisticsOrderService;
import com.steccn.sales.mapper.KsLogisticsOrderMapper;
import com.steccn.sales.vo.KsLogisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【ks_logistics_order(物流单)】的数据库操作Service实现
* @createDate 2023-11-29 14:16:04
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class KsLogisticsOrderServiceImpl extends ServiceImpl<KsLogisticsOrderMapper, KsLogisticsOrder>
    implements KsLogisticsOrderService{
    private final KsSendNoticeMapper ksSendNoticeMapper;
    private final ResPartnerMapper resPartnerMapper;
    private final ProductProductMapper productProductMapper;


    @Override
    public Page<KsLogisticsVO> getLogisticsRecord(KsLogisticsDTO dto) {
        //2.条件查询
        LambdaQueryWrapper<KsLogisticsOrder> condition = new LambdaQueryWrapper<KsLogisticsOrder>()
                .eq(KsLogisticsOrder::getWarehouseNames, dto.getWarehouseName())
                .eq(KsLogisticsOrder::getSendDate,dto.getDate())
                .orderByDesc(KsLogisticsOrder::getName);
        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.查询分页数据
        IPage<KsLogisticsOrder> resPage = page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的信息
        List<KsLogisticsVO> resRecords = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resPage.getRecords())) {
            List<KsLogisticsOrder> records = resPage.getRecords();
            //发货通知单号
            Set<Long> sendIds = records.stream().map(KsLogisticsOrder::getSendNoticeId).collect(Collectors.toSet());
            List<KsSendNotice> ksSendNotices = ksSendNoticeMapper.selectList(new LambdaQueryWrapper<KsSendNotice>().in(KsSendNotice::getId, sendIds));
            //客户
            Set<Integer> customerIds = records.stream().map(KsLogisticsOrder::getCustomerId).collect(Collectors.toSet());
            List<ResPartner> ResPartners = resPartnerMapper.selectList(new LambdaQueryWrapper<ResPartner>().in(ResPartner::getId, customerIds));
            //产品
            Set<Long> productIds = records.stream().map(KsLogisticsOrder::getProductId).collect(Collectors.toSet());
            List<ProductProduct> productProducts = productProductMapper.selectList(new LambdaQueryWrapper<ProductProduct>().in(ProductProduct::getId, productIds));
            //采购员
            Set<Integer> saleIds = records.stream().map(KsLogisticsOrder::getSaleId).collect(Collectors.toSet());
            List<KsSaleIdAndNameDTO> salesInfo = resPartnerMapper.selectSaleName(saleIds);
            for (KsLogisticsOrder ksLogisticsOrder : records) {
                KsLogisticsVO vo = new KsLogisticsVO();
                BeanUtils.copyProperties(ksLogisticsOrder, vo);
                //发货通知单号
                KsSendNotice ksSendNotice = ksSendNotices.stream().filter(v -> v.getId().equals(vo.getSendNoticeId())).findFirst().orElse(new KsSendNotice());
                vo.setSendNoticeName(ksSendNotice.getName());
                //客户
                ResPartner resPartner = ResPartners.stream().filter(v -> v.getId().equals(vo.getCustomerId())).findFirst().orElse(new ResPartner());
                vo.setCustomerName(resPartner.getName());
                //产品
                ProductProduct productProduct = productProducts.stream().filter(v -> v.getId().equals(vo.getProductId())).findFirst().orElse(new ProductProduct());
                vo.setProductName(productProduct.getDefaultCode());
                //包装类型
                vo.setPackTypeName(SalesConstant.salesPackTypeMapping.getNameByCode(vo.getPackType()));
                //采购员
                KsSaleIdAndNameDTO ksSaleIdAndNameDTO = salesInfo.stream().filter(v -> v.getSaleId().equals(vo.getSaleId())).findFirst().orElse(new KsSaleIdAndNameDTO());
                vo.setSaleName(ksSaleIdAndNameDTO.getSaleName());
                resRecords.add(vo);
            }
        }
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }
}




