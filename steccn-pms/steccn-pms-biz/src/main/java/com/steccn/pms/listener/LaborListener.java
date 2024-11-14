package com.steccn.pms.listener;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.exception.SteccnRuntimeException;
import com.steccn.minio.fegin.RemoteFileFeignApi;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.domain.PmsLaborProtectionStandardDetail;
import com.steccn.pms.service.PmsLaborProtectionStandardDetailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import com.alibaba.excel.enums.CellDataTypeEnum;

@Slf4j
@RequiredArgsConstructor
@Data
public class LaborListener extends AnalysisEventListener<Map<Integer, String>> {


    private static final int BATCH_COUNT = 100;
    private List<Map<String, String>> cachedDataList = new ArrayList<>();
    private Map<String, Integer> headerMap;

    private final PmsLaborProtectionStandardDetailService protectionStandardDetailService;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteFileFeignApi remoteFileFeignApi;
    private List<List<String>> errorRows = new ArrayList<>();
    private Integer errorSize = 0;

    private   Map<String,List<Map<String, String>>> excelDataMap = Maps.newHashMap();
    private String fileName;
    private Set<String> processedNames = new HashSet<>();

    public LaborListener(PmsLaborProtectionStandardDetailService protectionStandardDetailService,
                         RemoteSpareFeignApi remoteSpareFeignApi,RemoteFileFeignApi remoteFileFeignApi, String fileName) {
        this.protectionStandardDetailService = protectionStandardDetailService;
        this.remoteSpareFeignApi = remoteSpareFeignApi;
        this.remoteFileFeignApi = remoteFileFeignApi;
        this.fileName = fileName; // 设置文件名

    }

