package com.steccn.qms.controller;

import cn.hutool.core.util.StrUtil;
import com.steccn.common.vo.R;
import com.steccn.qms.constant.QtThirdPartyConstant;
import com.steccn.qms.dto.QtThirdPartyDefectiveDTO;
import com.steccn.qms.service.QtThirdPartyService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/third/party")
@Slf4j
public class QtThirdPartyController {

    private final QtThirdPartyService qtThirdPartyService;

    /**
     * 接口已过期  - 迁移到steccn-data-platform-come
     * @param request
     * @param dto
     * @return
     */
    @PostMapping("/defective")
    @ApiOperation("保存推送过来的缺陷项")
    @Deprecated
    public R<Integer> saveDefective(HttpServletRequest request, @RequestBody QtThirdPartyDefectiveDTO dto) {
//        //校验请求头里是否含有clientId
//        String clientId = request.getHeader(QtThirdPartyConstant.STR_CLIENT_ID);
//        if (StrUtil.isBlank(clientId)) {
//            return R.failure("请求头中必须携带client-id");
//        }
//        if (!QtThirdPartyConstant.THIRD_CLIENT_ID.equals(clientId)) {
//            return R.failure("非法访问，请携带正确的client-id");
//        }
//        return qtThirdPartyService.saveDefective(dto);
         throw new RuntimeException("接口过期");
    }
}
