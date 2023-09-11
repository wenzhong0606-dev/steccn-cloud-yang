package com.steccn.mes.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.vo.R;
import com.steccn.mes.service.MesEqpInsTaskService;
import com.steccn.mes.service.MesEquipmentService;
import com.steccn.mes.vo.MesEqpVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 获取设备信息
 *
 * @author AndrewLuo
 * @date 2023/09/01
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mes/eqp")
@Slf4j
public class MesEqpController {
    private final MesEquipmentService mesEquipmentService;
    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;
    private final MesEqpInsTaskService mesEqpInsTaskService;

    @GetMapping("/thirdParty")
    @ApiOperation(value = "获取设备信息")
    public R<List<MesEqpVO>> getEqpInfo(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(mesEquipmentService.getEqpInfo());
    }

    @GetMapping("/dashboard/task")
    @ApiOperation(value = "点检任务图表")
    public R<QtLineChartBarDTO> taskCompleteChart(@RequestParam("startTime")LocalDateTime startTime,@RequestParam("endTime") LocalDateTime endTime) {
        return R.success(mesEqpInsTaskService.taskComplete(startTime,endTime));
    }

    @GetMapping("/dashboard/item")
    @ApiOperation(value = "点检项图表")
    public R<QtLineChartBarDTO> itemCompleteChart(@RequestParam("startTime")LocalDateTime startTime,@RequestParam("endTime") LocalDateTime endTime) {
        return R.success(mesEqpInsTaskService.itemComplete(startTime,endTime));
    }

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(OaConstant.CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }
}
