package com.steccn.hikvision.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.hikvision.vo.HikCompanyVO;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/05/17
 */
@FeignClient(contextId = "remoteMeterReadFeignApi", value = FeignConstant.STECCN_HIK_SERVICE_NAME)
public interface RemoteHikFeignApi {
    /**
     * 获取海康威视公司数据 f1 companyId f2 companyName
     * @param dto
     * @return
     */
    @PostMapping("/feign/hikvision/company")
    R<List<HikCompanyVO>> hikCompanyInfo(@RequestBody TupleDTO<String,String> dto);

    /**
     * 根据流程单号匹配图片URL
     * @param weighNo
     * @return
     */
    @GetMapping("/feign/hikvision/weighRegUrl")
    R<String> weighRegUrl(@RequestParam("weighNo") String weighNo);
}
