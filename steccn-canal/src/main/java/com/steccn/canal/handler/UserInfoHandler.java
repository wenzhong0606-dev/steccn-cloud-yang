package com.steccn.canal.handler;

import com.steccn.canal.domain.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

@CanalTable(value = "user_info")
@Component
@Slf4j
public class UserInfoHandler implements EntryHandler<UserInfo> {
    /**
     * 新增操作
     *
     * @param user
     */
    @Override
    public void insert(UserInfo user) {
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
    public void update(UserInfo before, UserInfo after) {
        //你的逻辑
        log.info("更新 {} {}", before, after);
    }

    /**
     * 删除操作
     *
     * @param user
     */
    @Override
    public void delete(UserInfo user) {
        //你的逻辑
        log.info("删除 {}", user);
    }
}
