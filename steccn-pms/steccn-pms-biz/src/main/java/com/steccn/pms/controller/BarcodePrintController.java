package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.pms.dto.BarcodePrintDTO;
import com.steccn.pms.dto.BarcodePrintRecordDTO;
import com.steccn.pms.vo.BarcodePrintRecordVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * @author yequan
 * @desc 条码打印控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/barcode")
@Slf4j
public class BarcodePrintController {

    @ApiOperation("打印")
    @PostMapping("/print")
    public R<Boolean> print(@RequestBody BarcodePrintDTO dto) {
        return R.success();
    }

    @ApiOperation("打印记录")
    @PostMapping("/print/record")
    public R<List<BarcodePrintRecordVO>> printRecord(@RequestBody BarcodePrintRecordDTO dto) {
        return R.success();
    }
}
