package com.steccn.file.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.file.domain.FileInfo;
import org.apache.ibatis.annotations.Mapper;

/**
* @author W10
* @description 针对表【file_info(文件信息)】的数据库操作Mapper
* @createDate 2022-09-05 14:31:50
* @Entity file.domain.FileInfo
*/
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {

}




