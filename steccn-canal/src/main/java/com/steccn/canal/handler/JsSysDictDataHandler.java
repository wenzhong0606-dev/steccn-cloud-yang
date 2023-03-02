package com.steccn.canal.handler;

import com.steccn.canal.domain.JsSysDictData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

@CanalTable(value = "js_sys_dict_data")
@Component
@Slf4j
public class JsSysDictDataHandler implements EntryHandler<JsSysDictData> {
    /**
     * 新增操作
     *
     * @param user
     */
    @Override
    public void insert(JsSysDictData user) {
        //你的逻辑
        log.info("新增 {}", user);
    }

    /**
     * 对于更新操作来讲，before 中的属性只包含变更的属性，after 包含所有属性，通过对比可发现那些属性更新了
     *
     * @param before
     * @param after
     */
    @Override
    public void update(JsSysDictData before, JsSysDictData after) {
        //你的逻辑
        log.info("更新 {} {}", before, after);
    }

    /**
     * 删除操作
     *
     * @param user
     */
    @Override
    public void delete(JsSysDictData user) {
        //你的逻辑
        log.info("删除 {}", user);
    }
}
