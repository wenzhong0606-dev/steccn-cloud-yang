package com.steccn.platform.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SysOfficeService;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 机构feign控制器
 *
 * @author AndrewLuo
 * @date 2022/09/08
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/sys/office")
public class SysOfficeFeignController {
    private final SysOfficeService sysOfficeService;

    @GetMapping()
    public R<List<SysOfficeVO>> listFlatDataByType(@RequestParam String type) {
        return R.success(sysOfficeService.flatData(type));
    }

    @GetMapping("/name")
    public R<String> listFlatDataByType(@RequestParam Integer id) {
        return R.success(sysOfficeService.findNameById(id));
    }

    @PostMapping("/ids")
    @ApiOperation("根据主键id查询机构的信息")
    R<Map<Integer, SysOfficeVO>> listById(@RequestBody Set<Integer> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysOfficeService.listById(idList));
    }

    @PostMapping("/ch")
    @ApiOperation("根据主键id查询机构的中文")
    R<Map<Integer, String>> listByCh(@RequestBody Set<Integer> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysOfficeService.listByCh(idList));
    }
}
