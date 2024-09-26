package com.steccn.dp.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.dp.vo.DpNatureGasVO;
import com.steccn.dp.vo.DpOnOffCountVO;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.dp.vo.DpWeightVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/11/23
 */
@FeignClient(contextId = "RemoteDpFeignApi", value = FeignConstant.STECCN_DP_SERVICE_NAME)
public interface RemoteDpFeignApi {

    /**
     * 电子秤读数
     * @param dto
     * @return
     */
    @PostMapping("/feign/dp/weight/count")
    @ApiOperation(value = "电子秤读数")
    R<List<DpWeightVO>> count(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto);

    /**
     * 电子秤读数
     * @param dto
     * @return
     */
    @PostMapping("/feign/dp/weight/countMap")
    @ApiOperation(value = "电子秤读数Map")
    R<Map<String, BigDecimal>> countMap(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto);

    /**
     * 电子秤读数按天
     * @param dto
     * @return
     */
    @PostMapping("/feign/dp/weight/countAsDay")
    @ApiOperation(value = "电子秤读数按天")
    R<List<DpValueDateVO>> countAsDay(@RequestBody DpPmsOperateDTO dto);

    @GetMapping("/feign/dp/weight/push")
    @ApiOperation(value = "推送电子秤读数")
    R<Boolean> push();

    /**
     * 耗料数
     * @param dto
     * @return
     */
    @PostMapping("/feign/dp/weight/consume")
    @ApiOperation(value = "耗料数")
    R<List<CommonReportDTO>> consumeMaterial(@RequestBody DpPmsOperateDTO dto);

    /**
     * 玻璃片数光电信号
     * @param dto
     * @return
     */
    @PostMapping("/feign/dp/onOff/list")
    @ApiOperation(value = "玻璃片数光电信号")
    R<List<DpOnOffCountVO>> onOffList(@RequestBody TupleDTO<LocalDate,LocalDate> dto);


    /**
     * 保留数采7天的数据
     * @param dto
     * @return
     */
    @PostMapping("/feign/dp/coldEnd/deleteData")
    @ApiOperation(value = "只保留7天的数据")
    R<Boolean> deleteData(@RequestBody TupleDTO<LocalDateTime,String> dto);

    /**
     * 获取天然气数据
     * @param dto
     * @return
     */
    @PostMapping("/feign/dp/gas/data")
    @ApiOperation(value = "获取天然气数据")
    R<List<DpNatureGasVO>> natureGasData(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto);


    @PostMapping("/feign/dp/output")
    @ApiOperation(value = "产量")
    R<Map<Long,BigDecimal>> output(@RequestBody Set<Long> orderIds);
}
