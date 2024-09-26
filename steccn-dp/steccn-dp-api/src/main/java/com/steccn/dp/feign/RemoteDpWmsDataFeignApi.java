package com.steccn.dp.feign;

import cn.hutool.db.Page;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpWmsDataDTO;
import com.steccn.dp.vo.AdsWmsInventoryVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

/**
 * @author yequan
 * @desc 数据中台 - 仓储数据
 * @date 2023/12/1
 */
@FeignClient(contextId = "RemoteDpWmsDataFeignApi", value = FeignConstant.STECCN_DP_SERVICE_NAME)
public interface RemoteDpWmsDataFeignApi {


    @ApiOperation("获取期末库存数据")
    @PostMapping("/feign/dp/wms/data/endingData")
    R<List<AdsWmsInventoryVO>> endingData(@Valid @RequestBody DpWmsDataDTO dto) ;


    @ApiOperation("获取期初期末库存分页数据")
    @PostMapping("/feign/dp/wms/data/endingData/list")
    R<List<AdsWmsInventoryVO>> datalist (@Valid @RequestBody DpWmsDataDTO dto);

}
