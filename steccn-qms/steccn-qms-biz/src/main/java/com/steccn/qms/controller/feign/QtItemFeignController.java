package com.steccn.qms.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.qms.service.QtDefectiveItemService;
import com.steccn.qms.vo.QtDefectiveItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yequan
 * @desc 缺陷项|检验项配置
 * @date 2023/10/18
 */
@RestController
@Api("缺陷项|检验项配置")
@RequestMapping("/feign/qms/item")
@RequiredArgsConstructor
public class QtItemFeignController {

    private final QtDefectiveItemService qtDefectiveItemService;

    @ApiOperation("根据类别获取缺陷项集合")
    @GetMapping("/defect")
    R<List<QtDefectiveItemVO>> getDefectItemByCategory(@RequestParam("categoryName") String categoryName) {
        return R.success(qtDefectiveItemService.getDefectItemByCategory(categoryName));
    }
}
