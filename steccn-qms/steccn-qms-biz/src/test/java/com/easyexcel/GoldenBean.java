package com.easyexcel;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

@Data
public class GoldenBean {

    @Alias("操作者主账号名称")
    private String f1;
    @Alias("操作者姓名")
    private String f2;
    @Alias("操作者组织")
    private String f3;
    @Alias("审批者主账号名称")
    private String f4;
    @Alias("审批者姓名")
    private String f5;
    @Alias("审批者组织")
    private String f6;
}
