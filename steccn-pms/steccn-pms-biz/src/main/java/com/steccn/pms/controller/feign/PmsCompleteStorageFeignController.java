package com.steccn.pms.controller.feign;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.dto.PmsOperateDriverDTO;
import com.steccn.pms.dto.ReceiptStatusDTO;
import com.steccn.pms.service.PmsCompleteStorageInfoService;
import com.steccn.pms.service.PmsCompleteStorageService;
import com.steccn.pms.service.PmsOfficeManager;
import com.steccn.pms.service.PmsOperateDriverService;
import com.steccn.pms.vo.PmsCompleteStorageVO;
import com.steccn.pms.vo.PmsOperateDriverCompleteStorageVO;
import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final PmsOfficeManager pmsOfficeManager;

    @PostMapping("/complete")
    @ApiOperation("根据完成生产订单入库单编码修改状态")
    @SysLog("complete")
    public R<Boolean> completeStorageReviseStatue(@RequestBody PmsCompleteStorageDTO dto) {
        return pmsCompleteStorageService.completeStorageReviseStatue(dto.getCode(),dto.getConfirmName());
    }

    @PostMapping("/revise/complete")
    @ApiOperation("根据完成生产订单入库单编码修改状态为已冲销")
    @SysLog("revise/complete")
    public R<Boolean> completeStorageStatue(@RequestBody PmsCompleteStorageDTO dto) {
        return pmsCompleteStorageService.completeStorageStatue(dto.getCode(),dto.getConfirmName());
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

    @GetMapping("/code/query")
    @ApiOperation("根据仓储入库单号查询入库凭证是否存在")
    public R<PmsCompleteStorageVO> getCompleteStorageInfoByCode(@RequestParam("code")String code){
        return R.success(pmsCompleteStorageService.getCompleteStorage(code));
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

    @PostMapping("/substrate/in/storage")
    @ApiOperation("基片统计报表-入库产量")
    R<List<CommonReportDTO>> substrateInStorage(@RequestBody DpPmsOperateDTO dto){
        return R.success(pmsCompleteStorageInfoService.substrate(dto));
    }

    @PostMapping("/substrate/produce/line")
    @ApiOperation("基片统计报表-产线产量明细")
    R<List<CommonReportDTO>> substrateProduceLine(@RequestBody DpPmsOperateDTO dto){
        return R.success(pmsCompleteStorageInfoService.substrateProduceLine(dto));
    }

    @PostMapping("/sjg/in/storage/sum")
    @ApiOperation(value = "入库汇总")
    R<Map<String, BigDecimal>> sjgInStorageSum(@RequestBody PmsOperateDriverDTO dto) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        List<SysOfficeVO> sysOfficeVOList = pmsOfficeManager.getChildren(dto.getOfficeId(), SysOfficeConstant.WORKSHOP);
        Assert.notEmpty(sysOfficeVOList, "不存在车间节点");
        Set<Integer> wsIdSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        List<SysOfficeVO> lineList = pmsOfficeManager.getChildren(wsIdSet, SysOfficeConstant.PRODUCT_LINE);
        Assert.notNull(lineList, "制造部下暂无产线");
        Set<Integer> lineIdSet = lineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        return R.success(pmsCompleteStorageService.sjgInStorage(granularity, dto.getStart(), dto.getEnd(), lineIdSet, 2));
    }

}
