package com.steccn.platform.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.dto.SysScreenConfigDTO;
import com.steccn.platform.service.SysOauthClientDetailService;
import com.steccn.platform.service.SysScreenConfigDetailService;
import com.steccn.platform.service.SysScreenConfigService;
import com.steccn.platform.vo.SysScreenConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 屏幕配置控制器
 *
 * @author AndrewLuo
 * @date 2024/04/22
 */
@Api("屏幕配置控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/screen")
public class SysScreenController {
    private final SysScreenConfigService sysScreenConfigService;
    private final SysOauthClientDetailService sysOauthClientDetailService;

    @ApiOperation("新增屏幕配置")
    @PostMapping()
    public R<Boolean> add(@RequestBody SysScreenConfigDTO dto) {
        return R.success(sysScreenConfigService.add(dto));
    }

    @ApiOperation("新增屏幕配置")
    @PostMapping("list")
    public R<Page<SysScreenConfigVO>> pageList(@RequestBody BasePageDTO dto) {
        return R.success(sysScreenConfigService.pageList(dto));
    }

    @ApiOperation("外部访问屏幕配置列表")
    @PostMapping("/out/list")
    public R<Page<SysScreenConfigVO>> pageListOut(HttpServletRequest request,@RequestBody BasePageDTO dto) {
        validRequestHeader(request);
        return R.success(sysScreenConfigService.pageList(dto));
    }

    @ApiOperation("修改屏幕配置")
    @PutMapping()
    public R<Boolean> updateData(@RequestBody SysScreenConfigDTO dto) {
        return R.success(sysScreenConfigService.updateData(dto));
    }

    @ApiOperation("删除屏幕配置")
    @DeleteMapping("/{id}")
    public R<Boolean> deleteData(@PathVariable("id") Integer id) {
        return R.success(sysScreenConfigService.deleteData(id));
    }

    @ApiOperation("外部访问获取屏幕配置详情")
    @GetMapping("/{id}")
    public R<SysScreenConfigVO> infoOut(@PathVariable("id") Integer id) {
        return R.success(sysScreenConfigService.info(id));
    }

    @ApiOperation("获取屏幕配置详情")
    @GetMapping("/out/{id}")
    public R<SysScreenConfigVO> info(HttpServletRequest request,@PathVariable("id") Integer id) {
        validRequestHeader(request);
        return R.success(sysScreenConfigService.info(id));
    }

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = sysOauthClientDetailService.clientEncodePassword().stream().collect(Collectors.toList());
        String clientId = request.getHeader(OaDataConstant.STR_CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }
}
