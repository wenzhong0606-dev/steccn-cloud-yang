package com.steccn.file.controller.feign;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.file.domain.FileInfo;
import com.steccn.file.service.FileInfoService;
import com.steccn.minio.dto.FileInfoDto;
import com.steccn.minio.vo.FileInfoVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/5
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/minio/file")
public class FileInfoFeignController {

    private final FileInfoService fileInfoService;

    @RequestMapping("/ids")
    @ApiOperation("根据主键id查询文件信息")
    public R<Map<Long, FileInfoVO>> listById(@RequestBody List<Long> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(fileInfoService.listByIdList(idList));
    }

    @PostMapping("/upload")
    @ResponseBody
    public R<FileInfoVO> uploadFile(FileInfoDto fileInfoDto){
        FileInfoVO vo = new FileInfoVO();
        BeanUtil.copyProperties(fileInfoService.uploadFile(fileInfoDto),vo);
        return  R.success(vo);
    }
}
