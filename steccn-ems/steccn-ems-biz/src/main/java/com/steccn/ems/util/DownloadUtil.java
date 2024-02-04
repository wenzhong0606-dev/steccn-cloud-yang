package com.steccn.ems.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.WriteContext;
import com.alibaba.excel.enums.WriteDirectionEnum;
import com.alibaba.excel.metadata.data.ClientAnchorData;
import com.alibaba.excel.util.FileTypeUtils;
import com.alibaba.excel.util.StyleUtil;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;
import com.steccn.ems.dto.EpDownloadStrDTO;
import com.steccn.ems.vo.ConsumePartitionContrastTableVO;
import com.steccn.ems.vo.EpConsumeDetailRowVO;
import com.steccn.ems.vo.EpConsumeDetailVO;
import com.steccn.qms.constant.QtManagerConstant;
import org.apache.poi.ss.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/12/08
 */
public class DownloadUtil {
    /**
     * 对下载进行封装
     * @param response 响应
     * @param modelName 服务器上的模版名字
     * @param downloadName 下载文件的名字
     * @param fixMap 固定值的map
     * @param dataList 数据list
     * @param base64 图片base64编码
     * @throws IOException
     */
    public static void downloadTemplate(HttpServletResponse response, String modelName, String downloadName, Map<String, Object> fixMap, List<?> dataList, String base64) throws IOException {
        //1、从服务器获取模板数据
        InputStream modelIn = DownloadModelUtil.getModelInputStream(modelName);
        //2、设置下载头
        setDownloadHeader(response,downloadName);
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).autoCloseStream(Boolean.FALSE).withTemplate(modelIn).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            // 这里注意 入参用了forceNewRow 代表在写入list的时候不管list下面有没有空行 都会创建一行，然后下面的数据往后移动。默认 是false，会直接使用下一行，如果没有则创建。
            // forceNewRow 如果设置了true,有个缺点 就是他会把所有的数据都放到内存了，所以慎用
            // 简单的说 如果你的模板有list,且list不是最后一行，下面还有数据需要填充 就必须设置 forceNewRow=true 但是这个就会把所有数据放到内存 会很耗内存
            // 如果数据量大 list不是最后一行 参照下一个
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.FALSE).build();
            excelWriter.fill(dataList, fillConfig, writeSheet);
            if(StrUtil.isNotBlank(base64)){
                //BASE64Decoder decoder = new BASE64Decoder();
                //byte[] bytes = decoder.decodeBuffer(base64.replace("data:image/png;base64,",""));
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] bytes = decoder.decode(base64.replace("data:image/png;base64,",""));
                for (int i = 0; i < bytes.length; ++i) {
                    if (bytes[i] < 0) {
                        // 调整异常数据
                        bytes[i] += 1024;
                    }
                }
                if (bytes != null) {
                    //拿到context数据
                    WriteContext writeContext = excelWriter.writeContext();
                    Workbook workbook = writeContext.writeWorkbookHolder().getWorkbook();
                    Sheet sheet = writeContext.writeSheetHolder().getCachedSheet();
                    Drawing<?> drawing = sheet.getDrawingPatriarch();
                    if (drawing == null) {
                        drawing = sheet.createDrawingPatriarch();
                    }
                    CreationHelper helper = sheet.getWorkbook().getCreationHelper();
                    int index = workbook.addPicture(bytes,
                            FileTypeUtils.getImageTypeFormat(bytes));
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setDx2(-StyleUtil.getCoordinate(5));
                    anchor.setDx1(StyleUtil.getCoordinate(5));
                    // 图片位置设置代码
                    anchor.setRow1(StyleUtil.getCellCoordinate(0, 0, 7));
                    anchor.setCol1(StyleUtil.getCellCoordinate(0, 0, 0));
                    anchor.setRow2(StyleUtil.getCellCoordinate(0, 0, 10) + 1);
                    anchor.setCol2(StyleUtil.getCellCoordinate(0, 0, 6) + 1);
                    anchor.setAnchorType(ClientAnchorData.AnchorType.MOVE_AND_RESIZE.getValue());
                    drawing.createPicture(anchor, index);
                }
            }
            excelWriter.fill(fixMap, writeSheet);
            // bytes为要导出的图像文件字节数组

            response.getOutputStream().flush();
        }
    }

    /**
     * 下载excel Response封装
     * @param response
     * @return
     */
    public static void setDownloadHeader(HttpServletResponse response,String name) throws UnsupportedEncodingException {
        response.setContentType(QtManagerConstant.QT_CHART_EXCEL_CONTENT_TYPE);
        response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode(name, QtManagerConstant.QT_CHART_EXCEL_ENCODE).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }

    /**
     * 对能耗分区进行专门封装
     * @param response 响应
     * @param modelName 服务器上的模版名字
     * @param downloadName 下载文件的名字
     * @param fixMap 固定值的map
     * @param res 数据list
     * @param base64 图片base64编码
     * @throws IOException
     */
    public static void downloadEnergyTemplate(HttpServletResponse response, String modelName, String downloadName, Map<String, Object> fixMap, EpConsumeDetailVO res, String base64) throws IOException {
        //1、从服务器获取模板数据
        InputStream modelIn = DownloadModelUtil.getModelInputStream(modelName);
        //2、设置下载头
        setDownloadHeader(response,downloadName);
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).autoCloseStream(Boolean.FALSE).withTemplate(modelIn).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            // 这里注意 入参用了forceNewRow 代表在写入list的时候不管list下面有没有空行 都会创建一行，然后下面的数据往后移动。默认 是false，会直接使用下一行，如果没有则创建。
            // forceNewRow 如果设置了true,有个缺点 就是他会把所有的数据都放到内存了，所以慎用
            // 简单的说 如果你的模板有list,且list不是最后一行，下面还有数据需要填充 就必须设置 forceNewRow=true 但是这个就会把所有数据放到内存 会很耗内存
            // 如果数据量大 list不是最后一行 参照下一个
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.FALSE).build();
            FillConfig fillConfigHORIZONTAL = FillConfig.builder().direction(WriteDirectionEnum.HORIZONTAL).build();
            List<EpConsumeDetailRowVO> records = res.getDataList().getRecords();
            excelWriter.fill(new FillWrapper("commonData",records), fillConfig, writeSheet);
            //需要对listString数据处理才可被填充
            List<EpDownloadStrDTO> headData = new ArrayList<>();
            res.getHeadData().forEach(tmp->{
                EpDownloadStrDTO strTmp = new EpDownloadStrDTO(tmp);
                headData.add(strTmp);
            });
            excelWriter.fill(new FillWrapper("headData",headData),fillConfigHORIZONTAL,writeSheet);
            for(int i = 0; i < records.size(); i++){
                //需要对listString数据进行处理才可被填充
                List<EpDownloadStrDTO> data = new ArrayList<>();
                records.get(i).getConsumeList().forEach(tmp -> {
                   EpDownloadStrDTO strTmp = new EpDownloadStrDTO(tmp);
                   data.add(strTmp);
                });
                excelWriter.fill(new FillWrapper("data" + i,data),fillConfigHORIZONTAL,writeSheet);
            }
            if(StrUtil.isNotBlank(base64)){
                //BASE64Decoder decoder = new BASE64Decoder();
                //byte[] bytes = decoder.decodeBuffer(base64.replace("data:image/png;base64,",""));
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] bytes = decoder.decode(base64.replace("data:image/png;base64,",""));
                for (int i = 0; i < bytes.length; ++i) {
                    if (bytes[i] < 0) {
                        // 调整异常数据
                        bytes[i] += 1024;
                    }
                }
                if (bytes != null) {
                    //拿到context数据
                    WriteContext writeContext = excelWriter.writeContext();
                    Workbook workbook = writeContext.writeWorkbookHolder().getWorkbook();
                    Sheet sheet = writeContext.writeSheetHolder().getCachedSheet();
                    Drawing<?> drawing = sheet.getDrawingPatriarch();
                    if (drawing == null) {
                        drawing = sheet.createDrawingPatriarch();
                    }
                    CreationHelper helper = sheet.getWorkbook().getCreationHelper();
                    int index = workbook.addPicture(bytes,
                            FileTypeUtils.getImageTypeFormat(bytes));
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setDx2(-StyleUtil.getCoordinate(5));
                    anchor.setDx1(StyleUtil.getCoordinate(5));
                    // 图片位置设置代码
                    anchor.setRow1(StyleUtil.getCellCoordinate(0, 0, 7));
                    anchor.setCol1(StyleUtil.getCellCoordinate(0, 0, 0));
                    anchor.setRow2(StyleUtil.getCellCoordinate(0, 0, 10) + 1);
                    anchor.setCol2(StyleUtil.getCellCoordinate(0, 0, 6) + 1);
                    anchor.setAnchorType(ClientAnchorData.AnchorType.MOVE_AND_RESIZE.getValue());
                    drawing.createPicture(anchor, index);
                }
            }
            excelWriter.fill(fixMap, writeSheet);
            // bytes为要导出的图像文件字节数组

            response.getOutputStream().flush();
        }
    }

    /**
     * 对能耗分区对比特殊封装
     * @param response 响应
     * @param modelName 服务器上的模版名字
     * @param downloadName 下载文件的名字
     * @param fixMap 固定值的map
     * @param res 数据list
     * @param base64 图片base64编码
     * @throws IOException
     */
    public static void downloadEnergyTemplate2(HttpServletResponse response, String modelName, String downloadName, Map<String, Object> fixMap, EpConsumeDetailVO res, String base64, ConsumePartitionContrastTableVO data2) throws IOException {
        //1、从服务器获取模板数据
        InputStream modelIn = DownloadModelUtil.getModelInputStream(modelName);
        //2、设置下载头
        setDownloadHeader(response,downloadName);
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).autoCloseStream(Boolean.FALSE).withTemplate(modelIn).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            // 这里注意 入参用了forceNewRow 代表在写入list的时候不管list下面有没有空行 都会创建一行，然后下面的数据往后移动。默认 是false，会直接使用下一行，如果没有则创建。
            // forceNewRow 如果设置了true,有个缺点 就是他会把所有的数据都放到内存了，所以慎用
            // 简单的说 如果你的模板有list,且list不是最后一行，下面还有数据需要填充 就必须设置 forceNewRow=true 但是这个就会把所有数据放到内存 会很耗内存
            // 如果数据量大 list不是最后一行 参照下一个
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
            FillConfig fillConfig2 = FillConfig.builder().forceNewRow(Boolean.FALSE).build();
            FillConfig fillConfigHORIZONTAL = FillConfig.builder().direction(WriteDirectionEnum.HORIZONTAL).build();
            //第一个表格数据填充
            List<EpConsumeDetailRowVO> records = res.getDataList().getRecords();
            excelWriter.fill(new FillWrapper("commonData",records), fillConfig, writeSheet);
            //需要对listString数据处理才可被填充
            List<EpDownloadStrDTO> headData = new ArrayList<>();
            res.getHeadData().forEach(tmp->{
                EpDownloadStrDTO strTmp = new EpDownloadStrDTO(tmp);
                headData.add(strTmp);
            });
            excelWriter.fill(new FillWrapper("headData",headData),fillConfigHORIZONTAL,writeSheet);
            //行变列进行渲染
            for(int i = 0; i < records.get(i).getConsumeList().size(); i++) {
                List<EpDownloadStrDTO> data = new ArrayList<>();
                for(int j = 0; j < records.size(); j++){
                    EpDownloadStrDTO strTmp = new EpDownloadStrDTO(records.get(j).getConsumeList().get(i));
                    data.add(strTmp);
                }
                excelWriter.fill(new FillWrapper("data" + i,data),fillConfig2,writeSheet);
            }
            //第二个表格数据填充

            data2.getRowList().forEach(tmp->{
                if(Objects.nonNull(tmp.getSumValue())){
                    tmp.setSumValueStr(tmp.getSumValue().toString());
                }
                if(Objects.nonNull(tmp.getMinValue())){
                    tmp.setMinValueStr(tmp.getMinValue().toString());
                }
                if(Objects.nonNull(tmp.getMaxValue())){
                    tmp.setMaxValueStr(tmp.getMaxValue().toString());
                }
                if(Objects.nonNull(tmp.getAvgValue())){
                    tmp.setAvgValueStr(tmp.getAvgValue().toString());
                }
            });
            excelWriter.fill(new FillWrapper("commonData2",data2.getRowList()), fillConfig2, writeSheet);
            //需要对listString数据处理才可被填充
            List<EpDownloadStrDTO> headData2 = new ArrayList<>();
            data2.getDateStrList().forEach(tmp->{
                EpDownloadStrDTO strTmp = new EpDownloadStrDTO(tmp);
                headData2.add(strTmp);
            });
            //横向填充
            excelWriter.fill(new FillWrapper("headData2",headData2),fillConfigHORIZONTAL,writeSheet);
