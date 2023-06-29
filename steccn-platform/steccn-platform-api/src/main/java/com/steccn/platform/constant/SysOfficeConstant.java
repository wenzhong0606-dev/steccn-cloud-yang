package com.steccn.platform.constant;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @author yequan
 */
@ApiModel("组织机构常量")
public interface SysOfficeConstant {

    /**
     * 仓库货架的类型
     */
    String WAREHOUSE_TYPE = "12";
    String SHELF_TYPE = "5";

    /**
     * 湖北博华缺陷采样数据
     */
    String HBBH_DEFECTIVE_SAMPLE_LINE = "HBBH_DEFECTIVE_SAMPLE_LINE";


    /**
     * 根节点
     */
    Integer ROOT_LEVEL = 1;

    /**
     * 状态正常
     */
    String normal = "0";


    /**
     * 车间 产线 工位 客户 供应商
     */
    String WORKSHOP = "8";
    String PRODUCT_LINE = "9";
    String STATION = "13";
    String CUSTOMER = "7";
    String SUPPLIER = "11";

    /**
     * 省级 市级公司 后勤部门 生产部门 岗位 工段 部门
     */
    String PROVINCIAL_COMPANY = "1";
    String MUNICIPAL_COMPANY = "2";
    String LOGISTICS_DEPARTMENT = "3";
    String PRODUCT_DEPARTMENT = "4";
    String POST = "6";
    String SECTION = "10";
    String DEPARTMENT = "15";


    /**
     * 进站0  出站1
     */
    @Getter
    @AllArgsConstructor
    enum ARRIVALS_OR_OUTBOUND {
        //远传仪表
        ARRIVALS(0, "进站"),
        //非远传仪表
        OUTBOUND(1, "出站");
        private Integer code;
        private String name;

        /**
         * 根据
         *
         * @param code
         * @return
         */
        public static ARRIVALS_OR_OUTBOUND getInstrumentType(Integer code) {
            for (ARRIVALS_OR_OUTBOUND value : ARRIVALS_OR_OUTBOUND.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }
}
