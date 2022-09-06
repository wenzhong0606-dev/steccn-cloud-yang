package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.service.PmProcessService;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.vo.PmProcessVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 工序管理相关内容
 *
 * @author Mongknife
 * @date 2022/9/5
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/process")
@Slf4j
public class PmProcessController {

    private final PmProcessService processService;

    @ApiModelProperty("新增工序")
    @PostMapping
    public R<Boolean> add(@Valid @RequestBody PmProcessDTO dto) {
        return processService.saveProcess(dto);
    }

    @ApiModelProperty("工序分页列表")
    @PostMapping("/list")
    public R<Page<PmProcessVO>> pageList(@RequestBody PmProcessDTO dto) {
        return R.success(processService.pageList(dto));
    }

    @ApiModelProperty("删除工序")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(processService.delete(id));
    }

    @ApiModelProperty("修改工序")
    @PutMapping
    public R<Boolean> edit (@Valid @RequestBody PmProcessDTO dto) {
        return processService.edit(dto);
    }

}
