package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmCraftRouteDTO;
import com.steccn.pms.service.PmCraftRouteService;
import com.steccn.pms.vo.PmCraftRouteVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 工艺路线
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/craftRoute")
@Slf4j
public class PmCraftRouteController {

    private final PmCraftRouteService pmCraftRouteService;

    @ApiModelProperty("新增工艺路线")
    @PostMapping
    public R<Boolean> add(@Valid @RequestBody PmCraftRouteDTO dto) {
        return pmCraftRouteService.add(dto);
    }

    @ApiModelProperty("工艺路线分页列表")
    @PostMapping("/list")
    public R<Page<PmCraftRouteVO>> pageList(@RequestBody PmCraftRouteDTO dto) {
        return R.success(pmCraftRouteService.pageList(dto));
    }

    @ApiModelProperty("编辑工艺路线")
    @PutMapping
    public R<Boolean> edit(@Valid @RequestBody PmCraftRouteDTO dto) {
        return pmCraftRouteService.edit(dto);
    }

    @ApiModelProperty("删除工艺路线")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return pmCraftRouteService.delete(id);
    }

}
