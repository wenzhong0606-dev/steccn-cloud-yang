package com.steccn.qms.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.qms.vo.QtDefectiveItemVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * @author yequan
 * @desc 缺陷项|检验项配置
 * @date 2023/10/18
 */
@FeignClient(contextId = "remoteItemFeignApi", value = FeignConstant.STECCN_QMS_SERVICE_NAME)
public interface RemoteItemFeignApi {

    @ApiOperation("根据类别获取缺陷项集合")
    @GetMapping("/feign/qms/item/defect")
    R<List<QtDefectiveItemVO>> getDefectItemByCategory(@RequestParam("categoryName") String categoryName);

}
