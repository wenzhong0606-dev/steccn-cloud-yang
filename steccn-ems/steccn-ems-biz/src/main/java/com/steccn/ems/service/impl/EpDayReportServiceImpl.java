package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.*;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.*;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.*;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
import com.steccn.pms.vo.JpAnalysisReportVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.excel.strategy.ExcelFillCellMergeStrategy;
import com.steccn.qms.excel.strategy.HeadContentStyleStrategy;
import com.steccn.qms.excel.strategy.RowHeightStyleStrategy;
import com.steccn.qms.util.MysqlDateKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 能耗日统计报表
 * @date 2023/6/1
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EpDayReportServiceImpl implements EpDayReportService {
    /**
     * 自定义bean，调用缓存
     */
    @Autowired
    private EpDayReportService self;

    private final EpEnergyTypeService epEnergyTypeService;
    private final EpPartitionService epPartitionService;
    private final EpMeterReadService meterReadService;
    private final RemoteReportWorkFeignApi reportWorkFeignApi;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;
    private final EpMeterReadService epMeterReadService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final EpDiscountFactorService epDiscountFactorService;
    private final RemotePmsFeignApi remotePmsFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final EpDayCountReportService epDayCountReportService;

    @Override
    @Cacheable(cacheNames = "ems_count#86400", unless = "#flag == false")
    public EpDayReportCountVO count(LocalDate start, boolean flag) {
        EpDayReportCountVO resVo = new EpDayReportCountVO();
        //获取一个月最后一天--如果当前日期大于end，则只显示到end，否则至显示到当前日期
        LocalDate end = getEndDate(start);
        //获取表头
        resVo.setHead(countHead(start, end));
        //获取所有的能源类型
        List<EpEnergyTypeVO> energyTypeList = epEnergyTypeService.getEpEnergyTypeVOList();
        //获取指定的组织结构名称和主键
        Map<Integer, List<TripleDTO<Long, Integer, String>>> defaultNodeMap = epPartitionService.dayReportCountDefaultNode();
        //异步执行任务
        try {
            List<EpDayReportCountRowVO> dataList = asyncTask(start, end, energyTypeList, defaultNodeMap);
            resVo.setDataList(dataList);
            BigDecimal ec11 = null, ec12 = null , ec1 = null, ec2 = null, ed = null, pb = null;
            //计算能源消费量总计
            BigDecimal equivalentValueSum = null;
            BigDecimal equalValueSum = null;
            for (EpDayReportCountRowVO ele : dataList) {
                if ("电".equals(ele.getEnergyType()) && ele.getDepartmentName().contains("基片二车间")) {
                    ed = ele.getEquivalentValue();
                } else if ("天然气".equals(ele.getEnergyType())) {
                    ec11 = ele.getEquivalentValue();
                } else if ("燃料油".equals(ele.getEnergyType())) {
                    ec2 = ele.getEquivalentValue();
                } else if ("LNG".equals(ele.getEnergyType())) {
                    ec12 = ele.getEquivalentValue();
                }

                equalValueSum = BigDecimalUtil.add(equalValueSum, ele.getEqualValue());
                equivalentValueSum = BigDecimalUtil.add(equivalentValueSum, ele.getEquivalentValue());
            }
            EpDayReportCountRowVO rowSum = new EpDayReportCountRowVO();
            rowSum.setEnergyType("能源消费量总计");
            rowSum.setEqualValue(BigDecimalUtil.setScale(equalValueSum, 2));
            rowSum.setEquivalentValue(BigDecimalUtil.setScale(equivalentValueSum, 2));
            dataList.add(rowSum);
            //计算单位产量综合能耗
            //单位产量综合能耗Eb = 1000 *（ec1 / (c1 * 1.08) + ec2 / c1 + ed) / Pb
            //ec1：“天然气”的能源消费量当量值总计
            //c1：当月窑龄系数（对照系数的有效期）
            //ec2：“燃料油”的能源消费量当量值总计
            //ed：“基片”行的能源消费量当量值
            //Pb：所有列的班次日期对应的基片入库总量加总，单位为吨（班次日期T的基片入库总量，相当于生产管理->统计分析报表->基片日报表中，班次日期T所有产线的“入库(t)"合计值加总）
            SysDictVO sysDictVO3 = FeignOptional.of(() -> remoteDictFeignApi.getByCode("2","discount_factor_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取日期范围内 有效的折标系数-窑龄系数
            Assert.notNull(sysDictVO3,"数据字典-窑龄系数不存在");
            List<EpDiscountFactorVO> validDiscountList3 = epDiscountFactorService.getValidDiscountList(sysDictVO3.getId(), start, end);
            EpDiscountFactorVO discountFactor3 = new EpDiscountFactorVO();
            if(CollUtil.isNotEmpty(validDiscountList3)){
                discountFactor3 = validDiscountList3.get(0);
            }
            BigDecimal c1 = GetValueUtil.get(discountFactor3, x -> x.getValue());
            SysOfficeTypeKeyDTO sysOfficeTypeKeyDTO = new SysOfficeTypeKeyDTO();
            sysOfficeTypeKeyDTO.setType(Lists.newArrayList(SysOfficeConstant.DEPARTMENT));
            sysOfficeTypeKeyDTO.setKeyList(Lists.newArrayList("基片组"));
            List<SysOfficeVO> officeList = FeignOptional.of(() -> remoteOfficeFeign.getListByTypeAndNameList(sysOfficeTypeKeyDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if(CollUtil.isNotEmpty(officeList)){
                pb = FeignOptional.of(() -> remotePmsFeignApi.partInStorageTon(new TripleDTO<>(officeList.get(0).getId(),start, end)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            }
            //单位产量综合能耗Eb = 1000 *（ec1 / (c1 * 1.08) + ec2 / c1 + ed) / Pb
            BigDecimal tmp1 = BigDecimalUtil.add(c1, new BigDecimal("1.08"));
            ec1 = BigDecimalUtil.add(ec11, ec12);
            tmp1 = BigDecimalUtil.divide(ec1, tmp1);
            BigDecimal tmp2 = BigDecimalUtil.divide(ec2, c1);
            tmp2 = BigDecimalUtil.add(tmp2, ed);
            BigDecimal tmp = BigDecimalUtil.add(tmp1,tmp2);
            tmp = BigDecimalUtil.divide(tmp , pb);
            tmp = BigDecimalUtil.multiply(tmp,BigDecimalUtil.THOUSAND);
            EpDayReportCountRowVO rowSumConvert = new EpDayReportCountRowVO();
            rowSumConvert.setEnergyType("单位产量综合能耗");
            rowSumConvert.setUnit("kgce/t");
            rowSumConvert.setEquivalentValue(BigDecimalUtil.setScale(tmp, 2));
            dataList.add(rowSumConvert);
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
        }
        //提示产线
        Map<String, List<String>> confMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confListString(new TripleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_ELEC, Sets.newHashSet(EpManagerConstant.ELECConfContentEnum.SJG_LINE14_LIST.toString(), EpManagerConstant.ELECConfContentEnum.SJG_LINE58_LIST.toString()), 3)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<String> hintList1 = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE14_LIST.toString(), Lists.newArrayList());
        List<String> hintList2 = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE58_LIST.toString(), Lists.newArrayList());
//        List<String> hintList3 = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG1_LINE14_LIST.toString(), Lists.newArrayList());
        hintList1.addAll(hintList2);
//        hintList1.addAll(hintList3);
        resVo.setHintLineList(hintList1);
        return resVo;
    }

    @Override
    public List<TupleDTO<String, String>> dataTwinCount() {
        LocalDate parse = LocalDate.now().withDayOfMonth(1);
        boolean flag = Boolean.FALSE;
        if (LocalDate.now().getMonthValue() > parse.getMonthValue()) {
            flag = Boolean.TRUE;
        }
        EpDayReportCountVO count = self.count(parse, flag);
//        List<EpDayReportCountRowVO> sum = count.getDataList().stream().filter(v -> StrUtil.isNotBlank(v.getDepartmentName())
//                &&v.getDepartmentName().equals("合计")).collect(Collectors.toList());
        List<TupleDTO<String,String>> results = Lists.newArrayList();
        for(EpDayReportCountRowVO vo:count.getDataList()){
            if(vo.getEnergyType().equals("电")&&vo.getDepartmentName().equals("合计")){
                //每月汇总
                TupleDTO<String,String> result = new TupleDTO<>();
                result.setField1("耗电量(每月汇总)");
                result.setField2(Objects.isNull(vo.getSumStr())?"0":vo.getSumStr().toString());
                results.add(result);
                //电耗，当前值
                TupleDTO<String,String> result2 = new TupleDTO<>();
                result2.setField1("耗电量(当前值)");
                BigDecimal v1 = vo.getConsumeList().get(vo.getConsumeList().size() - 2);
                LocalTime now = LocalTime.now();
                BigDecimal v2 = BigDecimalUtil.divide(new BigDecimal(now.toSecondOfDay()),new BigDecimal(86400));
                BigDecimal v3 = BigDecimalUtil.multiply(v1,v2,0);
                result2.setField2(Objects.isNull(v3)?"0":v3.toString());
                results.add(result2);
            }else if(vo.getEnergyType().equals("水")&&vo.getDepartmentName().equals("合计")){
                TupleDTO<String,String> result = new TupleDTO<>();
                result.setField1("耗水量");
                result.setField2(Objects.isNull(vo.getSumStr())?"0":vo.getSumStr().toString());
                results.add(result);
            }else if(vo.getEnergyType().equals("天然气")){
                TupleDTO<String,String> result = new TupleDTO<>();
                result.setField1("天然气");
                result.setField2(Objects.isNull(vo.getSumStr())?"0":vo.getSumStr().toString());
                results.add(result);
            }else if(vo.getEnergyType().equals("LNG")){
                TupleDTO<String,String> result = new TupleDTO<>();
                result.setField1("LNG");
                result.setField2(Objects.isNull(vo.getSumStr())?"0":vo.getSumStr().toString());
                results.add(result);
            }else if(vo.getEnergyType().equals("燃料油")){
                TupleDTO<String,String> result = new TupleDTO<>();
                result.setField1("燃料油");
                result.setField2(Objects.isNull(vo.getSumStr())?"0":vo.getSumStr().toString());
                results.add(result);
            }else if(vo.getEnergyType().equals("电（其他）")&&vo.getDepartmentName().equals("余热发电")){
                //每月汇总
                TupleDTO<String,String> result = new TupleDTO<>();
                result.setField1("余热发电(每月汇总)");
                result.setField2(Objects.isNull(vo.getSumStr())?"0":vo.getSumStr().toString());
                results.add(result);
                //电耗，当前值
                TupleDTO<String,String> result2 = new TupleDTO<>();
                result2.setField1("余热发电(当前值)");
                BigDecimal v1 = vo.getConsumeList().get(vo.getConsumeList().size() - 2);
                LocalTime now = LocalTime.now();
                BigDecimal v2 = BigDecimalUtil.divide(new BigDecimal(now.toSecondOfDay()),new BigDecimal(86400));
                BigDecimal v3 = BigDecimalUtil.multiply(v1,v2,0);
                result2.setField2(Objects.isNull(v3)?"0":v3.toString());
                results.add(result2);
            }else if(vo.getEnergyType().equals("电（其他）")&&vo.getDepartmentName().equals("光伏发电一")){
                EpDayReportCountRowVO station2 = count.getDataList().stream().filter(v -> StrUtil.isNotBlank(v.getDepartmentName())
                &&v.getDepartmentName().equals("光伏发电二")).findFirst().orElse(null);
                //每月汇总
                TupleDTO<String,String> result = new TupleDTO<>();
                result.setField1("光伏发电(每月汇总)");
                BigDecimal v = BigDecimalUtil.add(station2.getSumStr(), vo.getSumStr());
                result.setField2(Objects.isNull(v)?"0":v.toString());
                results.add(result);
                //电耗，当前值
                TupleDTO<String,String> result2 = new TupleDTO<>();
                result2.setField1("光伏发电(当前值)");
                BigDecimal v1 = BigDecimalUtil.add(vo.getConsumeList().get(vo.getConsumeList().size() - 2),
                        station2.getConsumeList().get(station2.getConsumeList().size() - 2));
                LocalTime now = LocalTime.now();
                BigDecimal v2 = BigDecimalUtil.divide(new BigDecimal(now.toSecondOfDay()),new BigDecimal(86400));
                BigDecimal v3 = BigDecimalUtil.multiply(v1,v2,0);
                result2.setField2(Objects.isNull(v3)?"0":v3.toString());
                results.add(result2);
            }
        }
        return results;
    }

    @Override
    public void countDownload(LocalDate month, boolean flag, HttpServletResponse response) throws IOException {
        try {
            EpDayReportCountVO data = count(month, flag);
            SteccnTableAssertUtil.notEmpty(data.getDataList(), "数据为空");
            //写表头
            List<List<String>> list = Lists.newArrayList();
            list.add(Lists.newArrayList(data.getHead().getEnergyType()));
            list.add(Lists.newArrayList(data.getHead().getUnit()));
            list.add(Lists.newArrayList(data.getHead().getDepartmentName()));
            data.getHead().getConsumeList().forEach(tmp->{
                list.add(Lists.newArrayList(tmp));
            });
            list.add(Lists.newArrayList(data.getHead().getSumStr()));
            list.add(Lists.newArrayList(data.getHead().getEquivalentValue()));
            list.add(Lists.newArrayList(data.getHead().getEqualValue()));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("能耗日统计报表", "UTF-8");
            fileName += LocalDateUtil.localDateFormat(month, "yyyy-MM");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            //数据
            List<List<Object>> dataList = Lists.newArrayList();
            data.getDataList().forEach(ele -> {
                List<Object> dataEle = Lists.newArrayList();
                dataEle.add(ele.getEnergyType());
                dataEle.add(ele.getUnit());
                dataEle.add(ele.getDepartmentName());
                if(CollUtil.isEmpty(ele.getConsumeList())){
                    for(int i = 0;i < data.getHead().getConsumeList().size();i++){
                        dataEle.add(null);
                    }
                }else {
                    for(int i = 0;i < data.getHead().getConsumeList().size();i++){
                        dataEle.add(ele.getConsumeList().get(i));
                    }
                }
                dataEle.add(ele.getSumStr());
                dataEle.add(ele.getEquivalentValue());
                dataEle.add(ele.getEqualValue());
                dataList.add(dataEle);
            });
            int[] mergeColumnIndex = {0};
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
            WriteSheet writeSheet = EasyExcel.writerSheet(0, "能耗日统计报表")
                    .head(list)
                    .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                    .registerWriteHandler(new HeadContentStyleStrategy())
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex))
                    .build();
            excelWriter.write(dataList, writeSheet);
            excelWriter.finish();
            response.flushBuffer();
        } catch (Exception e) {
            log.error("{}", e.getLocalizedMessage());
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
            Map<String, String> map = MapUtils.newHashMap();
            map.put("status", "failure");
            map.put("message", "下载文件失败" + e.getMessage());
            response.getWriter().println(JSON.toJSONString(map));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean countTask(LocalDate date) {
        EpDayReportCountVO count = count(date, Boolean.FALSE);
        Boolean flag = Boolean.FALSE;
        if(CollUtil.isNotEmpty(count.getDataList())){
            epDayCountReportService.remove(new LambdaQueryWrapper<EpDayCountReport>().eq(EpDayCountReport::getClassDate,date));
            List<EpDayCountReport> insertList = Lists.newArrayList();
            for (EpDayReportCountRowVO row : count.getDataList()) {
                EpDayCountReport model = new EpDayCountReport();
                model.setEnergyType(row.getEnergyType());
                model.setDepartmentName(row.getDepartmentName());
                model.setEnergyUnit(row.getUnit());
                model.setClassDate(date);
                if(CollUtil.isNotEmpty(row.getConsumeList())){
                    model.setConsumeValue(row.getConsumeList().get(0));
                    insertList.add(model);
                }
            }
            flag = epDayCountReportService.saveBatch(insertList);
        }
        return flag;
    }

    @Override
    @Cacheable(cacheNames = "unitConsumeCount#86400", unless = "#flag == false")
    public EpDayReportCountVO unitConsumeCount(LocalDate start, boolean flag) {
        EpDayReportCountVO resVo = new EpDayReportCountVO();
        //获取一个月最后一天--如果当前日期大于end，则只显示到end，否则至显示到当前日期
        LocalDate end = getEndDate(start);
        //获取表头
        resVo.setHead(countHead(start, end));
        //获取所有的能源类型
        List<EpEnergyTypeVO> energyTypeList = epEnergyTypeService.getEpEnergyTypeVOList();
        Optional<EpEnergyTypeVO> eleOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_ELECTRIC.equals(v.getId())).findFirst();
        EpEnergyTypeVO epEnergy = eleOp.get();
        //获取指定的组织结构名称和主键
        Map<Integer, List<TripleDTO<Long, Integer, String>>> defaultNodeMap = epPartitionService.dayReportCountDefaultNode();
        List<TripleDTO<Long, Integer, String>> partitionList = defaultNodeMap.get(EpManagerConstant.ENERGY_ELECTRIC);
        //获取配置map
        Map<String, List<Integer>> confMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confListInteger(new TripleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_ELEC, EpManagerConstant.ELECConfContentEnum.allContentConf(), 2)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //基片制造部
        List<Integer> jpzzbList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.JPZZB_LIST.toString(), Lists.newArrayList());
        //深加工四条产线【一二三四】
        List<Integer> sjgLine14List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE14_LIST.toString(), Lists.newArrayList());
        //深加工四条产线【五六七八】
        List<Integer> sjgLine58List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE58_LIST.toString(), Lists.newArrayList());
        //员工餐厅和宿舍】
        List<Integer> diningList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.DINING_AND_ROOM_LIST.toString(), Lists.newArrayList());
        //公用电【深加工二车间（东线）->公用电,深加工二车间（西线）->公用电】
        List<Integer> gydList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.GYD_LIST.toString(), Lists.newArrayList());
        //风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
        List<Integer> fl610List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.FL610_LIST.toString(), Lists.newArrayList());
        //风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
        List<Integer> fl15List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.FL15_LIST.toString(), Lists.newArrayList());
        //大数据控制中心
        List<Integer> dsjList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.DSJ_LIST.toString(), Lists.newArrayList());

        //深加工8条产线
        List<Integer> sjgLineList = Lists.newArrayList();
        sjgLine14List.forEach(ele -> sjgLineList.add(ele));
        sjgLine58List.forEach(ele -> sjgLineList.add(ele));

