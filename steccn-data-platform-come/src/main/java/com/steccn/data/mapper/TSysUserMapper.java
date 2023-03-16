package com.steccn.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.data.domain.TSysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author yequan
 * @description 针对表【t_sys_user】的数据库操作Mapper
 * @createDate 2023-03-14 17:05:42
 * @Entity com.steccn.domain.TSysUser
 */
@Mapper
public interface TSysUserMapper extends BaseMapper<TSysUser> {


}




