package com.steccn.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsShelfUpStrategy;
import com.steccn.wms.dto.WmsShelfUpStrategyDTO;
import com.steccn.wms.service.WmsShelfUpStrategyService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 上架策略
 * </p>
 *
 * @author yss
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/wms/shelfup/strategy")
public class WmsShelfUpStrategyController {

    @Autowired
    private WmsShelfUpStrategyService wmsShelfUpStrategyService;

    /**
     * @explain: 获取上架策略列表
     * @Date
     **/
    @GetMapping("/getShelfUpStrategyList")
    @ApiOperation(value = "获取上架策略列表")
    public R<Page<WmsShelfUpStrategy>> getShelfUpStrategyList( WmsShelfUpStrategyDTO dto) {

        return R.success(wmsShelfUpStrategyService.getShelfUpStrategyList(dto));
    }

    @PostMapping("/addOrUpdateShelfUpStrategy")
    @ApiOperation(value = "新增或者编辑上架策略")
    public R<Boolean> addOrUpdateShelfUpStrategy(@RequestBody WmsShelfUpStrategy dto) {
        return R.success(wmsShelfUpStrategyService.addOrUpdateShelfUpStrategy(dto));
    }
    @DeleteMapping("/delShelfUpStrategy")
    @ApiOperation(value = "删除上架策略")
    public R<Boolean> delShelfUpStrategy( WmsShelfUpStrategy dto) {
        return R.success(wmsShelfUpStrategyService.delShelfUpStrategy(dto));
    }

}
