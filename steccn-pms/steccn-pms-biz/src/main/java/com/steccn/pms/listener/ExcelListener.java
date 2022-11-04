package com.steccn.pms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import com.steccn.pms.dto.PmsProducePlanExcelDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/4 10:05
 */
@Slf4j
public class ExcelListener extends AnalysisEventListener<PmsProducePlanExcelDTO> {


    List<PmsProducePlanExcelDTO> producePlanExcelDTOList;
    public ExcelListener(List<PmsProducePlanExcelDTO> res) {
        this.producePlanExcelDTOList=res;
    }

    @Override
    public void invoke(PmsProducePlanExcelDTO pmsProducePlanExcelDTO, AnalysisContext analysisContext) {
        //获取行号
        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();
        pmsProducePlanExcelDTO.setRowIndex(rowIndex+1);
        producePlanExcelDTOList.add(pmsProducePlanExcelDTO);
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("数据解析完成！");
    }

}
