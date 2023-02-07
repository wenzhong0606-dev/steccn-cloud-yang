package com.steccn.oa.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.oa.dto.OaLoginParam;
import com.steccn.oa.dto.OaUserDTO;
import com.steccn.oa.dto.OaUserInfo;
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

   @GetMapping("/oa/menuList")
   R getMenuList(@RequestParam("userId") String userId);

    /**
     * 获取应用token
     *
     * @param loginId
     * @return
     */
   @GetMapping("/oa/ssoToken")
   R getSsoToken(@RequestParam("loginId") String loginId);


    /**
     * 根据用户名查询用户
     * @param username 用户id
     * @return
     */
   @GetMapping("/oa/username")
   R<OaUserDTO> loadByUsername(@RequestParam("username") String username);

    /**
     * 根据用户名查询用户信息
     * @param loginId
     * @return
     */
   @GetMapping("/oa/userInfo")
   R<OaUserInfo> getOaUserInfo(@RequestParam("loginId") String loginId);

    /**
     * 根据工号查询id
     * @param workCode
     * @return
     */
    @GetMapping("/oa/user/id/{workCode}")
    R<Long> getUserIdByWorkCode(@PathVariable("workCode") String workCode);
}
