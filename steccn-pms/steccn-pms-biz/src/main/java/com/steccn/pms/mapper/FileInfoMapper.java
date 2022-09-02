package com.steccn.pms.mapper;

import com.steccn.pms.domain.FileInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【file_info(文件信息)】的数据库操作Mapper
* @createDate 2022-09-02 17:01:20
* @Entity com.steccn.pms.domain.FileInfo
*/
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {

}




