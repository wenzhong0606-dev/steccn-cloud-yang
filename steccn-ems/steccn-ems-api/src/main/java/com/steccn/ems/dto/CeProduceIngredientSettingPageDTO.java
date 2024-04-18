package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 查询生产原料设置分页列表-请求对象
 */
@Data
public class CeProduceIngredientSettingPageDTO extends BasePageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 原料名称
     */
    private String materialName;

}