//        LocalDateTime startLdt = LocalDateTime.of(start, ClassUtil.MORNING_START_TIME);
//        LocalDateTime endLdt = LocalDateTime.of(end.plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集
        List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
//        List<TupleDTO<LocalDate, Integer>> classRangeList = ClassUtil.classRange(startLdt, endLdt);
//        Set<LocalDate> dateSet = classRangeList.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<Long> partitionIdList = partitionList.stream().map(TupleDTO::getField1).collect(Collectors.toList());
        //获取全量数据表
        List<EpMeterReadAllDataVO> classDateAllData = meterReadService.classDateAllData(Sets.newHashSet(timeList), epEnergy.getId(), partitionIdList);
        //每个节点，每天的数据
        //基片制造部=b1+b2+b3
        List<EpMeterReadAllDataVO> jpList = classDateAllData.stream().filter(v -> jpzzbList.contains(v.getSysOfficeId())).collect(Collectors.toList());
        //构建基片制造部
        EpDayReportCountRowVO jpRow = new EpDayReportCountRowVO();
        List<BigDecimal> jpValueList = Lists.newArrayList();

        Map<LocalDate, List<BigDecimal>> jpResMap = Maps.newHashMap();
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = jpList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
            jpResMap.put(ele, Lists.newArrayList(reduce));
        }
        jpRow.setConsumeList(jpValueList);
        jpRow.setUnit(epEnergy.getMeasureUnitName());
        jpRow.setEnergyType(epEnergy.getName());
        jpRow.setDepartmentName("基片");

        //构建生活用电 = 员工餐厅 + 员工宿舍
        EpDayReportCountRowVO shRow = new EpDayReportCountRowVO();
        List<EpMeterReadAllDataVO> shList = classDateAllData.stream().filter(v -> diningList.contains(v.getSysOfficeId())).collect(Collectors.toList());
        List<BigDecimal> shValueList = Lists.newArrayList();
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = shList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
            shValueList.add(BigDecimalUtil.setScale(reduce, 0));
        }
        BigDecimal shSum = BigDecimalUtil.sum(shValueList);
        shRow.setSumStr(BigDecimalUtil.setScale(shSum, 0));
        shRow.setConsumeList(shValueList);
        shRow.setUnit(epEnergy.getMeasureUnitName());
        shRow.setEnergyType(epEnergy.getName());
        shRow.setDepartmentName("生活用电");

        //深加工一~八产线
        List<TripleDTO<Long, Integer, String>> linePartitionList = partitionList.stream().filter(v -> sjgLine14List.contains(v.getField2())
                || sjgLine58List.contains(v.getField2())).collect(Collectors.toList());
        Set<Integer> lineOfficeIdSet = Sets.newHashSet();
        Map<String, List<BigDecimal>> lineResMap = Maps.newHashMap();

        List<EpDayReportCountRowVO> lineRowList = Lists.newArrayList();
        for (TripleDTO<Long, Integer, String> line : linePartitionList) {
            //获取产线id集合
            lineOfficeIdSet.add(line.getField2());
            List<EpMeterReadAllDataVO> lineList = classDateAllData.stream().filter(v -> line.getField1().equals(v.getOfficeId())).collect(Collectors.toList());
            //构建基片制造部
            EpDayReportCountRowVO lineRow = new EpDayReportCountRowVO();
            List<BigDecimal> lineValueList = Lists.newArrayList();
            for (LocalDate ele : timeList) {
                List<BigDecimal> timeDataList = lineList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal timeValue = BigDecimalUtil.sum(timeDataList);
                //a1.1 a2.1 a3.1 a4.1 a5.1 a6.1 a7.1 a8.1
                lineResMap.put(mapKey(line.getField2(), ele), Lists.newArrayList(timeValue));
            }
            lineRow.setConsumeList(lineValueList);
            lineRow.setUnit("kWh/㎡");
            lineRow.setEnergyType(epEnergy.getName());
            lineRow.setDepartmentName(line.getField3());
            lineRow.setOfficeId(line.getField2());
            lineRowList.add(lineRow);
        }

        //获取几条产线的产量及对应比例，用于分摊公用电
        Map<String, BigDecimal> productNumMap = FeignOptional.of(() -> reportWorkFeignApi.classDateProductNum(new TripleDTO<>(lineOfficeIdSet, start, end)), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
        TupleDTO<Map<String, BigDecimal>, Map<String, BigDecimal>> tupleDTO = getProductNumRateMap(sjgLine14List, sjgLine58List, Lists.newArrayList(),timeList, productNumMap);
        Map<String, BigDecimal> productNumLineRateMap = tupleDTO.getField1();
        Map<String, BigDecimal> productNumRateMap = tupleDTO.getField2();

        //获取公用电
        gydRate(classDateAllData, gydList, Lists.newArrayList(sjgLine14List, sjgLine58List), timeList, productNumLineRateMap, lineResMap);

        //获取风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
        //a1.3 a2.3 a3.3 a4.3 a5.3 a6.3 a7.3 a8.3
        fl610(classDateAllData, fl610List, sjgLineList, timeList, productNumRateMap, lineResMap);

        //获取风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
        //获取b2
        fl15(classDateAllData, fl15List, timeList, jpResMap);

        //获取总能耗N 按b1、a1.1 + a1.2 + a1.3、a2.1 + a2.2 + a2.3、... 、a8.1 + a8.2 + a8.3按照比例分摊
        //N分摊为b3、a1.4、a2.4、... 、a8.4共9个值
        Map<String, BigDecimal> nRateMap = getNRateMap(sjgLineList, timeList, jpResMap, lineResMap);
        dsj(classDateAllData, dsjList, sjgLineList, timeList, nRateMap, jpResMap, lineResMap);

        //重新计算基片行和产线行
        ftLineRow(lineRowList, timeList, lineResMap, new EpDiscountFactorVO(), new EpDiscountFactorVO());
        //构造深加工产线产量的数据
        //深加工1-4车间需要小计
        //深加工5-8车间需要小计
        Map<String,BigDecimal> valueMap = Maps.newHashMap();
        Map<String,BigDecimal> valueMap1 = Maps.newHashMap();
        Map<String,BigDecimal> valueMap2 = Maps.newHashMap();

        for (int i = 0; i < lineRowList.size(); i++) {
            EpDayReportCountRowVO row = lineRowList.get(i);
            List<BigDecimal> valueList = Lists.newArrayList();
            BigDecimal rowConsume = null;
            BigDecimal rowPm = null;
            for (int j = 1; j < timeList.size(); j++) {
                BigDecimal consume = row.getConsumeList().get(j - 1);
                LocalDate time = timeList.get(j - 1);
                BigDecimal pm = productNumMap.get(row.getOfficeId() + "_" + time);
                BigDecimal divide = BigDecimalUtil.divide(consume, pm, 1);
                MapUtil.add(valueMap, time + "_0", consume, (x, y) -> BigDecimalUtil.add(x, y));
                MapUtil.add(valueMap, time + "_1", pm, (x, y) -> BigDecimalUtil.add(x, y));
                rowConsume = BigDecimalUtil.add(rowConsume, consume);
                rowPm = BigDecimalUtil.add(rowPm, pm);
                if (i < 4) {
                    MapUtil.add(valueMap1, time + "_0", consume, (x, y) -> BigDecimalUtil.add(x, y));
                    MapUtil.add(valueMap1, time + "_1", pm, (x, y) -> BigDecimalUtil.add(x, y));
                } else {
                    MapUtil.add(valueMap2, time + "_0", consume, (x, y) -> BigDecimalUtil.add(x, y));
                    MapUtil.add(valueMap2, time + "_1", pm, (x, y) -> BigDecimalUtil.add(x, y));
                }
                valueList.add(divide);
            }
            row.setSumStr(BigDecimalUtil.divide(rowConsume, rowPm, 1));
            row.setConsumeList(valueList);
        }

        //添加合计和总计
        EpDayReportCountRowVO count1 = countSum(timeList, lineRowList.get(0), valueMap1, "小计（深加工二车间）");
        lineRowList.add(4, count1);
        EpDayReportCountRowVO count2 = countSum(timeList, lineRowList.get(0), valueMap2, "小计（深加工三车间）");
        lineRowList.add(lineRowList.size(), count2);
        EpDayReportCountRowVO count3 = countSum(timeList, lineRowList.get(0), valueMap, "合计");
        lineRowList.add(lineRowList.size(), count3);

        //需要天然气和燃料油
        EpDayReportCountRowVO gasOil = new EpDayReportCountRowVO();
        gasOil.setUnit("kcal/kg");
        gasOil.setDepartmentName("基片");
        //基片二车间
        Integer officeId = 76;
        EpPartition gasOilEp = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, officeId), false);
        Assert.notNull(gasOilEp, "燃料油单耗");
        List<EpMeterReadAllDataVO> gasOilValueList = epMeterReadService.kcalDataUnit(Sets.newHashSet(timeList), Lists.newArrayList(gasOilEp.getId()));
        Map<String,BigDecimal> gasOilValueMap = Maps.newHashMap();
        gasOilValueList.forEach(ele -> {
            gasOilValueMap.put(ele.getReadDate() + "_0", ele.getConsume());
            gasOilValueMap.put(ele.getReadDate() + "_1", ele.getProductNum());
        });
        List<BigDecimal> gasOilUnitConsumeList = Lists.newArrayList();
        List<BigDecimal> gasOilConsumeList = Lists.newArrayList();
        List<BigDecimal> gasOilPmList = Lists.newArrayList();
        for (int j = 1; j < timeList.size(); j++) {
            LocalDate time = timeList.get(j - 1);
            BigDecimal consume = gasOilValueMap.get(time + "_0");
            BigDecimal pm = gasOilValueMap.get(time + "_1");
            BigDecimal unitConsume = BigDecimalUtil.divide(consume, pm, 1);
            gasOilUnitConsumeList.add(unitConsume);
            gasOilConsumeList.add(consume);
            gasOilPmList.add(pm);
        }
        gasOil.setSumStr(BigDecimalUtil.divide(BigDecimalUtil.sum(gasOilConsumeList), BigDecimalUtil.sum(gasOilPmList)));
        gasOil.setEnergyType("天然气+燃料油+LNG");
        gasOil.setConsumeList(gasOilUnitConsumeList);
        lineRowList.add(gasOil);

        //提示产线
        Map<String, List<String>> hintConfMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confListString(new TripleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_ELEC, Sets.newHashSet(EpManagerConstant.ELECConfContentEnum.SJG_LINE14_LIST.toString(), EpManagerConstant.ELECConfContentEnum.SJG_LINE58_LIST.toString()), 3)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<String> hintList1 = hintConfMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE14_LIST.toString(), Lists.newArrayList());
        List<String> hintList2 = hintConfMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE58_LIST.toString(), Lists.newArrayList());
        hintList1.addAll(hintList2);
        resVo.setHintLineList(hintList1);
        resVo.setDataList(lineRowList);
        return resVo;
    }

    /**
     * 合计和小计
     * @param timeList
     * @param row
     * @param valueMap
     * @param department
     * @return
     */
    private EpDayReportCountRowVO countSum(List<LocalDate> timeList, EpDayReportCountRowVO row, Map<String, BigDecimal> valueMap, String department) {
        EpDayReportCountRowVO count = new EpDayReportCountRowVO();
        BeanUtils.copyProperties(row, count);
        List<BigDecimal> valueCount1List = Lists.newArrayList();
        count.setDepartmentName(department);
        BigDecimal rowConsume = null;
        BigDecimal rowPm = null;
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate time = timeList.get(i - 1);
            BigDecimal consume = valueMap.get(time + "_0");
            BigDecimal pm = valueMap.get(time + "_1");
            valueCount1List.add(BigDecimalUtil.divide(consume, pm, 1));
            rowConsume = BigDecimalUtil.add(rowConsume,consume);
            rowPm = BigDecimalUtil.add(rowPm,pm);
        }
        count.setConsumeList(valueCount1List);
        count.setSumStr(BigDecimalUtil.divide(rowConsume, rowPm, 1));
        return count;
    }

    @Override
    public List<CommonReportDTO> heatPowerElecWater(LocalDate start, LocalDate end, Integer granularityInt) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(granularityInt);
        Assert.notNull(granularity, "粒度不存在");
        List<CommonReportDTO> resList = Lists.newArrayList();
        Map<Integer, List<TripleDTO<Long, Integer, String>>> defaultNodeMap = epPartitionService.jpCountReportHeatPowerDefaultNode();
        List<TripleDTO<Long, Integer, String>> eleConfList = defaultNodeMap.getOrDefault(EpManagerConstant.ENERGY_ELECTRIC_OTHER, Lists.newArrayList());
        EpEnergyType electricType = epEnergyTypeService.getById(7);
        Assert.notNull(electricType, "能源类型-电不存在");
        EpEnergyType waterType = epEnergyTypeService.getById(2);
        Assert.notNull(waterType, "能源类型-水不存在");
        //获取余热发电-电
        List<TripleDTO<Long, Integer, String>> heatPowerList = eleConfList.stream().filter(ele -> "余热发电".equals(ele.getField3())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(heatPowerList)) {
            List<Integer> hpIdSet = heatPowerList.stream().map(TripleDTO::getField2).collect(Collectors.toList());
            Map<Integer, EpInstrumentAccount> accountMap = epPartitionService.listByOfficeId(EpManagerConstant.ENERGY_ELECTRIC_OTHER, Sets.newHashSet(hpIdSet));
            if (CollUtil.isNotEmpty(accountMap)) {
                Set<Integer> accoutIdSet = accountMap.values().stream().map(EpInstrumentAccount::getId).collect(Collectors.toSet());
                List<EpMeterRead> dbList = epMeterReadService.list(new LambdaQueryWrapper<EpMeterRead>()
                        .between(EpMeterRead::getReadDate, start, end)
                        .in(EpMeterRead::getMeterId, accoutIdSet));
                if (CollUtil.isNotEmpty(dbList)) {
                    hpIdSet.forEach(ele -> {
                        EpInstrumentAccount acc = accountMap.get(ele);
                        if (Objects.nonNull(acc)) {
                            List<EpMeterRead> collect = dbList.stream().filter(aa -> acc.getId().equals(aa.getMeterId())).collect(Collectors.toList());
                            CommonReportDTO res = jpHeatPowerElecRow(granularity, start, end, electricType, collect);
                            resList.add(res);
                        }
                    });
                }
            }
        }
        //获取余热发电-水
        List<TripleDTO<Long, Integer, String>> waterConfList = defaultNodeMap.getOrDefault(EpManagerConstant.ENERGY_WATER, Lists.newArrayList());
        List<TripleDTO<Long, Integer, String>> waterOfficeList = waterConfList.stream().filter(ele -> "余热发电".equals(ele.getField3())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(waterOfficeList)){
            List<Integer> hpIdSet = waterOfficeList.stream().map(TripleDTO::getField2).collect(Collectors.toList());
            Map<Integer, EpInstrumentAccount> waterAccountMap = epPartitionService.listByOfficeId(EpManagerConstant.ENERGY_WATER, Sets.newHashSet(hpIdSet));
            hpIdSet.forEach(ele -> {
                CommonReportDTO res = jpHeatPowerWaterRow(granularity, start, end, waterType, waterAccountMap.get(ele));
                resList.add(res);
            });
        }
        return resList;
    }

    @Override
    public List<CommonReportDTO> gfPowerList(LocalDate start, LocalDate end, Integer granularityInt) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(granularityInt);
        Assert.notNull(granularity, "粒度不存在");
        EpEnergyType electricType = epEnergyTypeService.getById(7);
        Assert.notNull(electricType, "能源类型-电不存在");
        List<CommonReportDTO> resList = Lists.newArrayList();
        List<TripleDTO<Long, Integer, String>> defaultConfList = epPartitionService.jpCountReportGfDefaultNode();
        if (CollUtil.isNotEmpty(defaultConfList)) {
            Set<Integer> hpIdSet = defaultConfList.stream().map(TripleDTO::getField2).collect(Collectors.toSet());
            //获取能耗
            Map<Integer, EpInstrumentAccount> accountMap = epPartitionService.listByOfficeId(EpManagerConstant.ENERGY_ELECTRIC_OTHER, hpIdSet);
            if(CollUtil.isNotEmpty(accountMap)){
                Set<Integer> accIdSet = accountMap.values().stream().map(EpInstrumentAccount::getId).collect(Collectors.toSet());
                List<EpMeterRead> dbList = epMeterReadService.list(new LambdaQueryWrapper<EpMeterRead>()
                        .between(EpMeterRead::getReadDate, start, end)
                        .in(EpMeterRead::getMeterId, accIdSet));
                if (CollUtil.isNotEmpty(dbList)) {
                    CommonReportDTO res = jpHeatPowerElecRow(granularity, start, end, electricType, dbList);
                    resList.add(res);
                }
            }
        }
        return resList;
    }

    @Override
    public List<CommonReportDTO> scList(LocalDate start, LocalDate end, Integer granularityInt) {
        //脱硫脱硝/电：取值与能耗日统计报表中“电/余热发电站（站用电）”相同
        //脱硫脱硝/水：抄表管理->水表->设备动力部->脱硫脱硝，根据读数差计算每日耗水量
        //确定粒度存在
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(granularityInt);
        Assert.notNull(granularity, "粒度不存在");
        Map<Integer, List<TripleDTO<Long, Integer, String>>> defaultNodeMap = epPartitionService.jpCountReportScDefaultNode();
        List<CommonReportDTO> resList = Lists.newArrayList();
        //电
        List<TripleDTO<Long, Integer, String>> eleConfList = defaultNodeMap.getOrDefault(EpManagerConstant.ENERGY_ELECTRIC_OTHER, Lists.newArrayList());
        EpEnergyType electricType = epEnergyTypeService.getById(7);
        Assert.notNull(electricType, "能源类型-电不存在");
        EpEnergyType waterType = epEnergyTypeService.getById(2);
        Assert.notNull(waterType, "能源类型-水不存在");
        if (CollUtil.isNotEmpty(eleConfList)) {
            List<Integer> hpIdSet = eleConfList.stream().map(TripleDTO::getField2).collect(Collectors.toList());
            Map<Integer, EpInstrumentAccount> accountMap = epPartitionService.listByOfficeId(EpManagerConstant.ENERGY_ELECTRIC_OTHER, Sets.newHashSet(hpIdSet));
            if (CollUtil.isNotEmpty(accountMap)) {
                Set<Integer> accoutIdSet = accountMap.values().stream().map(EpInstrumentAccount::getId).collect(Collectors.toSet());
                List<EpMeterRead> dbList = epMeterReadService.list(new LambdaQueryWrapper<EpMeterRead>()
                        .between(EpMeterRead::getReadDate, start, end)
                        .in(EpMeterRead::getMeterId, accoutIdSet));
                if (CollUtil.isNotEmpty(dbList)) {
                    hpIdSet.forEach(ele -> {
                        EpInstrumentAccount acc = accountMap.get(ele);
                        if (Objects.nonNull(acc)) {
                            List<EpMeterRead> collect = dbList.stream().filter(aa -> acc.getId().equals(aa.getMeterId())).collect(Collectors.toList());
                            CommonReportDTO res = jpHeatPowerElecRow(granularity, start, end, electricType, collect);
                            resList.add(res);
                        }
                    });
                }
            }
        }
        //获取余热发电-水
        List<TripleDTO<Long, Integer, String>> waterConfList = defaultNodeMap.getOrDefault(EpManagerConstant.ENERGY_WATER, Lists.newArrayList());
        if(CollUtil.isNotEmpty(waterConfList)){
            List<Integer> hpIdSet = waterConfList.stream().map(TripleDTO::getField2).collect(Collectors.toList());
            Map<Integer, EpInstrumentAccount> waterAccountMap = epPartitionService.listByOfficeId(EpManagerConstant.ENERGY_WATER, Sets.newHashSet(hpIdSet));
            hpIdSet.forEach(ele -> {
                CommonReportDTO res = jpHeatPowerWaterRow(granularity, start, end, waterType, waterAccountMap.get(ele));
                resList.add(res);
            });
        }
        return resList;
    }

    /**
     * 基片统计报表-余热发电-水
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param energy      能源类型
     * @param account     仪表
     * @return
     */
    private CommonReportDTO jpHeatPowerWaterRow(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, EpEnergyType energy, EpInstrumentAccount account) {
        //查询全量数据表
        LocalDateTime startLdt = LocalDateTime.of(start, ClassUtil.MORNING_START_TIME);
        LocalDateTime endLdt = LocalDateTime.of(end.plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集
        List<TupleDTO<LocalDate, Integer>> classRangeList = ClassUtil.classRange(startLdt, endLdt);
        Set<LocalDate> dateSet = classRangeList.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        //获取全量数据表
        List<EpMeterReadAllDataVO> dataList = meterReadService.classDateAllData(dateSet, energy.getId(), Lists.newArrayList(account.getPartitionNode()));
        CommonReportDTO waterReport = new CommonReportDTO();
        Map<String, BigDecimal> valueMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dataList)) {
            dataList.forEach(ele -> {
                String convert = MysqlDateKeyUtil.convert(granularity, ele.getReadDate());
                MapUtil.add(valueMap, convert, ele.getConsume(), (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        waterReport.setName(energy.getName());
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.get(energy.getMeasureUnit()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        waterReport.setUnit(GetValueUtil.get(sysDictVO, x -> x.getName()));
        waterReport.setValueMap(valueMap);
        return waterReport;
    }

    /**
     * 基片统计报表-余热发电-电
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param energy      能源类型
     * @param dbList      数据
     * @return
     */
    private CommonReportDTO jpHeatPowerElecRow(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, EpEnergyType energy, List<EpMeterRead> dbList) {
        CommonReportDTO heatPowerRow = new CommonReportDTO();
        Map<String, BigDecimal> valueMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                String convert = MysqlDateKeyUtil.convert(granularity, ele.getReadDate());
                MapUtil.add(valueMap, convert, ele.getAdminValue(), (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        heatPowerRow.setName(energy.getName());
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.get(energy.getMeasureUnit()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        heatPowerRow.setUnit(GetValueUtil.get(sysDictVO, x -> x.getName()));
        heatPowerRow.setValueMap(valueMap);
        return heatPowerRow;
    }

    /**
     * 异步执行任务
     *
     * @param start          开始日期
     * @param end            结束日期
     * @param energyTypeList 能源类型
     * @param defaultNodeMap 默认节点map
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private List<EpDayReportCountRowVO> asyncTask(LocalDate start, LocalDate end, List<EpEnergyTypeVO> energyTypeList, Map<Integer, List<TripleDTO<Long, Integer, String>>> defaultNodeMap) throws ExecutionException, InterruptedException {
        List<EpDayReportCountRowVO> dataList = Lists.newArrayList();
        //获取全部厚度
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        SysDictVO sysDictVO1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode("0", "discount_factor_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDictVO2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode("1", "discount_factor_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDictVO1,"数据字典-当量折标系数不存在");
        Assert.notNull(sysDictVO2,"数据字典-等价折标系数不存在");
        //获取日期范围内 有效的折标系数-当量折标系数
        List<EpDiscountFactorVO> validDiscountList1 = epDiscountFactorService.getValidDiscountList(sysDictVO1.getId(), start, end);
        //获取日期范围内 有效的折标系数-等价折标系数
        List<EpDiscountFactorVO> validDiscountList2 = epDiscountFactorService.getValidDiscountList(sysDictVO2.getId(), start, end);

        CompletableFuture<List<EpDayReportCountRowVO>> elecFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //判断电能源
                Optional<EpDiscountFactorVO> discount1 = validDiscountList1.stream().filter(ele -> Integer.valueOf(1).equals(ele.getEnergyType())).findFirst();
                Optional<EpDiscountFactorVO> discount2 = validDiscountList2.stream().filter(ele -> Integer.valueOf(1).equals(ele.getEnergyType())).findFirst();

                //计算电耗
                Optional<EpEnergyTypeVO> eleOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_ELECTRIC.equals(v.getId())).findFirst();
                EpEnergyTypeVO elecEnergy = eleOp.get();
                List<EpDayReportCountRowVO> elecList = calElec(elecEnergy, start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_ELECTRIC), discount1.orElse(new EpDiscountFactorVO()), discount2.orElse(new EpDiscountFactorVO()));
                //获取能源类型
                //获取电表（其他） - 余热发电的仪表
                List<TripleDTO<Long, Integer, String>> eleConfList = defaultNodeMap.getOrDefault(EpManagerConstant.ENERGY_ELECTRIC_OTHER, Lists.newArrayList());
                Optional<TripleDTO<Long, Integer, String>> heatPowerOp = eleConfList.stream().filter(ele -> ele.getField3().contains("余热发电站")).findFirst();
                if (heatPowerOp.isPresent()) {
                    Map<Integer, EpInstrumentAccount> accountMap = epPartitionService.listByOfficeId(EpManagerConstant.ENERGY_ELECTRIC_OTHER, Sets.newHashSet(heatPowerOp.get().getField2()));
                    if (CollUtil.isNotEmpty(accountMap)) {
                        Optional<EpInstrumentAccount> heatPowerAccount = accountMap.values().stream().findFirst();
                        EpDayReportCountRowVO heatPowerRow = heatPowerRow(start, end, elecEnergy, heatPowerAccount.get());
                        //能源消费量当量值tc 转换
                        BigDecimal shEquivalentValue = BigDecimalUtil.multiply(heatPowerRow.getSumStr(), GetValueUtil.get(discount1.get(), x -> x.getValue()));
                        shEquivalentValue = BigDecimalUtil.divide(shEquivalentValue, new BigDecimal("10000"));
                        heatPowerRow.setEquivalentValue(BigDecimalUtil.setScale(shEquivalentValue, 2));
                        //能源消费量等量值tce 转换
                        BigDecimal shEqualValue = BigDecimalUtil.multiply(heatPowerRow.getSumStr(), GetValueUtil.get(discount2.get(), x -> x.getValue()));
                        shEqualValue = BigDecimalUtil.divide(shEqualValue, new BigDecimal("10000"));
                        heatPowerRow.setEqualValue(BigDecimalUtil.setScale(shEqualValue, 2));
                        elecList.add(heatPowerRow);
                    }
                }
                //插入合计行
                EpDayReportCountRowVO sumRow = countRow(elecList, elecEnergy, discount1.get(), discount2.get());
                elecList.add(sumRow);
                return elecList;
            } catch (Exception e) {
                log.error("elec -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        CompletableFuture<List<EpDayReportCountRowVO>> oilFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //获取用油量数据
                Optional<EpEnergyTypeVO> oilOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_OIL.equals(v.getId())).findFirst();
                //判断天然气能源
                Optional<EpDiscountFactorVO> discount1 = validDiscountList1.stream().filter(ele -> EpManagerConstant.ENERGY_OIL.equals(ele.getEnergyType())).findFirst();
                Optional<EpDiscountFactorVO> discount2 = validDiscountList2.stream().filter(ele -> EpManagerConstant.ENERGY_OIL.equals(ele.getEnergyType())).findFirst();
                List<EpDayReportCountRowVO> oilRowList = consumeRow(oilOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_OIL),discount1.orElse(new EpDiscountFactorVO()),discount2.orElse(new EpDiscountFactorVO()));
                return oilRowList;
            } catch (Exception e) {
                log.error("oil -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        CompletableFuture<List<EpDayReportCountRowVO>> gasFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //获取用气量
                Optional<EpEnergyTypeVO> gasOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_GAS.equals(v.getId())).findFirst();
                //判断天然气能源
                Optional<EpDiscountFactorVO> discount1 = validDiscountList1.stream().filter(ele -> EpManagerConstant.ENERGY_GAS.equals(ele.getEnergyType())).findFirst();
                Optional<EpDiscountFactorVO> discount2 = validDiscountList2.stream().filter(ele -> EpManagerConstant.ENERGY_GAS.equals(ele.getEnergyType())).findFirst();
                List<EpDayReportCountRowVO> gasRes = consumeRow(gasOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_GAS),discount1.orElse(new EpDiscountFactorVO()),discount2.orElse(new EpDiscountFactorVO()));
                return gasRes;
            } catch (Exception e) {
                log.error("gas -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        CompletableFuture<List<EpDayReportCountRowVO>> waterFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //获取用水量
                Optional<EpEnergyTypeVO> waterOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_WATER.equals(v.getId())).findFirst();
                return waterRow(waterOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_WATER));
            } catch (Exception e) {
                log.error("water -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        CompletableFuture<List<EpDayReportCountRowVO>> airFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //获取用气量
                Optional<EpEnergyTypeVO> airOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_AIR.equals(v.getId())).findFirst();
                return consumeRow(airOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_AIR), new EpDiscountFactorVO(), new EpDiscountFactorVO());
            } catch (Exception e) {
                log.error("air -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        CompletableFuture<List<EpDayReportCountRowVO>> lngFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                //判断天然气能源
                Optional<EpDiscountFactorVO> discount1 = validDiscountList1.stream().filter(ele -> EpManagerConstant.ENERGY_GAS.equals(ele.getEnergyType())).findFirst();
                Optional<EpDiscountFactorVO> discount2 = validDiscountList2.stream().filter(ele -> EpManagerConstant.ENERGY_GAS.equals(ele.getEnergyType())).findFirst();
                //获取lng
                Optional<EpEnergyTypeVO> lngOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_LNG.equals(v.getId())).findFirst();
                return consumeRow(lngOp.get(), start, end, defaultNodeMap.get(EpManagerConstant.ENERGY_LNG), discount1.orElse(new EpDiscountFactorVO()), discount2.orElse(new EpDiscountFactorVO()));
            } catch (Exception e) {
                log.error("lng -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        CompletableFuture<List<EpDayReportCountRowVO>> anFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                List<EpDayReportCountRowVO> resList = Lists.newArrayList();
                Optional<EpEnergyTypeVO> eleOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_AN.equals(v.getId())).findFirst();
                EpEnergyTypeVO elecEnergy = eleOp.get();

                //获取用电量
                //获取电表（其他） - 余热发电的仪表
                List<TripleDTO<Long, Integer, String>> eleConfList = defaultNodeMap.getOrDefault(EpManagerConstant.ENERGY_AN, Lists.newArrayList());
                List<TripleDTO<Long, Integer, String>> heatPowerList = eleConfList.stream().filter(ele -> !"余热发电站（站用电）".equals(ele.getField3())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(heatPowerList)) {
                    List<Integer> hpIdSet = heatPowerList.stream().map(TripleDTO::getField2).collect(Collectors.toList());
                    Map<Integer, EpInstrumentAccount> accountMap = epPartitionService.listByOfficeId(EpManagerConstant.ENERGY_AN, Sets.newHashSet(hpIdSet));
                    if (CollUtil.isNotEmpty(accountMap)) {
                        hpIdSet.forEach(ele -> {
                            EpDayReportCountRowVO res = heatPowerRow(start, end, elecEnergy, accountMap.get(ele));
                            resList.add(res);
                        });
                    }
                }
                return resList;
            } catch (Exception e) {
                log.error("AN -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        CompletableFuture<List<EpDayReportCountRowVO>> co3Future = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                List<EpDayReportCountRowVO> resList = Lists.newArrayList();
                Optional<EpEnergyTypeVO> eleOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_CO3.equals(v.getId())).findFirst();
                EpEnergyTypeVO elecEnergy = eleOp.get();

                //获取用电量
                //获取电表（其他） - 余热发电的仪表
                List<TripleDTO<Long, Integer, String>> eleConfList = defaultNodeMap.getOrDefault(EpManagerConstant.ENERGY_CO3, Lists.newArrayList());
                List<TripleDTO<Long, Integer, String>> heatPowerList = eleConfList.stream().filter(ele -> !"余热发电站（站用电）".equals(ele.getField3())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(heatPowerList)) {
                    List<Integer> hpIdSet = heatPowerList.stream().map(TripleDTO::getField2).collect(Collectors.toList());
                    Map<Integer, EpInstrumentAccount> accountMap = epPartitionService.listByOfficeId(EpManagerConstant.ENERGY_CO3, Sets.newHashSet(hpIdSet));
                    if (CollUtil.isNotEmpty(accountMap)) {
                        hpIdSet.forEach(ele -> {
                            EpDayReportCountRowVO res = heatPowerRow2(start, end, elecEnergy, accountMap.get(ele));
                            resList.add(res);
                        });
                    }
                }
                return resList;
            } catch (Exception e) {
                log.error("AN -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        CompletableFuture<List<EpDayReportCountRowVO>> elecOtherFuture = CompletableFuture.supplyAsync(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                List<EpDayReportCountRowVO> resList = Lists.newArrayList();
                Optional<EpEnergyTypeVO> eleOp = energyTypeList.stream().filter(v -> EpManagerConstant.ENERGY_ELECTRIC_OTHER.equals(v.getId())).findFirst();
                EpEnergyTypeVO elecEnergy = eleOp.get();

                //获取用电量
                //获取电表（其他） - 余热发电的仪表
                List<TripleDTO<Long, Integer, String>> eleConfList = defaultNodeMap.getOrDefault(EpManagerConstant.ENERGY_ELECTRIC_OTHER, Lists.newArrayList());
                List<TripleDTO<Long, Integer, String>> heatPowerList = eleConfList.stream().filter(ele -> !"余热发电站（站用电）".equals(ele.getField3())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(heatPowerList)) {
                    List<Integer> hpIdSet = heatPowerList.stream().map(TripleDTO::getField2).collect(Collectors.toList());
                    Map<Integer, EpInstrumentAccount> accountMap = epPartitionService.listByOfficeId(EpManagerConstant.ENERGY_ELECTRIC_OTHER, Sets.newHashSet(hpIdSet));
                    if (CollUtil.isNotEmpty(accountMap)) {
                        hpIdSet.forEach(ele -> {
                            EpDayReportCountRowVO res = heatPowerRow(start, end, elecEnergy, accountMap.get(ele));
                            resList.add(res);
                        });
                    }
                }
                return resList;
            } catch (Exception e) {
                log.error("elecOther -> {}", e.getLocalizedMessage());
                throw new BusinessException("业务执行异常");
            }
        }).exceptionally(ex -> Lists.newArrayList());

        //5个都执行完毕，才来执行继续
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(elecFuture, oilFuture, gasFuture, waterFuture, airFuture, lngFuture,anFuture,co3Future);
        completableFuture.isDone();
        dataList.addAll(elecFuture.get());
        dataList.addAll(waterFuture.get());
        dataList.addAll(gasFuture.get());
        dataList.addAll(oilFuture.get());
        //添加lng
        dataList.addAll(lngFuture.get());
        //添加压缩空气
        dataList.addAll(airFuture.get());
        dataList.addAll(elecOtherFuture.get());
        dataList.addAll(anFuture.get());
        dataList.addAll(co3Future.get());
        return dataList;
    }

    /**
     * 根据开始时间获取结束时间
     * 当前班次日期-1
     *
     * @param start 开始时间
     * @return
     */
    private static LocalDate getEndDate(LocalDate start) {
        LocalDateTime endLdt = LocalDateUtil.getLastDayOfMonthStr(start);
        LocalDate end = endLdt.toLocalDate();
        LocalDate classDate = LocalDateUtil.getClassDate(LocalDateTime.now(), Boolean.TRUE);
        classDate = classDate.minusDays(1);
        if (end.isAfter(classDate)) {
            end = classDate;
        }
        return end;
    }

    /**
     * 获取余热发电行
     *
     * @param start  开始日期
     * @param end    结束日期
     * @param energy 能源类型
     * @return
     */
    private EpDayReportCountRowVO heatPowerRow(LocalDate start, LocalDate end, EpEnergyTypeVO energy, EpInstrumentAccount account ) {
        //计算余热发电-能源抄表
        List<EpMeterRead> dbList = epMeterReadService.list(new LambdaQueryWrapper<EpMeterRead>()
                .between(EpMeterRead::getReadDate, start, end)
                .eq(EpMeterRead::getMeterId, account.getId()));
        EpDayReportCountRowVO heatPowerRow = new EpDayReportCountRowVO();
        Map<LocalDate, BigDecimal> dpMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                dpMap.put(ele.getReadDate(), BigDecimalUtil.setScale(ele.getAdminValue(), 0));
            });
        }
        List<BigDecimal> valueList = Lists.newArrayList();
        List<LocalDate> dateList = ShardTimeUtil.cutDate("D", start, end);
        dateList.forEach(ele -> {
            valueList.add(dpMap.get(ele));
        });
        heatPowerRow.setEnergyType(energy.getName());
        heatPowerRow.setDepartmentName(account.getMeterName());
        heatPowerRow.setUnit(energy.getMeasureUnitName());
        heatPowerRow.setConsumeList(valueList);
        BigDecimal heatPowerSum = BigDecimalUtil.sum(valueList);
        heatPowerRow.setSumStr(BigDecimalUtil.setScale(heatPowerSum, 0));
        return heatPowerRow;
    }

    /**
     * 计算消石灰
     *
     * @param start  开始日期
     * @param end    结束日期
     * @param energy 能源类型
     * @return
     */
    private EpDayReportCountRowVO heatPowerRow2(LocalDate start, LocalDate end, EpEnergyTypeVO energy, EpInstrumentAccount account ) {
        //计算余热发电-能源抄表
        List<EpMeterRead> dbList = epMeterReadService.list(new LambdaQueryWrapper<EpMeterRead>()
                .between(EpMeterRead::getReadDate, start, end)
                .eq(EpMeterRead::getMeterId, account.getId()));
        EpDayReportCountRowVO heatPowerRow = new EpDayReportCountRowVO();
        Map<LocalDate, BigDecimal> dpMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                dpMap.put(ele.getReadDate(), BigDecimalUtil.setScale(ele.getAdminValue(), 1));
            });
        }
        List<BigDecimal> valueList = Lists.newArrayList();
        List<LocalDate> dateList = ShardTimeUtil.cutDate("D", start, end);
        dateList.forEach(ele -> {
            valueList.add(dpMap.get(ele));
        });
        heatPowerRow.setEnergyType(energy.getName());
        heatPowerRow.setDepartmentName(account.getMeterName());
        heatPowerRow.setUnit(energy.getMeasureUnitName());
        heatPowerRow.setConsumeList(valueList);
        BigDecimal heatPowerSum = BigDecimalUtil.sum(valueList);
        heatPowerRow.setSumStr(BigDecimalUtil.setScale(heatPowerSum, 1));
        return heatPowerRow;
    }

    /**
     * 插入合计行
     *
     * @param dataList 数据集合
     * @param energy   能源类型
     * @return
     */
    private EpDayReportCountRowVO countRow(List<EpDayReportCountRowVO> dataList, EpEnergyTypeVO energy, EpDiscountFactorVO discount1, EpDiscountFactorVO discount2) {
        //对行进行合计
        List<BigDecimal> countRowList = Lists.newArrayList();
        for (EpDayReportCountRowVO rowVO : dataList) {
            List<BigDecimal> consumeList = rowVO.getConsumeList();
            for (int i = 0; i < consumeList.size(); i++) {
                if (i > countRowList.size() - 1) {
                    countRowList.add(i, null);
                }
                BigDecimal add = BigDecimalUtil.add(countRowList.get(i), consumeList.get(i));
                countRowList.set(i, BigDecimalUtil.setScale(add, 0));
            }
        }
        EpDayReportCountRowVO countRow = new EpDayReportCountRowVO();
        countRow.setConsumeList(countRowList);
        countRow.setEnergyType(energy.getName());
        countRow.setUnit(energy.getMeasureUnitName());
        countRow.setDepartmentName("合计");
        BigDecimal countRowValue = BigDecimalUtil.sum(countRowList);
        countRow.setSumStr(BigDecimalUtil.setScale(countRowValue, 0));
        //能源消费量当量值tc 转换
        BigDecimal shEquivalentValue = BigDecimalUtil.multiply(countRow.getSumStr(), GetValueUtil.get(discount1, x -> x.getValue()));
        shEquivalentValue = BigDecimalUtil.divide(shEquivalentValue, BigDecimalUtil.THOUSAND);
        countRow.setEquivalentValue(BigDecimalUtil.setScale(shEquivalentValue, 2));
        //能源消费量等量值tce 转换
        BigDecimal shEqualValue = BigDecimalUtil.multiply(countRow.getSumStr(), GetValueUtil.get(discount2, x -> x.getValue()));
        shEqualValue = BigDecimalUtil.divide(shEqualValue, BigDecimalUtil.THOUSAND);
        countRow.setEqualValue(BigDecimalUtil.setScale(shEqualValue, 2));
        return countRow;
    }

    /**
     * 计算电
     *
     * @param epEnergy      能源类型
     * @param start         开始日期
     * @param end           结束日期
     * @param partitionList 所有节点的集合
     * @return
     */
    private List<EpDayReportCountRowVO> calElec(EpEnergyTypeVO epEnergy, LocalDate start, LocalDate end, List<TripleDTO<Long, Integer, String>> partitionList, EpDiscountFactorVO discount1, EpDiscountFactorVO discount2) {
        List<EpDayReportCountRowVO> resList = Lists.newArrayList();
        //获取配置map
        Map<String, List<Integer>> confMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confListInteger(new TripleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_ELEC, EpManagerConstant.ELECConfContentEnum.allContentConf(), 2)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //基片二车间
        List<Integer> jp2List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.JP_2.toString(), Lists.newArrayList());
        //基片一车间
        List<Integer> jp1List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.JP_1.toString(), Lists.newArrayList());
        //深加工四条产线【一二三四】
        List<Integer> sjgLine14List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE14_LIST.toString(), Lists.newArrayList());
        //深加工四条产线【五六七八】
        List<Integer> sjgLine58List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG_LINE58_LIST.toString(), Lists.newArrayList());
        //深加工一车间四条产线【一二三四】
