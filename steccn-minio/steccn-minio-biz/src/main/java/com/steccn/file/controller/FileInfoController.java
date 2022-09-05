package com.steccn.file.controller;

import com.steccn.common.vo.R;
import com.steccn.file.domain.FileInfo;
import com.steccn.file.service.FileInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/5
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/minio")
public class FileInfoController {

    private final FileInfoService fileInfoService;


    @RequestMapping(value = "/test",method = RequestMethod.POST)
    @ResponseBody
    public R<FileInfo> uploadFile(@RequestParam("file") MultipartFile multipartFile){
      return  fileInfoService.uploadFile(multipartFile);
    }
}
