package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/07
 */
@FeignClient(contextId = "remoteOfficeFeign", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteOfficeFeign {
    /**
     * 根据officeType查询机构
     *
     * @param officeType 机构类型
     * @return
     */
    @GetMapping("/platform/sys/office")
    @ApiOperation("根据officeType查询机构")
    R<List<SysOfficeVO>> list(@RequestParam("officeType") String officeType);

    /**
     * 根据officeType查询机构
     *
     * @param officeType 机构类型
     * @return
     */
    @GetMapping("/platform/sys/office/data")
    @ApiOperation("平级机构数据")
    R<List<SysOfficeVO>> data();
}
