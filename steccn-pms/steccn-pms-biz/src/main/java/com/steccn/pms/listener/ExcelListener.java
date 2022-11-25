package com.steccn.pms.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.CellExtra;
import com.steccn.pms.dto.PmsProducePlanExcelDTO;
import com.steccn.pms.vo.ProducePlanMsgVO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/4 10:05
 */
@Slf4j
public class ExcelListener extends AnalysisEventListener<PmsProducePlanExcelDTO> {


    List<PmsProducePlanExcelDTO> producePlanExcelDTOList;
    List<ProducePlanMsgVO> producePlanMsgVOList;
    public ExcelListener(List<PmsProducePlanExcelDTO> res, List<ProducePlanMsgVO> producePlanMsgVOList) {
        this.producePlanExcelDTOList=res;
        this.producePlanMsgVOList = producePlanMsgVOList;
    }

    @Override
    public void invoke(PmsProducePlanExcelDTO pmsProducePlanExcelDTO, AnalysisContext analysisContext) {
        //获取行号
        Integer rowIndex = analysisContext.readRowHolder().getRowIndex();
        pmsProducePlanExcelDTO.setRowIndex(rowIndex+1);
        producePlanExcelDTOList.add(pmsProducePlanExcelDTO);
    }

    /**
     * 解析异常获取异常行列的信息
     * @param exception
     * @param context
     * @throws Exception
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("解析失败，但是继续解析下一行:{}", exception.getMessage());
        if (exception instanceof ExcelDataConvertException) {
            ProducePlanMsgVO planMsgVO = new ProducePlanMsgVO();
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException)exception;
            log.error("第{}行，第{}列解析异常，数据为:{}", excelDataConvertException.getRowIndex(),
                    excelDataConvertException.getColumnIndex(), excelDataConvertException.getCellData());

            planMsgVO.setRowIndex(excelDataConvertException.getRowIndex());
            planMsgVO.setErrorMsgList(String.format("第%s列解析异常，数据为:%s",excelDataConvertException.getColumnIndex(),excelDataConvertException.getCellData()));
            producePlanMsgVOList.add(planMsgVO);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        log.info("数据解析完成！");
    }

}
