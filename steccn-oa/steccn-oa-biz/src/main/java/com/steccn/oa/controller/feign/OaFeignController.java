package com.steccn.oa.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.oa.service.HrmresourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * oaFeign
 *
 * @author Mongknife
 * @date 2023/2/17
 */
@RestController
@Api("oaFeign")
@RequestMapping("/feign/oa")
@RequiredArgsConstructor
@Slf4j
public class OaFeignController {

    private final HrmresourceService hrmresourceService;

    @PostMapping("/user/ch")
    @ApiOperation("根据主键id查询oa用户的中文名称")
    public R<Map<Long, String>> listByIdCh(@RequestBody List<Long> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(hrmresourceService.listByIdCh(idList));
    }


}
