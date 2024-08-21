package com.steccn.dp.controller;

import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.service.OpcUaDataCountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Api("数采监控")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dp/back/data/count")
public class OpcUaDataCountController {

    private final OpcUaDataCountService opcUaDataCountService;

    @PostMapping("/category")
    @ApiOperation(value = "数采大类统计")
    public R<List<TupleDTO<String, Integer>>> category(@Valid @RequestBody TupleDTO<LocalDateTime, Object> dto) {
        return R.success(opcUaDataCountService.category(dto.getField1()));
    }

    @PostMapping("/detail")
    @ApiOperation(value = "大类明细统计")
    public R<List<TripleDTO<String, String, Integer>>> detail(@Valid @RequestBody TupleDTO<LocalDateTime, String> dto) {
        return R.success(opcUaDataCountService.detail(dto.getField1(), dto.getField2()));
    }
}
