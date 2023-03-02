package com.steccn.canal.handler;

import cn.hutool.core.lang.Assert;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.canal.domain.JsSysDictData;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.util.Objects;

@CanalTable(value = "js_sys_dict_data")
@Component
@Slf4j
@RequiredArgsConstructor
public class JsSysDictDataHandler implements EntryHandler<JsSysDictData> {

    private final RemoteDictFeignApi remoteDictFeignApi;

    /**
     * 新增操作
     *
     * @param user
     */
    @Override
    public void insert(JsSysDictData user) {
        //查询数据是否已存在
        SysDictVO dictVO = FeignOptional.of(() -> remoteDictFeignApi.get(Long.valueOf(user.getDictCode())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (Objects.isNull(dictVO)) {
            SysDictDTO sysDictDTO = new SysDictDTO();
            sysDictDTO.setId(Long.valueOf(user.getDictCode()));
            sysDictDTO.setType(user.getDictType());
            sysDictDTO.setName(user.getDictLabel());
            sysDictDTO.setCode(user.getDictValue());
            sysDictDTO.setParentId(Long.valueOf(user.getParentCode()));
            sysDictDTO.setSort(Integer.valueOf(user.getTreeSort()));
            Boolean aBoolean = FeignOptional.of(() -> remoteDictFeignApi.save(sysDictDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            log.info("新增 {}" + aBoolean, user);
        }
    }

    /**
     * 对于更新操作来讲，before 中的属性只包含变更的属性，after 包含所有属性，通过对比可发现那些属性更新了
     *
     * @param before
     * @param after
     */
    @Override
    public void update(JsSysDictData before, JsSysDictData after) {
        SysDictDTO sysDictDTO = new SysDictDTO();
        sysDictDTO.setId(Long.valueOf(after.getDictCode()));
        sysDictDTO.setType(after.getDictType());
        sysDictDTO.setName(after.getDictLabel());
        sysDictDTO.setCode(after.getDictValue());
        sysDictDTO.setParentId(Long.valueOf(after.getParentCode()));
        sysDictDTO.setSort(Integer.valueOf(after.getTreeSort()));
        Boolean aBoolean = FeignOptional.of(() -> remoteDictFeignApi.update(sysDictDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        log.info("更新 {} {}" + aBoolean, before, after);
    }

    /**
     * 删除操作
     *
     * @param user
     */
    @Override
    public void delete(JsSysDictData user) {
        Boolean aBoolean = FeignOptional.of(() -> remoteDictFeignApi.delete(Long.valueOf(user.getDictCode())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        log.info("删除 {}" + aBoolean, user);
    }
}
