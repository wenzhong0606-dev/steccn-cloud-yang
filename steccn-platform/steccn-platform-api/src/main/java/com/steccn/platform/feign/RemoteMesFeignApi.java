package com.steccn.platform.feign;

import com.steccn.common.vo.R;
import com.steccn.platform.dto.MesUserDTO;
import com.steccn.platform.vo.MesClassificationVO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 请求mes接口api
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
@FeignClient(name = "remoteMesFeignApi", url = "${mes.url}", decode404 = true)
public interface RemoteMesFeignApi {
    /**
     * 根据分类id查询分类信息
     *
     * @param classIds
     * @return
     */
    @PostMapping(value = "/web/mesClassification/getNameByIds")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    Map<String,String> getClassInfoById(@RequestBody Set<String> classIds);

    /**
     * 根据分类id查询分类信息
     *
     * @param mesUserDTO
     * @return
     */
    @PostMapping(value = "/web/user/save")
    @ResponseBody
    String saveUser(@RequestBody MesUserDTO mesUserDTO);

    /**
     * 根据用户名查询mes用户名
     * @param loginCode
     * @return
     */
    @GetMapping(value = "/web/user/getUserCode")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    R<String> getUserCodeByLoginCode(@RequestParam("loginCode") String loginCode);

    /**
     * 停用mes用户
     * @param loginCode
     * @return
     */
    @GetMapping(value = "/web/user/disable")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    R<String> disableUser(@RequestParam("loginCode") String loginCode);
}
