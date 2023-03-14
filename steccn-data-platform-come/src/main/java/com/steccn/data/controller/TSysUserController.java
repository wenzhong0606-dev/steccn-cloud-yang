package com.steccn.data.controller;

import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Lists;
import com.steccn.common.vo.R;
import com.steccn.data.domain.TSysUser;
import com.steccn.data.mapper.TSysUserMapper;
import com.steccn.data.service.TSysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc
 * @date 2023/3/14
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/data/platform")
public class TSysUserController {

    private final TSysUserService tSysUserService;
    private final TSysUserMapper tSysUserMapper;

    @GetMapping("/test")
    public R<Boolean> test() {
        for (int i = 1; i < 2; i++) {
            TSysUser tSysUser = new TSysUser();
            tSysUser.setId(IdUtil.getSnowflake().nextId());
            tSysUser.setUsername(i + "");
            tSysUser.setRecordTime(LocalDateTime.now());
            tSysUserService.save(tSysUser);
        }
        return R.success();
    }

    @GetMapping("/test1")
    public R<Boolean> test1() {
        for (int i = 1; i < 2; i++) {
            TSysUser tSysUser = new TSysUser();
            tSysUser.setId(IdUtil.getSnowflake().nextId());
            tSysUser.setUsername(i + "");
            tSysUser.setRecordTime(LocalDateTime.now());
            tSysUserMapper.insert(tSysUser);
        }
        return R.success();
    }
}
