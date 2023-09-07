package com.steccn.canal.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.canal.domain.JsSysEqpSpare;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.dto.EqpSpareDto;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.qms.feign.RemoteCheckTaskDetailFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Objects;

@CanalTable(value = "mes_eqp_spare")
@Component
@Slf4j
@RequiredArgsConstructor
public class JsSysEqpSpareHandler implements EntryHandler<JsSysEqpSpare> {
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteCheckTaskDetailFeignApi remoteCheckTaskDetailFeignApi;

    /**
     * 新增操作
     *
     * @param spare
     */
    public void insert(JsSysEqpSpare spare) {
//        查询数据是否存在,不存在则插入，存在则跳过
        EqpSpareVO eqpSpareVO = FeignOptional.of(() -> remoteSpareFeignApi.getById(Long.valueOf(spare.getId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (Objects.isNull(eqpSpareVO.getId())){
            EqpSpareDto eqpSpareDto = new EqpSpareDto();
            BeanUtil.copyProperties(spare,eqpSpareDto);
            eqpSpareDto.setCreateDate(LocalDateTimeUtil.of(spare.getCreateDate()));
            eqpSpareDto.setUpdateDate(LocalDateTimeUtil.of(spare.getUpdateDate()));
            System.out.println(eqpSpareDto.getCreateDate());
            Boolean aBoolean = FeignOptional.of(() -> remoteSpareFeignApi.save(eqpSpareDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //新增至qms
            EqpSpareDto eqpSpareDtoQms = new EqpSpareDto();
            BeanUtils.copyProperties(spare,eqpSpareDtoQms);
            if(StrUtil.isNotBlank(spare.getId())){
                eqpSpareDtoQms.setId(Long.parseLong(spare.getId()));
            }
            Boolean bBoolean = FeignOptional.of(() -> remoteCheckTaskDetailFeignApi.synchronizeMaterial(eqpSpareDtoQms), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
            log.info("新增 {}" + aBoolean, spare);
        }

    }

    /**
     * 对于更新操作来讲，before 中的属性只包含变更的属性，after 包含所有属性，通过对比可发现那些属性更新了
     *
     * @param before
     * @param after
     */
    public void update(JsSysEqpSpare before, JsSysEqpSpare after) {
        EqpSpareDto eqpSpareDto = new EqpSpareDto();
        BeanUtil.copyProperties(after,eqpSpareDto);
        Boolean aBoolean = FeignOptional.of(() -> remoteSpareFeignApi.update(eqpSpareDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //新增至qms
        EqpSpareDto eqpSpareDtoQms = new EqpSpareDto();
        BeanUtils.copyProperties(after,eqpSpareDtoQms);
        if(StrUtil.isNotBlank(after.getId())){
            eqpSpareDtoQms.setId(Long.parseLong(after.getId()));
        }
        Boolean bBoolean = FeignOptional.of(() -> remoteCheckTaskDetailFeignApi.synchronizeMaterial(eqpSpareDtoQms), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
        log.info("更新 {} {}" + aBoolean, before, after);

    }

    /**
     * 删除操作
     *
     * @param spare
     */
    public void delete(JsSysEqpSpare spare) {
        Boolean aBoolean = FeignOptional.of(()->remoteSpareFeignApi.delete(Long.valueOf(spare.getId())),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        log.info("删除 {}" + aBoolean, spare);
    }
}
