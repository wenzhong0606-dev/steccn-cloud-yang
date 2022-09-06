package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtCheckItemPageDTO;
import com.steccn.qms.dto.QtDefectiveItemPageDTO;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.vo.QtCheckItemListVO;
import com.steccn.qms.vo.QtDefectiveItemVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

/**
 * @desc 缺陷项类别管理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/defective")
@Slf4j
@ApiModel("缺陷项控制器")
public class QtDefectiveItemController implements Serializable {

    private final QtDefectiveItemService qtDefectiveItemService;

    @PostMapping("/list")
    @ApiOperation(value = "缺陷项列表")
    public R<Page<QtDefectiveItemVO>> pageList(@RequestBody QtDefectiveItemPageDTO dto) {
        return R.success(qtDefectiveItemService.pageList(dto));
    }




}
