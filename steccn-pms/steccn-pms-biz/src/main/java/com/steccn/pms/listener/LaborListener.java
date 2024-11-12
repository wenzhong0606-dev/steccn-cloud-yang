package com.steccn.pms.listener;

import cn.hutool.core.lang.Assert;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.domain.PmsLaborProtectionStandardDetail;
import com.steccn.pms.service.PmsLaborProtectionStandardDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.alibaba.excel.enums.CellDataTypeEnum;

@Slf4j
@RequiredArgsConstructor
public class LaborListener extends AnalysisEventListener<Map<Integer, String>> {

    /**
     * 每隔5条存储数据库，实际使用中可以100条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;
    private List<Map<String, String>> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    private Map<String, Integer> headerMap;

    private final PmsLaborProtectionStandardDetailService protectionStandardDetailService;
    private final RemoteSpareFeignApi remoteSpareFeignApi;

    private   Map<String,List<Map<String, String>>> excelDataMap = Maps.newHashMap();

    /**
     * 读取表头时调用
     */
    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        headerMap = processHeader(headMap);
        log.info("表头解析完成: {}", JSON.toJSONString(headerMap));
    }

    /**
     * 这个每一条数据解析都会来调用
     */
    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex();
        log.info("处理第 {} 行数据: {}", rowIndex, JSON.toJSONString(data));

        if (headerMap != null && data != null) { // 确保 headerMap 和 data 不为空
            Map<String, String> rowData = processData(data, headerMap);
            if (rowData != null) {
                log.info("解析到一条数据:{}", JSON.toJSONString(rowData));
                cachedDataList.add(rowData);
                if (cachedDataList.size() >= BATCH_COUNT) {
                    cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
                }
            }
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!cachedDataList.isEmpty()) {
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

    private Map<String, Integer> processHeader(Map<Integer, ReadCellData<?>> headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();
        for (Map.Entry<Integer, ReadCellData<?>> entry : headerRow.entrySet()) {
            ReadCellData<?> cellData = entry.getValue();
            if (cellData.getType() == CellDataTypeEnum.STRING) {
                String header = cellData.getStringValue();
                int columnIndex = entry.getKey();
                headerMap.put(header, columnIndex);
            }
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
}