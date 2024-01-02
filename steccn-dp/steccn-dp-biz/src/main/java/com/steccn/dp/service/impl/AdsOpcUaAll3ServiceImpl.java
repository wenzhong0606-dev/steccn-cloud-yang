package com.steccn.dp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.*;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.common.vo.BaseValueNameVO;
import com.steccn.common.vo.BaseValueVO;
import com.steccn.common.vo.CommonBarLineVO;
import com.steccn.dp.DpConstant;
import com.steccn.dp.domain.AdsOpcUaAll3;
import com.steccn.dp.dto.*;
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
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.vo.PmRawMaterialVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
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
    private final RemotePmsFeignApi remotePmsFeignApi;

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
    public List<DpValueDateVO> weightCountAsDay(DpPmsOperateDTO dto) {
        List<DpValueDateVO> result = Lists.newArrayList();
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        result = weightCountAsDayTool(granularity, new TupleDTO<>(dto.getStart().atTime(8, 0, 0), dto.getEnd().plusDays(1).atTime(7, 59, 59)));
        return result;
    }

    @Override
    public QtLineChartBarDTO weightCountDashBoard(DpPmsOperateDTO dto) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        List<DpWeightVO> tripleDTOS = weightCount(granularity, new TupleDTO<>(dto.getStart().atTime(8, 0, 0), dto.getEnd().plusDays(1).atTime(7, 59, 59)));
        SteccnTableAssertUtil.notEmpty(tripleDTOS, "暂无数据");
        //找到所有的的tagName
        List<String> tagNameList = tripleDTOS.stream().map(DpWeightVO::getTagId).distinct().sorted().collect(Collectors.toList());
        Set<String> refSet = tripleDTOS.stream().map(DpWeightVO::getTagId).collect(Collectors.toSet());
        Map<String, List<SysConfigurationDTO>> dpWeight = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>("DP_WEIGHT", refSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //组图表
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
//        if(dto.getStart().equals(dto.getEnd())&&CollUtil.isEmpty(dateList)){
//            dateList.add(dto.getStart());
//        }
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate start = dateList.get(i - 1);
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(XAxisUtil.convert(granularity, start));
            //获取值的方式必须统一
            String key = MysqlDateKeyUtil.convert(granularity, start);
            List<DpWeightVO> resultTmp = tripleDTOS.stream().filter(v -> v.getDateStr().equals(key))
                    .sorted(Comparator.comparing(DpWeightVO::getTagId)).collect(Collectors.toList());
            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
            //若为空则空柱子
            if(CollUtil.isEmpty(resultTmp)){
                for(String str:tagNameList){
                    String tmpName = dpWeight.get(str).get(0).getRefContent();
                    if(tmpName.contains("1#")){
                        tmpName = tmpName.substring(2);
                    }
                    datList.add(new TupleDTO<>(tmpName, null));
                }
            }else {
                for(DpWeightVO tmp:resultTmp){
                    String tmpName = dpWeight.get(tmp.getTagId()).get(0).getRefContent();
                    if(tmpName.contains("1#")){
                        tmpName = tmpName.substring(2);
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
    public List<WeightCountDTO> countDTO(DpPmsOperateDTO dto) {
        List<WeightCountDTO> results = Lists.newArrayList();
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        List<DpWeightVO> tripleDTOS = weightCount(granularity, new TupleDTO<>(dto.getStart().atTime(8, 0, 0), dto.getEnd().plusDays(1).atTime(7, 59, 59)));
        if(CollUtil.isEmpty(tripleDTOS)){
            return results;
        }
        //找到所有的的tagName
        List<String> tagNameList = tripleDTOS.stream().map(DpWeightVO::getTagId).distinct().sorted().collect(Collectors.toList());
        Set<String> refSet = tripleDTOS.stream().map(DpWeightVO::getTagId).collect(Collectors.toSet());
        Map<String, List<SysConfigurationDTO>> dpWeight = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>("DP_WEIGHT", refSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        for(String str:tagNameList){
            String tmpName = dpWeight.get(str).get(0).getRefContent();
            if(tmpName.contains("1#")){
                tmpName = tmpName.substring(2);
            }
            WeightCountDTO result = new WeightCountDTO();
            result.setUnit("吨");
            result.setName(tmpName);
            results.add(result);
        }
        //组数据
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate start = dateList.get(i - 1);
            //获取值的方式必须统一
            String key = MysqlDateKeyUtil.convert(granularity, start);
            List<DpWeightVO> resultTmp = tripleDTOS.stream().filter(v -> v.getDateStr().equals(key))
                    .sorted(Comparator.comparing(DpWeightVO::getTagId)).collect(Collectors.toList());
            //若为空则空柱子
            if(CollUtil.isEmpty(resultTmp)){
                for(String str:tagNameList){
                    String tmpName = dpWeight.get(str).get(0).getRefContent();
                    if(tmpName.contains("1#")){
                        tmpName = tmpName.substring(2);
                    }
                    String finalTmpName = tmpName;
                    WeightCountDTO countTmp = results.stream().filter(v -> v.getName().equals(finalTmpName)).findFirst().orElse(null);
                    Map<String,BigDecimal> valueMap = Maps.newHashMap();
                    if(Objects.isNull(countTmp.getValueMap())){
                        countTmp.setValueMap(valueMap);
                    }else {
                        valueMap = countTmp.getValueMap();
                    }
                    valueMap.put(key,null);
                }
            }else {
                for(DpWeightVO tmp:resultTmp){
                    String tmpName = dpWeight.get(tmp.getTagId()).get(0).getRefContent();
                    if(tmpName.contains("1#")){
                        tmpName = tmpName.substring(2);
                    }
                    String finalTmpName = tmpName;
                    WeightCountDTO countTmp = results.stream().filter(v -> v.getName().equals(finalTmpName)).findFirst().orElse(null);
                    Map<String,BigDecimal> valueMap = Maps.newHashMap();
                    if(Objects.isNull(countTmp.getValueMap())){
                        countTmp.setValueMap(valueMap);
                    }else {
                        valueMap = countTmp.getValueMap();
                    }
                    valueMap.put(key,BigDecimalUtil.divide(tmp.getValue(),new BigDecimal(1000),2));
                }

            }
        }
        return results;
    }

    @Override
    public DpSalesContractVO salesContract(TupleDTO<LocalDate, LocalDate> dto) {
        DpSalesContractVO vo = new DpSalesContractVO();
        List<CommonBarLineVO> results = Lists.newArrayList();
        //调用三方接口
        SalesContractDTO contractDTO = new SalesContractDTO();
        contractDTO.setServer_code("query_contract");
        contractDTO.setStart_date(dto.getField1().toString());
        contractDTO.setEnd_date(dto.getField2().toString());
        String jsonResult = remoteSalesFeignApi.contractDashBoard(contractDTO);
        JSONObject jsonObject = JSONObject.parseObject(jsonResult);
        if(jsonObject.getJSONObject("result")
                .getJSONObject("mdMappings").getJSONArray("customer")==null){
            SteccnTableAssertUtil.notEmpty(Lists.newArrayList(), "暂无数据");
        }
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

    @Override
    public SteccnTableDataDTO<Map<String, String>> weightDetail(LocalDate date) {
        List<AdsOpcUaAll3> adsOpcUaAll3s = adsOpcUaAll3Mapper.selectList(new LambdaQueryWrapper<AdsOpcUaAll3>()
                .select(AdsOpcUaAll3::getTagId,AdsOpcUaAll3::getServertime,AdsOpcUaAll3::getValue)
                .ge(AdsOpcUaAll3::getServertime, date.atTime(8, 0, 0))
                .lt(AdsOpcUaAll3::getServertime, date.plusDays(1).atTime(7, 59, 59)));
        Assert.notEmpty(adsOpcUaAll3s,"数据为空");
        //按照时间升序排列
        List<LocalDateTime> serverTimeList = adsOpcUaAll3s.stream()
                .map(AdsOpcUaAll3::getServertime)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        //按照时间组成map
        Map<LocalDateTime, List<AdsOpcUaAll3>> serverTimeMap = adsOpcUaAll3s.stream()
                .collect(Collectors.groupingBy(AdsOpcUaAll3::getServertime));
        //生成报表数据
        SteccnTableDataDTO<Map<String,String>> result = new SteccnTableDataDTO();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("num","序号",null));
        //获取名称
        Set<String> refSet = adsOpcUaAll3s.stream().map(AdsOpcUaAll3::getTagId).collect(Collectors.toSet());
        Map<String, List<SysConfigurationDTO>> dpWeight = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>("DP_WEIGHT", refSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //组第一层表格
        List<String> tagNameList = new ArrayList<>(refSet);
        Collections.sort(tagNameList);
        tagNameList.add(tagNameList.remove(0));
        for(String tagName:tagNameList){
            columns.add(new SteccnTableColumn(tagName+"_name",dpWeight.get(tagName).get(0).getRefContent(),
                    Lists.newArrayList(new SteccnTableColumn(tagName,"实际重量",null))));
        }
        columns.add(new SteccnTableColumn("total","总计",null));
        //开始组数据
        List<Map<String,String>> tableList = Lists.newArrayList();
        //总计数据
        Map<String,BigDecimal> count = Maps.newHashMap();
        for(int i = 0; i < serverTimeList.size(); i++){
            Map<String,String> oneLine = Maps.newHashMap();
            LocalDateTime dateTime = serverTimeList.get(i);
            List<AdsOpcUaAll3> dataColumn = serverTimeMap.get(dateTime);
            oneLine.put("time",dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            oneLine.put("num",String.valueOf(i+1));
            oneLine.put("type","0");
            for(AdsOpcUaAll3 tmp:dataColumn){
                oneLine.put(tmp.getTagId(),tmp.getValue().toString());
                //合计
                BigDecimalUtil.accumulate(count,tmp.getTagId(),tmp.getValue());
            }
            tableList.add(oneLine);
        }
        //总计行
        Map<String,String> oneLine = Maps.newHashMap();
        oneLine.put("time","统计报表");
        oneLine.put("type","1");
        for(Map.Entry<String,BigDecimal> entry:count.entrySet()){
            oneLine.put(entry.getKey(),entry.getValue().toString());
        }
        //合计碎玻璃和硅砂
        BigDecimal countG = BigDecimalUtil.add(count.get(DpConstant.DP_WEIGHT_GUISHA_1_TAGID),count.get(DpConstant.DP_WEIGHT_GUISHA_2_TAGID));
        oneLine.put(DpConstant.DP_WEIGHT_GUISHA_1_TAGID,countG.toString());
        oneLine.put(DpConstant.DP_WEIGHT_GUISHA_2_TAGID,countG.toString());
        BigDecimal countS = BigDecimalUtil.add(count.get(DpConstant.DP_WEIGHT_GLASS_1_TAGID),count.get(DpConstant.DP_WEIGHT_GLASS_2_TAGID));
        oneLine.put(DpConstant.DP_WEIGHT_GLASS_1_TAGID,countS.toString());
        oneLine.put(DpConstant.DP_WEIGHT_GLASS_2_TAGID,countS.toString());
        BigDecimal totalSum = count.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        oneLine.put("total",totalSum.toString());
        tableList.add(oneLine);
        List<PmRawMaterialVO> pmRawMaterialVOS = FeignOptional.of(() -> remotePmsFeignApi.listByDate(new TupleDTO<>(date, date)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        if(CollUtil.isNotEmpty(pmRawMaterialVOS)){
            Map<String,String> oneLine2 = Maps.newHashMap();
            oneLine2.put("time","确认数量");
            oneLine2.put("type","2");
            pmRawMaterialVOS.forEach(tmp->{
                oneLine2.put(tmp.getTagId(),tmp.getValue().toString());
            });
            BigDecimal totalReduce = pmRawMaterialVOS.stream().map(PmRawMaterialVO::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
            oneLine2.put("total",totalReduce.toString());
            tableList.add(oneLine2);
        }else {
            //若无则需要返回总计行数据
            Map<String,String> oneLine2 = Maps.newHashMap();
            oneLine2.put("time","确认数量");
            oneLine2.put("type","2");
            for(Map.Entry<String,BigDecimal> entry:count.entrySet()){
                oneLine2.put(entry.getKey(),entry.getValue().toString());
            }
            //合计碎玻璃和硅砂
            oneLine2.put(DpConstant.DP_WEIGHT_GUISHA_1_TAGID,countG.toString());
            oneLine2.put(DpConstant.DP_WEIGHT_GUISHA_2_TAGID,countG.toString());
            oneLine2.put(DpConstant.DP_WEIGHT_GLASS_1_TAGID,countS.toString());
            oneLine2.put(DpConstant.DP_WEIGHT_GLASS_2_TAGID,countS.toString());
            oneLine2.put("total",totalSum.toString());
            tableList.add(oneLine2);
        }
        result.setTableList(tableList);
        result.setColumns(columns);
        return result;
    }

    public List<DpWeightVO> weightCount(QtManagerConstant.QtGranularityEnum granularity, TupleDTO<LocalDateTime,LocalDateTime> dto) {
        List<DpWeightVO> weightCountList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            weightCountList = adsOpcUaAll3Mapper.weightCountAsDayAndTag(dto);
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            weightCountList = adsOpcUaAll3Mapper.weightCountAsWeekAndTag(dto);
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            weightCountList = adsOpcUaAll3Mapper.weightCountAsMonthAndTag(dto);
        } else {
            weightCountList = adsOpcUaAll3Mapper.weightCountAsYearAndTag(dto);
        }
        //合并硅砂和碎玻璃
        if(CollUtil.isNotEmpty(weightCountList)){
            //重置
            weightCountList = weightCountList.stream()
                    .map(weight -> {
                        if (weight.getTagId() != null && weight.getTagId().equals(DpConstant.DP_WEIGHT_GUISHA_2_TAGID)) {
                            weight.setTagId(DpConstant.DP_WEIGHT_GUISHA_1_TAGID);
                        }
                        if (weight.getTagId() != null && weight.getTagId().equals(DpConstant.DP_WEIGHT_GLASS_2_TAGID)) {
                            weight.setTagId(DpConstant.DP_WEIGHT_GLASS_1_TAGID);
                        }
                        return weight;
                    })
                    .collect(Collectors.toList());
            //合并
            // 使用 Stream API 进行按月份和 tagId 合并累加
            weightCountList = weightCountList.stream()
                    .collect(Collectors.toMap(
                            v -> v.getDateStr() + "-" + v.getTagId(),
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
        return weightCountList;
    }

    public List<DpValueDateVO> weightCountAsDayTool(QtManagerConstant.QtGranularityEnum granularity, TupleDTO<LocalDateTime,LocalDateTime> dto){
        List<DpValueDateVO> weightCountList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            weightCountList = adsOpcUaAll3Mapper.weightCountAsDay(dto);
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            weightCountList = adsOpcUaAll3Mapper.weightCountAsYear(dto);
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            weightCountList = adsOpcUaAll3Mapper.weightCountAsMonth(dto);
        } else {
            weightCountList = adsOpcUaAll3Mapper.weightCountAsYear(dto);
        }
        return weightCountList;
    }
}




