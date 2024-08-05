package com.steccn.ems.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.Tuple5DTO;
import com.steccn.common.vo.R;
import com.steccn.ems.vo.EpPmsConsumeVO;
import org.apache.tomcat.jni.Local;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @desc 能源的耗用数
 */
@FeignClient(contextId = "remoteMeterReadFeignApi", value = FeignConstant.STECCN_EMS_SERVICE_NAME)
public interface RemoteMeterReadFeignApi {


    /**
     * 获取指定能源的耗用数
     *
     * @param dto 能源类型，车间，日期start，日期end，粒度  5元组
     * @return
     */
    @PostMapping("/feign/ems/meter/read/energy/consume")
    R<CommonReportDTO> energyConsume(@RequestBody Tuple5DTO<Integer, Integer, LocalDate, LocalDate, Integer> dto);


    /**
     * 获取指定能源的能耗
     *
     * @param dto 能源类型，车间，日期start，日期end，粒度  5元组
     * @return
     */
    @PostMapping("/feign/ems/meter/read/energy/consume/office")
    R<Map<String, BigDecimal>> energyConsumeOffice(@RequestBody Tuple5DTO<Integer, List<Integer>, LocalDate, LocalDate, Integer> dto);

    /**
     * 获取日期范围的能源的单耗
     *
     * @param dto
     * @return
     */
    @PostMapping("/feign/ems/meter/read/consume/use/unit/consume/date/range")
    R<EpPmsConsumeVO> consumeUseUnitConsumeDateRange(@RequestBody TripleDTO<Integer, LocalDate, LocalDate> dto);

    /**
     * 获取指定能源的单耗
     *
     * @param dto 能源类型，车间，日期start，日期end，粒度  5元组
     * @return
     */
    @PostMapping("/feign/ems/meter/read/unit/consume/office")
    R<Map<String, BigDecimal>> unitConsumeOffice(@RequestBody Tuple5DTO<Integer, List<Integer>, LocalDate, LocalDate, Integer> dto);

    /**
     * 余热发电
     *
     * @param dto  日期start，日期end，粒度  3元组
     * @return
     */
    @PostMapping("/feign/ems/meter/read/heat/power")
    R<List<CommonReportDTO>> heatPower(@RequestBody TripleDTO<LocalDate, LocalDate, Integer> dto);


    /**
     * 光伏发电
     *
     * @param dto 日期start，日期end，粒度  3元组
     * @return
     */
    @PostMapping("/feign/ems/meter/read/gf/power")
    R<List<CommonReportDTO>> gfPowerList(@RequestBody TripleDTO<LocalDate, LocalDate, Integer> dto);

    /**
     *  脱硫脱硝/电：取值与能耗日统计报表中“电/余热发电站（站用电）”相同
     *  脱硫脱硝/水：抄表管理->水表->设备动力部->脱硫脱硝，根据读数差计算每日耗水量
     *
     * @param dto 日期start，日期end，粒度  3元组
     * @return
     */
    @PostMapping("/feign/ems/meter/read/sc")
    R<List<CommonReportDTO>> scList(@RequestBody TripleDTO<LocalDate, LocalDate, Integer> dto);


    /**
     * 获取所有远传仪表的code
     * @return
     */
    @PostMapping("/feign/ems/meter/read/account")
    R<List<String>> accountList();
}
