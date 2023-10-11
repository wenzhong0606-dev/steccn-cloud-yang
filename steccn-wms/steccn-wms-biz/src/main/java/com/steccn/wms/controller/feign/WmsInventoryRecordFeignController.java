package com.steccn.wms.controller.feign;


import cn.hutool.core.bean.BeanUtil;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsInventoryRecord;
import com.steccn.wms.dto.WmsInventoryRecordDTO;
import com.steccn.wms.service.WmsInventoryRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api("仓储库存流水记录feign")
@RequestMapping("/feign/wms/inventory/record")
@RequiredArgsConstructor
@Slf4j
public class WmsInventoryRecordFeignController {



    private final WmsInventoryRecordService wmsInventoryRecordService;
    @ApiOperation("库存流水记录保存")
    @PostMapping("/save")
    public R<Boolean> save(@RequestBody List<WmsInventoryRecordDTO> wmsInventoryList){
        List<WmsInventoryRecord> wmsInventoryRecords = BeanUtil.copyToList(wmsInventoryList, WmsInventoryRecord.class);
        return R.success(wmsInventoryRecordService.saveBatch(wmsInventoryRecords));
    }
}
