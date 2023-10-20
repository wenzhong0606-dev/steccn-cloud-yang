package com.steccn.pms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 改变单据状态请求类
 * @date 2023/10/17
 */
@Data
public class ReceiptStatusDTO implements Serializable {

    /**
     * 单据集合
     */
    private List<String> inStorageVoucherList;

    /**
     * 状态
     */
    private Integer state;
}
