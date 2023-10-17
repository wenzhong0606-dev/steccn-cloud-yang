package com.steccn.pms.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.dto.ReceiptStatusDTO;
import com.steccn.pms.service.PmsCompleteStorageInfoService;
import com.steccn.pms.service.PmsCompleteStorageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final PmsCompleteStorageInfoService pmsCompleteStorageInfoService;

    @PostMapping("/complete")
    @ApiOperation("根据完成生产订单入库单编码修改状态")
    @SysLog("complete")
    public R<Boolean> completeStorageReviseStatue(@RequestBody PmsCompleteStorageDTO dto) {
        return pmsCompleteStorageService.completeStorageReviseStatue(dto.getCode(),dto.getConfirmName());
    }

    @ApiOperation("完工入库单据状态变更")
    @PostMapping("/change/state")
    R<Boolean> changeReceiptState(@RequestBody ReceiptStatusDTO dto) {
        if (CollUtil.isEmpty(dto.getInStorageVoucherList()) || Objects.isNull(dto.getState())) {
            return R.failure("参数异常");
        }
        return R.success(pmsCompleteStorageService.changeReceiptState(dto));
    }

    @GetMapping("/code")
    @ApiOperation("根据仓储入库单号查询入库凭证是否存在")
    public R<Boolean> getCompleteStorageByCode(@RequestParam("code")String code){
        return R.success(pmsCompleteStorageService.getCompleteStorageByCode(code));
    }

    @PostMapping("/order/num")
    @ApiOperation("查询订单入库量")
    public R<Map<String, BigDecimal>> inStorage(@RequestBody TripleDTO<List<Integer>, LocalDateTime, LocalDateTime> tripleDTO) {
        return R.success(pmsCompleteStorageInfoService.inStorage(tripleDTO.getField1(), tripleDTO.getField2(), tripleDTO.getField3()));
    }

    @PostMapping("/week/num")
    @ApiOperation("查询订单入库量-产线|周")
    R<Map<String, BigDecimal>> inStorageWeek(@RequestBody TripleDTO<List<Integer>, LocalDateTime, LocalDateTime> tripleDTO) {
        return R.success(pmsCompleteStorageInfoService.inStorageWeek(tripleDTO.getField1(), tripleDTO.getField2(), tripleDTO.getField3()));
    }

    @PostMapping("/month/num")
    @ApiOperation("查询订单入库量-产线|月")
    R<Map<String, BigDecimal>> inStorageMonth(@RequestBody TripleDTO<List<Integer>, LocalDateTime, LocalDateTime> tripleDTO){
        return R.success(pmsCompleteStorageInfoService.inStorageMonth(tripleDTO.getField1(), tripleDTO.getField2(), tripleDTO.getField3()));
    }

}
