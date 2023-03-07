package com.steccn.canal.handler;

import cn.hutool.core.bean.BeanUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.canal.domain.JsSysEqpSpare;
import com.steccn.canal.domain.JsSysSpareUnitConversion;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.dto.EqpSpareDto;
import com.steccn.platform.dto.SysSpareUnitConversionDTO;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.util.Objects;


@CanalTable(value = "mes_eqp_spare_unit_conversion")
@Component
@Slf4j
@RequiredArgsConstructor
public class JsSysSpareUnitConversionHandler implements EntryHandler<JsSysSpareUnitConversion>{
    private final RemoteSpareFeignApi remoteSpareFeignApi;

    /**
     * 新增操作
     *
     * @param conversion
     */
    public void insert(JsSysSpareUnitConversion conversion) {
        //查询数据是否已经存在
        SysSpareUnitConversionVO sysSpareUnitConversionVO = FeignOptional.of(() -> remoteSpareFeignApi.getConversion(conversion.getId()),
                FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (Objects.isNull(sysSpareUnitConversionVO.getId())){
            SysSpareUnitConversionDTO sysSpareUnitConversionDTO = new SysSpareUnitConversionDTO();
            BeanUtil.copyProperties(conversion,sysSpareUnitConversionDTO);
            Boolean aBoolean = FeignOptional.of(() -> remoteSpareFeignApi.saveConversion(sysSpareUnitConversionDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            log.info("新增 {}" +aBoolean,conversion);
        }

    }

    /**
     * 对于更新操作来讲，before 中的属性只包含变更的属性，after 包含所有属性，通过对比可发现那些属性更新了
     *
     * @param before
     * @param after
     */
    public void update(JsSysSpareUnitConversion before, JsSysSpareUnitConversion after) {
        SysSpareUnitConversionDTO sysSpareUnitConversionDTO = new SysSpareUnitConversionDTO();
        BeanUtil.copyProperties(after,sysSpareUnitConversionDTO);
        Boolean aBoolean = FeignOptional.of(() -> remoteSpareFeignApi.updateConversion(sysSpareUnitConversionDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        log.info("更新 {} {}" + aBoolean, before, after);
    }

    /**
     * 删除操作
     *
     * @param conversion
     */
    public void delete(JsSysSpareUnitConversion conversion) {
        Boolean aBoolean = FeignOptional.of(() -> remoteSpareFeignApi.deleteConversion(conversion.getId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        log.info("删除 {}" + aBoolean, conversion);
    }
}
