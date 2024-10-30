package com.steccn.wms.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsSignFeedback;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsSignFeedbackDTO;
import com.steccn.wms.dto.WmsStorageReceiptPageDTO;
import com.steccn.wms.service.WmsSignFeedbackService;
import com.steccn.wms.vo.WmsSignFeedbackVO;
import com.steccn.wms.vo.WmsStorageReceiptVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wms/sign/feedback")
@RequiredArgsConstructor
@Slf4j
public class WmsSignFeedbackController {

    private final WmsSignFeedbackService wmsSignFeedbackService;


    @ApiOperation("新增")
    @PostMapping()
    @SysLog("物流单反馈新增")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody WmsSignFeedbackDTO dto) {
        return R.success(wmsSignFeedbackService.add(dto));
    }


    @ApiOperation("修改")
    @PostMapping("/update")
    @SysLog("物流单反馈修改")
    public R<Boolean> update(@Validated(InsertGroup.class) @RequestBody WmsSignFeedbackDTO dto) {
        return R.success(wmsSignFeedbackService.updateSignFeedback(dto));
    }



    @ApiOperation("获取详情")
    @GetMapping()
    @SysLog("物流单反馈详情")
    public R<WmsSignFeedbackVO> get(@RequestParam("id") Long id) {
        return R.success(wmsSignFeedbackService.get(id));
    }


    @ApiOperation("分页列表")
    @PostMapping("/list")
    public R<Page<WmsSignFeedbackVO>> pageList(@RequestBody WmsSignFeedbackDTO dto) {
        return R.success(wmsSignFeedbackService.pageList(dto));
    }

    @ApiOperation("确认签收")
    @PostMapping("/affirm")
    public R<Boolean> affirm(@RequestBody WmsSignFeedbackDTO dto){
        return R.success(wmsSignFeedbackService.affirm(dto));
    }

    @ApiOperation("退回")
    @PostMapping("/sendBack")
    public R<Boolean> sendBack(@RequestBody WmsSignFeedbackDTO dto){
        return R.success(wmsSignFeedbackService.sendBack(dto));
    }

    @ApiOperation("已退回数量")
    @GetMapping("/sendBack/count")
    public R<Long> sendBackCount(){
        return R.success(wmsSignFeedbackService.countSendBack());
    }

    @ApiOperation("已退回分页列表")
    @PostMapping("sendBack/list")
    public R<Page<WmsSignFeedbackVO>> sendBackPageList(@RequestBody WmsSignFeedbackDTO dto) {
        return R.success(wmsSignFeedbackService.sendBackList(dto));
    }
}
