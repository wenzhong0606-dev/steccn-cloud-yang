package com.steccn.canal.handler;

import com.steccn.authserver.util.FeignOptional;
import com.steccn.canal.domain.JsSysDictData;
import com.steccn.canal.domain.JsSysDictType;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.dto.SysDictTypeDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.lang.ref.PhantomReference;
import java.util.Objects;

@CanalTable(value = "js_sys_dict_type")
@Component
@Slf4j
@RequiredArgsConstructor
public class JsSysDictTypeHandler implements EntryHandler<JsSysDictType> {


    private final RemoteDictFeignApi remoteDictFeignApi;




    /**
     * 新增操作
     * @param user
     */
    public void insert(JsSysDictType user) {
        //查询数据是否已存在
        SysDictTypeDTO dict = FeignOptional.of(() -> remoteDictFeignApi.typeById(Long.valueOf(user.getId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (Objects.isNull(dict.getId())){
            //不存在则插入
            SysDictTypeDTO sysDictTypeDTO = new SysDictTypeDTO();
            sysDictTypeDTO.setType(user.getDictType());
            sysDictTypeDTO.setId(Long.valueOf(user.getId()));
            sysDictTypeDTO.setName(user.getDictName());
            Boolean aBoolean = FeignOptional.of(() -> remoteDictFeignApi.saveType(sysDictTypeDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            log.info("新增 {}" + aBoolean, user);
        }
    }

    /**
     * 对于更新操作来讲，before 中的属性只包含变更的属性，after 包含所有属性，通过对比可发现那些属性更新了
     *
     * @param before
     * @param after
     */
    public void update(JsSysDictType before, JsSysDictType after) {

        SysDictTypeDTO sysDictTypeDTO = new SysDictTypeDTO();
        sysDictTypeDTO.setType(after.getDictType());
        sysDictTypeDTO.setId(Long.valueOf(after.getId()));
        sysDictTypeDTO.setName(after.getDictName());
        Boolean aBoolean = FeignOptional.of(() -> remoteDictFeignApi.updateType(sysDictTypeDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        log.info("更新 {} {}" + aBoolean , before, after );
    }


    /**
     * 删除操作
     *
     * @param user
     */
    public void delete(JsSysDictType user) {
        Boolean aBoolean = FeignOptional.of(() -> remoteDictFeignApi.deleteType(Long.valueOf(user.getId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        log.info("删除 {}" + aBoolean, user);
    }



}
