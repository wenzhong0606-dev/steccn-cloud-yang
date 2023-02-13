package com.steccn.platform.controller.feign;


import com.steccn.common.vo.R;
import com.steccn.platform.service.SysOfficeCollectService;
import com.steccn.platform.vo.SysOfficeCollectVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * @author yequan
 * @desc 采集点编码
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/sys/office/collect")
public class SysOfficeCollectFeignController {

    private final SysOfficeCollectService sysOfficeCollectService;

    /**
     * 查询工位的进出站的采集点编码配置
     *
     * @param type 进站0 出站1
     * @return
     */
    @GetMapping("/station")
    @ApiOperation("查询工位的进出站的采集点编码配置")
    R<List<SysOfficeCollectVO>> listOnlyStation(@RequestParam("type") Integer type) {
        return R.success(sysOfficeCollectService.listOnlyStation(type));
    }

    @GetMapping("/station/out")
    @ApiOperation("获取工位的出站采集点")
    R<SysOfficeCollectVO> outCollect(@RequestParam("stationId") Integer stationId){
        return R.success(sysOfficeCollectService.outCollect(stationId));
    }
}
