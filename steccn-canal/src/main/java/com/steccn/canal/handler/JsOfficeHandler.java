package com.steccn.canal.handler;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.canal.domain.JsOffice;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@CanalTable(value = "js_sys_office")
@Component
@Slf4j
@RequiredArgsConstructor
public class JsOfficeHandler implements EntryHandler<JsOffice> {


    private final RemoteOfficeFeign remoteOfficeFeign;

    /**
     * 新增
     *
     * @param jsOffice
     */
    @Override
    public void insert(JsOffice jsOffice) {
        //根据机构编码查询机构数据是否存在
        Set<String> codes = Sets.newHashSet(jsOffice.getOfficeCode());
        Map<String, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(codes), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (CollUtil.isEmpty(officeMap)) {
            SysOfficeDTO sysOfficeDTO = new SysOfficeDTO();
            BeanUtil.copyProperties(jsOffice, sysOfficeDTO);
            //保存机构信息
            Boolean aBoolean = FeignOptional.of(() -> remoteOfficeFeign.save(sysOfficeDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            log.info("新增 {}" + aBoolean, jsOffice);
        }
    }

    /**
     * 更新
     *
     * @param before
     * @param after
     */
    @Override
    public void update(JsOffice before, JsOffice after) {
        SysOfficeDTO sysOfficeDTO = new SysOfficeDTO();
        BeanUtil.copyProperties(after, sysOfficeDTO);
        //保存机构信息
        Boolean aBoolean = FeignOptional.of(() -> remoteOfficeFeign.update(sysOfficeDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        log.info("更新 {}" + aBoolean, after);
    }


    /**
     * 删除
     *
     * @param jsOffice
     */
    @Override
    public void delete(JsOffice jsOffice) {
        SysOfficeDTO sysOfficeDTO = new SysOfficeDTO();
        BeanUtil.copyProperties(jsOffice, sysOfficeDTO);
        Boolean aBoolean = FeignOptional.of(() -> remoteOfficeFeign.delete(sysOfficeDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        log.info("删除 {}" + aBoolean, sysOfficeDTO);
    }
}
