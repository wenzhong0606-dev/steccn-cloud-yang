package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.dto.SteccnTableColumn;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.pms.dto.DataCollectDTO;
import com.steccn.pms.service.PmsDataCollectReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
                ,"ATY_INPUT"));
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
                        ,new SteccnTableColumn("ATY_INPUT","氨逃逸(ppm)",null))));
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
        return null;
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
            if(jsonObjectTmp.getString("errorCode")==null){
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
