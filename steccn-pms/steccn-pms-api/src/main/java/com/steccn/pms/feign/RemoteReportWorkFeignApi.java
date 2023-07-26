package com.steccn.pms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.EpMeterReadRowDTO;
import com.steccn.pms.vo.PmsReportWorkVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chenhuan
 * @date 2022/12/9 11:11
 */
@FeignClient(contextId = "RemoteReportWorkFeignApi", value = FeignConstant.STECCN_PMS_SERVICE_NAME)
public interface RemoteReportWorkFeignApi {

    @PostMapping("/feign/pms/report/work/listByClassTime")
    @ApiOperation("根据班次时间查询报工信息")
    R<List<PmsReportWorkVO>> listByClassTime(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> tupleDTO);


    @ApiOperation("报工班组信息")
    @PostMapping("/feign/pms/report/work/class/group")
    R<Map<Long, Long>> reportWorkClassGroup(@RequestBody Set<Long> reportWorkIdSet);

    /**
     * 获取深加工产线-订单的报工记录
     * @param sjgDataList 深加工产线数据
     * @return
     */
    @ApiOperation("获取深加工产线-订单的报工记录")
    @PostMapping("/feign/pms/report/work/sjg/order/rw")
    R<List<EpMeterReadRowDTO>> sjgOrderRw(@RequestBody List<EpMeterReadRowDTO> sjgDataList);

    @ApiOperation("获取指定日期，指定产线的所有产量")
    @PostMapping("/feign/pms/report/work/num/line/date")
    R<Map<String, BigDecimal>> classDateProductNum(@RequestBody TripleDTO<Set<Integer>, LocalDate, LocalDate> dto);

    /**
     * 获取车间班次日期中班次报工最多的班组
     *
     * @param tupleDTO
     * @return
     */
    @ApiOperation("获取车间班次日期中班次报工最多的班组")
    @PostMapping("/feign/pms/report/work/workshop/class/group")
    R<Map<String, Long>> workshopClassGroupMap(TupleDTO<Set<LocalDate>, Map<Integer, Set<Integer>>> tupleDTO);



    /**
     *
     * 原料耗用量
     * @param tripleDTO
     * @return
     */
    @ApiOperation("原料耗用量")
    @PostMapping("/feign/pms/report/work/original/consume")
    R<Map<String, BigDecimal>> getUseCount(@RequestBody TripleDTO<List<Integer>, LocalDateTime, LocalDateTime> tripleDTO);


    /**
     *
     * 原料耗用量-每个订单
     * @param tripleDTO
     * @return
     */
    @ApiOperation("原料耗用量")
    @PostMapping("/feign/pms/report/work/original/consume/perOrder")
    R<Map<String, BigDecimal>> useCountPerOrder(@RequestBody TripleDTO<List<Integer>, LocalDateTime, LocalDateTime> tripleDTO);

}
