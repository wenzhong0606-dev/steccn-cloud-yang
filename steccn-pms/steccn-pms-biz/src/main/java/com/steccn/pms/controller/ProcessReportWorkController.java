package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.ProcessReportWorkPageDTO;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Adolph.Ye
 * @desc 工序报工控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/process/report/work")
@Slf4j
public class ProcessReportWorkController {

    @ApiModelProperty("工序分页列表")
    @PostMapping("/page")
    public R<Page<ProcessReportWorkPageVO>> pageList(@RequestBody ProcessReportWorkPageDTO dto) {
        return R.success();
    }


}
