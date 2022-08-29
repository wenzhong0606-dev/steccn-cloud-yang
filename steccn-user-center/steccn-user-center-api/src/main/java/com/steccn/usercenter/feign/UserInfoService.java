package com.steccn.usercenter.feign;

import com.steccn.common.vo.R;
import com.steccn.usercenter.dto.UserSaveDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(contextId = "userInfoService", value = "user-center")
public interface UserInfoService {
    @RequestMapping("/user/userinfo/name/{name}")
    R getUserInfoByname(@PathVariable String name);


    @RequestMapping("/user/userinfo/save")
    R<Boolean> saveUserInfo(@RequestBody UserSaveDTO userSaveDTO);
}