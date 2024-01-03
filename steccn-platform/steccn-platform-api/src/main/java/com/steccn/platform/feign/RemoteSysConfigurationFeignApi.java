package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysConfigurationDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
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
    R<Map<String, String>> sysConfRef(@RequestBody TupleDTO<String, Set<Integer>> tupleDTO);

    /**
     * 根据code获取list的配置
     * @param TripleDTO type content
     * @return list
     */
    @ApiOperation(value = "根据code获取list的配置")
    @PostMapping("/feign/platform/sys/conf/list/integer")
    R<Map<String, List<Integer>>> confListInteger(@RequestBody TripleDTO<String, Set<String>, Integer> dto);

    /**
     * 根据code获取list的配置
     * @param tupleDTO type content
     * @return list
     */
    @ApiOperation(value = "根据code获取list的配置")
    @PostMapping("/feign/platform/sys/conf/list")
    R<Map<String, List<SysConfigurationDTO>>> confList(@RequestBody TupleDTO<String,Set<String>> tupleDTO);

    /**
     * 根据code获取list的配置
     *
     * @param TripleDTO type content
     * @return list
     */
    @ApiOperation(value = "根据code获取list的配置")
    @PostMapping("/feign/platform/sys/conf/list/string")
    R<Map<String, List<String>>> confListString(@RequestBody TripleDTO<String, Set<String>,Integer> dto);

    /**
     * 根据type获取content
     * @param type
     * @return
     */
    @ApiOperation(value = "根据code获取list的配置")
    @GetMapping("/feign/platform/sys/conf/contentByType")
    R<Set<String>> contentByType(@RequestParam("type") String type);

    /**
     * 根据type和content获取信息
     * @param dto
     * @return
     */
    @ApiOperation(value = "根据类型和content获取信息")
    @PostMapping("/feign/platform/sys/conf/infoByContentAndType")
    R<SysConfigurationDTO> infoByContentAndType(@RequestBody TupleDTO<String, String> dto);
}
