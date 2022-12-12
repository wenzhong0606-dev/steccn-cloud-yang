package com.steccn.platform.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.dto.SparePageDTO;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.vo.EqpSpareOaVO;
import com.steccn.platform.vo.EqpSpareVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/2
 */
@Api("备件信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/eqpSpare")
public class EqpSpareController {

    private final SysEqpSpareService eqpSpareService;

    @ApiOperation("获取备件列表")
    @GetMapping("/searchSpares")
    public List<EqpSpareVO> searchSpares(@RequestParam("name") String name) {
        return eqpSpareService.listSpares(name);
    }

    @ApiOperation("根据编码获取备件")
    @GetMapping("/getByCode")
    public R<EqpSpareVO> getByCode(@RequestParam("code") String code) {
        return R.success(eqpSpareService.getByCode(code));
    }

    @RequestMapping("/listByCode")
    @ApiOperation("根据code查询备件的数据")
    public R<Map<String, EqpSpareVO>> listByCode(@RequestBody List<String> codeList) {
        if (CollUtil.isEmpty(codeList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(eqpSpareService.listByCode(codeList));
    }

    @RequestMapping("/listById")
    @ApiOperation("根据id查询备件的数据")
    public R<Map<Long, EqpSpareVO>> listById(@RequestBody List<Long> materialIds) {
        if (CollUtil.isEmpty(materialIds)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(eqpSpareService.listById(materialIds));
    }

    @PostMapping("/listPage")
    @ApiOperation("获取备件分页列表")
    public R<Page<EqpSpareVO>> listPage(@RequestBody SparePageDTO sparePageDTO) {
        return R.success(eqpSpareService.listPage(sparePageDTO));
    }

    @PostMapping("/list")
    @ApiOperation("获取备件列表")
    public List<EqpSpareVO> list(@RequestBody SpareDTO spareDTO) {
        return eqpSpareService.listCondition(spareDTO);
    }

    @GetMapping("/oa/page")
    @ApiOperation("oa获取备件列表")
    public R<Page<EqpSpareOaVO>> all() {
        EqpSpareOaVO vo = new EqpSpareOaVO();
        vo.setMaterialCode("物料名称");
        vo.setMaterialName("物料编码");
        vo.setCategoryName("物料类别");
        List<EqpSpareOaVO> resList = Lists.newArrayList(vo);
        return R.success(new Page<EqpSpareOaVO>().setRecords(resList));
    }

    @PostMapping("/getByTypeIdSpareList")
    @ApiOperation("根据分类获取物料信息")
    public R<List<EqpSpareVO>> getByTypeIdSpareList(@RequestBody  Set<String> types) {
        return R.success(eqpSpareService.getByTypeIdSpareList(types));
    }

}
