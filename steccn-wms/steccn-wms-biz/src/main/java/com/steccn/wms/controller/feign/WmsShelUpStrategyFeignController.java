package com.steccn.wms.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsShelfUpStrategy;
import com.steccn.wms.service.WmsShelfUpStrategyService;
import com.steccn.wms.vo.WmsMaterialShelfUpVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/20
 */
@RestController
@Api("上架策略feign")
@RequestMapping("/feign/wms/shelfUp/strategy")
@RequiredArgsConstructor
@Slf4j
public class WmsShelUpStrategyFeignController {

    private final WmsShelfUpStrategyService wmsShelfUpStrategyService;

    @PostMapping("/material/shelf/upInfo")
    @ApiOperation("物料货架信息")
    public R<Map<Long, WmsMaterialShelfUpVO>> materialShelfUpInfo(@RequestBody Set<Long> materialIds) {
        return R.success(wmsShelfUpStrategyService.materialShelfUpInfo(materialIds));
    }
}
