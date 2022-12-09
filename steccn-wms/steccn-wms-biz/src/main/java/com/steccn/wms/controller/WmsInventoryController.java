package com.steccn.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.vo.WmsInventoryVO;
import com.steccn.wms.dto.WmsInventoryPageDTO;
import com.steccn.wms.service.WmsInventoryService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @ApiOperation("调拨单分页列表")
    @PostMapping("/list")
    public R<Page<WmsInventoryVO>> pageList(@RequestBody WmsInventoryPageDTO dto) {
        return R.success(wmsInventoryService.pageList(dto));
    }
}
