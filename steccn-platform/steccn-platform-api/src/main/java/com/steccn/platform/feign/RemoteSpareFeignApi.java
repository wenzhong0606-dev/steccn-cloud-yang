package com.steccn.platform.feign;


import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(contextId = "RemoteSpareFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteSpareFeignApi {

    /**
     * 根据名称编码查询备件信息
     * @param name
     * @param code
     * @return R<Map<Long, EqpSpareVO>>
     */
    @RequestMapping("/feign/platform/eqpSpare/searchSpares")
    @ApiOperation("根据名称编码查询备件信息")
    R<Map<Long, EqpSpareVO>> searchSpares(@RequestParam("name") String name, @RequestParam("code") String code);

    @GetMapping("/feign/platform/eqpSpare/getByCode")
    @ApiOperation("根据名称编码查询备件信息")
    R<EqpSpareVO> getByCode( @RequestParam("code") String code);
}
