package com.steccn.common.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yequan
 * @desc 上海凯盛数据行对象
 * @date 2023/9/7
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("上海凯盛数据行对象")
public class SteccnRow<T,R> {

    /**
     * 固定列
     */
    private T fixRow;

    /**
     * 动态列
     */
    private R dynamicRow;
}
