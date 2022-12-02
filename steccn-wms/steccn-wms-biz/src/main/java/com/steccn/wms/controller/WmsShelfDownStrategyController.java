package com.steccn.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsShelfDownStrategy;
import com.steccn.wms.dto.WmsShelfDownStrategyDTO;
import com.steccn.wms.service.WmsShelfDownStrategyService;
import com.steccn.wms.vo.WmsShelfDownStrategyVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 下架策略
 * </p>
 *
 * @author yss
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/wms/shelfdown/strategy")
public class WmsShelfDownStrategyController {

    @Autowired
    private WmsShelfDownStrategyService wmsShelfDownStrategyService;

    /**
     * @explain: 获取下架策略列表
     * @Date
     **/
    @GetMapping("/getShelfDownStrategyList")
    @ApiOperation(value = "获取下架策略列表")
    public R<Page<WmsShelfDownStrategyVO>> getShelfDownStrategyList(@RequestBody WmsShelfDownStrategyDTO dto) {

        return R.success(wmsShelfDownStrategyService.getShelfDownStrategyList(dto));
    }

    @PostMapping("/addOrUpdateShelfDownStrategy")
    @ApiOperation(value = "新增或者编辑下架策略")
    public R<Boolean> addOrUpdateShelfDownStrategy(@RequestBody WmsShelfDownStrategy dto) {
        return R.success(wmsShelfDownStrategyService.addOrUpdateShelfDownStrategy(dto));
    }
    @DeleteMapping("/delShelfDownStrategy")
    @ApiOperation(value = "删除下架策略")
    public R<Boolean> delShelfDownStrategy( WmsShelfDownStrategy dto) {
        return R.success(wmsShelfDownStrategyService.delShelfDownStrategy(dto));
    }

}
