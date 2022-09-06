package com.steccn.platform.feign;


import com.steccn.platform.vo.SysDictVO;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@FeignClient(contextId = "remoteDictFeign", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteDictFeign {


    /**
     * 根据主键id查询数据字典的值
     *
     * @param idList 数据字典id集合
     * @return
     */
    @RequestMapping("/platform/sys/dict/ids")
    @ApiOperation("根据主键id查询数据字典的值")
    R<Map<Long, SysDictVO>> listById(@RequestBody List<Long> idList);
}
