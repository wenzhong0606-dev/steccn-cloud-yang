package com.steccn.pms.controller;

import com.steccn.common.vo.R;
import com.steccn.pms.service.PmsWorkOrderBomService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 生产订单bom控制器
 *
 * @author Mongknife
 * @date 2022/10/17
 */

@RestController
@RequestMapping("/pms/produce/order/bom")
@RequiredArgsConstructor
@Slf4j
public class ProduceOrderBomController {

    private final PmsWorkOrderBomService pmsWorkOrderBomService;

    @ApiOperation("删除物料信息")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Integer id){
        return R.success(pmsWorkOrderBomService.delete(id));
    }

}