    /**
     * 这个每一条数据解析都会来调用
     */
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex();
        log.info("处理第 {} 行数据: {}", rowIndex, JSON.toJSONString(data));
        if (rowIndex == 1){
            //处理第二行表头
            headerMap = processHeader(data);
        }else if (headerMap != null && data != null) { // 确保 headerMap 和 data 不为空
            Map<String, String> rowData = processData(data, headerMap);
            if (rowData != null) {
                String errorMessage = validateData(rowData);
                if (errorMessage == null) {
                    log.info("解析到一条数据:{}", JSON.toJSONString(rowData));
                    cachedDataList.add(rowData);
                } else {
                    // 记录错误条数
                    errorSize++;
                }
                List<String> errorRow = new ArrayList<>(data.values());
                errorRow.add(errorMessage);
                errorRows.add(errorRow);
                cachedDataList.add(rowData);

            }
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!cachedDataList.isEmpty()) {
            if (!errorRows.isEmpty()) {
                try {
                    writeErrorFile(errorRows);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            // 处理数据
            List<PmsLaborProtectionStandardDetail> detailData = protectionStandardDetailService.list(new LambdaQueryWrapper<PmsLaborProtectionStandardDetail>()
                    .orderByDesc(PmsLaborProtectionStandardDetail::getStandardId));
            List<Long> materialIdList = detailData.stream().map(PmsLaborProtectionStandardDetail::getMaterialId).distinct().collect(Collectors.toList());
            Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIdList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
             excelDataMap = Maps.newHashMap();
            for (int i = 0; i < cachedDataList.size(); i++) {
                List<Map<String, String>> list = Lists.newArrayList();
                for (int index = 0; index < materialIdList.size() ; index++) {
                    Map<String, String> rowMap = Maps.newHashMap();
                    Map<String, String> stringStringMap = cachedDataList.get(i);

                    EqpSpareVO spareVO = spareMap.get(materialIdList.get(index));
                    StringBuilder materialName = new StringBuilder();
                    materialName.append(spareVO.getName());
                    if(cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getCode())){
                        materialName.append("/");
                        materialName.append(spareVO.getCode());
                    }
                    if(cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getSpecifications())){
                        materialName.append("/");
                        materialName.append(spareVO.getSpecifications());
                    }
                    String s = stringStringMap.get(materialName.toString());
                    rowMap.put(materialIdList.get(index) + "_claimNum", s);
                    list.add(rowMap);

                }
                excelDataMap.put(cachedDataList.get(i).get("姓名"), list);
            }
        }

    }



    private Map<String, Integer> processHeader(Map<Integer,String> headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (Map.Entry<Integer, String> entry : headerRow.entrySet()) {
            String header = entry.getValue();
            int columnIndex = entry.getKey();
            headerMap.put(header, columnIndex);
        }
//         验证表头是否包含所有必要的字段
        List<PmsLaborProtectionStandardDetail> detailData = protectionStandardDetailService.list(new LambdaQueryWrapper<PmsLaborProtectionStandardDetail>()
                .orderByDesc(PmsLaborProtectionStandardDetail::getStandardId));
        List<Long> materialIdList = detailData.stream().map(PmsLaborProtectionStandardDetail::getMaterialId).distinct().collect(Collectors.toList());
        Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIdList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<String> head = new HashSet<>();
        head.add("姓名");
        materialIdList.forEach(tmp->{
            EqpSpareVO spareVO = spareMap.get(tmp);
            StringBuilder materialName = new StringBuilder();
            materialName.append(spareVO.getName());
            if(cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getCode())){
                materialName.append("/");
                materialName.append(spareVO.getCode());
            }
            if(cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getSpecifications())){
                materialName.append("/");
                materialName.append(spareVO.getSpecifications());
            }
            head.add(materialName.toString());
        });
        if (!headerMap.keySet().containsAll(head)) {
            Assert.isTrue(Boolean.FALSE,"表头不完整，缺少以下字段: {}", head.stream().filter(h -> !headerMap.containsKey(h)).collect(Collectors.joining(", ")));
        }
        return headerMap;
    }

    private Map<String, String> processData(Map<Integer, String> data, Map<String, Integer> headerMap) {
        Map<String, String> rowData = new HashMap<>();
        for (String header : headerMap.keySet()) {
            int columnIndex = headerMap.get(header);
            String cellData = data.get(columnIndex);
            rowData.put(header, cellData);
        }
        return rowData;
    }

    public Map<String,List<Map<String, String>>>  saveData() {
        return excelDataMap;
    }

    private String validateData(Map<String, String> rowData) {
        String name = rowData.get("姓名");
        if (name == null || name.trim().isEmpty()) {
            return "姓名不能为空";
        }else if (name.length() < 2 || name.length() > 4){
            return "姓名通常由2到4个汉字组成,长度不符合要求";
        }
        // 检查每个字符是否为中文字符
        for (char c : name.toCharArray()) {
            if (!Character.UnicodeBlock.of(c).equals(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)) {
                return "名字不允许包含数字、字母、特殊符号等非中文字符";
            }
        }
        // 检查名字是否重复
        if (!processedNames.add(name)) {
            return "姓名重复";
        }

        for (String key : rowData.keySet()) {
            if (!"姓名".equals(key)) {
                String value = rowData.get(key);
                if (Objects.nonNull(value)) {
                    try {

                        double number = Double.parseDouble(value);
                        if (number <= 0) {
                            return key + " 的待领必须大于零";
                        }
                    } catch (NumberFormatException e) {
                        return key + " 的待领数量必须是数字";
                    }
                }
            }
        }

        return null;
    }


    public File writeErrorFile(List<List<String>> errorRows) throws IOException {

        //获取表头数据
        List<PmsLaborProtectionStandardDetail> detailData = protectionStandardDetailService.list(new LambdaQueryWrapper<PmsLaborProtectionStandardDetail>()
                .orderByDesc(PmsLaborProtectionStandardDetail::getStandardId));
        List<Long> materialIdList = detailData.stream().map(PmsLaborProtectionStandardDetail::getMaterialId).distinct().collect(Collectors.toList());
        Map<Long, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIdList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<List<String>> head = new ArrayList<List<String>>();
        head.add(Arrays.asList("填写须知：\n" +
                "\n" +
                "1. 导入文件的表头必须和模板一致\n" +
                "\n" +
                "2. 导入列表不能为空，至少填写一个人员\n" +
                "3. 每个人员的姓名必须完全正确\n" +
                "4. 对于系统内有重名的人员，暂不支持导入，建议页面上单独添加\n" +
                "5. 无需领用的劳保用品可不填写待领数量\n" +
                "\n" +
                "6. 出现以下错误时请相应更正后重新上传：\n" +
                "\n" +
                "1）姓名不能为空\n" +
                "\n" +
                "2）待领数量必须为数字\n" +
                "\n" +
                "3）待领数量必须大于0\n" +
                "\n" +
                "4）该人员在系统中不存在\n" +
                "\n" +
                "5）系统中存在重名的人员\n" +
                "6）每个人员只能填写一行","姓名"));
        materialIdList.forEach(tmp -> {

            EqpSpareVO spareVO = spareMap.get(tmp);
            StringBuilder materialName = new StringBuilder();
            materialName.append(spareVO.getName());
            if (cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getCode())) {
                materialName.append("/");
                materialName.append(spareVO.getCode());
            }
            if (cn.hutool.core.util.StrUtil.isNotBlank(spareVO.getSpecifications())) {
                materialName.append("/");
                materialName.append(spareVO.getSpecifications());
            }
            head.add(Arrays.asList("填写须知：\n" +
                    "\n" +
                    "1. 导入文件的表头必须和模板一致\n" +
                    "\n" +
                    "2. 导入列表不能为空，至少填写一个人员\n" +
                    "3. 每个人员的姓名必须完全正确\n" +
                    "4. 对于系统内有重名的人员，暂不支持导入，建议页面上单独添加\n" +
                    "5. 无需领用的劳保用品可不填写待领数量\n" +
                    "\n" +
                    "6. 出现以下错误时请相应更正后重新上传：\n" +
                    "\n" +
                    "1）姓名不能为空\n" +
                    "\n" +
                    "2）待领数量必须为数字\n" +
                    "\n" +
                    "3）待领数量必须大于0\n" +
                    "\n" +
                    "4）该人员在系统中不存在\n" +
                    "\n" +
                    "5）系统中存在重名的人员\n" +
                    "6）每个人员只能填写一行",materialName.toString()));
        });
        // 使用上传的文件名生成错误文件名
        String errorFileName = "错误数据_" + fileName + ".xlsx";
        File tempFile = new File(errorFileName);
        WriteCellStyle headStyle = new WriteCellStyle();
        WriteCellStyle contentStyle = new WriteCellStyle();
        HorizontalCellStyleStrategy horizontalCellStyleStrategy = new HorizontalCellStyleStrategy(headStyle, contentStyle);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            EasyExcel.write(fos, List.class)
                    .registerWriteHandler(horizontalCellStyleStrategy)
                    .sheet("错误数据")
                    .head(head)
                    .doWrite(errorRows);
            return tempFile;
        } catch (IOException e) {
            throw new SteccnRuntimeException("写入错误文件时发生错误: ", e);
        }

    }
}