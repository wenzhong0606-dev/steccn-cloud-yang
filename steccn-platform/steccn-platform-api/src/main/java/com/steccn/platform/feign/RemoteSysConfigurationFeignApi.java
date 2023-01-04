package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @desc 配置文件feign接口
 */
@FeignClient(contextId = "remoteSysConfigurationFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteSysConfigurationFeignApi {

    /**
     * 获取产线的映射配置
     *
     * @param lineSet 产线集合
     * @return r对象
     */
    @ApiOperation(value = "获取产线的映射配置")
    @PostMapping("/feign/platform/sys/conf/line")
    R<Map<Integer, String>> sysConfLine(Set<Integer> lineSet);
}
