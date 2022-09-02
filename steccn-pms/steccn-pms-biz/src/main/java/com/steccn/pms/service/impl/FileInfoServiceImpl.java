package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.FileInfo;
import com.steccn.pms.service.FileInfoService;
import com.steccn.pms.mapper.FileInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【file_info(文件信息)】的数据库操作Service实现
* @createDate 2022-09-02 17:01:20
*/
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo>
    implements FileInfoService{

}




