package com.steccn.common.mybatis;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
//import com.wenxiangcn.wxcloud.common.core.constant.CommonConstants;
//import com.wenxiangcn.wxcloud.common.data.tenant.TenantContextHolder;
//import com.wenxiangcn.wxcloud.common.security.service.WxcloudUser;
//import com.wenxiangcn.wxcloud.common.security.util.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 自动补充插入或更新时的值
 *
 * @author Y
 * @date 2022-01-06 19:10
 */
public class MybatisPlusMetaObjectHandler /*implements MetaObjectHandler*/ {

//	@Override
//	public void insertFill(MetaObject metaObject) {
//		WxcloudUser user = SecurityUtils.getUser();
//		long userId = ObjectUtil.isNull(user) ? 0 : user.getId();
//		LocalDateTime now = LocalDateTime.now();
//
//		strictInsertFill(metaObject, "createBy", Long.class, userId);
//		strictInsertFill(metaObject, "updateBy", Long.class, userId);
//
//		setFieldValByName("createTime", now, metaObject);
//		setFieldValByName("updateTime", now, metaObject);
//		setFieldValByName("delFlag", CommonConstants.STATUS_NORMAL, metaObject);
//
//		strictInsertFill(metaObject, "tenantId", Integer.class, TenantContextHolder.getTenantId());
//	}
//
//	@Override
//	public void updateFill(MetaObject metaObject) {
//		WxcloudUser user = SecurityUtils.getUser();
//		long userId = ObjectUtil.isNull(user) ? 0 : user.getId();
//		setFieldValByName("updateBy", userId, metaObject);
//		setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
//	}

}
