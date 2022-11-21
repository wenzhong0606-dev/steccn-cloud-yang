package com.steccn.qms.util;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.WriteContext;
import com.alibaba.excel.metadata.data.ClientAnchorData;
import com.alibaba.excel.util.FileTypeUtils;
import com.alibaba.excel.util.StyleUtil;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.steccn.qms.constant.QtManagerConstant;
import org.apache.poi.ss.usermodel.*;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * 下载模版工具类
 *
 * @author AndrewLuo
 * @date 2022/11/18
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
                BASE64Decoder decoder = new BASE64Decoder();
                byte[] bytes = decoder.decodeBuffer(base64.replace("data:image/png;base64,",""));
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
}
