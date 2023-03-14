package com.steccn.data.controller;

import cn.hutool.core.util.IdUtil;
import com.steccn.common.vo.R;
import com.steccn.data.domain.TSysUser;
import com.steccn.data.service.TSysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yequan
 * @desc
 * @date 2023/3/14
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/data/platform")
public class TSysUserController {

    @Autowired
    private final TSysUserService tSysUserService;

    @GetMapping("/test")
    public R<Boolean> epMeter() {
        for (int i = 1; i < 20; i++) {
            TSysUser tSysUser = new TSysUser();
            tSysUser.setId(IdUtil.getSnowflake().nextId());
            tSysUser.setUsername(i + "");
            tSysUserService.save(tSysUser);
        }
        return R.success();
    }

}
