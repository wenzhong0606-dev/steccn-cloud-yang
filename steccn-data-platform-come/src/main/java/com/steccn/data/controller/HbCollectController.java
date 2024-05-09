package com.steccn.data.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.ValidList;
import com.steccn.common.vo.R;
import com.steccn.data.constant.HbThirdPartyConstant;
import com.steccn.data.dto.*;
import com.steccn.data.service.EpMeterCollectService;
import com.steccn.data.service.HbCollectService;
import com.steccn.data.service.MesEqpSignalService;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author yequan
 * @desc 湖北博华数据采集
 */
@Api("湖北博华数据采集")
@RestController
@RequiredArgsConstructor
@RequestMapping("/data/platform")
public class HbCollectController {

    private final HbCollectService hbCollectService;
    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;
    private final MesEqpSignalService mesEqpSignalService;

    /**
     * 能源绩效-远传仪表
     * @param request
     * @param dto
     * @return
     */
    @PostMapping("/ep/meter")
    public R<Boolean> epMeter(HttpServletRequest request, @RequestBody CollectCommonDTO<EpMeterCollectDTO> dto) {
        validRequestHeader(request);
        Assert.notEmpty(dto.getData(), "数据集合为空");
        return R.success(hbCollectService.epMeter(dto));
    }

    /**
     * 生产管理-进出站计数
     * @param request
     * @param dto
     * @return
     */
    @PostMapping("/pm/station")
    public R<Boolean> pmStation(HttpServletRequest request, @RequestBody CollectCommonDTO<PmStationCollectDTO> dto) {
        validRequestHeader(request);
        Assert.notEmpty(dto.getData(), "数据集合为空");
        return R.success(hbCollectService.pmStation(dto));
    }

    /**
     * 保存推送过来的缺陷项
     * @param request
     * @param dto
     * @return
     */
    @PostMapping("/qt/defect")
    public R<Boolean> saveDefective(HttpServletRequest request, @RequestBody QtThirdPartyDefectiveDTO dto) {
        validRequestHeader(request);
        return R.success(hbCollectService.saveDefective(dto));
    }

    /**
     * 设备管理-设备启停信号
     * @param request
     * @param dto
     * @return
     */
    @PostMapping("/mes/signal")
    public R<Boolean> mesEqpSignal(HttpServletRequest request, @RequestBody @Validated CollectCommonValidDTO<MesEqpSignalDTO> dto) {
        validRequestHeader(request);
        Assert.notEmpty(dto.getData(), "数据集合为空");
        return R.success(hbCollectService.mesEqpSignal(dto));
    }

    @GetMapping("/mes/generateReport")
    public R<Boolean> generateReport() {
        return R.success(mesEqpSignalService.saveReport());
    }

    /**
     * 设备管理-设备启停信号
     * @param request
     * @param dto
     * @return
     */
    @PostMapping("/qt/screen")
    public R<Boolean> screenData(HttpServletRequest request, @RequestBody @Validated CollectCommonValidDTO<ScreenDataDTO> dto) {
        validRequestHeader(request);
        Assert.notEmpty(dto.getData(), "数据集合为空");
        return R.success(hbCollectService.saveScreenData(dto));
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
