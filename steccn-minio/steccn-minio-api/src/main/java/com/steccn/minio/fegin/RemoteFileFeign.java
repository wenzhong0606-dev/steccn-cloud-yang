package com.steccn.minio.fegin;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.minio.vo.FileInfoVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@FeignClient(contextId = "remoteFileFeign",value = FeignConstant.STECCN_MINIO_SERVICE_NAME)
public interface RemoteFileFeign {

    /**
     * 根据主键id查询文件信息
     *
     * @param idList 文件id集合
     * @return
     */
    @RequestMapping("/minio/file/ids")
    @ApiOperation("根据主键id查询文件信息")
    R<Map<Long, FileInfoVO>> listById(@RequestBody List<Long> idList);
}
