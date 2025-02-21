package com.steccn.minio.fegin;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.minio.dto.FileInfoDto;
import com.steccn.minio.vo.FileInfoVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@FeignClient(contextId = "remoteFileFeignApi",value = FeignConstant.STECCN_MINIO_SERVICE_NAME)
public interface RemoteFileFeignApi {

    /**
     * 根据主键id查询文件信息
     *
     * @param idList 文件id集合
     * @return
     */
    @RequestMapping("feign/minio/file/ids")
    @ApiOperation("根据主键id查询文件信息")
    R<Map<Long, FileInfoVO>> listById(@RequestBody List<Long> idList);

    @PostMapping("feign/minio/file/upload")
    @ResponseBody
    R<FileInfoVO> uploadFile(FileInfoDto fileInfoDto);
}
