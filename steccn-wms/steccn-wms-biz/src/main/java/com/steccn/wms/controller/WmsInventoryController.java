package com.steccn.wms.controller;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.dto.CaseSiteDTO;
import com.steccn.wms.dto.WmsInventoryDTO;
import com.steccn.wms.dto.WmsInventoryPageDTO;
import com.steccn.wms.service.WmsInventoryService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 库存信息
 *
 * @author Mongknife
 * @date 2022/12/9
 */

@RestController
@RequestMapping("/wms/inventory")
@RequiredArgsConstructor
@Slf4j
public class WmsInventoryController {

    private final WmsInventoryService wmsInventoryService;

    @ApiOperation("库存分页列表")
    @PostMapping("/list")
    public R<Page<WmsInventoryVO>> pageList(@RequestBody WmsInventoryPageDTO dto) {
        return R.success(wmsInventoryService.pageList(dto));
    }

    @ApiOperation("冻结")
    @PutMapping("/freeze")
    public R<Boolean> freeze(@RequestBody WmsInventoryDTO dto) {
        Assert.notNull(dto.getId(), "库存id不能为空");
        return R.success(wmsInventoryService.freeze(dto));
    }

    @ApiOperation("解冻")
    @PutMapping("/unFreeze")
    public R<Boolean> unFreeze(@RequestBody WmsInventoryDTO dto) {
        Assert.notNull(dto.getId(), "库存id不能为空");
        return R.success(wmsInventoryService.unfreeze(dto));
    }

    @ApiOperation("根据箱号获取仓库货架信息")
    @PostMapping("/stockByCaseNo")
    public R<Map<String, CaseSiteDTO>> stockByCaseNo(@RequestBody Set<String> caseNos){
        return R.success(wmsInventoryService.stockByCaseNo(caseNos));
    }

}
