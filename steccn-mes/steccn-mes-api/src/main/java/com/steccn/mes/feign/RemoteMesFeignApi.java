package com.steccn.mes.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.mes.vo.MesEqpVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * mes 远程调用
 *
 * @author AndrewLuo
 * @date 2023/09/05
 */
@FeignClient(contextId = "RemoteMesServiceFeignApi", value = FeignConstant.STECCN_MES_SERVICE_NAME)
public interface RemoteMesFeignApi {

    /**
     * 根据code获取设备信息
     * @param code
     * @return
     */
    @GetMapping("/feign/mes/eqp/getByCode")
    R<MesEqpVO> getByCode(@RequestParam("code") String code);
}
