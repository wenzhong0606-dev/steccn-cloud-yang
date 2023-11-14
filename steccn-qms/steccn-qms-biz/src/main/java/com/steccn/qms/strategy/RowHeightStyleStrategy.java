package com.steccn.qms.strategy;

import com.alibaba.excel.write.style.row.AbstractRowHeightStyleStrategy;
import org.apache.poi.ss.usermodel.Row;

/**
 * @author yequan
 * @desc 行高策略
 * @date 2023/11/14
 */
public class RowHeightStyleStrategy extends AbstractRowHeightStyleStrategy {

    float headHeight;
    float contentHeight;

    public RowHeightStyleStrategy(float headHeight , float contentHeight) {
        this.headHeight = headHeight;
        this.contentHeight = contentHeight;
    }

    protected void setHeadColumnHeight(Row row, int relativeRowIndex) {
        // 设置 头 行高
        row.setHeightInPoints((float) headHeight);
    }

    protected void setContentColumnHeight(Row row, int relativeRowIndex) {
        // 设置 内容 行高
        row.setHeightInPoints((float) contentHeight);
    }
}