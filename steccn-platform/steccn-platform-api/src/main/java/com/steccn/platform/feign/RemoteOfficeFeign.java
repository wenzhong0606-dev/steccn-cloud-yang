package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * @param
     * @return
     */
    @GetMapping("/platform/sys/office/data")
    @ApiOperation("平级机构数据")
    R<List<SysOfficeVO>> data();

    /**
     * 根据officeType查询机构
     *
     * @param type 机构类型
     * @return
     */
    @GetMapping("feign/platform/sys/office")
    @ApiOperation("平级机构数据")
    R<List<SysOfficeVO>> flatData(@RequestParam("type") String type);

    /**
     * 根据id查询名字
     * @param id 机构id
     * @return
     */
    @GetMapping("feign/platform/sys/office/name")
    @ApiOperation("通过id获取名字")
    R<String> getNameById(@RequestParam("id") Integer id);


    /**
     * 根据主键id查询机构的信息
     *
     * @param idList 机构id集合
     * @return
     */
    @PostMapping("/feign/platform/sys/office/ids")
    @ApiOperation("根据主键id查询机构的信息")
    R<Map<Integer, SysOfficeVO>> listById(@RequestBody Set<Integer> idList);


    /**
     * 根据主键id查询机构的中文
     *
     * @param idList 机构id集合
     * @return
     */
    @PostMapping("/feign/platform/sys/office/ch")
    @ApiOperation("根据主键id查询机构的信息")
    R<Map<Integer, String>> listByIdCh(@RequestBody Set<Integer> idList);
}
