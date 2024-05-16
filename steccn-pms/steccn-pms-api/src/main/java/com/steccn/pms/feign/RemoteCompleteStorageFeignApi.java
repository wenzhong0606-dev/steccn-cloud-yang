package com.steccn.pms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.dto.PmsOperateDriverDTO;
import com.steccn.pms.dto.ReceiptStatusDTO;
import com.steccn.pms.vo.PmsCompleteStorageVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chenhuan
 * @date 2022/12/9 11:11
 */
@FeignClient(contextId = "RemoteCompleteStorageFeignApi", value = FeignConstant.STECCN_PMS_SERVICE_NAME)
public interface RemoteCompleteStorageFeignApi {


    /**
     * 完工入库变更状态
     * @param dto
     * @return
     */
    @ApiOperation("完工入库变更状态")
    @PostMapping("/feign/pms/complete/storage/complete")
    R<Boolean> completeStorageReviseStatue(@RequestBody PmsCompleteStorageDTO dto);

    @ApiOperation("完工入库变更状态")
    @PostMapping("/feign/pms/complete/storage/revise/complete")
    R<Boolean> completeStorageStatue(@RequestBody PmsCompleteStorageDTO dto);

    @ApiOperation("完工入库单据状态变更")
    @PostMapping("/feign/pms/complete/storage/change/state")
    R<Boolean> changeReceiptState(@RequestBody ReceiptStatusDTO dto);

    @GetMapping("/feign/pms/complete/storage/code")
    @ApiOperation("根据仓储入库单号查询入库凭证是否存在")
    R<Boolean> getCompleteStorageByCode(@RequestParam("code")String code);

    @GetMapping("/feign/pms/complete/storage/code/query")
    @ApiOperation("根据仓储入库单号查询入库凭证是否存在")
    R<PmsCompleteStorageVO> getCompleteStorage(@RequestParam("code")String code);

    @PostMapping("/feign/pms/complete/storage/order/num")
    @ApiOperation("查询订单入库量-产线|班次日期|班次|订单")
    R<Map<String, BigDecimal>> inStorage(@RequestBody TripleDTO<List<Integer>, LocalDateTime, LocalDateTime> dto);

    @PostMapping("/feign/pms/complete/storage/week/num")
    @ApiOperation("查询订单入库量-产线|周")
    R<Map<String, BigDecimal>> inStorageWeek(@RequestBody TripleDTO<List<Integer>,LocalDateTime, LocalDateTime> dto);

    @PostMapping("/feign/pms/complete/storage/month/num")
    @ApiOperation("查询订单入库量-产线|月")
    R<Map<String, BigDecimal>> inStorageMonth(@RequestBody TripleDTO<List<Integer>, LocalDateTime, LocalDateTime> tripleDTO);

    @PostMapping("/feign/pms/complete/storage/sjg/in/storage/sum")
    @ApiOperation(value = "入库汇总")
    R<Map<String, BigDecimal>> sjgInStorageSum(@RequestBody PmsOperateDriverDTO dto);

    @ApiOperation("入库单编码获取生产订单产线名称")
    @PostMapping("/feign/pms/complete/storage/storage/receipt/code/get/line/name")
    R<Map<String,String>> storageReceiptCodeGetLineName(@RequestBody Set<String> codeSet);
}
