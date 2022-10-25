package com.steccn.pms.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.pms.service.PmsProduceOrderCraftService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 生产订单工艺控制器
 *
 * @author Mongknife
 * @date 2022/10/17
 */
@RestController
@RequestMapping("/pms/produce/order/craft")
@RequiredArgsConstructor
@Slf4j
public class ProduceOrderCraftController {

    private final PmsProduceOrderCraftService pmsProduceOrderCraftService;

    @ApiOperation("删除工艺信息")
    @DeleteMapping("/{id}")
    @SysLog("删除工艺信息")
    public R<Boolean> delete(@PathVariable("id") Integer id){
        return R.success(pmsProduceOrderCraftService.delete(id));
    }
}
