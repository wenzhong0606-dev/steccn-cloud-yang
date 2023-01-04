package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
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
     * 获取根据ref内容提供获取映射配置
     *
     * @param tupleDTO 类型，映射数据集合
     * @return r对象
     */
    @ApiOperation(value = "获取根据ref内容提供获取映射配置")
    @PostMapping("/feign/platform/sys/conf/ref")
    R<Map<String, String>> sysConfRef(TupleDTO<String, Set<Integer>> tupleDTO);
}
