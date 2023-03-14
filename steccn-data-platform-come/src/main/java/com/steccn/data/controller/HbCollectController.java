package com.steccn.data.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.data.constant.HbThirdPartyConstant;
import com.steccn.data.dto.CollectCommonDTO;
import com.steccn.data.dto.EpMeterCollectDTO;
import com.steccn.data.dto.PmStationCollectDTO;
import com.steccn.data.dto.QtThirdPartyDefectiveDTO;
import com.steccn.data.service.EpMeterCollectService;
import com.steccn.data.service.HbCollectService;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author yequan
 * @desc 湖北博华数据采集
 */
@Api("湖北博华数据采集")
//@RestController
//@RequiredArgsConstructor
@RequestMapping("/data/platform")
public class HbCollectController {

    private HbCollectService hbCollectService;
    private RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    @PostMapping("/ep/meter")
    @ApiOperation(value = "能源绩效-远传仪表")
    public R<Boolean> epMeter(HttpServletRequest request, @RequestBody CollectCommonDTO<EpMeterCollectDTO> dto) {
        validRequestHeader(request);
        Assert.notEmpty(dto.getData(), "数据集合为空");
        return R.success(hbCollectService.epMeter(dto));
    }

    @PostMapping("/pm/station")
    @ApiOperation(value = "生产管理-进出站计数")
    public R<Boolean> pmStation(HttpServletRequest request, @RequestBody CollectCommonDTO<PmStationCollectDTO> dto) {
        validRequestHeader(request);
        Assert.notEmpty(dto.getData(), "数据集合为空");
        return R.success(hbCollectService.pmStation(dto));
    }

    @PostMapping("/qt/defect")
    @ApiOperation("保存推送过来的缺陷项")
    public R<Boolean> saveDefective(HttpServletRequest request, @RequestBody QtThirdPartyDefectiveDTO dto) {
        validRequestHeader(request);
        return R.success(hbCollectService.saveDefective(dto));
    }

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(HbThirdPartyConstant.STR_CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }
}
