package com.steccn.platform.mapper;

import com.steccn.platform.domain.PlatformPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【platform_post(岗位表)】的数据库操作Mapper
* @createDate 2025-07-31 16:40:21
* @Entity com.steccn.platform.domain.PlatformPost
*/
@Mapper
public interface PlatformPostMapper extends BaseMapper<PlatformPost> {

}




