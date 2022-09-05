package com.steccn.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.vo.R;
import com.steccn.file.domain.FileInfo;
import org.springframework.web.multipart.MultipartFile;

/**
* @author MongKnife
* @description 针对表【file_info(文件信息)】的数据库操作Service
* @createDate 2022-09-05 13:34:24
*/
public interface FileInfoService extends IService<FileInfo> {

    R<FileInfo> uploadFile(MultipartFile multipartFile);
}
