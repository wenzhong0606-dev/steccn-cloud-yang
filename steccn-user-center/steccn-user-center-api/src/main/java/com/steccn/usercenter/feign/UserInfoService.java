package com.steccn.usercenter.feign;

import com.steccn.common.vo.R;
import com.steccn.usercenter.dto.UserSaveDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(contextId = "userInfoService", value = "user-center")
public interface UserInfoService {
    @GetMapping("/user/userinfo/name")
    R getUserInfoByname(@RequestParam("name") String name);


    @PostMapping("/user/userinfo/save/test")
    R<Boolean> saveUserInfo(@RequestBody UserSaveDTO userSaveDTO);
}