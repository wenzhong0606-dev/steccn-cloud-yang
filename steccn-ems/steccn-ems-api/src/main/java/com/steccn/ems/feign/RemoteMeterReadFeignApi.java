package com.steccn.ems.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.Tuple5DTO;
import com.steccn.common.vo.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDate;
import java.util.List;

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
    R<List<CommonReportDTO>> gfPowerList(TripleDTO<LocalDate, LocalDate, Integer> dto);
}
