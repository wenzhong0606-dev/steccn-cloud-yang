package com.steccn.authserver;

import com.steccn.common.vo.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

//@FeignClient("user-center")
@FeignClient(contextId = "userInfoService", value = "user-center")
public interface UserInfoService {
    @RequestMapping("/user/userinfo/name/{name}")
    R getUserInfoByname(@PathVariable String name);
}