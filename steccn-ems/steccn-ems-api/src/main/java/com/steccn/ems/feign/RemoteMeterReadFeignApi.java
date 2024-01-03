package com.steccn.ems.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.Tuple5DTO;
import com.steccn.common.vo.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDate;

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

}
