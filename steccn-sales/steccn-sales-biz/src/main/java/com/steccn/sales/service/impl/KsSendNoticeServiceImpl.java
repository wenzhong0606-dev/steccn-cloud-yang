package com.steccn.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.ems.vo.EpDeepConsumeContrastVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.sales.domain.KsSendNotice;
import com.steccn.sales.domain.ResPartner;
import com.steccn.sales.mapper.KsSendNoticeMapper;
import com.steccn.sales.mapper.ResPartnerMapper;
import com.steccn.sales.service.KsSendNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【ks_send_notice(发货通知单)】的数据库操作Service实现
* @createDate 2023-11-29 15:13:10
*/
@Service
@RequiredArgsConstructor
public class KsSendNoticeServiceImpl extends ServiceImpl<KsSendNoticeMapper, KsSendNotice>
    implements KsSendNoticeService{
    private final ResPartnerMapper resPartnerMapper;

    @Override
    public EpDeepConsumeContrastVO orderQuantityByCus(LocalDate start, LocalDate end, Integer type) {
        String colum = "sum(sale_qty) As saleQty";
        if(type.equals(OaConstant.NUM_1)){
            colum = "sum(send_qty) As saleQty";
        }
        List<KsSendNotice> data = list(new QueryWrapper<KsSendNotice>()
                .select(colum+",customer_id As customerId")
                .groupBy("customerId")
                .lambda()
                .ge(KsSendNotice::getSendDate, start)
                .le(KsSendNotice::getSendDate, end));
        SteccnTableAssertUtil.notEmpty(data,"数据为空");
        List<ResPartner> resPartners = resPartnerMapper.selectList(new LambdaQueryWrapper<ResPartner>()
                .in(ResPartner::getId, data.stream().map(KsSendNotice::getCustomerId).collect(Collectors.toSet())));
        Map<Integer, String> partnerMap = resPartners.stream().collect(Collectors.toMap(ResPartner::getId, ResPartner::getName));
        EpDeepConsumeContrastVO result = new EpDeepConsumeContrastVO();
        List<String> xAxis = Lists.newArrayList();
        List<BigDecimal> yData = Lists.newArrayList();
        data.forEach(tmp->{
            xAxis.add(partnerMap.get(tmp.getCustomerId()));
            yData.add(tmp.getSaleQty());
        });
        result.setXAxis(xAxis);
        result.setYData(yData);
        return result;
    }

    @Override
    public EpDeepConsumeContrastVO orderQuantityBySpe(LocalDate start, LocalDate end, Integer type) {
        String colum = "sum(sale_qty) As saleQty";
        if(type.equals(OaConstant.NUM_1)){
            colum = "sum(send_qty) As saleQty";
        }
        List<KsSendNotice> data = list(new QueryWrapper<KsSendNotice>()
                .select(colum+",model_specs As modelSpecs")
                .groupBy("modelSpecs")
                .lambda()
                .ge(KsSendNotice::getSendDate, start)
                .le(KsSendNotice::getSendDate, end));
        SteccnTableAssertUtil.notEmpty(data,"数据为空");
        EpDeepConsumeContrastVO result = new EpDeepConsumeContrastVO();
        List<String> xAxis = Lists.newArrayList();
        List<BigDecimal> yData = Lists.newArrayList();
        data.forEach(tmp->{
            xAxis.add(tmp.getModelSpecs());
            yData.add(tmp.getSaleQty());
        });
        result.setXAxis(xAxis);
        result.setYData(yData);
        return result;
    }

    @Override
    public EpDeepConsumeContrastVO orderQuantityByAttr(LocalDate start, LocalDate end, Integer type) {
        String colum = "sum(sale_qty) As saleQty";
        if(type.equals(OaConstant.NUM_1)){
            colum = "sum(send_qty) As saleQty";
        }
        List<KsSendNotice> data = list(new QueryWrapper<KsSendNotice>()
                .select(colum+", " +
                        "CASE" +
                        "    WHEN position('双膜无印' in model_specs) > 0 THEN '双膜无印'" +
                        "    WHEN position('无膜丝印' in model_specs) > 0 THEN '无膜丝印'" +
                        "    WHEN position('镀膜无印' in model_specs) > 0 THEN '镀膜无印'" +
                        "    WHEN position('无膜无印' in model_specs) > 0 THEN '无膜无印'" +
                        "    else '其他' END AS modelSpecs")
                .groupBy("modelSpecs")
                .lambda()
                .ge(KsSendNotice::getSendDate, start)
                .le(KsSendNotice::getSendDate, end));
        SteccnTableAssertUtil.notEmpty(data,"数据为空");
        EpDeepConsumeContrastVO result = new EpDeepConsumeContrastVO();
        List<String> xAxis = Lists.newArrayList();
        List<BigDecimal> yData = Lists.newArrayList();
        data.forEach(tmp->{
            if(!tmp.getModelSpecs().equals("其他")){
                xAxis.add(tmp.getModelSpecs());
                yData.add(tmp.getSaleQty());
            }
        });
        result.setXAxis(xAxis);
        result.setYData(yData);
        return result;
    }

    @Override
    public EpDeepConsumeContrastVO orderQuantityByCls(LocalDate start, LocalDate end, Integer type) {
        String colum = "sum(sale_qty) As saleQty";
        if(type.equals(OaConstant.NUM_1)){
            colum = "sum(send_qty) As saleQty";
        }
        List<KsSendNotice> data = list(new QueryWrapper<KsSendNotice>()
                .select(colum+", " +
                        "CASE" +
                        "    WHEN position('面板' in model_specs) > 0 THEN '面板'" +
                        "    WHEN position('背板' in model_specs) > 0 THEN '背板'" +
                        "    else '其他' END AS modelSpecs")
                .groupBy("modelSpecs")
                .lambda()
                .ge(KsSendNotice::getSendDate, start)
                .le(KsSendNotice::getSendDate, end));
        SteccnTableAssertUtil.notEmpty(data,"数据为空");
        EpDeepConsumeContrastVO result = new EpDeepConsumeContrastVO();
        List<String> xAxis = Lists.newArrayList();
        List<BigDecimal> yData = Lists.newArrayList();
        data.forEach(tmp->{
            if(!tmp.getModelSpecs().equals("其他")){
                xAxis.add(tmp.getModelSpecs());
                yData.add(tmp.getSaleQty());
            }
        });
        result.setXAxis(xAxis);
        result.setYData(yData);
        return result;
    }

    @Override
    public EpDeepConsumeContrastVO orderQuantityByThick(LocalDate start, LocalDate end, Integer type) {
        String colum = "sum(sale_qty) As saleQty";
        if(type.equals(OaConstant.NUM_1)){
            colum = "sum(send_qty) As saleQty";
        }
        List<KsSendNotice> data = list(new QueryWrapper<KsSendNotice>()
                .select(colum+", " +
                        "CASE" +
                        "    WHEN position('1.6mm' in model_specs) > 0 THEN '1.6mm'" +
                        "    WHEN position('2.0mm' in model_specs) > 0 THEN '2.0mm'" +
                        "    WHEN position('4.0mm' in model_specs) > 0 THEN '4.0mm'" +
                        "    else '其他' END AS modelSpecs")
                .groupBy("modelSpecs")
                .lambda()
                .ge(KsSendNotice::getSendDate, start)
                .le(KsSendNotice::getSendDate, end));
        SteccnTableAssertUtil.notEmpty(data,"数据为空");
        EpDeepConsumeContrastVO result = new EpDeepConsumeContrastVO();
        List<String> xAxis = Lists.newArrayList();
        List<BigDecimal> yData = Lists.newArrayList();
        data.forEach(tmp->{
            if(!tmp.getModelSpecs().equals("其他")){
                xAxis.add(tmp.getModelSpecs());
                yData.add(tmp.getSaleQty());
            }
        });
        result.setXAxis(xAxis);
        result.setYData(yData);
        return result;
    }
}




