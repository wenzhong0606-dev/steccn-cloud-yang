package com.steccn.sales.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.ems.vo.EpDeepConsumeContrastVO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.sales.constant.SalesConstant;
import com.steccn.sales.domain.*;
import com.steccn.sales.dto.*;
import com.steccn.sales.mapper.*;
import com.steccn.sales.service.KsAbnormalCauseKsLogisticsOrderRelService;
import com.steccn.sales.service.KsAbnormalCauseService;
import com.steccn.sales.service.KsLogisticsCompanyService;
import com.steccn.sales.service.KsLogisticsOrderService;
import com.steccn.sales.vo.KsLogisticsVO;
import com.steccn.sales.vo.KsShippingReconciliationVO;
import com.steccn.wms.dto.WmsSignFeedbackDTO;
import com.steccn.wms.vo.WmsSignFeedbackVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final KsLogisticsOrderMapper ksLogisticsOrderMapper;
    private final KsLogisticsCompanyMapper ksLogisticsCompanyMapper;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final KsAbnormalCauseService ksAbnormalCauseService;
    private final KsAbnormalCauseKsLogisticsOrderRelService ksAbnormalCauseKsLogisticsOrderRelService;
    private final KsLogisticsCompanyService ksLogisticsCompanyService;

    @Override
    public Page<KsLogisticsVO> getLogisticsRecord(KsLogisticsDTO dto) {
        //需要传入仓库对应的货架
        SysOfficeTypeKeyDTO typeKey = new SysOfficeTypeKeyDTO();
        typeKey.setType(Lists.newArrayList("12"));
        typeKey.setKeyList(Lists.newArrayList(dto.getWarehouseName()));
        List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.getListByTypeAndNameList(typeKey), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysOfficeParentDTO parentDTO = new SysOfficeParentDTO();
        parentDTO.setParentIds(sysOfficeVOS.stream().map(SysOfficeVO::getId).collect(Collectors.toSet()));
        parentDTO.setOfficeType("5");
        List<SysOfficeVO> sysOfficeVOS1 = FeignOptional.of(() -> remoteOfficeFeign.listByParentIdAndType(parentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if(CollUtil.isNotEmpty(sysOfficeVOS1)){
            dto.getWarehouseName().addAll(sysOfficeVOS1.stream().map(SysOfficeVO::getOfficeName).collect(Collectors.toSet()));
        }
        //2.条件查询
        LambdaQueryWrapper<KsLogisticsOrder> condition = new LambdaQueryWrapper<KsLogisticsOrder>()
                .in(KsLogisticsOrder::getWarehouseNames, dto.getWarehouseName())
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

    @Override
    public QtLineChartBarDTO logisticsRecordDashBoard(KsLogisticsDTO dto) {
        //1、获取数据
        //需要传入仓库对应的货架
        SysOfficeTypeKeyDTO typeKey = new SysOfficeTypeKeyDTO();
        typeKey.setType(Lists.newArrayList("12"));
        typeKey.setKeyList(Lists.newArrayList(dto.getWarehouseName()));
        List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.getListByTypeAndNameList(typeKey), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysOfficeParentDTO parentDTO = new SysOfficeParentDTO();
        parentDTO.setParentIds(sysOfficeVOS.stream().map(SysOfficeVO::getId).collect(Collectors.toSet()));
        parentDTO.setOfficeType("5");
        List<SysOfficeVO> sysOfficeVOS1 = FeignOptional.of(() -> remoteOfficeFeign.listByParentIdAndType(parentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if(CollUtil.isNotEmpty(sysOfficeVOS1)){
            dto.getWarehouseName().addAll(sysOfficeVOS1.stream().map(SysOfficeVO::getOfficeName).collect(Collectors.toSet()));
        }
        List<KsLogisticsDashBoardDTO> dashBoard = ksLogisticsOrderMapper.selectLogisticsDashBoard(dto);
        SteccnTableAssertUtil.notEmpty(dashBoard, "暂无数据");
        //2、组数据
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        List<Integer> locationList = dashBoard.stream().filter(v->Objects.nonNull(v.getCustomerId())).map(KsLogisticsDashBoardDTO::getCustomerId).distinct().sorted().collect(Collectors.toList());
        List<Integer> companyList = dashBoard.stream().filter(v->Objects.nonNull(v.getCompanyId())).map(KsLogisticsDashBoardDTO::getCompanyId).distinct().sorted().collect(Collectors.toList());
        //收货地址
        List<ResPartner> resPartners = resPartnerMapper.selectList(new LambdaQueryWrapper<ResPartner>().in(ResPartner::getId, locationList));
        //公司
        List<KsLogisticsCompany> company = ksLogisticsCompanyMapper.selectList(new LambdaQueryWrapper<KsLogisticsCompany>().in(KsLogisticsCompany::getId, companyList));
        for(Integer tmp:locationList){
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            String location = resPartners.stream().filter(v->v.getId().equals(tmp)).findFirst().orElse(null).getName();
            stackGraphDTO.setXAxis(location);
            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
            companyList.forEach(companyTmp->{
                KsLogisticsDashBoardDTO ksLogisticsDashBoardDTO = dashBoard.stream().filter(v -> v.getCustomerId().equals(tmp) && Objects.nonNull(v.getCompanyId()) && v.getCompanyId().equals(companyTmp)).findFirst().orElse(null);
                KsLogisticsCompany companyInfo = company.stream().filter(v -> v.getId().equals(companyTmp)).findFirst().orElse(null);
                if(Objects.isNull(ksLogisticsDashBoardDTO)){
                    datList.add(new TupleDTO<>(companyInfo.getName(),BigDecimal.ZERO));
                }else {
                    datList.add(new TupleDTO<>(companyInfo.getName(),new BigDecimal(ksLogisticsDashBoardDTO.getValue())));
                }
            });
            stackGraphDTO.setData(datList);
            dataList.add(stackGraphDTO);
        }
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }

    @Override
    public Page<KsShippingReconciliationVO> shippingReconciliation(KsShippingReconciliationDTO dto) {
        //自定义sql查询订单信息列表
       return ksLogisticsOrderMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
    }

    @Override
    public BigDecimal salesDeliverySum(TupleDTO<LocalDate, LocalDate> dto) {
        BigDecimal bigDecimal = ksLogisticsOrderMapper.selectDeliverySum(dto);
        return bigDecimal;
    }

    @Override
    public EpDeepConsumeContrastVO logisticsNum(LocalDate start, LocalDate end) {
        List<KsLogisticsOrder> data = list(new QueryWrapper<KsLogisticsOrder>()
                .select("sum(sale_qty) As saleQty,send_date As sendDate")
                .groupBy("sendDate")
                .lambda()
                .ge(KsLogisticsOrder::getSendDate, start)
                .le(KsLogisticsOrder::getSendDate, end));
        SteccnTableAssertUtil.notEmpty(data,"数据为空");
        EpDeepConsumeContrastVO result = new EpDeepConsumeContrastVO();
        List<String> xAxis = Lists.newArrayList();
        List<BigDecimal> yData = Lists.newArrayList();
        data.forEach(tmp->{
            xAxis.add(tmp.getSendDate().toString());
            yData.add(tmp.getSaleQty());
        });
        result.setXAxis(xAxis);
        result.setYData(yData);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateOrderByOrderCode(KsLogisticsOrderDTO dto) {
        KsLogisticsOrder one = getOne(new LambdaQueryWrapper<KsLogisticsOrder>().eq(KsLogisticsOrder::getName, dto.getName()));
        one.setState("feedback");
        one.setSignType(dto.getSignType());
        one.setSignQty(dto.getSignQty());
        one.setPieceQty(dto.getPieceQty());
        one.setSignDate(dto.getSignDate());
        updateById(one);
        KsAbnormalCause ksAbnormalCause = new KsAbnormalCause();
        ksAbnormalCause.setName(dto.getAbnormalCause());
        ksAbnormalCauseService.save(ksAbnormalCause);
        Integer ksAbnormalCauseId = ksAbnormalCause.getId();
        KsAbnormalCauseKsLogisticsOrderRel ksAbnormalCauseKsLogisticsOrderRel = new KsAbnormalCauseKsLogisticsOrderRel();
        ksAbnormalCauseKsLogisticsOrderRel.setKsLogisticsOrderId(Math.toIntExact(one.getId()));
        ksAbnormalCauseKsLogisticsOrderRel.setKsAbnormalCauseId(ksAbnormalCauseId);
        ksAbnormalCauseKsLogisticsOrderRelService.save(ksAbnormalCauseKsLogisticsOrderRel);
        return Boolean.TRUE;
    }

    @Override
    public Map<String, String> salesOrderCarCode(Set<String> salesOrders) {
        Map<String, String> res = Maps.newHashMap();
        List<KsLogisticsOrder> list = list(new LambdaQueryWrapper<KsLogisticsOrder>().in(KsLogisticsOrder::getName, salesOrders));
        if (CollUtil.isNotEmpty(list)){
            list.forEach( ksLogisticsOrder -> {
                res.put(ksLogisticsOrder.getName(),ksLogisticsOrder.getCarCode());
            });
        }
        return res;
    }

    @Override
    public  String salesOrderCompany(String salesOrder) {
        String company = null;
        if (Objects.nonNull(salesOrder)){
            KsLogisticsOrder one = getOne(new LambdaQueryWrapper<KsLogisticsOrder>().eq(KsLogisticsOrder::getName, salesOrder));
            company = ksLogisticsCompanyService.getOne(new LambdaQueryWrapper<KsLogisticsCompany>().eq(KsLogisticsCompany::getId, one.getLogisticsCompanyId())).getName();
        }
        return company;
    }

    @Override
    public Integer salesOrderCompanyId(String name) {
        if (Objects.nonNull(name)){
            KsLogisticsOrder one = getOne(new LambdaQueryWrapper<KsLogisticsOrder>().eq(KsLogisticsOrder::getName, name));
           return one.getLogisticsCompanyId();
        }
        return null;
    }

    @Override
    public Page<WmsSignFeedbackVO> getSignFeedback(WmsSignFeedbackDTO dto) {
        if(StrUtil.isNotBlank(dto.getSignatureType())){
            if(dto.getSignatureType().equals("0")){
                dto.setSignatureType("normal");
            }else if(dto.getSignatureType().equals("1")){
                dto.setSignatureType("abnormal");
            }
        }
        Page<WmsSignFeedbackVO> result = ksLogisticsOrderMapper.logisticsPage(new Page(dto.getCurrent(), dto.getSize()),dto);
        return result;
    }
}




