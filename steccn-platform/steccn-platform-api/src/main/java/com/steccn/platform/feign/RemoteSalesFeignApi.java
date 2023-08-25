package com.steccn.platform.feign;

import com.steccn.common.vo.R;
import com.steccn.platform.dto.SalesUserNameDTO;
import com.steccn.platform.vo.SalesTokenVO;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 销售系统接口
 *
 * @author AndrewLuo
 * @date 2023/08/23
 */
@FeignClient(name = "remoteSalesFeignApi", url = "${sales.url}", decode404 = true)
public interface RemoteSalesFeignApi {
    /**
     * 根据分类id查询分类信息
     *
     * @param dto
     * @return
     */
    @PostMapping(value = "/api/get_token")
    @Headers(value = {"content-type=application/json"})
    @ResponseBody
    String getToken(@RequestBody SalesUserNameDTO dto);
}
