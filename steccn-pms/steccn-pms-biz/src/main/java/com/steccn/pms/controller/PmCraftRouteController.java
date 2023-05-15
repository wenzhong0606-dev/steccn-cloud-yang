package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.dto.PmCraftRouteDTO;
import com.steccn.pms.dto.PmCraftRouteProcessDTO;
import com.steccn.pms.dto.PmProcessStationDTO;
import com.steccn.pms.service.PmCraftRouteService;
import com.steccn.pms.vo.PmCraftRouteProcessVO;
import com.steccn.pms.vo.PmCraftRouteVO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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

    @ApiOperation("新增工艺路线")
    @PostMapping
    @SysLog("新增工艺信息")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody PmCraftRouteDTO dto) {
        return pmCraftRouteService.add(dto);
    }

    @ApiOperation("工艺路线分页列表")
    @PostMapping("/list")
    public R<Page<PmCraftRouteVO>> pageList(@RequestBody PmCraftRouteDTO dto) {
        return R.success(pmCraftRouteService.pageList(dto));
    }

    @ApiOperation("编辑工艺路线")
    @PutMapping
    @SysLog("编辑工艺路线")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @Valid @RequestBody PmCraftRouteDTO dto) {
        return pmCraftRouteService.edit(dto);
    }

    @ApiOperation("删除工艺路线")
    @DeleteMapping("/{id}")
    @SysLog("删除工艺路线")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return pmCraftRouteService.delete(id);
    }

    @ApiOperation("校验当前物料下的工艺版本")
    @GetMapping("/verifyVersion")
    public R<Boolean> verifyVersion(@RequestParam("id") Long id, @RequestParam("version") String version) {
        return pmCraftRouteService.verifyVersion(id, version);
    }

    @ApiModelProperty("获取工艺路线下工序信息")
    @GetMapping("/getProcessInfo")
    public R<PmCraftRouteProcessVO> getProcessInfo(@RequestParam("processId") Long processId, @RequestParam("craftRouteId") String craftRouteId) {
        return pmCraftRouteService.getProcessInfo(processId, craftRouteId);
    }

    @ApiOperation("根据产线以及工艺信息下的可执行工位")
    @GetMapping("/stations")
    public R<List<SysOfficeVO>> stations(@ApiParam("产线id") @RequestParam("productLineId") Integer productLineId,
                                                 @ApiParam("工艺id") @RequestParam("craftRouteId") Integer craftRouteId){
        return R.success(pmCraftRouteService.stations(productLineId,craftRouteId));
    }
}
