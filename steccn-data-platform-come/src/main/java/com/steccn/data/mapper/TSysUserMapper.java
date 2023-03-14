package com.steccn.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.data.domain.TSysUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * @author yequan
 * @description 针对表【t_sys_user】的数据库操作Mapper
 * @createDate 2023-03-14 17:05:42
 * @Entity com.steccn.domain.TSysUser
 */
@Mapper
public interface TSysUserMapper extends BaseMapper<TSysUser> {

    @Insert("insert into t_sys_user(id,username,record_time)values(#{id},#{username},#{recordTime})")
    int insertOrder(@Param("id") Long id, @Param("username") String username, @Param("recordTime") LocalDateTime recordTime);
}




