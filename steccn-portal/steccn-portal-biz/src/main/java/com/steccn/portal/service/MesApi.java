package com.steccn.portal.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * mes系统登录菜单相关
 *
 * @author Mongknife
 * @date 2022/8/31
 */
@FeignClient(name = "mesMenuService",url = "61.177.139.229:380",decode404 = true)
public interface MesApi {

    /**
     * 获取mes 菜单
     * @param parentCode 上级菜单节点编码
     * @return
     */
    @GetMapping(value = "/web/a/menuRoute")
    @ResponseBody
    List<Map<String,Object>> menuRoute(String parentCode);
}
