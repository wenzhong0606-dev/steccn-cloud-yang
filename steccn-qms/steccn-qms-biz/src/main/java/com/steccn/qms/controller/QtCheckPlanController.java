package com.steccn.qms.controller;

import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtCheckItemSaveDTO;
import com.steccn.qms.dto.QtCheckPlanDTO;
import com.steccn.qms.service.QtCheckPlanService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/06
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/check/plan")
@Slf4j
public class QtCheckPlanController {
    @Autowired
    private QtCheckPlanService qtCheckPlanService;

    @PostMapping
    @ApiOperation(value = "新建检验方案")
    public R<Boolean> add(@RequestBody QtCheckPlanDTO dto) {
        return R.success(qtCheckPlanService.add(dto));
    }
}
