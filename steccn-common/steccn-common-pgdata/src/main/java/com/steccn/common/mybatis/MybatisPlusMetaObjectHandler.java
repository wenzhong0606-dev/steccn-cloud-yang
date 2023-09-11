package com.steccn.common.mybatis;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.constant.MpConstant;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.logging.log4j.util.Strings;
import java.time.LocalDateTime;

/**
 * 自动补充插入或更新时的值
 *
 * @author Y
 * @date 2022-01-06 19:10
 */
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        SteccnUser user = SecurityUtils.getUser();
        long userId = ObjectUtil.isNull(user) ? 0 : user.getUserId();
        String name = ObjectUtil.isNull(user) ? Strings.EMPTY : user.getName();
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createBy", Long.class, userId);
        strictInsertFill(metaObject, "createName", String.class, name);
        strictInsertFill(metaObject, "updateBy", Long.class, userId);
        strictInsertFill(metaObject, "updateName", String.class, name);
        setFieldValByName("createTime", now, metaObject);
        setFieldValByName("updateTime", now, metaObject);
        setFieldValByName("delFlag", MpConstant.DELETE_FLAG_NORMAL, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        SteccnUser user = SecurityUtils.getUser();
        long userId = ObjectUtil.isNull(user) ? 0 : user.getUserId();
        String name = ObjectUtil.isNull(user) ? Strings.EMPTY : user.getName();
        setFieldValByName("updateBy", userId, metaObject);
        setFieldValByName("updateName", name, metaObject);
        setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}
