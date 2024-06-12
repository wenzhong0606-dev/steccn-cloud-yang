package com.steccn.sales.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.time.LocalDate;

@Data
@ApiModel("发货对账")
public class KsShippingReconciliationVO {

    private String code; // 物流单号
    private String logisticsCompany; //物流公司
    private String customerName;//客户名称
    private String modelSpecs; // 物料
    private String driverAdds; // 收货地址
    private String driverName; // 司机姓名
    private String carCode; // 车牌
    private LocalDate sendDate; // 计划发货日期
    private LocalDate actualDeliveryDate; // 实际发货日期
    private Integer pieceQty; // 箱数
    private Integer saleQty; // 发货数
}