//        List<Integer> sjg1LIST = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.SJG1_LINE14_LIST.toString(), Lists.newArrayList());
        //员工餐厅和宿舍】
        List<Integer> diningList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.DINING_AND_ROOM_LIST.toString(), Lists.newArrayList());
        //公用电【深加工二车间（东线）->公用电,深加工二车间（西线）->公用电】
        List<Integer> gydList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.GYD_LIST.toString(), Lists.newArrayList());
        //风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
        List<Integer> fl610List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.FL610_LIST.toString(), Lists.newArrayList());
        //风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
        List<Integer> fl15List = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.FL15_LIST.toString(), Lists.newArrayList());
        //大数据控制中心
        List<Integer> dsjList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.DSJ_LIST.toString(), Lists.newArrayList());
        //原料车间
        List<Integer> ylList = confMap.getOrDefault(EpManagerConstant.ELECConfContentEnum.YL_LIST.toString(), Lists.newArrayList());
        //深加工8条产线
        List<Integer> sjgLineList = Lists.newArrayList();
        sjgLine14List.forEach(ele -> sjgLineList.add(ele));
        sjgLine58List.forEach(ele -> sjgLineList.add(ele));
//        sjg1LIST.forEach(ele -> sjgLineList.add(ele));

        LocalDateTime startLdt = LocalDateTime.of(start, ClassUtil.MORNING_START_TIME);
        LocalDateTime endLdt = LocalDateTime.of(end.plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集
        List<TupleDTO<LocalDate, Integer>> classRangeList = ClassUtil.classRange(startLdt, endLdt);
        Set<LocalDate> dateSet = classRangeList.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<Long> partitionIdList = partitionList.stream().map(TupleDTO::getField1).collect(Collectors.toList());
        //获取全量数据表
        List<EpMeterReadAllDataVO> classDateAllData = meterReadService.classDateAllData(dateSet, epEnergy.getId(), partitionIdList);
        //每个节点，每天的数据
        //基片制造部=b1+b2+b3(基片二车间)
        List<EpMeterReadAllDataVO> jpList = classDateAllData.stream().filter(v -> jp2List.contains(v.getSysOfficeId())).collect(Collectors.toList());
        List<LocalDate> timeList = dateSet.stream().sorted(Comparator.comparing(x -> x)).collect(Collectors.toList());
        //构建基片组
        EpDayReportCountRowVO jpRow = new EpDayReportCountRowVO();
        List<BigDecimal> jpValueList = Lists.newArrayList();
        Map<LocalDate, List<BigDecimal>> jpResMap = Maps.newHashMap();
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = jpList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
            jpResMap.put(ele, Lists.newArrayList(reduce));
        }
        jpRow.setConsumeList(jpValueList);
        jpRow.setUnit(epEnergy.getMeasureUnitName());
        jpRow.setEnergyType(epEnergy.getName());
        jpRow.setDepartmentName("基片二车间");

        //基片制造部=b1+b2+b3(基片一车间)
        List<EpMeterReadAllDataVO> jp1ListValue = classDateAllData.stream().filter(v -> jp1List.contains(v.getSysOfficeId())).collect(Collectors.toList());
        List<LocalDate> jp1TimeList = dateSet.stream().sorted(Comparator.comparing(x -> x)).collect(Collectors.toList());
        //构建基片组
        EpDayReportCountRowVO jp1Row = new EpDayReportCountRowVO();
        List<BigDecimal> jp1ValueList = Lists.newArrayList();
        Map<LocalDate, List<BigDecimal>> jp1ResMap = Maps.newHashMap();
        for (LocalDate ele : jp1TimeList) {
            List<BigDecimal> timeDataList = jp1ListValue.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
            jp1ResMap.put(ele, Lists.newArrayList(reduce));
        }
        jp1Row.setConsumeList(jp1ValueList);
        jp1Row.setUnit(epEnergy.getMeasureUnitName());
        jp1Row.setEnergyType(epEnergy.getName());
        jp1Row.setDepartmentName("基片一车间");
        //构建生活用电 = 员工餐厅 + 员工宿舍
        EpDayReportCountRowVO shRow = new EpDayReportCountRowVO();
        List<EpMeterReadAllDataVO> shList = classDateAllData.stream().filter(v -> diningList.contains(v.getSysOfficeId())).collect(Collectors.toList());
        List<BigDecimal> shValueList = Lists.newArrayList();
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = shList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
            shValueList.add(BigDecimalUtil.setScale(reduce, 0));
        }
        BigDecimal shSum = BigDecimalUtil.sum(shValueList);
        shRow.setSumStr(BigDecimalUtil.setScale(shSum, 0));
        shRow.setConsumeList(shValueList);
        shRow.setUnit(epEnergy.getMeasureUnitName());
        shRow.setEnergyType(epEnergy.getName());
        shRow.setDepartmentName("生活用电");
        //能源消费量当量值tc 转换
        BigDecimal shEquivalentValue = BigDecimalUtil.multiply(shSum, GetValueUtil.get(discount1, x -> x.getValue()));
        shEquivalentValue = BigDecimalUtil.divide(shEquivalentValue, BigDecimalUtil.THOUSAND);
        shRow.setEquivalentValue(BigDecimalUtil.setScale(shEquivalentValue, 2));
        //能源消费量等量值tce 转换
        BigDecimal shEqualValue = BigDecimalUtil.multiply(shSum, GetValueUtil.get(discount2, x -> x.getValue()));
        shEqualValue = BigDecimalUtil.divide(shEqualValue, BigDecimalUtil.THOUSAND);
        shRow.setEqualValue(BigDecimalUtil.setScale(shEqualValue, 2));
        //深加工一~八产线  深加工一车间
        List<TripleDTO<Long, Integer, String>> linePartitionList = partitionList.stream().filter(v -> sjgLine14List.contains(v.getField2())
                || sjgLine58List.contains(v.getField2())
//                || sjg1LIST.contains(v.getField2())
        ).collect(Collectors.toList());
        Set<Integer> lineOfficeIdSet = Sets.newHashSet();
        Map<String, List<BigDecimal>> lineResMap = Maps.newHashMap();

        List<EpDayReportCountRowVO> lineRowList = Lists.newArrayList();
        for (TripleDTO<Long, Integer, String> line : linePartitionList) {
            //获取产线id集合
            lineOfficeIdSet.add(line.getField2());
            List<EpMeterReadAllDataVO> lineList = classDateAllData.stream().filter(v -> line.getField1().equals(v.getOfficeId())).collect(Collectors.toList());
            //构建基片制造部
            EpDayReportCountRowVO lineRow = new EpDayReportCountRowVO();
            List<BigDecimal> lineValueList = Lists.newArrayList();
            for (LocalDate ele : timeList) {
                List<BigDecimal> timeDataList = lineList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal timeValue = BigDecimalUtil.sum(timeDataList);
                //a1.1 a2.1 a3.1 a4.1 a5.1 a6.1 a7.1 a8.1
                lineResMap.put(mapKey(line.getField2(), ele), Lists.newArrayList(timeValue));
            }
            lineRow.setConsumeList(lineValueList);
            lineRow.setUnit(epEnergy.getMeasureUnitName());
            lineRow.setEnergyType(epEnergy.getName());
            lineRow.setDepartmentName(line.getField3());
            lineRow.setOfficeId(line.getField2());
            lineRowList.add(lineRow);
        }
        //基片，产线行，生活用电
        resList.add(jp1Row);
        resList.add(jpRow);
        resList.addAll(lineRowList);
        resList.add(shRow);

        //获取几条产线的产量及对应比例，用于分摊公用电
        Map<String, BigDecimal> productNumMap = FeignOptional.of(() -> reportWorkFeignApi.classDateProductNum(new TripleDTO<>(lineOfficeIdSet, start, end)), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
//        TupleDTO<Map<String, BigDecimal>, Map<String, BigDecimal>> tupleDTO = getProductNumRateMap(sjgLine14List, sjgLine58List, sjg1LIST,timeList, productNumMap);
        TupleDTO<Map<String, BigDecimal>, Map<String, BigDecimal>> tupleDTO = getProductNumRateMap(sjgLine14List, sjgLine58List, Lists.newArrayList(),timeList, productNumMap);
        Map<String, BigDecimal> productNumLineRateMap = tupleDTO.getField1();
        Map<String, BigDecimal> productNumRateMap = tupleDTO.getField2();

        //获取公用电
        gydRate(classDateAllData, gydList, Lists.newArrayList(sjgLine14List, sjgLine58List), timeList, productNumLineRateMap, lineResMap);

        //分摊源数据=空压机房+路灯+维修车间；基片二车间分50%；加工分50%，分别按产量比例分到每一条产线  并把原料车间数据加入
        meterRate(classDateAllData,Lists.newArrayList(660,674,3810,77), Lists.newArrayList(sjgLine14List, sjgLine58List,jp2List), timeList, productNumLineRateMap, lineResMap,jpResMap);
        //获取风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
        //a1.3 a2.3 a3.3 a4.3 a5.3 a6.3 a7.3 a8.3
//        fl610(classDateAllData, fl610List, sjgLineList, timeList, productNumRateMap, lineResMap);

        //获取风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
        //获取b2
//        fl15(classDateAllData, fl15List, timeList, jpResMap);

        //获取总能耗N 按b1、a1.1 + a1.2 + a1.3、a2.1 + a2.2 + a2.3、... 、a8.1 + a8.2 + a8.3按照比例分摊
        //N分摊为b3、a1.4、a2.4、... 、a8.4共9个值
//        Map<String, BigDecimal> nRateMap = getNRateMap(sjgLineList, timeList, jpResMap, lineResMap);
//        dsj(classDateAllData, dsjList, sjgLineList, timeList, nRateMap, jpResMap, lineResMap);

        //重新计算基片行和产线行
        ftJpRow(jpRow, timeList, jpResMap,discount1,discount2);
        ftLineRow(lineRowList, timeList, lineResMap,discount1,discount2);
        return resList;
    }

    /**
     * 产线行分摊-数据+合计调整
     *
     * @param lineRowList 产线行集合
     * @param timeList    班次时间集合
     * @param lineResMap  产线数据集合
     */
    private void ftLineRow(List<EpDayReportCountRowVO> lineRowList, List<LocalDate> timeList, Map<String, List<BigDecimal>> lineResMap, EpDiscountFactorVO discount1, EpDiscountFactorVO discount2) {
        for (EpDayReportCountRowVO row : lineRowList) {
            BigDecimal rowSum = BigDecimal.ZERO;
            List<BigDecimal> consumeList = row.getConsumeList();
            for (int i = 0; i < timeList.size(); i++) {
                List<BigDecimal> valueList = lineResMap.get(mapKey(row.getOfficeId(), timeList.get(i)));
                BigDecimal sum = BigDecimalUtil.sum(valueList);
                BigDecimal format = BigDecimalUtil.setScale(sum, 0);
                consumeList.add(i, format);
                rowSum = BigDecimalUtil.add(format, rowSum);
            }
            BigDecimal value = BigDecimalUtil.setScale(rowSum, 0);
            row.setSumStr(value);
            //能源消费量当量值tc 转换
            BigDecimal shEquivalentValue = BigDecimalUtil.multiply(value, GetValueUtil.get(discount1, x -> x.getValue()));
            shEquivalentValue = BigDecimalUtil.divide(shEquivalentValue, BigDecimalUtil.THOUSAND);
            row.setEquivalentValue(BigDecimalUtil.setScale(shEquivalentValue, 2));
            //能源消费量等量值tce 转换
            BigDecimal shEqualValue = BigDecimalUtil.multiply(value, GetValueUtil.get(discount2, x -> x.getValue()));
            shEqualValue = BigDecimalUtil.divide(shEqualValue, BigDecimalUtil.THOUSAND);
            row.setEqualValue(BigDecimalUtil.setScale(shEqualValue, 2));
        }
    }

    /**
     * 重新计算分摊的行-数据+合计调整
     *
     * @param jpRow
     * @param timeList
     * @param jpResMap
     */
    private void ftJpRow(EpDayReportCountRowVO jpRow, List<LocalDate> timeList, Map<LocalDate, List<BigDecimal>> jpResMap, EpDiscountFactorVO discount1, EpDiscountFactorVO discount2) {
        List<BigDecimal> consumeList = jpRow.getConsumeList();
        BigDecimal rowSum = BigDecimal.ZERO;
        for (int i = 0; i < timeList.size(); i++) {
            List<BigDecimal> valueList = jpResMap.get(timeList.get(i));
            BigDecimal sum = BigDecimalUtil.sum(valueList);
            BigDecimal format = BigDecimalUtil.setScale(sum, 0);
            //能源消费量当量值tc 转换
            //能源消费量等量值tce 转换
            consumeList.add(i, format);
            rowSum = BigDecimalUtil.add(format, rowSum);
        }
        BigDecimal value = BigDecimalUtil.setScale(rowSum, 0);
        jpRow.setSumStr(value);
        //能源消费量当量值tc 转换
        BigDecimal shEquivalentValue = BigDecimalUtil.multiply(value, GetValueUtil.get(discount1, x -> x.getValue()));
        shEquivalentValue = BigDecimalUtil.divide(shEquivalentValue, BigDecimalUtil.THOUSAND);
        jpRow.setEquivalentValue(BigDecimalUtil.setScale(shEquivalentValue, 2));
        //能源消费量等量值tce 转换
        BigDecimal shEqualValue = BigDecimalUtil.multiply(value, GetValueUtil.get(discount2, x -> x.getValue()));
        shEqualValue = BigDecimalUtil.divide(shEqualValue, BigDecimalUtil.THOUSAND);
        jpRow.setEqualValue(BigDecimalUtil.setScale(shEqualValue, 2));
    }

    /**
     * 基于N按b1、a1.1 + a1.2 + a1.3、a2.1 + a2.2 + a2.3、... 、a8.1 + a8.2 + a8.3共9部分的比例分摊，则N分摊为b3、a1.4、a2.4、... 、a8.4共9个值
     *
     * @param classDateAllData 全量数据表
     * @param dsjList          大数据节点
     * @param sjgLineList      深加工产线节点
     * @param timeList         时间集合
     * @param nRateMap         分摊比例map
     * @param jpResMap         基片map
     * @param lineResMap       深加工产线map
     */
    private void dsj(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> dsjList, List<Integer> sjgLineList, List<LocalDate> timeList, Map<String, BigDecimal> nRateMap, Map<LocalDate, List<BigDecimal>> jpResMap, Map<String, List<BigDecimal>> lineResMap) {
        List<EpMeterReadAllDataVO> dsjDataList = classDateAllData.stream().filter(v -> dsjList.contains(v.getSysOfficeId())).collect(Collectors.toList());
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = dsjDataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal dsjValue = BigDecimalUtil.sum(timeDataList);
            //基片分摊
            BigDecimal jpValue = BigDecimalUtil.multiply(dsjValue, nRateMap.get(mapKey(-1, ele)));
            jpResMap.get(ele).add(jpValue);
            //产线分摊value
            for (Integer line : sjgLineList) {
                BigDecimal lineValue = BigDecimalUtil.multiply(dsjValue, nRateMap.get(mapKey(line, ele)));
                lineResMap.get(mapKey(line, ele)).add(lineValue);
            }
        }
    }

    /**
     * N按b1、a1.1 + a1.2 + a1.3、a2.1 + a2.2 + a2.3、... 、a8.1 + a8.2 + a8.3共9部分的比例分摊，
     * 则N分摊为b3、a1.4、a2.4、... 、a8.4共9个值
     *
     * @param sjgLineList 深加工产线集合
     * @param timeList    班次日期集合
     * @param jpResMap    基片制造部数据
     * @param lineResMap  产线数据
     * @return 分摊比例map
     */
    private Map<String, BigDecimal> getNRateMap(List<Integer> sjgLineList, List<LocalDate> timeList, Map<LocalDate, List<BigDecimal>> jpResMap, Map<String, List<BigDecimal>> lineResMap) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        for (LocalDate classDate : timeList) {
            BigDecimal b1 = jpResMap.get(classDate).get(0);
            BigDecimal sum = b1;
            Map<Integer, BigDecimal> lineDataMap = Maps.newHashMap();

            for (Integer line : sjgLineList) {
                List<BigDecimal> lineDataList = lineResMap.get(mapKey(line, classDate));
                //ax.1 + ax.2 + ax.3
                BigDecimal lineDaySum = BigDecimalUtil.sum(lineDataList);
                sum = BigDecimalUtil.add(sum, lineDaySum);
                lineDataMap.put(line, lineDaySum);
            }
            //基片制造部比例
            resMap.put(mapKey(-1, classDate), BigDecimalUtil.divide(b1, sum));
            //深加工产线比例
            for (Map.Entry<Integer, BigDecimal> entry : lineDataMap.entrySet()) {
                resMap.put(mapKey(entry.getKey(), classDate), BigDecimalUtil.divide(entry.getValue(), sum));
            }
        }
        return resMap;
    }

    /**
     * 获取b2
     *
     * @param classDateAllData 全量数据表
     * @param fl15             风冷螺杆式空压机1 + 风冷螺杆式空压机2 + 风冷螺杆式空压机3 + 风冷螺杆式空压机4 + 风冷螺杆式空压机5 + 微热吸附式干燥器1 + 微热吸附式干燥器2
     * @param timeList         时间集合
     * @param jpResMap         基片的数据集合
     */
    private void fl15(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> fl15, List<LocalDate> timeList, Map<LocalDate, List<BigDecimal>> jpResMap) {
        List<EpMeterReadAllDataVO> fl610DataList = classDateAllData.stream().filter(v -> fl15.contains(v.getSysOfficeId())).collect(Collectors.toList());
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = fl610DataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal b2 = BigDecimalUtil.sum(timeDataList);
            jpResMap.get(ele).add(b2);
        }
    }

    /**
     * 查询的6个总能耗值加总记为M 分摊到产线
     *
     * @param classDateAllData  全量数据表
     * @param fl610             风冷螺杆式空压机6 + 风冷螺杆式空压机7 + 风冷螺杆式空压机8 + 风冷螺杆式空压机9 + 风冷螺杆式空压机10 + 微热吸附式干燥器3
     * @param sjgLineList       深加工产线集合
     * @param timeList          时间集合
     * @param productNumRateMap 产量比例集合
     * @param lineResMap        产线数据集合
     */
    private void fl610(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> fl610, List<Integer> sjgLineList, List<LocalDate> timeList, Map<String, BigDecimal> productNumRateMap, Map<String, List<BigDecimal>> lineResMap) {
        List<EpMeterReadAllDataVO> fl610DataList = classDateAllData.stream().filter(v -> fl610.contains(v.getSysOfficeId())).collect(Collectors.toList());
        for (LocalDate ele : timeList) {
            List<BigDecimal> timeDataList = fl610DataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
            BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
            //a1.3 a2.3 a3.3 a4.3 a5.3 a6.3 a7.3 a8.3
            for (Integer sjgLine : sjgLineList) {
                String mapKey = mapKey(sjgLine, ele);
                BigDecimal rateValue = BigDecimalUtil.multiply(reduce, productNumRateMap.get(mapKey));
                lineResMap.get(mapKey).add(rateValue);
            }
        }
    }

    /**
     * 公用电 对应2个集合
     *
     * @param classDateAllData  全量数据表
     * @param gydList           公用电集合
     * @param lineList          产线集合【1-4 ，5-8】
     * @param timeList          时间集合
     * @param productNumRateMap 产量集合
     * @param lineResMap        产线返回集合
     */
    private void gydRate(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> gydList, List<List<Integer>> lineList, List<LocalDate> timeList, Map<String, BigDecimal> productNumRateMap, Map<String, List<BigDecimal>> lineResMap) {
        for (int i = 0; i < gydList.size(); i++) {
            int finalI = i;
            List<EpMeterReadAllDataVO> gydDataList = classDateAllData.stream().filter(v -> gydList.get(finalI).equals(v.getSysOfficeId())).collect(Collectors.toList());
            //a1.2 a2.2 a3.2 a4.2
            //a5.2 a6.2 a7.2 a8.2
            for (LocalDate ele : timeList) {
                List<BigDecimal> timeDataList = gydDataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
                for (Integer sjgLine : lineList.get(i)) {
                    String mapKey = mapKey(sjgLine, ele);
                    //新分摊逻辑，公用电平均分到四条产线
//                    BigDecimal rateValue = BigDecimalUtil.multiply(reduce, productNumRateMap.get(mapKey));
                    BigDecimal rateValue = BigDecimalUtil.multiply(reduce, new BigDecimal(0.25));
                    lineResMap.get(mapKey).add(rateValue);
                }
            }
        }
    }

    /**
     * 电表分摊
     *
     * @param classDateAllData  全量数据表
     * @param nodeList           公用电集合
     * @param lineList          产线集合【1-4 ，5-8】
     * @param timeList          时间集合
     * @param productNumRateMap 产量集合
     * @param lineResMap        产线返回集合
     */
    private void meterRate(List<EpMeterReadAllDataVO> classDateAllData, List<Integer> nodeList, List<List<Integer>> lineList, List<LocalDate> timeList, Map<String, BigDecimal> productNumRateMap, Map<String, List<BigDecimal>> lineResMap,Map<LocalDate, List<BigDecimal>> jpResMap) {
        for (int i = 0; i < nodeList.size(); i++) {
            int finalI = i;
            List<EpMeterReadAllDataVO> gydDataList = classDateAllData.stream().filter(v -> nodeList.get(finalI).equals(v.getSysOfficeId())).collect(Collectors.toList());
            //a1.2 a2.2 a3.2 a4.2
            //a5.2 a6.2 a7.2 a8.2
            for (LocalDate ele : timeList) {
                List<BigDecimal> timeDataList = gydDataList.stream().filter(v -> v.getReadDate().isEqual(ele)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal reduce = BigDecimalUtil.sum(timeDataList);
//                for (Integer sjgLine : lineList.get(i)) {
//                    String mapKey = mapKey(sjgLine, ele);
//                    BigDecimal rateValue = BigDecimalUtil.multiply(reduce, productNumRateMap.get(mapKey));
//                    lineResMap.get(mapKey).add(rateValue);
//
//                }
                for(List<Integer> lines:lineList){
                    for (Integer line : lines) {
                        if(!line.equals(76)&&!nodeList.get(finalI).equals(77)){
                            //深加工产线分摊
                            String mapKey = mapKey(line, ele);
                            BigDecimal rateValue = BigDecimalUtil.multiply(reduce, new BigDecimal(0.0625));
                            lineResMap.get(mapKey).add(rateValue);
                        }else if(line.equals(76)&&nodeList.get(finalI).equals(77)){
                            //原料车间加至基片
                            jpResMap.get(ele).add(reduce);
                        }else if(line.equals(76)&&!nodeList.get(finalI).equals(77)){
                            //百分之50分摊至基片
                            BigDecimal rateValue = BigDecimalUtil.multiply(reduce, new BigDecimal(0.5));
                            jpResMap.get(ele).add(rateValue);
                        }
                    }
                }
            }
        }
    }


    /**
     * 获取产量比例的map
     *
     * @param sjgLine14List 深加工四条产线【一二三四】
     * @param sjgLine58List 深加工四条产线【五六七八】
     * @param timeList      班次日期集合
     * @param productNumMap 产量集合
     * @return
     */
    private TupleDTO<Map<String, BigDecimal>, Map<String, BigDecimal>> getProductNumRateMap(List<Integer> sjgLine14List, List<Integer> sjgLine58List,List<Integer> sjg1LineList, List<LocalDate> timeList, Map<String, BigDecimal> productNumMap) {
        Map<String, BigDecimal> productNumLineRateMap = Maps.newHashMap();
        Map<String, BigDecimal> productNumRateMap = Maps.newHashMap();

        //深加工8条产线
        List<Integer> sjgLineList = Lists.newArrayList();
        sjgLine14List.forEach(ele -> sjgLineList.add(ele));
        sjgLine58List.forEach(ele -> sjgLineList.add(ele));
        if(CollUtil.isNotEmpty(sjg1LineList)){
            sjg1LineList.forEach(ele -> sjgLineList.add(ele));
        }

        //获取几条产线的每日产量对比
        for (LocalDate classDate : timeList) {
            //深加工四条产线【一二三四】
            BigDecimal sum14 = BigDecimal.ZERO;
            for (Integer line : sjgLine14List) {
                sum14 = BigDecimalUtil.add(sum14, productNumMap.getOrDefault(mapKey(line, classDate), BigDecimal.ZERO));
            }
            for (Integer line : sjgLine14List) {
                String mapKey = mapKey(line, classDate);
                BigDecimal num = productNumMap.getOrDefault(mapKey, BigDecimal.ZERO);
                productNumLineRateMap.put(mapKey, BigDecimalUtil.divide(num, sum14));
            }
            //深加工四条产线【五六七八】
            BigDecimal sum58 = BigDecimal.ZERO;
            for (Integer line : sjgLine58List) {
                sum58 = BigDecimalUtil.add(sum58, productNumMap.getOrDefault(mapKey(line, classDate), BigDecimal.ZERO));
            }
            for (Integer line : sjgLine58List) {
                String mapKey = mapKey(line, classDate);
                BigDecimal num = productNumMap.getOrDefault(mapKey, BigDecimal.ZERO);
                productNumLineRateMap.put(mapKey, BigDecimalUtil.divide(num, sum58));
            }
            if(CollUtil.isNotEmpty(sjg1LineList)){
                //深加工一车间
                BigDecimal sum1 = BigDecimal.ZERO;
                for (Integer line : sjg1LineList) {
                    sum1 = BigDecimalUtil.add(sum1, productNumMap.getOrDefault(mapKey(line, classDate), BigDecimal.ZERO));
                }
                for (Integer line : sjg1LineList) {
                    String mapKey = mapKey(line, classDate);
                    BigDecimal num = productNumMap.getOrDefault(mapKey, BigDecimal.ZERO);
                    productNumLineRateMap.put(mapKey, BigDecimalUtil.divide(num, sum1));
                }
            }
            //深加工八条产线【一二三四五六七八】
            BigDecimal sum = BigDecimal.ZERO;
            for (Integer line : sjgLineList) {
                sum = BigDecimalUtil.add(sum, productNumMap.getOrDefault(mapKey(line, classDate), BigDecimal.ZERO));
            }
            for (Integer line : sjgLineList) {
                String mapKey = mapKey(line, classDate);
                BigDecimal num = productNumMap.getOrDefault(mapKey, BigDecimal.ZERO);
                productNumRateMap.put(mapKey, BigDecimalUtil.divide(num, sum));
            }
        }
        return new TupleDTO<>(productNumLineRateMap, productNumRateMap);
    }

    /**
     * 产线和日期拼接成key
     *
     * @param line
     * @param date
     * @return
     */
    private String mapKey(Integer line, LocalDate date) {
        StringBuilder sb = new StringBuilder();
        sb.append(line).append("_").append(date);
        return sb.toString();
    }

    /**
     * 获取能耗行
     *
     * @param epEnergy      能源类型
     * @param start         开始日期
     * @param end           结束日期
     * @param partitionList 节点集合
     * @return
     */
    private List<EpDayReportCountRowVO> consumeRow(EpEnergyTypeVO epEnergy, LocalDate start, LocalDate end, List<TripleDTO<Long, Integer, String>> partitionList, EpDiscountFactorVO discount1, EpDiscountFactorVO discount2) {
        List<EpDayReportCountRowVO> rowList = Lists.newArrayList();
        if (CollUtil.isEmpty(partitionList) || Objects.isNull(epEnergy)) {
            return rowList;
        }
        //查询全量数据表
        LocalDateTime startLdt = LocalDateTime.of(start, ClassUtil.MORNING_START_TIME);
        LocalDateTime endLdt = LocalDateTime.of(end.plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集
        List<TupleDTO<LocalDate, Integer>> classRangeList = ClassUtil.classRange(startLdt, endLdt);
        Set<LocalDate> dateSet = classRangeList.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<Long> partitionIdList = partitionList.stream().map(TupleDTO::getField1).collect(Collectors.toList());
        //获取全量数据表
        List<EpMeterReadAllDataVO> classDateAllData = meterReadService.classDateAllData(dateSet, epEnergy.getId(), partitionIdList);

        for (TripleDTO<Long, Integer, String> tripleDTO : partitionList) {
            EpDayReportCountRowVO row = new EpDayReportCountRowVO();
            List<BigDecimal> consumeList = Lists.newArrayList();
            List<EpMeterReadAllDataVO> dataList = classDateAllData.stream().filter(v -> v.getOfficeId().equals(tripleDTO.getField1())).collect(Collectors.toList());
            List<LocalDate> timeList = dateSet.stream().sorted(Comparator.comparing(x -> x)).collect(Collectors.toList());
            for (LocalDate classDate : timeList) {
                List<BigDecimal> valueList = dataList.stream().filter(v -> v.getReadDate().isEqual(classDate)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal dataValue = BigDecimalUtil.sum(valueList);
                consumeList.add(BigDecimalUtil.setScale(dataValue, UnitLengthUtil.UNIT_TON_LENGTH));
            }
            row.setConsumeList(consumeList);
            row.setEnergyType(epEnergy.getName());
            row.setUnit(epEnergy.getMeasureUnitName());
            row.setDepartmentName(tripleDTO.getField3());
            BigDecimal sum = BigDecimalUtil.sum(consumeList);
            row.setSumStr(BigDecimalUtil.setScale(sum, UnitLengthUtil.UNIT_TON_LENGTH));

            //能源消费量当量值tc 转换
            BigDecimal equivalentValue = BigDecimalUtil.multiply(row.getSumStr(), GetValueUtil.get(discount1, x -> x.getValue()));
            //判断类型
            if(!EpManagerConstant.ENERGY_OIL.equals(epEnergy.getId())){
                equivalentValue = BigDecimalUtil.divide(equivalentValue, new BigDecimal("10000"));
            }
            row.setEquivalentValue(BigDecimalUtil.setScale(equivalentValue, 2));
            //能源消费量等量值tce 转换
            BigDecimal equalValue = BigDecimalUtil.multiply(row.getSumStr(), GetValueUtil.get(discount2, x -> x.getValue()));
            //判断类型
            if(!EpManagerConstant.ENERGY_OIL.equals(epEnergy.getId())) {
                equalValue = BigDecimalUtil.divide(equalValue, new BigDecimal("10000"));
            }
            row.setEqualValue(BigDecimalUtil.setScale(equalValue, 2));
            rowList.add(row);
        }

        //如果超过一行，则添加一个合计行
        if (rowList.size() > 1) {
            EpDayReportCountRowVO countRow = countRow(rowList, epEnergy, new EpDiscountFactorVO(), new EpDiscountFactorVO());
            rowList.add(countRow);
        }
        return rowList;
    }


    /**
     * 获取能耗行
     *
     * @param epEnergy      能源类型
     * @param start         开始日期
     * @param end           结束日期
     * @param partitionList 节点集合
     * @return
     */
    private List<EpDayReportCountRowVO> waterRow(EpEnergyTypeVO epEnergy, LocalDate start, LocalDate end, List<TripleDTO<Long, Integer, String>> partitionList) {
        List<EpDayReportCountRowVO> rowList = Lists.newArrayList();
        if (CollUtil.isEmpty(partitionList) || Objects.isNull(epEnergy)) {
            return rowList;
        }
        //查询全量数据表
        LocalDateTime startLdt = LocalDateTime.of(start, ClassUtil.MORNING_START_TIME);
        LocalDateTime endLdt = LocalDateTime.of(end.plusDays(1), ClassUtil.MORNING_START_TIME.minusNanos(1));
        //②时间区间内所有班次集
        List<TupleDTO<LocalDate, Integer>> classRangeList = ClassUtil.classRange(startLdt, endLdt);
        Set<LocalDate> dateSet = classRangeList.stream().map(v -> v.getField1()).collect(Collectors.toSet());
        List<Long> partitionIdList = partitionList.stream().map(TupleDTO::getField1).collect(Collectors.toList());
        //获取全量数据表
        List<EpMeterReadAllDataVO> classDateAllData = meterReadService.classDateAllData(dateSet, epEnergy.getId(), partitionIdList);
        //获取基片节点
        Optional<TripleDTO<Long, Integer, String>> jpOp = partitionList.stream().filter(ele -> ele.getField3().indexOf("基片") != -1).findFirst();
        TripleDTO<Long, Integer, String> jpEle = null;
        if (jpOp.isPresent()) {
            jpEle = jpOp.get();
            partitionList.remove(jpEle);
            partitionList.add(jpEle);
        }

        Optional<TripleDTO<Long, Integer, String>> glOp = partitionList.stream().filter(ele -> ele.getField3().indexOf("锅炉") != -1).findFirst();
        if (glOp.isPresent()) {
            partitionList.remove(glOp.get());
        }

        Map<LocalDate, BigDecimal> otherMap = Maps.newHashMap();
        for (TripleDTO<Long, Integer, String> tripleDTO : partitionList) {
            EpDayReportCountRowVO row = new EpDayReportCountRowVO();
            List<BigDecimal> consumeList = Lists.newArrayList();
            //需求改动余热发电 + 锅炉 => 余热发电 再次改动 脱硫脱硝+锅炉 => 余热发电
            List<EpMeterReadAllDataVO> dataList;
            if(tripleDTO.getField3().equals("余热发电") && glOp.isPresent()){
                dataList = classDateAllData.stream().filter(v -> v.getOfficeId().equals(tripleDTO.getField1()) || v.getOfficeId().equals(glOp.get().getField1())).collect(Collectors.toList());
            }else{
                dataList = classDateAllData.stream().filter(v -> v.getOfficeId().equals(tripleDTO.getField1())).collect(Collectors.toList());
            }

            List<LocalDate> timeList = dateSet.stream().sorted(Comparator.comparing(x -> x)).collect(Collectors.toList());
            for (LocalDate classDate : timeList) {
                List<BigDecimal> valueList = dataList.stream().filter(v -> v.getReadDate().isEqual(classDate)).map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
                BigDecimal dataValue = BigDecimalUtil.sum(valueList);
                BigDecimal dataInt = BigDecimalUtil.setScale(dataValue, 0);
                if (Objects.nonNull(jpEle) && tripleDTO != jpEle) {
                    otherMap.put(classDate, BigDecimalUtil.add(otherMap.get(classDate), dataInt));
                } else if (Objects.nonNull(jpEle) && tripleDTO == jpEle) {
//                    dataInt = BigDecimalUtil.subtract(dataInt, otherMap.get(classDate));
                    dataInt = dataValue;
                }
                consumeList.add(BigDecimalUtil.setScale(dataInt, 0));
            }
            row.setConsumeList(consumeList);
            row.setEnergyType(epEnergy.getName());
            row.setUnit(epEnergy.getMeasureUnitName());
            row.setDepartmentName(tripleDTO.getField3());
            BigDecimal sum = BigDecimalUtil.sum(consumeList);
            row.setSumStr(BigDecimalUtil.setScale(sum, 0));
            rowList.add(row);
        }
        //需要将基片放到第一位
        if (CollUtil.isNotEmpty(rowList)) {
            EpDayReportCountRowVO rowVO = rowList.get(rowList.size() - 1);
            rowList.remove(rowVO);
            rowList.add(0, rowVO);
        }
        //如果超过一行，则添加一个合计行
        if (rowList.size() > 1) {
            EpDayReportCountRowVO countRow = countRow(rowList, epEnergy, new EpDiscountFactorVO(), new EpDiscountFactorVO());
            rowList.add(countRow);
        }
        return rowList;
    }


    /**
     * 获取表头
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return
     */
    private EpDayReportCountHeadVO countHead(LocalDate start, LocalDate end) {
        //获取一个月的所有日期
        List<LocalDate> dateList = LocalDateUtil.getLocalDateByStartEndDate(start, end);
        EpDayReportCountHeadVO head = new EpDayReportCountHeadVO();
        head.setEnergyType("能源类型");
        head.setUnit("单位");
        head.setDepartmentName("部门");
        List<String> dateStrList = Lists.newArrayList();
        //年月日 eg. 2023-06-06
        //月日 eg.06-06
        dateList.forEach(ele -> dateStrList.add(LocalDateTimeUtil.format(ele, "MM-dd")));
        head.setConsumeList(dateStrList);
        head.setSumStr("总计");
        head.setEquivalentValue("能源消费量当量值（tce）");
        head.setEqualValue("能源消费量等价值（tce）");
        return head;
    }
}
