package com.steccn.wms.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.vo.WmsInventoryRecordVO;
import com.steccn.wms.domain.WmsInventoryRecord;
import com.steccn.wms.dto.WmsInventoryRecordDTO;
import com.steccn.wms.dto.WmsInventoryRecordPageDTO;
import com.steccn.wms.service.WmsInventoryRecordService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 库存流水记录
 *
 * @author Mongknife
 * @date 2023/10/11
 */
@RestController
@RequestMapping("/wms/inventory/record")
@RequiredArgsConstructor
@Slf4j
public class WmsInventoryRecordController {

    private final WmsInventoryRecordService wmsInventoryRecordService;
    @ApiOperation("库存流水列表")
    @PostMapping("/list")
    public R<Page<WmsInventoryRecordVO>> pageList(@RequestBody WmsInventoryRecordPageDTO dto) {
        return R.success(wmsInventoryRecordService.pageList(dto));
    }

    @ApiOperation("库存流水记录保存")
    @PostMapping("/save")
    public R<Boolean> save(@RequestBody List<WmsInventoryRecordDTO> wmsInventoryList){
        List<WmsInventoryRecord> wmsInventoryRecords = BeanUtil.copyToList(wmsInventoryList, WmsInventoryRecord.class);
        return R.success(wmsInventoryRecordService.saveBatch(wmsInventoryRecords));
    }

}
