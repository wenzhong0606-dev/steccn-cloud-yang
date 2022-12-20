package com.steccn.common.constant;

/**
 * feign的服务信息常量
 *
 * @author Adolph.Ye
 * @date 2022/7/7
 */
public interface FeignConstant {

    /**
     * 统一门户的服务名称
     */
    String STECCN_PORTAL_SERVICE_NAME = "steccn-portal";

    /**
     * 统一门户的服务名称
     */
    String STECCN_OA_SERVICE_NAME = "steccn-oa";

    /**
     * 认证服务
     */
    String STECCN_AUTH_SERVER = "auth-server";

    /**
     * 通用服务名称
     */
    String STECCN_PLATFORM_SERVICE_NAME = "steccn-platform";

    /**
     * 文件服务
     */
    String STECCN_MINIO_SERVICE_NAME = "steccn-minio";

    /**
     * 生产系统服务名称
     */
    String STECCN_PMS_SERVICE_NAME ="steccn-pms";

    /**
     * 质量系统服务名称
     */
    String STECCN_QMS_SERVICE_NAME ="steccn-qms";

    /**
     * 仓储系统服务名称
     */
    String STECCN_WMS_SERVICE_NAME = "steccn-wms";

    /**
     * 数据中台服务名称
     */
    String STECCN_DATA_PLATFORM_SERVICE_NAME = "steccn-data-platform";
}
