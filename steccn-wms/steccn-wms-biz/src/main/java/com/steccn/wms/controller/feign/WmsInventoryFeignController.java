package com.steccn.wms.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.wms.dto.CaseSiteDTO;
import com.steccn.wms.service.WmsInventoryService;
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
 * @date 2022/12/12
 */
@RestController
@Api("仓储库存feign")
@RequestMapping("/feign/wms/inventory")
@RequiredArgsConstructor
@Slf4j
public class WmsInventoryFeignController {

    private final WmsInventoryService wmsInventoryService;

    @ApiOperation("根据箱号获取仓库货架信息")
    @PostMapping("/stockByCaseNo")
    public R<Map<String, CaseSiteDTO>> stockByCaseNo(@RequestBody Set<String> caseNos) {
        return R.success(wmsInventoryService.stockByCaseNo(caseNos));
    }
}
