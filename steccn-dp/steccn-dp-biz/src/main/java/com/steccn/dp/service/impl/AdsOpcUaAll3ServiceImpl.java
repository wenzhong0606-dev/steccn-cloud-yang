package com.steccn.dp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.common.vo.BaseValueNameVO;
import com.steccn.common.vo.BaseValueVO;
import com.steccn.common.vo.CommonBarLineVO;
import com.steccn.dp.domain.AdsOpcUaAll3;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.dto.SalesContractCustomerDTO;
import com.steccn.dp.dto.SalesContractModelDTO;
import com.steccn.dp.dto.SalesContractClassDTO;
import com.steccn.dp.service.AdsOpcUaAll3Service;
import com.steccn.dp.mapper.AdsOpcUaAll3Mapper;
import com.steccn.dp.vo.DpSalesContractVO;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.dp.vo.DpWeightVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.dto.SalesContractDTO;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.feign.RemoteSalesFeignApi;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【ads_opc_ua_all3(tagId包含Raw_YL.ZHM.fact_weigh的数据)】的数据库操作Service实现
* @createDate 2023-11-23 09:27:09
*/
@Service
@RequiredArgsConstructor
public class AdsOpcUaAll3ServiceImpl extends ServiceImpl<AdsOpcUaAll3Mapper, AdsOpcUaAll3>
    implements AdsOpcUaAll3Service{
    private final AdsOpcUaAll3Mapper adsOpcUaAll3Mapper;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;
    private final RemoteSalesFeignApi remoteSalesFeignApi;

    @Override
    public List<DpWeightVO> getWeighCount(TupleDTO<LocalDateTime, LocalDateTime> dto) {
        List<DpWeightVO> result = Lists.newArrayList();
        result = adsOpcUaAll3Mapper.weightCount(dto);
        if(CollUtil.isNotEmpty(result)){
            Set<String> refSet = result.stream().map(DpWeightVO::getTagId).collect(Collectors.toSet());
            Map<String, List<SysConfigurationDTO>> dpWeight = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>("DP_WEIGHT", refSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            result.forEach(tmp->{
                tmp.setTagName(dpWeight.get(tmp.getTagId()).get(0).getRefContent());
            });
        }
        return result;
    }

    @Override
    public List<DpWeightVO> getWeighCountDate(TupleDTO<LocalDate, LocalDate> dto) {
        List<DpWeightVO> weighCount = getWeighCount(new TupleDTO<>(dto.getField1().atTime(8, 0, 0), dto.getField2().plusDays(1).atTime(7, 59, 59)));
        List<DpWeightVO> results = Lists.newArrayList();
        if(CollUtil.isNotEmpty(weighCount)){
            //合并硅砂和碎玻璃
            List<DpWeightVO> sand = weighCount.stream().filter(v -> v.getTagName().contains("硅砂")).collect(Collectors.toList());
            List<DpWeightVO> glass = weighCount.stream().filter(v -> v.getTagName().contains("碎玻璃")).collect(Collectors.toList());
            BigDecimal sandValue = sand.stream().map(DpWeightVO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal glassValue = glass.stream().map(DpWeightVO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            for(DpWeightVO tmp:weighCount){
                if(tmp.getTagName().equals("2#硅砂")||tmp.getTagName().equals("2#碎玻璃")){
                    continue;
                }else if(tmp.getTagName().equals("1#硅砂")){
                    DpWeightVO result = new DpWeightVO();
                    BeanUtil.copyProperties(tmp,result);
                    result.setValue(BigDecimalUtil.divide(sandValue,new BigDecimal(1000),2) );
                    result.setTagName("硅砂");
                    results.add(result);
                }else if(tmp.getTagName().equals("1#碎玻璃")){
                    DpWeightVO result = new DpWeightVO();
                    BeanUtil.copyProperties(tmp,result);
                    result.setValue(BigDecimalUtil.divide(glassValue,new BigDecimal(1000),2) );
                    result.setTagName("碎玻璃");
                    results.add(result);
                }else {
                    DpWeightVO result = new DpWeightVO();
                    BeanUtil.copyProperties(tmp,result);
                    result.setValue(BigDecimalUtil.divide(tmp.getValue(),new BigDecimal(1000),2) );
                    results.add(result);
                }

            }
            results = results.stream().sorted(Comparator.comparing(DpWeightVO::getTagId)).collect(Collectors.toList());
        }
        return results;
    }

    @Override
    public List<DpValueDateVO> weightCountAsDay(TupleDTO<LocalDateTime, LocalDateTime> dto) {
        List<DpValueDateVO> result = Lists.newArrayList();
        result = adsOpcUaAll3Mapper.weightCountAsDay(dto);
        return result;
    }

    @Override
    public QtLineChartBarDTO weightCountDashBoard(DpPmsOperateDTO dto) {
        List<DpWeightVO> result = Lists.newArrayList();
        result = adsOpcUaAll3Mapper.weightCountAsDayAndTag(new TupleDTO<>(dto.getStart().atTime(8,0,0),dto.getEnd().plusDays(1).atTime(7,59,59)));
        SteccnTableAssertUtil.notEmpty(result, "暂无数据");
        //找到所有的的tagName
        List<String> tagNameList = result.stream().map(DpWeightVO::getTagId).distinct().sorted().collect(Collectors.toList());
        if(dto.getType().equals(OaConstant.NUM_1)){
            //把日期重置成第一天
            result.forEach(tmp->{
                tmp.setDate(tmp.getDate().withDayOfMonth(1));
            });
            // 使用 Stream API 进行按月份和 tagId 合并累加
            result = result.stream()
                    .collect(Collectors.toMap(
                            v -> v.getDate() + "-" + v.getTagId(),
                            Function.identity(),
                            (v1, v2) -> {
                                v1.setValue(BigDecimalUtil.add(v1.getValue(), v2.getValue()));
                                return v1;
                            }
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
        }
        Set<String> refSet = result.stream().map(DpWeightVO::getTagId).collect(Collectors.toSet());
        Map<String, List<SysConfigurationDTO>> dpWeight = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>("DP_WEIGHT", refSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //组图表
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        int dayCount = 0;
        if(dto.getType().equals(OaConstant.NO)){
            dayCount = (int)ChronoUnit.DAYS.between(dto.getStart(), dto.getEnd());
        }else {
            dayCount =  dto.getEnd().getMonthValue() - dto.getStart().getMonthValue();
        }
        for (int i = 0; i <= dayCount; i++) {
            LocalDate start = dto.getStart();
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            //0是日报表，1是月报表
            if(dto.getType().equals(OaConstant.NO)){
                start = dto.getStart().plusDays(i);
                stackGraphDTO.setXAxis(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }else {
                start = dto.getStart().withDayOfMonth(1).plusMonths(i);
                stackGraphDTO.setXAxis(start.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            }
            LocalDate finalStart = start;
            List<DpWeightVO> resultTmp = result.stream().filter(v -> v.getDate().equals(finalStart))
                    .sorted(Comparator.comparing(DpWeightVO::getTagId)).collect(Collectors.toList());
            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
            //若为空则空柱子
            if(CollUtil.isEmpty(resultTmp)){
                for(String str:tagNameList){
                    String tmpName = dpWeight.get(str).get(0).getRefContent();
                    //2#则跳过
                    if(tmpName.contains("2#")){
                        continue;
                    }
                    if(tmpName.contains("1#")){
                        tmpName = tmpName.substring(2);
                    }
                    datList.add(new TupleDTO<>(tmpName, null));
                }
            }else {
                for(DpWeightVO tmp:resultTmp){
                    String tmpName = dpWeight.get(tmp.getTagId()).get(0).getRefContent();
                    //2#则跳过
                    if(tmpName.contains("2#")){
                        continue;
                    }
                    if(tmpName.contains("1#")){
                        tmpName = tmpName.substring(2);
                    }
                    //若是硅砂和碎玻璃则累加
                    if(tmp.getTagId().equals("Raw_YL.ZHM.fact_weigh1_FLOAT")){
                        DpWeightVO twoResult = resultTmp.stream().filter(v -> v.getTagId().equals("Raw_YL.ZHM.fact_weigh2_FLOAT")).findFirst().orElse(null);
                        if(Objects.nonNull(twoResult)){
                            tmp.setValue(BigDecimalUtil.divide(BigDecimalUtil.add(tmp.getValue(),twoResult.getValue()),new BigDecimal(1000),2));
                        }
                    }
                    if(tmp.getTagId().equals("Raw_YL.ZHM.fact_weigh9_FLOAT")){
                        DpWeightVO twoResult = resultTmp.stream().filter(v -> v.getTagId().equals("Raw_YL.ZHM.fact_weigh10_FLOAT")).findFirst().orElse(null);
                        if(Objects.nonNull(twoResult)){
                            tmp.setValue(BigDecimalUtil.divide(BigDecimalUtil.add(tmp.getValue(),twoResult.getValue()),new BigDecimal(1000),2));
                        }
                    }
                    datList.add(new TupleDTO<>(tmpName, BigDecimalUtil.divide(tmp.getValue(),new BigDecimal(1000),2)));
                }

            }
            stackGraphDTO.setData(datList);
            dataList.add(stackGraphDTO);
        }
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }

    @Override
    public DpSalesContractVO salesContract(TupleDTO<LocalDate, LocalDate> dto) {
        DpSalesContractVO vo = new DpSalesContractVO();
        List<CommonBarLineVO> results = Lists.newArrayList();
        //调用三方接口
        SalesContractDTO contractDTO = new SalesContractDTO();
        contractDTO.setServer_code("query_contract");
        contractDTO.setStart_date(dto.getField1().toString());
        contractDTO.setStart_date(dto.getField2().toString());
        String jsonResult = remoteSalesFeignApi.contractDashBoard(contractDTO);
        JSONObject jsonObject = JSONObject.parseObject(jsonResult);
        List<SalesContractCustomerDTO> customers = jsonObject.getJSONObject("result")
                .getJSONObject("mdMappings").getJSONArray("customer").toJavaList(SalesContractCustomerDTO.class);
        List<SalesContractModelDTO> models = jsonObject.getJSONObject("result")
                .getJSONObject("mdMappings").getJSONArray("model").toJavaList(SalesContractModelDTO.class);
        List<SalesContractClassDTO> productClass = jsonObject.getJSONObject("result")
                .getJSONObject("mdMappings").getJSONArray("class").toJavaList(SalesContractClassDTO.class);
        SteccnTableAssertUtil.notEmpty(customers, "暂无数据");
        //开始组数据 customers
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO1 = new CommonBarLineVO();
        //根据粒度 分割日期
        List<String> xAisList1 = Lists.newArrayList();
        List<BigDecimal> quantityList1 = Lists.newArrayList();
        List<BigDecimal> rateList1 = Lists.newArrayList();
        customers.forEach(tmp->{
            xAisList1.add(tmp.getCustomer_name());
            quantityList1.add(tmp.getQty());
            rateList1.add(tmp.getRatio());
        });
        List<String> legendList1 = Arrays.asList("下单量（㎡）", "百分比%");
        commonBarLineVO1.setXAxis(new BaseValueVO(xAisList1));
        commonBarLineVO1.setLegend(new BaseValueVO<>(legendList1));
        BaseValueNameVO<List<BigDecimal>> quantityBv1 = new BaseValueNameVO<>(quantityList1, "下单量（㎡）");
        BaseValueNameVO<List<BigDecimal>> rateBv1 = new BaseValueNameVO<>(rateList1, "百分比%");
        commonBarLineVO1.setSeries(Arrays.asList(quantityBv1, rateBv1));
        results.add(commonBarLineVO1);
        //开始组数据 models
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO2 = new CommonBarLineVO();
        //根据粒度 分割日期
        List<String> xAisList2 = Lists.newArrayList();
        List<BigDecimal> quantityList2 = Lists.newArrayList();
        List<BigDecimal> rateList2 = Lists.newArrayList();
        models.forEach(tmp->{
            xAisList2.add(tmp.getModel_specs());
            quantityList2.add(tmp.getQty());
            rateList2.add(tmp.getRatio());
        });
        List<String> legendList2 = Arrays.asList("接单量（㎡）", "占比%");
        commonBarLineVO2.setXAxis(new BaseValueVO(xAisList2));
        commonBarLineVO2.setLegend(new BaseValueVO<>(legendList2));
        BaseValueNameVO<List<BigDecimal>> quantityBv2 = new BaseValueNameVO<>(quantityList2, "下单量（㎡）");
        BaseValueNameVO<List<BigDecimal>> rateBv2 = new BaseValueNameVO<>(rateList2, "百分比%");
        commonBarLineVO2.setSeries(Arrays.asList(quantityBv2, rateBv2));
        results.add(commonBarLineVO2);
        //开始组数据 productClass
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO3 = new CommonBarLineVO();
        //根据粒度 分割日期
        List<String> xAisList3 = Lists.newArrayList();
        List<BigDecimal> quantityList3 = Lists.newArrayList();
        List<BigDecimal> rateList3 = Lists.newArrayList();
        productClass.forEach(tmp->{
            xAisList3.add(tmp.getProduct_class());
            quantityList3.add(tmp.getQty());
            rateList3.add(tmp.getRatio());
        });
        List<String> legendList3 = Arrays.asList("接单量（㎡）", "百分比%");
        commonBarLineVO3.setXAxis(new BaseValueVO(xAisList3));
        commonBarLineVO3.setLegend(new BaseValueVO<>(legendList3));
        BaseValueNameVO<List<BigDecimal>> quantityBv3 = new BaseValueNameVO<>(quantityList3, "接单量（㎡）");
        BaseValueNameVO<List<BigDecimal>> rateBv3 = new BaseValueNameVO<>(rateList3, "百分比%");
        commonBarLineVO3.setSeries(Arrays.asList(quantityBv3, rateBv3));
        results.add(commonBarLineVO3);
        vo.setCommonBarLineVO(results);
        vo.setCountData(customers.stream().map(SalesContractCustomerDTO::getQty).reduce(BigDecimal.ZERO,BigDecimal::add));
        return vo;
    }
}




