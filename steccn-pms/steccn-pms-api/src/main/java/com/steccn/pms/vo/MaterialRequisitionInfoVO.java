package com.steccn.pms.vo;

import com.steccn.pms.dto.MaterialRequisitionInfoDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 领料单详情VO
 *
 * @author Mongknife
 * @date 2022/11/2
 */
@Data
@ApiModel("领料单详情VO")
public class MaterialRequisitionInfoVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("关联单号")
    private String associatedNo;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("机构id")
    private List<Integer> organization;

    @ApiModelProperty("机构名称")
    private List<String> organizationName;

    @ApiModelProperty("需求数量")
    private BigDecimal quantity;

    @ApiModelProperty("领用数量")
    private BigDecimal receiveQuantity;

    @ApiModelProperty("需求时间")
    private String requireDate;

    @ApiModelProperty("货架id")
    private Integer shelfId;

    @ApiModelProperty("货架code")
    private String shelfCode;

    @ApiModelProperty("货架名称")
    private String shelfName;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位数量")
    private String unitName;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;


    @ApiModelProperty("需求人")
    private List<MaterialRequisitionInfoDemanderVO> demanderList;
}