//            //行变列进行渲染
            for(int i = 0; i < data2.getDateStrList().size(); i++) {
                List<EpDownloadStrDTO> line = new ArrayList<>();
                for(int j = 0; j < data2.getRowList().size(); j++){
                    EpDownloadStrDTO strTmp = new EpDownloadStrDTO( Objects.isNull(data2.getRowList().get(j).getValueList().get(i)) ? null :data2.getRowList().get(j).getValueList().get(i).toString());
                    line.add(strTmp);
                }
                excelWriter.fill(new FillWrapper("line" + i,line),fillConfig2,writeSheet);
            }
            if(StrUtil.isNotBlank(base64)){
                //BASE64Decoder decoder = new BASE64Decoder();
                //byte[] bytes = decoder.decodeBuffer(base64.replace("data:image/png;base64,",""));
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] bytes = decoder.decode(base64.replace("data:image/png;base64,",""));
                for (int i = 0; i < bytes.length; ++i) {
                    if (bytes[i] < 0) {
                        // 调整异常数据
                        bytes[i] += 1024;
                    }
                }
                if (bytes != null) {
                    //拿到context数据
                    WriteContext writeContext = excelWriter.writeContext();
                    Workbook workbook = writeContext.writeWorkbookHolder().getWorkbook();
                    Sheet sheet = writeContext.writeSheetHolder().getCachedSheet();
                    Drawing<?> drawing = sheet.getDrawingPatriarch();
                    if (drawing == null) {
                        drawing = sheet.createDrawingPatriarch();
                    }
                    CreationHelper helper = sheet.getWorkbook().getCreationHelper();
                    int index = workbook.addPicture(bytes,
                            FileTypeUtils.getImageTypeFormat(bytes));
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setDx2(-StyleUtil.getCoordinate(5));
                    anchor.setDx1(StyleUtil.getCoordinate(5));
                    // 图片位置设置代码
                    anchor.setRow1(StyleUtil.getCellCoordinate(0, 0, 7));
                    anchor.setCol1(StyleUtil.getCellCoordinate(0, 0, 0));
                    anchor.setRow2(StyleUtil.getCellCoordinate(0, 0, 10) + 1);
                    anchor.setCol2(StyleUtil.getCellCoordinate(0, 0, 6) + 1);
                    anchor.setAnchorType(ClientAnchorData.AnchorType.MOVE_AND_RESIZE.getValue());
                    drawing.createPicture(anchor, index);
                }
            }
            excelWriter.fill(fixMap, writeSheet);
            // bytes为要导出的图像文件字节数组

            response.getOutputStream().flush();
        }
    }
}
