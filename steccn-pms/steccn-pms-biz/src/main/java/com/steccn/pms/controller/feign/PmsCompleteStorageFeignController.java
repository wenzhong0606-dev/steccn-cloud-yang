package com.steccn.pms.controller.feign;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.service.PmsCompleteStorageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/13
 */
@RestController
@Api("完工入库feign")
@RequestMapping("/feign/pms/complete/storage")
@RequiredArgsConstructor
@Slf4j
public class PmsCompleteStorageFeignController {

    private final PmsCompleteStorageService pmsCompleteStorageService;

    @PostMapping("/complete")
    @ApiOperation("根据完成生产订单入库单编码修改状态")
    @SysLog("complete")
    public R<Boolean> completeStorageReviseStatue(@RequestBody PmsCompleteStorageDTO dto) {
        return pmsCompleteStorageService.completeStorageReviseStatue(dto.getCode(),dto.getConfirmName());
    }
}
