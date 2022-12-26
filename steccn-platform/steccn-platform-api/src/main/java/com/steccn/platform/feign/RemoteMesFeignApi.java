package com.steccn.platform.feign;

import com.steccn.platform.vo.MesClassificationVO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
