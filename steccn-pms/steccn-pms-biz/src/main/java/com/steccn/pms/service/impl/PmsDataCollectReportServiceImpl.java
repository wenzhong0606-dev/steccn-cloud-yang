package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.http.HttpUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.dto.SteccnRow;
import com.steccn.common.dto.SteccnTableColumn;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.CalculateDateUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.vo.QtTaskClassCalculate;
import com.steccn.oa.constant.OaConstant;
import com.steccn.pms.dto.DataCollectDTO;
import com.steccn.pms.service.PmsDataCollectReportService;
import com.steccn.pms.vo.SjgMonthCountReportTransverseVO;
import com.steccn.pms.vo.SjgMonthCountReportVO;
import com.steccn.qms.excel.strategy.ExcelFillCellMergeStrategy;
import com.steccn.qms.excel.strategy.HeadContentStyleStrategy;
import com.steccn.qms.excel.strategy.RowHeightStyleStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/01/10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PmsDataCollectReportServiceImpl implements PmsDataCollectReportService {
    @Override
    public SteccnTableDataDTO<Map<String, String>> turbineReport(LocalDate date) {
        List<Map<String, String>> data = postDataCollect(date, Lists.newArrayList("YR_DPU2_AI02_2_METER3"
                , "YR_DPU2_TC03_3_METER6"
                , "YR_DPU2_RT05_2_METER6"
                , "YR_DPU2_RT05_2_METER7"
                , "YR_DPU2_AI02_2_METER6"
                , "YR_DPU2_TC05_4_METER4"
                , "YR_DPU2_AI03_2_METER1"
                , "YR_DPU2_AI03_1_METER7"
                , "YR_DPU2_AI04_1_METER2"
                , "YR_DPU2_RT05_2_METER1"
                , "YR_DPU2_RT05_2_METER2"
                , "YR_DPU2_RT01_3_METER5"
                , "YR_DPU2_RT02_3_METER5"
                , "YR_DPU2_SH0027_AI_I"
                , "YR_DPU2_SH0029_AI_I"
                , "YR_DPU2_AI04_2_METER6"
                , "YR_DPU2_AI04_2_METER4"
                , "YR_SH0053_AI_I"
                , "YR_SH0055_AI_I"
                , "YR_SH0057_AI_I"
                , "YR_DPU2_AI04_2_METER7"));
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI02_2_METER3","主蒸汽压力(Mpa)",null));
        columns.add(new SteccnTableColumn("YR_DPU2_TC03_3_METER6","主蒸汽温度(℃)",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT05_2_METER6","汽轮机前轴承温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT05_2_METER7","汽轮机后轴承温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI02_2_METER6","真空(KPa)",null));
        columns.add(new SteccnTableColumn("YR_DPU2_TC05_4_METER4","气缸排气温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI03_2_METER1","润滑油压力",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI03_1_METER7","油箱油位",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI04_1_METER2","轴向位移",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT05_2_METER1","冷油器进水温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT05_2_METER2","冷油器出水温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT01_3_METER5","凝结器进水温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT02_3_METER5","凝结器出水温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_SH0027_AI_I","1#凝结泵电流",null));
        columns.add(new SteccnTableColumn("YR_DPU2_SH0029_AI_I","2#凝结泵电流",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI04_2_METER6","凝结器压力(pa)",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI04_2_METER4","射水泵出口压力(Mpa)",null));
        columns.add(new SteccnTableColumn("YR_SH0053_AI_I","1#循环水泵电流",null));
        columns.add(new SteccnTableColumn("YR_SH0055_AI_I","2#循环水泵电流",null));
        columns.add(new SteccnTableColumn("YR_SH0057_AI_I","3#循环水泵电流",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI04_2_METER7","循环水泵出口压力",null));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    @Override
    public void turbineReportDownload(LocalDate date, HttpServletResponse response) throws IOException {
        SteccnTableDataDTO<Map<String, String>> recordDto = turbineReport(date);
        List<SteccnTableColumn> columns = recordDto.getColumns();
        List<Map<String, String>> tableList = recordDto.getTableList();
        Assert.notEmpty(tableList,"暂无数据");
        //写表头
        List<List<String>> headList = new ArrayList<>();
        columns.forEach(h ->{
            headList.add(Lists.newArrayList(h.getLabel()));
        });
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("余热汽轮机报表", "UTF-8");
        fileName += LocalDateUtil.localDateFormat(LocalDate.now(), "yyyy-MM-dd");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        //数据
        List<List<Object>> dataList = Lists.newArrayList();
        tableList.forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            columns.forEach(h ->{
                String value = ele.get(h.getProp());
                try {
                    BigDecimal valueBd = BigDecimalUtil.valueOf(value);
                    dataEle.add(valueBd);
                } catch (Exception e) {
                    dataEle.add(value);
                }
            });
            dataList.add(dataEle);
        });
        int[] mergeColumnIndex = {0};
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(0, "余热汽轮机报表")
                .head(headList)
                .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new HeadContentStyleStrategy())
                .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex))
                .build();
        excelWriter.write(dataList, writeSheet);
        excelWriter.finish();
        response.flushBuffer();
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> boilerReport(LocalDate date) {
        List<Map<String, String>> data = postDataCollect(date, Lists.newArrayList("YR_AI06_2_METER3"
                , "YR_RT01_3_METER5"
                , "YR_AI05_2_METER4"
                , "YR_TC07_2_METER3"
                , "YR_TC05_3_METER3"
                , "YR_TC06_3_METER3"
                , "YR_TC05_3_METER4"
                , "YR_TC06_3_METER4"
                , "YR_TC07_2_METER4"
                , "YR_SH0049_AI_I"
                , "YR_RT01_3_METER6"
                , "YR_AI05_2_METER3"
                , "YR_SH0051_AI_I"
                , "YR_SH0059_AI_PV"
                , "YR_SH0059_AI_I"
                , "YR_RT01_3_METER2"
                , "YR_RT02_3_METER2"
                , "YR_RT01_3_METER3"
                , "YR_SH0061_AI_PV"
                , "YR_SH0061_AI_I"
                , "YR_RT03_3_METER2"
                , "YR_RT04_3_METER2"
                , "YR_RT03_3_METER3"
                , "YR_AI10_2_METER1"
                , "YR_AI10_2_METER2"
                , "YR_AI10_2_METER3"
                , "YR_AI10_2_METER4"
                , "YR_AI10_2_METER5"
                , "YR_DPU2_AI01_2_METER5"
                , "YR_DPU2_RT05_4_METER8"));
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("head1","除氧器",
                Lists.newArrayList(new SteccnTableColumn("YR_AI06_2_METER3","压力(MPa)",null)
                        ,new SteccnTableColumn("YR_RT01_3_METER5","温度(℃)",null))));
        columns.add(new SteccnTableColumn("head2","汽包",
                Lists.newArrayList(new SteccnTableColumn("YR_AI05_2_METER4","压力(MPa)",null))));
        columns.add(new SteccnTableColumn("head3","余热锅炉本体温度",
                Lists.newArrayList(new SteccnTableColumn("YR_TC07_2_METER3","锅炉烟道进口烟温",null)
                        ,new SteccnTableColumn("YR_TC05_3_METER3","2级蒸发器烟温",null)
                        ,new SteccnTableColumn("YR_TC06_3_METER3","3级蒸发器烟温",null)
                        ,new SteccnTableColumn("YR_TC05_3_METER4","1级省煤器温度",null)
                        ,new SteccnTableColumn("YR_TC06_3_METER4","2级省煤器温度",null)
                        ,new SteccnTableColumn("YR_TC07_2_METER4","2级除氧蒸发器温度",null))));
        columns.add(new SteccnTableColumn("head4","给水泵A",Lists.newArrayList(new SteccnTableColumn("YR_SH0049_AI_I","电流(A)",null)
                ,new SteccnTableColumn("YR_RT01_3_METER6","给水温度",null)
                ,new SteccnTableColumn("YR_AI05_2_METER3","给水压力",null))));
        columns.add(new SteccnTableColumn("head5","给水泵B",Lists.newArrayList(new SteccnTableColumn("YR_SH0051_AI_I","电流(A)",null))));
        columns.add(new SteccnTableColumn("head6","1#引风机",
                Lists.newArrayList(new SteccnTableColumn("YR_SH0059_AI_PV","频率(HZ)",null)
                        ,new SteccnTableColumn("YR_SH0059_AI_I","电流(A)",null)
                        ,new SteccnTableColumn("YR_RT01_3_METER2","电机轴承温度1#",null)
                        ,new SteccnTableColumn("YR_RT02_3_METER2","电机轴承温度2#",null)
                        ,new SteccnTableColumn("YR_RT01_3_METER3","电机温度",null))));
        columns.add(new SteccnTableColumn("head7","2#引风机",
                Lists.newArrayList(new SteccnTableColumn("YR_SH0061_AI_PV","频率(HZ)",null)
                        ,new SteccnTableColumn("YR_SH0061_AI_I","电流(A)",null)
                        ,new SteccnTableColumn("YR_RT03_3_METER2","电机轴承温度1#",null)
                        ,new SteccnTableColumn("YR_RT04_3_METER2","电机轴承温度2#",null)
                        ,new SteccnTableColumn("YR_RT03_3_METER3","电机温度",null))));
        columns.add(new SteccnTableColumn("head8","炉水/给水",
                Lists.newArrayList(new SteccnTableColumn("YR_AI10_2_METER1","炉水(PH)",null)
                        ,new SteccnTableColumn("YR_AI10_2_METER2","炉水电导率",null)
                        ,new SteccnTableColumn("YR_AI10_2_METER3","炉水磷酸根",null)
                        ,new SteccnTableColumn("YR_AI10_2_METER4","给水(PH)",null)
                        ,new SteccnTableColumn("YR_AI10_2_METER5","给水电导率",null))));
        columns.add(new SteccnTableColumn("head9","分汽缸供气",
                Lists.newArrayList(new SteccnTableColumn("YR_DPU2_AI01_2_METER5","压力(MPa)",null)
                        ,new SteccnTableColumn("YR_DPU2_RT05_4_METER8","温度(℃)",null))));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> thirtyFiveKvReport(LocalDate date) {
        List<Map<String, String>> data = postDataCollect(date, Lists.newArrayList("35KVHV_2AH1_I"
                ,"35KVHV_2AH4_I"
                ,"35KVHV_2AH5_I"
                ,"35KVHV_2AH6_I"
                ,"35KVHV_2AH7_I"
                ,"35KVHV_2AH8_I"
                ,"35KVHV_2AH9_I"
                ,"35KVHV_2AH10_I"
                ,"35KVHV_2AH11_I"
                ,"35KVHV_2AH12_I"
                ,"35KVHV_2AH13_I"
                ,"35KVHV_1AH14_I"
                ,"35KVHV_1AH13_I"
                ,"35KVHV_1AH12_I"
                ,"35KVHV_1AH11_I"
                ,"35KVHV_1AH10_I"
                ,"35KVHV_1AH9_I"
                ,"35KVHV_1AH8_I"
                ,"35KVHV_1AH7_I"
                ,"35KVHV_1AH6_I"
                ,"35KVHV_1AH5_I"
                ,"35KVHV_1AH4_I"
                ,"35KVHV_1AH1_I"));
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("head1","2AH1进线柜",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_2AH1_I","电流",null))));
        columns.add(new SteccnTableColumn("head2","2AH4出线柜",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_2AH4_I","电流",null))));
        columns.add(new SteccnTableColumn("head3","2AH5变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_2AH5_I","电流",null))));
        columns.add(new SteccnTableColumn("head4","2AH6变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_2AH6_I","电流",null))));
        columns.add(new SteccnTableColumn("head5","2AH7变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_2AH7_I","电流",null))));
        columns.add(new SteccnTableColumn("head6","2AH8变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_2AH8_I","电流",null))));
        columns.add(new SteccnTableColumn("head7","2AH9变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_2AH9_I","电流",null))));
        columns.add(new SteccnTableColumn("head8","2AH10变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_2AH10_I","电流",null))));
        columns.add(new SteccnTableColumn("head9","2AH11变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_2AH11_I","电流",null))));
        columns.add(new SteccnTableColumn("head10","2AH12变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_2AH12_I","电流",null))));
        columns.add(new SteccnTableColumn("head11","2AH13出线柜",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_2AH13_I","电流",null))));
        columns.add(new SteccnTableColumn("head12","1AH14母联柜",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH14_I","电流",null))));
        columns.add(new SteccnTableColumn("head13","1AH13出线柜",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH13_I","电流",null))));
        columns.add(new SteccnTableColumn("head14","1AH12变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH12_I","电流",null))));
        columns.add(new SteccnTableColumn("head15","1AH11变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH11_I","电流",null))));
        columns.add(new SteccnTableColumn("head16","1AH10变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH10_I","电流",null))));
        columns.add(new SteccnTableColumn("head17","1AH9变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH9_I","电流",null))));
        columns.add(new SteccnTableColumn("head18","1AH8变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH8_I","电流",null))));
        columns.add(new SteccnTableColumn("head19","1AH7变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH7_I","电流",null))));
        columns.add(new SteccnTableColumn("head20","1AH6变压器",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH6_I","电流",null))));
        columns.add(new SteccnTableColumn("head21","1AH5出线柜",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH5_I","电流",null))));
        columns.add(new SteccnTableColumn("head22","1AH4出线柜",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH4_I","电流",null))));
        columns.add(new SteccnTableColumn("head23","1AH1出线柜",
                Lists.newArrayList(new SteccnTableColumn("35KVHV_1AH1_I","电流",null))));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> desReport(LocalDate date) {
        List<Map<String, String>> data = postDataCollect(date, Lists.newArrayList("1#GYYFJ_PV"
                ,"1#GYYFJ_I"
                ,"1#YFJHZ_TI_INPUT"
                ,"1#YFJQZ_TI_INPUT"
                ,"1#YFJDJQ_TI_INPUT"
                ,"2#GYYFJ_PV"
                ,"2#GYYFJ_I"
                ,"2#YFJHZ_TI_INPUT"
                ,"2#YFJQZ_TI_INPUT"
                ,"2#YFJDJQ_TI_INPUT"
                ,"AS_FI_INPUT"
                ,"AS_PI_INPUT"
                ,"ASYSKQ_PI_INPUT"
                ,"CFB_A_INPUT"
                ,"CFB_B_INPUT"
                ,"CFBJK_TI_INPUT"
                ,"CFBJK_PI_INPUT"
                ,"CFBCY_PI_INPUT"
                ,"CCQJK_PI_INPUT"
                ,"CCQJK_TI_INPUT"
                ,"MCQHYS_PI_INPUT"
                ,"CCQCK_PI_INPUT"
                ,"CCQCK_TI_INPUT"
                ,"FLCLD_LI_INPUT"
                ,"SSHCLD_LI_INPUT"
                ,"FIQ1_INPUT"
                ,"1#CFBWQL_PI_INPUT"
                ,"2#CFBWQL_PI_INPUT"
                ,"CFBHD_TI_INPUT"
                ,"YSKQ_PI_INPUT"
                ,"NOX_INPUT"
                ,"SO2_INPUT"
                ,"KLW_INPUT"
                ,"ATY_INPUT"
                ,"TLTX_O2_INPUT"
                ,"TLTX_YQ_SD_INPUT"
                ,"TLTX_YQ_TI_INPUT"
                ,"TLTX_YQ_PI_INPUT"
                ,"TLTX_YQ_LS_INPUT"));
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("head1","1#引风机",
                Lists.newArrayList(new SteccnTableColumn("1#GYYFJ_PV","频率(Hz)",null)
                        ,new SteccnTableColumn("1#GYYFJ_I","电流(A)",null)
                        ,new SteccnTableColumn("1#YFJHZ_TI_INPUT","电机轴承温度1#",null)
                        ,new SteccnTableColumn("1#YFJQZ_TI_INPUT","电机轴承温度2#",null)
                        ,new SteccnTableColumn("1#YFJDJQ_TI_INPUT","电机温度(℃)",null))));
        columns.add(new SteccnTableColumn("head2","2#引风机",
                Lists.newArrayList(new SteccnTableColumn("2#GYYFJ_PV","频率(Hz)",null)
                        ,new SteccnTableColumn("2#GYYFJ_I","电流(A)",null)
                        ,new SteccnTableColumn("2#YFJHZ_TI_INPUT","电机轴承温度1#",null)
                        ,new SteccnTableColumn("2#YFJQZ_TI_INPUT","电机轴承温度2#",null)
                        ,new SteccnTableColumn("2#YFJDJQ_TI_INPUT","电机温度(℃)",null))));
        columns.add(new SteccnTableColumn("head3","氨水参数",
                Lists.newArrayList(new SteccnTableColumn("AS_FI_INPUT","氨水流量(L/H)",null)
                        ,new SteccnTableColumn("AS_PI_INPUT","氨水压力(Pa)",null)
                        ,new SteccnTableColumn("ASYSKQ_PI_INPUT","氨水雾化压缩空气压力(Pa)",null)
                        ,new SteccnTableColumn("CFB_A_INPUT","1#氨水罐液位(m)",null)
                        ,new SteccnTableColumn("CFB_B_INPUT","2#氨水罐液位(m)",null))));
        columns.add(new SteccnTableColumn("head4","脱硫脱销一体化",
                Lists.newArrayList(new SteccnTableColumn("CFBJK_TI_INPUT","脱硫塔进口烟气温度(℃)",null)
                        ,new SteccnTableColumn("CFBJK_PI_INPUT","脱硫塔入口负压(Pa)",null)
                        ,new SteccnTableColumn("CFBCY_PI_INPUT","脱硫塔差压(Pa)",null)
                        ,new SteccnTableColumn("CCQJK_PI_INPUT","除尘器进口烟气差压(Pa)",null)
                        ,new SteccnTableColumn("CCQJK_TI_INPUT","除尘器进口烟温(℃)",null)
                        ,new SteccnTableColumn("MCQHYS_PI_INPUT","除尘器压缩空气压力(Pa)",null)
                        ,new SteccnTableColumn("CCQCK_PI_INPUT","除尘器出口烟气差压(Pa)",null)
                        ,new SteccnTableColumn("CCQCK_TI_INPUT","除尘器出口烟温(℃)",null)
                        ,new SteccnTableColumn("FLCLD_LI_INPUT","废料仓料位(mm)",null)
                        ,new SteccnTableColumn("SSHCLD_LI_INPUT","石灰仓料位(m)",null)
                        ,new SteccnTableColumn("FIQ1_INPUT","氨水累积量(m³)",null)
                        ,new SteccnTableColumn("1#CFBWQL_PI_INPUT","脱硫塔1#压力(Pa)",null)
                        ,new SteccnTableColumn("2#CFBWQL_PI_INPUT","脱硫塔2#压力(Pa)",null)
                        ,new SteccnTableColumn("CFBHD_TI_INPUT","脱硫塔灰斗温度(℃)",null)
                        ,new SteccnTableColumn("2#YSKQ_PI_INPUT","厂区压缩空气压力(Pa)",null))));
        columns.add(new SteccnTableColumn("head5","烟气排放指标",
                Lists.newArrayList(new SteccnTableColumn("NOX_INPUT","脱硝后氮氧化物(mg/Nm³)",null)
                        ,new SteccnTableColumn("SO2_INPUT","脱硫后二氧化硫(mg/Nm³)",null)
                        ,new SteccnTableColumn("KLW_INPUT","除尘后颗粒物(mg/Nm³)",null)
                        ,new SteccnTableColumn("ATY_INPUT","氨逃逸(ppm)",null)
                        ,new SteccnTableColumn("TLTX_O2_INPUT","氧含量",null)
                        ,new SteccnTableColumn("TLTX_YQ_SD_INPUT","烟气湿度",null)
                        ,new SteccnTableColumn("TLTX_YQ_TI_INPUT","烟气温度",null)
                        ,new SteccnTableColumn("TLTX_YQ_PI_INPUT","烟气压力",null)
                        ,new SteccnTableColumn("TLTX_YQ_LS_INPUT","烟气流速",null))));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> melting1(LocalDate date) {
        List<Map<String, String>> data = postDataCollect(date, Lists.newArrayList("RY_FIC1_PV"
                ,"RY_FIC2_PV"
                ,"RY_FIC3_PV"
                ,"RY_FIC4_PV"
                ,"RY_FIC5_PV"
                ,"RY_FIC6_PV"
                ,"RY_FIC7_PV"
                ,"RY_FIC8_PV"
                ,"RY_FIC9_PV"
                ,"RY_FIC101_PV"
                ,"RY_FIC102_PV"
                ,"RY_FIC103_PV"
                ,"RY_FIC104_PV"
                ,"RY_FIC105_PV"
                ,"RY_FIC106_PV"
                ,"RY_FIC107_PV"
                ,"RY_FIC108_PV"
                ,"RY_FIC109_PV"
                ,"RY_TEL1_PV"
                ,"RY_TEL2_PV"
                ,"RY_TEL3_PV"
                ,"RY_TEL4_PV"
                ,"RY_TEL5_PV"
                ,"RY_TEL6_PV"
                ,"RY_TEL7_PV"
                ,"RY_TEL8_PV"
                ,"RY_TEL9_PV"
                ,"RY_PT511_PV"
                ,"RY_TE501_PV"
                ,"RY_TE503_PV"
                ,"RY_TE505_PV"
                ,"RY_TE506_PV"
                ,"RY_TE507_PV"
                ,"RY_TE508_PV"
                ,"RY_PIC81_PV_OUT"
                ,"RY_PIC81_MV"
                ,"RY_LIC92_PV_OUT"
                ,"RY_LIC92_MV"
                ,"RY_PIC303_PV_OUT"
                ,"RY_PIC10_PV_OUT"
                ,"RY_PIC308_PV_OUT"
                ,"RY_TE61_PV"
                ,"RY_TE63_PV"
        ));
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("head1","分支小炉流量(kg/h)天然气流量(m³/h)",
                Lists.newArrayList(new SteccnTableColumn("RY_FIC1_PV","1#小炉",null)
                        ,new SteccnTableColumn("RY_FIC2_PV","2#小炉",null)
                        ,new SteccnTableColumn("RY_FIC3_PV","3#小炉",null)
                        ,new SteccnTableColumn("RY_FIC4_PV","4#小炉",null)
                        ,new SteccnTableColumn("RY_FIC5_PV","5#小炉",null)
                        ,new SteccnTableColumn("RY_FIC6_PV","6#小炉",null)
                        ,new SteccnTableColumn("RY_FIC7_PV","7#小炉",null)
                        ,new SteccnTableColumn("RY_FIC8_PV","8#小炉",null)
                        ,new SteccnTableColumn("RY_FIC9_PV","9#小炉",null))));
        columns.add(new SteccnTableColumn("head2","分支小炉助燃风流量(m³/h)",
                Lists.newArrayList(new SteccnTableColumn("RY_FIC101_PV","1#小炉",null)
                        ,new SteccnTableColumn("RY_FIC102_PV","2#小炉",null)
                        ,new SteccnTableColumn("RY_FIC103_PV","3#小炉",null)
                        ,new SteccnTableColumn("RY_FIC104_PV","4#小炉",null)
                        ,new SteccnTableColumn("RY_FIC105_PV","5#小炉",null)
                        ,new SteccnTableColumn("RY_FIC106_PV","6#小炉",null)
                        ,new SteccnTableColumn("RY_FIC107_PV","7#小炉",null)
                        ,new SteccnTableColumn("RY_FIC108_PV","8#小炉",null)
                        ,new SteccnTableColumn("RY_FIC109_PV","9#小炉",null))));
        columns.add(new SteccnTableColumn("head3","蓄热室碹顶温度(℃)",
                Lists.newArrayList(new SteccnTableColumn("RY_TEL1_PV","1#",null)
                        ,new SteccnTableColumn("RY_TEL2_PV","2#",null)
                        ,new SteccnTableColumn("RY_TEL3_PV","3#",null)
                        ,new SteccnTableColumn("RY_TEL4_PV","4#",null)
                        ,new SteccnTableColumn("RY_TEL5_PV","5#",null)
                        ,new SteccnTableColumn("RY_TEL6_PV","6#",null)
                        ,new SteccnTableColumn("RY_TEL7_PV","7#",null)
                        ,new SteccnTableColumn("RY_TEL8_PV","8#",null)
                        ,new SteccnTableColumn("RY_TEL9_PV","9#",null))));
        columns.add(new SteccnTableColumn("head4","吊墙风压(pa)/温度(℃)",
                Lists.newArrayList(new SteccnTableColumn("RY_PT511_PV","吊墙风压",null)
                        ,new SteccnTableColumn("steelTemp","钢温度",
                                Lists.newArrayList(new SteccnTableColumn("RY_TE501_PV","左",null),new SteccnTableColumn("RY_TE503_PV","中",null),new SteccnTableColumn("RY_TE505_PV","右",null)))
                        ,new SteccnTableColumn("turnTemp","砖温度",
                                Lists.newArrayList(new SteccnTableColumn("RY_TE506_PV","左",null),new SteccnTableColumn("RY_TE507_PV","中",null),new SteccnTableColumn("RY_TE508_PV","右",null))))));
        columns.add(new SteccnTableColumn("RY_PIC81_PV_OUT","窑压(Pa)",null));
        columns.add(new SteccnTableColumn("RY_PIC81_MV","旋转闸板(%)",null));
        columns.add(new SteccnTableColumn("RY_LIC92_PV_OUT","液面(mm)",null));
        columns.add(new SteccnTableColumn("RY_LIC92_MV","投料频率(Hz)",null));
        columns.add(new SteccnTableColumn("RY_PIC303_PV_OUT","油压(mpa)",null));
        columns.add(new SteccnTableColumn("RY_PIC10_PV_OUT","天然气压(mpa)",null));
        columns.add(new SteccnTableColumn("RY_PIC308_PV_OUT","雾化气压(mpa)",null));
        columns.add(new SteccnTableColumn("RY_TE61_PV","烟道温度(℃)",null));
        columns.add(new SteccnTableColumn("RY_TE63_PV","烟根温度(℃)",null));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> melting2(LocalDate date) {
        List<Map<String, String>> data = postDataCollect(date, Lists.newArrayList("RY_TE2_PV"
                ,"RY_TE4_PV"
                ,"RY_TE5_PV"
                ,"RY_TE6_PV"
                ,"RY_TE7_PV"
                ,"RY_TE8_PV"
                ,"RY_TE10_PV"
                ,"RY_TE12_PV"
                ,"RY_TE13_PV"
                ,"RY_TE14_PV"
                ,"RY_TE15_PV"
                ,"RY_TE16_PV"
                ,"RY_TE32_PV"
                ,"RY_TE34_PV"
                ,"RY_TE35_PV"
                ,"RY_TE36_PV"
                ,"RY_TE37_PV"
                ,"RY_TE38_PV"
                ,"RY_TE40_PV"
                ,"RY_TE42_PV"
                ,"RY_TE43_PV"
                ,"RY_TE44_PV"
                ,"RY_TE45_PV"
                ,"RY_TE47_PV"
                ,"RY_TER10_PV"
                ,"RY_TE17_PV"
                ,"RY_TEL10_PV"
                ,"RY_TER11_PV"
                ,"RY_TER12_PV"
                ,"RY_TER13_PV"
                ,"RY_TER14_PV"
                ,"RY_TER15_PV"
                ,"RY_TER19_PV"
                ,"RY_TER20_PV"
                ,"RY_TER18_PV"
                ,"RY_TER35_PV"
                ,"RY_TER39_PV"
                ,"RY_TER40_PV"
                ,"RY_TER38_PV"
                ,"RY_TEL11_PV"
                ,"RY_TEL12_PV"
                ,"RY_TEL13_PV"
                ,"RY_TEL14_PV"
                ,"RY_TEL15_PV"
                ,"RY_TEL19_PV"
                ,"RY_TEL20_PV"
                ,"RY_TEL18_PV"
                ,"RY_TEL35_PV"
                ,"RY_TEL39_PV"
                ,"RY_TEL40_PV"
                ,"RY_TEL38_PV"

        ));
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("head1","熔化部温度(℃)",
                Lists.newArrayList(new SteccnTableColumn("head11","碹顶温度",
                                Lists.newArrayList(new SteccnTableColumn("RY_TE2_PV","投料池T2",null)
                                        ,new SteccnTableColumn("RY_TE4_PV","1#T4",null)
                                        ,new SteccnTableColumn("RY_TE5_PV","2#T5",null)
                                        ,new SteccnTableColumn("RY_TE6_PV","3#T6",null)
                                        ,new SteccnTableColumn("RY_TE7_PV","4#T7",null)
                                        ,new SteccnTableColumn("RY_TE8_PV","5#T8",null)
                                        ,new SteccnTableColumn("RY_TE10_PV","6#T10",null)
                                        ,new SteccnTableColumn("RY_TE12_PV","7#T12",null)
                                        ,new SteccnTableColumn("RY_TE13_PV","8#T13",null)
                                        ,new SteccnTableColumn("RY_TE14_PV","9#T14",null)
                                        ,new SteccnTableColumn("RY_TE15_PV","澄清1#T15",null)
                                        ,new SteccnTableColumn("RY_TE16_PV","澄清2#T16",null)
                                        ,new SteccnTableColumn("RY_TE32_PV","投料池T32",null)))
                        ,new SteccnTableColumn("head12","池底温度",Lists.newArrayList(new SteccnTableColumn("RY_TE34_PV","1#T34",null)
                                ,new SteccnTableColumn("RY_TE34_PV","1#T34",null)
                                ,new SteccnTableColumn("RY_TE35_PV","2#T35",null)
                                ,new SteccnTableColumn("RY_TE36_PV","3#T36",null)
                                ,new SteccnTableColumn("RY_TE37_PV","4#T37",null)
                                ,new SteccnTableColumn("RY_TE38_PV","5#T38",null)
                                ,new SteccnTableColumn("RY_TE40_PV","6#T40",null)
                                ,new SteccnTableColumn("RY_TE42_PV","7#T42",null)
                                ,new SteccnTableColumn("RY_TE43_PV","8#T43",null)
                                ,new SteccnTableColumn("RY_TE44_PV","9#T44",null)
                                ,new SteccnTableColumn("RY_TE45_PV","澄清1#T45",null)
                                ,new SteccnTableColumn("RY_TE47_PV","澄清2#T47",null))))));
        columns.add(new SteccnTableColumn("head2","横通路碹顶温度(℃)",
                Lists.newArrayList(new SteccnTableColumn("RY_TER10_PV","1#R10",null)
                        ,new SteccnTableColumn("RY_TE17_PV","2#T17",null)
                        ,new SteccnTableColumn("RY_TEL10_PV","3#L10",null))));
        columns.add(new SteccnTableColumn("head3","东纵通路碹顶温度(℃)",
                Lists.newArrayList(new SteccnTableColumn("RY_TER11_PV","1#R11",null)
                        ,new SteccnTableColumn("RY_TER12_PV","2#R12",null)
                        ,new SteccnTableColumn("RY_TER13_PV","3#R13",null)
                        ,new SteccnTableColumn("RY_TER14_PV","4#R14",null))));
        columns.add(new SteccnTableColumn("head4","东支通路温度(℃)",
                Lists.newArrayList(new SteccnTableColumn("head41","碹顶温度",
                                Lists.newArrayList(new SteccnTableColumn("RY_TER15_PV","1#R15",null)
                                        ,new SteccnTableColumn("RY_TER19_PV","2#R19",null)
                                        ,new SteccnTableColumn("RY_TER20_PV","3#R20",null)
                                        ,new SteccnTableColumn("RY_TER18_PV","4#R18",null)))
                        ,new SteccnTableColumn("head42","池底温度",Lists.newArrayList(new SteccnTableColumn("RY_TER35_PV","1#R35",null)
                                ,new SteccnTableColumn("RY_TER39_PV","2#R39",null)
                                ,new SteccnTableColumn("RY_TER40_PV","3#R40",null)
                                ,new SteccnTableColumn("RY_TER38_PV","4#R38",null))))));
        columns.add(new SteccnTableColumn("head5","西纵通路碹顶温度(℃)",
                Lists.newArrayList(new SteccnTableColumn("RY_TEL11_PV","1#L11",null)
                        ,new SteccnTableColumn("RY_TEL12_PV","2#L12",null)
                        ,new SteccnTableColumn("RY_TEL13_PV","3#L13",null)
                        ,new SteccnTableColumn("RY_TEL14_PV","4#L14",null))));
        columns.add(new SteccnTableColumn("head6","西支通路温度(℃)",
                Lists.newArrayList(new SteccnTableColumn("head61","碹顶温度",
                                Lists.newArrayList(new SteccnTableColumn("RY_TEL15_PV","1#L15",null)
                                        ,new SteccnTableColumn("RY_TEL19_PV","2#L19",null)
                                        ,new SteccnTableColumn("RY_TEL20_PV","3#L20",null)
                                        ,new SteccnTableColumn("RY_TEL18_PV","4#L18",null)))
                        ,new SteccnTableColumn("head62","池底温度",Lists.newArrayList(new SteccnTableColumn("RY_TEL35_PV","1#L35",null)
                                ,new SteccnTableColumn("RY_TEL39_PV","2#L39",null)
                                ,new SteccnTableColumn("RY_TEL40_PV","3#L40",null)
                                ,new SteccnTableColumn("RY_TEL38_PV","4#L38",null))))));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> annealing(LocalDate date,Integer type) {
        List<String> params = Lists.newArrayList("RY_TIC1101_PV"
                ,"RY_TIC"+type+"103_PV"
                ,"RY_TIC"+type+"105_PV"
                ,"RY_TIC"+type+"111_PV"
                ,"RY_TIC"+type+"112_PV"
                ,"RY_TIC"+type+"113_PV"
                ,"RY_TIC"+type+"201_PV"
                ,"RY_TIC"+type+"203_PV"
                ,"RY_TIC"+type+"205_PV"
                ,"RY_TIC"+type+"211_PV"
                ,"RY_TIC"+type+"212_PV"
                ,"RY_TIC"+type+"213_PV"
                ,"RY_TIC"+type+"301_PV"
                ,"RY_TIC"+type+"303_PV"
                ,"RY_TIC"+type+"305_PV"
                ,"RY_TIC"+type+"311_PV"
                ,"RY_TIC"+type+"312_PV"
                ,"RY_TIC"+type+"313_PV"
                ,"RY_TIC"+type+"401_PV"
                ,"RY_TIC"+type+"403_PV"
                ,"RY_TIC"+type+"405_PV"
                ,"RY_TIC"+type+"411_PV"
                ,"RY_TIC"+type+"412_PV"
                ,"RY_TIC"+type+"413_PV"
                ,"RY_TIC"+type+"501_PV");
        List<Map<String, String>> data = postDataCollect(date, params);
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        //计算班次
        data.forEach(tmp->{
            QtTaskClassCalculate time = CalculateDateUtil.calculateClassTime(date.atTime(LocalTime.parse(tmp.get("time"), DateTimeFormatter.ofPattern("HH:mm"))));
            if(time.getClassType().equals(OaConstant.NO)){
                tmp.put("classOrder","早班");
            }else {
                tmp.put("classOrder","晚班");
            }
        });
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("classOrder","班次",null));
        columns.add(new SteccnTableColumn("head1","退火窑空间温度",
                Lists.newArrayList(new SteccnTableColumn("head11","A1区板上",
                                Lists.newArrayList(new SteccnTableColumn(params.get(0),"南",null)
                                        ,new SteccnTableColumn(params.get(1),"中",null)
                                        ,new SteccnTableColumn(params.get(2),"北",null)))
                        ,new SteccnTableColumn("head12","A1区板下",
                                Lists.newArrayList(new SteccnTableColumn(params.get(3),"南",null)
                                ,new SteccnTableColumn(params.get(4),"中",null)
                                ,new SteccnTableColumn(params.get(5),"北",null)))
                        ,new SteccnTableColumn("head13","A2区板上",
                                Lists.newArrayList(new SteccnTableColumn(params.get(6),"南",null)
                                        ,new SteccnTableColumn(params.get(7),"中",null)
                                        ,new SteccnTableColumn(params.get(8),"北",null)))
                        ,new SteccnTableColumn("head14","A2区板下",
                                Lists.newArrayList(new SteccnTableColumn(params.get(9),"南",null)
                                        ,new SteccnTableColumn(params.get(10),"中",null)
                                        ,new SteccnTableColumn(params.get(11),"北",null)))
                        ,new SteccnTableColumn("head15","B区板上",
                                Lists.newArrayList(new SteccnTableColumn(params.get(12),"南",null)
                                        ,new SteccnTableColumn(params.get(13),"中",null)
                                        ,new SteccnTableColumn(params.get(14),"北",null)))
                        ,new SteccnTableColumn("head16","B区板下",
                                Lists.newArrayList(new SteccnTableColumn(params.get(15),"南",null)
                                        ,new SteccnTableColumn(params.get(16),"中",null)
                                        ,new SteccnTableColumn(params.get(17),"北",null)))
                        ,new SteccnTableColumn("head17","C区板上",
                                Lists.newArrayList(new SteccnTableColumn(params.get(18),"南",null)
                                        ,new SteccnTableColumn(params.get(19),"中",null)
                                        ,new SteccnTableColumn(params.get(20),"北",null)))
                        ,new SteccnTableColumn("head18","C区板下",
                                Lists.newArrayList(new SteccnTableColumn(params.get(21),"南",null)
                                        ,new SteccnTableColumn(params.get(22),"中",null)
                                        ,new SteccnTableColumn(params.get(23),"北",null)))
                        ,new SteccnTableColumn(params.get(24),"R区",null))));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> molding1(LocalDate date) {
        List<Map<String, String>> data = postDataCollect(date, Lists.newArrayList("YY_TR_S_1"
                ,"YY_CR_S_1"
                ,"YY_UP_S_1"
                ,"YY_DR_S_1"
                ,"YY_TR_A_1"
                ,"YY_CR_A_1"
                ,"YY_UP_A_1"
                ,"YY_DR_A_1"
                ,"YY_TR_S_2"
                ,"YY_CR_S_2"
                ,"YY_UP_S_2"
                ,"YY_DR_S_2"
                ,"YY_TR_A_2"
                ,"YY_CR_A_2"
                ,"YY_UP_A_2"
                ,"YY_DR_A_2"
                ,"YY_TR_S_3"
                ,"YY_CR_S_3"
                ,"YY_UP_S_3"
                ,"YY_DR_S_3"
                ,"YY_TR_A_3"
                ,"YY_CR_A_3"
                ,"YY_UP_A_3"
                ,"YY_DR_A_3"
                ,"YY_TR_S_4"
                ,"YY_CR_S_4"
                ,"YY_UP_S_4"
                ,"YY_DR_S_4"
                ,"YY_TR_A_4"
                ,"YY_CR_A_4"
                ,"YY_UP_A_4"
                ,"YY_DR_A_4"
        ));
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("head1","一线",
                Lists.newArrayList(new SteccnTableColumn("YY_TR_S_1","过度辊速度",null)
                        ,new SteccnTableColumn("YY_CR_S_1","副辊速度",null)
                        ,new SteccnTableColumn("YY_UP_S_1","上辊速度",null)
                        ,new SteccnTableColumn("YY_DR_S_1","下辊速度",null)
                        ,new SteccnTableColumn("YY_TR_A_1","过度辊电流",null)
                        ,new SteccnTableColumn("YY_CR_A_1","副辊电流",null)
                        ,new SteccnTableColumn("YY_UP_A_1","上辊电流",null)
                        ,new SteccnTableColumn("YY_DR_A_1","下辊电流",null))));
        columns.add(new SteccnTableColumn("head2","二线",
                Lists.newArrayList(new SteccnTableColumn("YY_TR_S_2","过度辊速度",null)
                        ,new SteccnTableColumn("YY_CR_S_2","副辊速度",null)
                        ,new SteccnTableColumn("YY_UP_S_2","上辊速度",null)
                        ,new SteccnTableColumn("YY_DR_S_2","下辊速度",null)
                        ,new SteccnTableColumn("YY_TR_A_2","过度辊电流",null)
                        ,new SteccnTableColumn("YY_CR_A_2","副辊电流",null)
                        ,new SteccnTableColumn("YY_UP_A_2","上辊电流",null)
                        ,new SteccnTableColumn("YY_DR_A_2","下辊电流",null))));
        columns.add(new SteccnTableColumn("head3","三线",
                Lists.newArrayList(new SteccnTableColumn("YY_TR_S_3","过度辊速度",null)
                        ,new SteccnTableColumn("YY_CR_S_3","副辊速度",null)
                        ,new SteccnTableColumn("YY_UP_S_3","上辊速度",null)
                        ,new SteccnTableColumn("YY_DR_S_3","下辊速度",null)
                        ,new SteccnTableColumn("YY_TR_A_3","过度辊电流",null)
                        ,new SteccnTableColumn("YY_CR_A_3","副辊电流",null)
                        ,new SteccnTableColumn("YY_UP_A_3","上辊电流",null)
                        ,new SteccnTableColumn("YY_DR_A_3","下辊电流",null))));
        columns.add(new SteccnTableColumn("head4","四线",
                Lists.newArrayList(new SteccnTableColumn("YY_TR_S_4","过度辊速度",null)
                        ,new SteccnTableColumn("YY_CR_S_4","副辊速度",null)
                        ,new SteccnTableColumn("YY_UP_S_4","上辊速度",null)
                        ,new SteccnTableColumn("YY_DR_S_4","下辊速度",null)
                        ,new SteccnTableColumn("YY_TR_A_4","过度辊电流",null)
                        ,new SteccnTableColumn("YY_CR_A_4","副辊电流",null)
                        ,new SteccnTableColumn("YY_UP_A_4","上辊电流",null)
                        ,new SteccnTableColumn("YY_DR_A_4","下辊电流",null))));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> molding2(LocalDate date) {
        List<Map<String, String>> data = postDataCollect(date, Lists.newArrayList("YY_TR_S_5"
                ,"YY_CR_S_5"
                ,"YY_UP_S_5"
                ,"YY_DR_S_5"
                ,"YY_TR_A_5"
                ,"YY_CR_A_5"
                ,"YY_UP_A_5"
                ,"YY_DR_A_5"
                ,"YY_TR_S_6"
                ,"YY_CR_S_6"
                ,"YY_UP_S_6"
                ,"YY_DR_S_6"
                ,"YY_TR_A_6"
                ,"YY_CR_A_6"
                ,"YY_UP_A_6"
                ,"YY_DR_A_6"
                ,"YY_TR_S_7"
                ,"YY_CR_S_7"
                ,"YY_UP_S_7"
                ,"YY_DR_S_7"
                ,"YY_TR_A_7"
                ,"YY_CR_A_7"
                ,"YY_UP_A_7"
                ,"YY_DR_A_7"
                ,"YY_TR_S_8"
                ,"YY_CR_S_8"
                ,"YY_UP_S_8"
                ,"YY_DR_S_8"
                ,"YY_TR_A_8"
                ,"YY_CR_A_8"
                ,"YY_UP_A_8"
                ,"YY_DR_A_8"
        ));
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("head1","五线",
                Lists.newArrayList(new SteccnTableColumn("YY_TR_S_5","过度辊速度",null)
                        ,new SteccnTableColumn("YY_CR_S_5","副辊速度",null)
                        ,new SteccnTableColumn("YY_UP_S_5","上辊速度",null)
                        ,new SteccnTableColumn("YY_DR_S_5","下辊速度",null)
                        ,new SteccnTableColumn("YY_TR_A_5","过度辊电流",null)
                        ,new SteccnTableColumn("YY_CR_A_5","副辊电流",null)
                        ,new SteccnTableColumn("YY_UP_A_5","上辊电流",null)
                        ,new SteccnTableColumn("YY_DR_A_5","下辊电流",null))));
        columns.add(new SteccnTableColumn("head2","六线",
                Lists.newArrayList(new SteccnTableColumn("YY_TR_S_6","过度辊速度",null)
                        ,new SteccnTableColumn("YY_CR_S_6","副辊速度",null)
                        ,new SteccnTableColumn("YY_UP_S_6","上辊速度",null)
                        ,new SteccnTableColumn("YY_DR_S_6","下辊速度",null)
                        ,new SteccnTableColumn("YY_TR_A_6","过度辊电流",null)
                        ,new SteccnTableColumn("YY_CR_A_6","副辊电流",null)
                        ,new SteccnTableColumn("YY_UP_A_6","上辊电流",null)
                        ,new SteccnTableColumn("YY_DR_A_6","下辊电流",null))));
        columns.add(new SteccnTableColumn("head3","七线",
                Lists.newArrayList(new SteccnTableColumn("YY_TR_S_7","过度辊速度",null)
                        ,new SteccnTableColumn("YY_CR_S_7","副辊速度",null)
                        ,new SteccnTableColumn("YY_UP_S_7","上辊速度",null)
                        ,new SteccnTableColumn("YY_DR_S_7","下辊速度",null)
                        ,new SteccnTableColumn("YY_TR_A_7","过度辊电流",null)
                        ,new SteccnTableColumn("YY_CR_A_7","副辊电流",null)
                        ,new SteccnTableColumn("YY_UP_A_7","上辊电流",null)
                        ,new SteccnTableColumn("YY_DR_A_7","下辊电流",null))));
        columns.add(new SteccnTableColumn("head4","八线",
                Lists.newArrayList(new SteccnTableColumn("YY_TR_S_8","过度辊速度",null)
                        ,new SteccnTableColumn("YY_CR_S_8","副辊速度",null)
                        ,new SteccnTableColumn("YY_UP_S_8","上辊速度",null)
                        ,new SteccnTableColumn("YY_DR_S_8","下辊速度",null)
                        ,new SteccnTableColumn("YY_TR_A_8","过度辊电流",null)
                        ,new SteccnTableColumn("YY_CR_A_8","副辊电流",null)
                        ,new SteccnTableColumn("YY_UP_A_8","上辊电流",null)
                        ,new SteccnTableColumn("YY_DR_A_8","下辊电流",null))));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> acStation(LocalDate date) {
        List<Map<String, String>> data = postDataCollect(date, Lists.newArrayList("KYZ_FE5001_INPUT"
                ,"KYZ_FE5002_INPUT"
                ,"KYZ_FE5003_INPUT"
                ,"KYZ_FE5004_INPUT"
                ,"KYZ_FE5005_INPUT"
                ,"KYZ_FE5006_INPUT"
                ,"KYZ_FE5001_LJ_INPUT"
                ,"KYZ_FE5002_LJ_INPUT"
                ,"KYZ_FE5003_LJ_INPUT"
                ,"KYZ_FE5004_LJ_INPUT"
                ,"KYZ_FE5005_LJ_INPUT"
                ,"KYZ_FE5006_LJ_INPUT"
                ,"KYZ_KYJ01_003_JZPQ_P"
                ,"KYZ_KYJ01_007_ZJ_T"
                ,"KYZ_KYJ01_010_JZ_T"
                ,"KYZ_CM5_6AN4_1_I1"
                ,"KYZ_KYJ02_003_JZPQ_P"
                ,"KYZ_KYJ02_007_ZJ_T"
                ,"KYZ_KYJ02_010_JZ_T"
                ,"KYZ_CM5_6AN4_2_I1"
                ,"KYZ_KYJ03_003_JZPQ_P"
                ,"KYZ_KYJ03_007_ZJ_T"
                ,"KYZ_KYJ03_010_JZ_T"
                ,"KYZ_CM5_6AN5_1_I1"
                ,"KYZ_KYJ04_003_JZPQ_P"
                ,"KYZ_KYJ04_007_ZJ_T"
                ,"KYZ_KYJ04_010_JZ_T"
                ,"KYZ_CM5_6AN5_2_I1"
                ,"KYZ_KYJ05_003_JZPQ_P"
                ,"KYZ_KYJ05_007_ZJ_T"
                ,"KYZ_KYJ05_010_JZ_T"
                ,"KYZ_CM5_6AN6_1_I1"
                ,"KYZ_KYJ06_003_JZPQ_P"
                ,"KYZ_KYJ06_007_ZJ_T"
                ,"KYZ_KYJ06_010_JZ_T"
                ,"KYZ_CM5_7AN6_1_I1"
                ,"KYZ_KYJ07_003_JZPQ_P"
                ,"KYZ_KYJ07_007_ZJ_T"
                ,"KYZ_KYJ07_010_JZ_T"
                ,"KYZ_CM5_7AN6_2_I1"
                ,"KYZ_KYJ08_003_JZPQ_P"
                ,"KYZ_KYJ08_007_ZJ_T"
                ,"KYZ_KYJ08_010_JZ_T"
                ,"KYZ_CM5_7AN5_1_I1"
                ,"KYZ_KYJ09_003_JZPQ_P"
                ,"KYZ_KYJ09_007_ZJ_T"
                ,"KYZ_KYJ09_010_JZ_T"
                ,"KYZ_CM5_7AN5_2_I1"
                ,"KYZ_KYJ10_003_JZPQ_P"
                ,"KYZ_KYJ10_007_ZJ_T"
                ,"KYZ_KYJ10_010_JZ_T"
                ,"KYZ_CM5_7AN4_1_I1"
                ,"KYZ_GZJ1_JRTTZ_T"
                ,"KYZ_CM5_6AN6_2_I1"
                ,"KYZ_GZJ2_JRTTZ_T"
                ,"KYZ_CM5_6AN6_3_I1"
                ,"KYZ_GZJ3_JRTTZ_T"
                ,"KYZ_CM5_7AN4_2_I1"));
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("KYZ_FE5001_INPUT","雾化气流量",null));
        columns.add(new SteccnTableColumn("KYZ_FE5002_INPUT","退火切裁用气流量",null));
        columns.add(new SteccnTableColumn("KYZ_FE5003_INPUT","深加工用气流量",null));
        columns.add(new SteccnTableColumn("KYZ_FE5004_INPUT","熔化用气流量",null));
        columns.add(new SteccnTableColumn("KYZ_FE5005_INPUT","深加工用气流量",null));
        columns.add(new SteccnTableColumn("KYZ_FE5006_INPUT","余热、原料用气流量",null));
        columns.add(new SteccnTableColumn("KYZ_FE5001_LJ_INPUT","雾化气流量累计",null));
        columns.add(new SteccnTableColumn("KYZ_FE5002_LJ_INPUT","退火切裁用气流量累计",null));
        columns.add(new SteccnTableColumn("KYZ_FE5003_LJ_INPUT","深加工用气流量累计",null));
        columns.add(new SteccnTableColumn("KYZ_FE5004_LJ_INPUT","熔化用气流量累计",null));
        columns.add(new SteccnTableColumn("KYZ_FE5005_LJ_INPUT","深加工用气流量累计",null));
        columns.add(new SteccnTableColumn("KYZ_FE5006_LJ_INPUT","余热、原料用气流量累计",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ01_003_JZPQ_P","1号空压机排气压力",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ01_007_ZJ_T","1号空压机主机温度",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ01_010_JZ_T","1号空压机机组温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_6AN4_1_I1","1号空压机电流",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ02_003_JZPQ_P","2号空压机排气压力",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ02_007_ZJ_T","2号空压机主机温度",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ02_010_JZ_T","2号空压机机组温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_6AN4_2_I1","2号空压机电流",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ03_003_JZPQ_P","3号空压机排气压力",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ03_007_ZJ_T","3号空压机主机温度",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ03_010_JZ_T","3号空压机机组温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_6AN5_1_I1","3号空压机电流",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ04_003_JZPQ_P","4号空压机排气压力",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ04_007_ZJ_T","4号空压机主机温度",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ04_010_JZ_T","4号空压机机组温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_6AN5_2_I1","4号空压机电流",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ05_003_JZPQ_P","5号空压机排气压力",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ05_007_ZJ_T","5号空压机主机温度",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ05_010_JZ_T","5号空压机机组温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_6AN6_1_I1","5号空压机电流",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ06_003_JZPQ_P","6号空压机排气压力",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ06_007_ZJ_T","6号空压机主机温度",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ06_010_JZ_T","6号空压机机组温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_7AN6_1_I1","6号空压机电流",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ07_003_JZPQ_P","7号空压机排气压力",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ07_007_ZJ_T","7号空压机主机温度",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ07_010_JZ_T","7号空压机机组温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_7AN6_2_I1","7号空压机电流",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ08_003_JZPQ_P","8号空压机排气压力",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ08_007_ZJ_T","8号空压机主机温度",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ08_010_JZ_T","8号空压机机组温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_7AN5_1_I1","8号空压机电流",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ09_003_JZPQ_P","9号空压机排气压力",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ09_007_ZJ_T","9号空压机主机温度",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ09_010_JZ_T","9号空压机机组温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_7AN5_2_I1","9号空压机电流",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ10_003_JZPQ_P","10号空压机排气压力",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ10_007_ZJ_T","10号空压机主机温度",null));
        columns.add(new SteccnTableColumn("KYZ_KYJ10_010_JZ_T","10号空压机机组温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_7AN4_1_I1","10号空压机电流",null));
        columns.add(new SteccnTableColumn("KYZ_GZJ1_JRTTZ_T","1号干燥机塔中温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_6AN6_2_I1","1号干燥机电流",null));
        columns.add(new SteccnTableColumn("KYZ_GZJ2_JRTTZ_T","2号干燥机塔中温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_6AN6_3_I1","2号干燥机电流",null));
        columns.add(new SteccnTableColumn("KYZ_GZJ3_JRTTZ_T","3号干燥机塔中温度",null));
        columns.add(new SteccnTableColumn("KYZ_CM5_7AN4_2_I1","3号干燥机电流",null));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    @Override
    public void acStationDownload(LocalDate date, HttpServletResponse response) throws IOException {
        SteccnTableDataDTO<Map<String, String>> recordDto = acStation(date);
        List<SteccnTableColumn> columns = recordDto.getColumns();
        List<Map<String, String>> tableList = recordDto.getTableList();
        Assert.notEmpty(tableList,"暂无数据");
        //写表头
        List<List<String>> headList = new ArrayList<>();
        columns.forEach(h ->{
            headList.add(Lists.newArrayList(h.getLabel()));
        });
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("空压站报表", "UTF-8");
        fileName += LocalDateUtil.localDateFormat(LocalDate.now(), "yyyy-MM-dd");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        //数据
        List<List<Object>> dataList = Lists.newArrayList();
        tableList.forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            columns.forEach(h ->{
                String value = ele.get(h.getProp());
                try {
                    BigDecimal valueBd = BigDecimalUtil.valueOf(value);
                    dataEle.add(valueBd);
                } catch (Exception e) {
                    dataEle.add(value);
                }
            });
            dataList.add(dataEle);
        });
        int[] mergeColumnIndex = {0};
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(0, "空压站报表")
                .head(headList)
                .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new HeadContentStyleStrategy())
                .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex))
                .build();
        excelWriter.write(dataList, writeSheet);
        excelWriter.finish();
        response.flushBuffer();
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> waterPump(LocalDate date) {
        List<Map<String, String>> data = postDataCollect(date, Lists.newArrayList("SBF_RSC_LIT_INPUT"
                ,"SBF_XFSC_LIT_INPUT"
                ,"SBF_XHSC_LIT_INPUT"
                ,"SBF_RSG_PIT_INPUT"
                ,"SBF_XHSG_OUT_PIT_INPUT"
                ,"SBF_XHSCK_TEMP_INPUT"
                ,"SBF_LQT_OUT_TEMP_INPUT"
                ,"SBF_LQT_IN_TEMP_INPUT"
                ,"SBF_AODOOR_TEMP_INPUT"
                ,"SBF_ST_LIT_INPUT"
                ,"SBF_ZXSB_1_HZ_PV"
                ,"SBF_ZXSB_2_HZ_PV"
                ,"SBF_ZXSB_3_HZ_PV"
                ,"SBF_ZXSB_4_HZ_PV"
                ,"SBF_RSB_1_HZ_PV"
                ,"SBF_RSB_2_HZ_PV"
                ,"SBF_RSB_3_HZ_PV"));
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("time","时间",null));
        columns.add(new SteccnTableColumn("SBF_RSC_LIT_INPUT","热水池液位",null));
        columns.add(new SteccnTableColumn("SBF_XFSC_LIT_INPUT","消防水液位",null));
        columns.add(new SteccnTableColumn("SBF_XHSC_LIT_INPUT","冷水池液位",null));
        columns.add(new SteccnTableColumn("SBF_RSG_PIT_INPUT","热水管出口压力",null));
        columns.add(new SteccnTableColumn("SBF_XHSG_OUT_PIT_INPUT","循环水管出口压力",null));
        columns.add(new SteccnTableColumn("SBF_XHSCK_TEMP_INPUT","循环水管出口温度",null));
        columns.add(new SteccnTableColumn("SBF_LQT_OUT_TEMP_INPUT","冷却塔进口温度",null));
        columns.add(new SteccnTableColumn("SBF_LQT_IN_TEMP_INPUT","冷却塔出口温度",null));
        columns.add(new SteccnTableColumn("SBF_AODOOR_TEMP_INPUT","室外温度",null));
        columns.add(new SteccnTableColumn("SBF_ST_LIT_INPUT","水塔水位",null));
        columns.add(new SteccnTableColumn("SBF_ZXSB_1_HZ_PV","循环水泵1频率",null));
        columns.add(new SteccnTableColumn("SBF_ZXSB_2_HZ_PV","循环水泵2频率",null));
        columns.add(new SteccnTableColumn("SBF_ZXSB_3_HZ_PV","循环水泵3频率",null));
        columns.add(new SteccnTableColumn("SBF_ZXSB_4_HZ_PV","循环水泵4频率",null));
        columns.add(new SteccnTableColumn("SBF_RSB_1_HZ_PV","热水泵1频率",null));
        columns.add(new SteccnTableColumn("SBF_RSB_2_HZ_PV","热水泵2频率",null));
        columns.add(new SteccnTableColumn("SBF_RSB_3_HZ_PV","热水泵3频率",null));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    @Override
    public void waterPumpDownload(LocalDate date, HttpServletResponse response) throws IOException {
        SteccnTableDataDTO<Map<String, String>> recordDto = waterPump(date);
        List<SteccnTableColumn> columns = recordDto.getColumns();
        List<Map<String, String>> tableList = recordDto.getTableList();
        Assert.notEmpty(tableList,"暂无数据");
        //写表头
        List<List<String>> headList = new ArrayList<>();
        columns.forEach(h ->{
            headList.add(Lists.newArrayList(h.getLabel()));
        });
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("水泵房报表", "UTF-8");
        fileName += LocalDateUtil.localDateFormat(LocalDate.now(), "yyyy-MM-dd");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        //数据
        List<List<Object>> dataList = Lists.newArrayList();
        tableList.forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            columns.forEach(h ->{
                String value = ele.get(h.getProp());
                try {
                    BigDecimal valueBd = BigDecimalUtil.valueOf(value);
                    dataEle.add(valueBd);
                } catch (Exception e) {
                    dataEle.add(value);
                }
            });
            dataList.add(dataEle);
        });
        int[] mergeColumnIndex = {0};
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(0, "水泵房报表")
                .head(headList)
                .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new HeadContentStyleStrategy())
                .registerWriteHandler(new ExcelFillCellMergeStrategy(1, mergeColumnIndex))
                .build();
        excelWriter.write(dataList, writeSheet);
        excelWriter.finish();
        response.flushBuffer();

    }

    protected List<Map<String,String>> postDataCollect(LocalDate date, List<String> points){
        List<Map<String, String>> result = Lists.newArrayList();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("date",date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        jsonObject.put("dpNames",points);
        String res = HttpUtil.post("http://192.168.18.241:9001/getReportData", jsonObject.toJSONString());
        JSONObject resultObj = JSONObject.parseObject(res);
        if(Objects.isNull(resultObj)){
            return result;
        }
        if(!resultObj.getInteger("errCode").equals(0)){
            log.error(resultObj.getString("errMsg"));
            return result;
        }
        Map<String,List<DataCollectDTO>> mapResult = Maps.newHashMap();
        JSONArray jsonArray = resultObj.getJSONArray("datalist");
        for(int i = 0; i < jsonArray.size(); i++){
            JSONObject jsonObjectTmp = jsonArray.getJSONObject(i);
            if(jsonObjectTmp.getString("errCode")==null){
                List<DataCollectDTO> dataTmp = jsonObjectTmp.getJSONArray("datas").toJavaList(DataCollectDTO.class);
                mapResult.put(jsonObjectTmp.getString("code"),dataTmp);
            }
        }
        //组数据
        //切割一天的时间
        List<LocalDateTime> hour = ShardTimeUtil.cutDateRangeNew("H", date.atTime(8, 0, 0), date.plusDays(1).atTime(7, 0, 0));
        for(LocalDateTime timeTmp:hour){
            Map<String,String> resultTmp = Maps.newHashMap();
            resultTmp.put("time",timeTmp.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            for(String point:points){
                List<DataCollectDTO> dataTmp = mapResult.get(point);
                if(CollUtil.isNotEmpty(dataTmp)){
                    BigDecimal valueTmp = dataTmp.stream().filter(v -> v.getTimestamp().equals(timeTmp)).findFirst().orElse(new DataCollectDTO()).getValue();
                    if(Objects.nonNull(valueTmp)){
                        resultTmp.put(point,valueTmp.setScale(2, RoundingMode.HALF_UP).toString());
                    }else{
                        resultTmp.put(point,null);
                    }
                }
            }
            result.add(resultTmp);
        }
        return result;
    }
}
