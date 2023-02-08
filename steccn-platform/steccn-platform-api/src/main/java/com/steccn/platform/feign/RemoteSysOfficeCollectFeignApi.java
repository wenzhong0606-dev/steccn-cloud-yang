package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.SysOfficeCollectVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * 类说明
 *
 * @author yequan
 * @desc 采集点编码配置
 */
@FeignClient(contextId = "remoteSysOfficeCollectFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteSysOfficeCollectFeignApi {

    /**
     * 查询进出站的采集点编码配置
     *
     * @param type 进站0 出站1
     * @return
     */
    @GetMapping("/feign/platform/sys/office/collect/station")
    @ApiOperation("查询工位的进出站的采集点编码配置")
    R<List<SysOfficeCollectVO>> listOnlyStation(@RequestParam("type") Integer type);

    @GetMapping("/feign/platform/sys/office/collect/station/out")
    @ApiOperation("获取工位的出站采集点")
    R<SysOfficeCollectVO> outCollect(@RequestParam("stationId") Integer stationId);
}
