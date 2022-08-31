package com.steccn.oa.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.oa.dto.OaLoginParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/08/31
 */
@FeignClient(contextId = "oaInfoService", value = FeignConstant.STECCN_OA_SERVICE_NAME)
public interface OaInfoService {
    /**
     * 校验用户名密码，获取userid
     *
     * @param oaLoginParam
     * @return R
     */
    @PostMapping("/oa/checkLogin")
    R checkLogin(@RequestBody OaLoginParam oaLoginParam);

    /**
     * 获取oa菜单
     *
     * @param userId
     * @return
     */
    @GetMapping("/oa/menuList/{userId}")
    R getMenuList(@PathVariable("userId") String userId);

    /**
     * 获取应用token
     *
     * @param loginId
     * @return
     */
    @GetMapping("/oa/ssoToken/{loginId}")
    R getSsoToken(@PathVariable("loginId") String loginId);
}
