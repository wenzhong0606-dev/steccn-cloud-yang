package com.steccn.qms.excel.strategy;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.util.List;

/**
 * @author yequan
 * @desc 表头样式和内容样式
 * @date 2023/11/14
 */
public class HeadContentStyleStrategy extends HorizontalCellStyleStrategy {

    /**
     * 样式构造函数
     */
    public HeadContentStyleStrategy() {
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 标题字体大小
        WriteFont contentWriteFont = new WriteFont();
        contentWriteFont.setFontHeightInPoints((short) 14);
        contentWriteFont.setColor(IndexedColors.WHITE.getIndex());
        headWriteCellStyle.setWriteFont(contentWriteFont);
        //标题黄色底纹
        headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        headWriteCellStyle.setWrapped(true);
        contentWriteCellStyle.setBorderBottom(BorderStyle.THIN);
        contentWriteCellStyle.setBorderLeft(BorderStyle.THIN);
        contentWriteCellStyle.setBorderRight(BorderStyle.THIN);
        contentWriteCellStyle.setBorderTop(BorderStyle.THIN);
        // 水平居中
        contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        // 垂直居中
        contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        // 设置自动换行，前提内容中需要加「\n」才有效
        contentWriteCellStyle.setWrapped(true);
        setHeadWriteCellStyle(headWriteCellStyle);
        List<WriteCellStyle> contentWriteCellStyleList = Lists.newArrayList();
        contentWriteCellStyleList.add(contentWriteCellStyle);
        setContentWriteCellStyleList(contentWriteCellStyleList);
    }
}
