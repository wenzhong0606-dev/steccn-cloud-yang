package com.steccn.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.vo.R;
import com.steccn.file.domain.FileInfo;
import com.steccn.minio.vo.FileInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
* @author MongKnife
* @description 针对表【file_info(文件信息)】的数据库操作Service
* @createDate 2022-09-05 13:34:24
*/
public interface FileInfoService extends IService<FileInfo> {

    R<FileInfo> uploadFile(MultipartFile multipartFile);

    Map<Long, FileInfoVO> listByIdList(List<Long> idList);
}
