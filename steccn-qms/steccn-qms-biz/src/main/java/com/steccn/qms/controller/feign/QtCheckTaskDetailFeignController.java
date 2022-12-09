package com.steccn.qms.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.qms.service.QtCheckTaskDetailService;
import com.steccn.qms.vo.QtCheckTaskDetailItemMaterialVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/9 9:28
 */
@RestController
@Api("任务详情feign")
@RequestMapping("/feign/qms/task/detail")
@RequiredArgsConstructor
public class QtCheckTaskDetailFeignController {

    private final QtCheckTaskDetailService qtCheckTaskDetailService;

    @ApiOperation("根据班次日期查询单片掰边重量、单片重量、拉引速度")
    @GetMapping("/getByClassTime")
    public R<List<QtCheckTaskDetailItemMaterialVO>> getByClassTime(LocalDateTime startTime, LocalDateTime endTime){
        return R.success(qtCheckTaskDetailService.getByClassTime(startTime, endTime));
    }
}
