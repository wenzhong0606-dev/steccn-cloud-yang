package com.steccn.qms.strategy;

import com.alibaba.excel.write.style.row.AbstractRowHeightStyleStrategy;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import java.util.Iterator;

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
//        row.setHeightInPoints((float) contentHeight);
        Iterator<Cell> cellIterator = row.cellIterator();
        if (!cellIterator.hasNext()) {
            return;
        }
        // 默认为 1行高度
        int maxHeight = 1;
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue();
                int len = value.length();
                int num = 0;
                if (len > 50) {
                    num = len % 50 > 0 ? len / 50 : len / 2 - 1;
                }
                if (num > 0) {
                    for (int i = 0; i < num; i++) {
                        value = value.substring(0, (i + 1) * 50 + i) + "\n" + value.substring((i + 1) * 50 + i, len + i);
                    }
                }
                if (value.contains("\n")) {
                    int length = value.split("\n").length;
                    maxHeight = Math.max(maxHeight, length / 2) + 1;
                }
            }
        }
        row.setHeightInPoints((short) ((maxHeight) * contentHeight));
    }
}