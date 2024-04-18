package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc 查询化石燃料设置分页列表-请求对象
 */
@Data
public class CeFossilFuelSettingPageDTO extends BasePageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 燃料名称
     */
    private String materialName;

}
