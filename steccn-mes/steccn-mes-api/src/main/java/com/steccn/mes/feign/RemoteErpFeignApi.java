package com.steccn.mes.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.mes.dto.ErpCustomerDTO;
import com.steccn.mes.dto.ErpTransferSaveDTO;
import com.steccn.mes.vo.ErpCustomerVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

/**
 * erp调用
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
@FeignClient(name = "remoteErpFeignApi", url = "${erp.url}", decode404 = true)

public interface RemoteErpFeignApi {

    /**
     * 获取客户信息
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/getcust")
    @ApiOperation("获取erp客户信息")
    R<List<ErpCustomerVO>> getErpCustomer(@RequestBody ErpCustomerDTO dto);

    /**
     * 保存直接调拨单
     * @param dto
     * @return
     */
    @PostMapping("/api/tcmes/savetransferdirect")
    @ApiOperation("保存直接调拨单")
    String saveTransfer(@RequestBody ErpTransferSaveDTO dto);


}
