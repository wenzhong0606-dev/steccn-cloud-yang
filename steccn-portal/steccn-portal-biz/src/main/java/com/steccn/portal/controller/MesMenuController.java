package com.steccn.portal.controller;

import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.steccn.common.vo.R;
import com.steccn.portal.service.MesApi;
import com.steccn.portal.service.MesMenuService;
import com.steccn.portal.util.MesToken;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

/**
 * mes系统接口
 *
 * @author Mongknife
 * @date 2022/8/31
 */
@Api(tags = "mes系统接口")
@RestController
@RequestMapping("/portal/mes")
@RequiredArgsConstructor
public class MesMenuController {

    private final MesMenuService mesMenuService;


    @ApiOperation("获取mes系统用户菜单数据")
    @GetMapping("/menuRoute")
    public R menuRoute(@RequestParam("userName") String userName) throws IOException {
        return mesMenuService.menuRoute(userName);
    }

    @ApiOperation(value = "获取mes跳过登录的token")
    @GetMapping("/mesToken")
    public R getMesToken(@RequestParam("userName") String userName) {
        R<Object> r = new R<>();
        String mesToken = mesMenuService.getMesToken(userName);
        if (Objects.isNull(mesToken)){
            r.setMsg("生成token失败");
        }else {
            r.setMsg("生成token成功");
            r.setData(mesToken);
        }
        return r;
    }

}


