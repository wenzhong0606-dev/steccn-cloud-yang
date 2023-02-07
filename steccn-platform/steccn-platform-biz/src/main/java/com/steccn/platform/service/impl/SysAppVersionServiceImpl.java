package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.minio.fegin.RemoteFileFeignApi;
import com.steccn.minio.vo.FileInfoVO;
import com.steccn.platform.domain.SysAppVersion;
import com.steccn.platform.dto.SysAppVersionDTO;
import com.steccn.platform.service.SysAppVersionService;
import com.steccn.platform.mapper.SysAppVersionMapper;
import com.steccn.platform.vo.SysAppVersionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【sys_app_version(app的版本控制表)】的数据库操作Service实现
 * @createDate 2023-01-31 14:02:33
 */
@Service
@RequiredArgsConstructor
public class SysAppVersionServiceImpl extends ServiceImpl<SysAppVersionMapper, SysAppVersion>
        implements SysAppVersionService {

    private final RemoteFileFeignApi remoteFileFeignApi;

    @Override
    public Page<SysAppVersionVO> pageList(SysAppVersionDTO dto) {
        Page<SysAppVersionVO> page = new Page<>();
        LambdaQueryWrapper<SysAppVersion> wrapper = new LambdaQueryWrapper<SysAppVersion>().orderByDesc(SysAppVersion::getCreateTime);
        Page<SysAppVersion> resPage = page(new Page<>(dto.getCurrent(), dto.getSize()), wrapper);
        BeanUtil.copyProperties(resPage, page);
        List<SysAppVersionVO> res = BeanUtil.copyToList(resPage.getRecords(), SysAppVersionVO.class);
        //获取文件的url
        List<Long> ids = Lists.newArrayList();
        res.forEach(sysAppVersionVO -> ids.add(sysAppVersionVO.getFileId()));
        Map<Long, FileInfoVO> fileInfoMap = FeignOptional.of(() -> remoteFileFeignApi.listById(ids), FeignConstant.STECCN_MINIO_SERVICE_NAME).get();
        res.forEach(sysAppVersionVO -> sysAppVersionVO.setFileUrl(fileInfoMap.get(sysAppVersionVO.getFileId()).getFileUrl()));
        return page.setRecords(res);
    }

    @Override
    public Boolean add(SysAppVersionDTO dto) {
        SysAppVersion model = new SysAppVersion();
        BeanUtil.copyProperties(dto, model);
        return save(model);
    }

    @Override
    public SysAppVersionVO getLatestVersion() {
        SysAppVersionVO sysAppVersionVO = new SysAppVersionVO();
        SysAppVersion version = getOne(new LambdaQueryWrapper<SysAppVersion>().orderByDesc(SysAppVersion::getCreateTime).last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(version)) {
            List<Long> ids = Lists.newArrayList();
            ids.add(version.getFileId());
            Map<Long, FileInfoVO> fileInfoMap = FeignOptional.of(() -> remoteFileFeignApi.listById(ids), FeignConstant.STECCN_MINIO_SERVICE_NAME).get();
            BeanUtil.copyProperties(version, sysAppVersionVO);
            sysAppVersionVO.setFileUrl(fileInfoMap.get(sysAppVersionVO.getFileId()).getFileUrl());
            return sysAppVersionVO;
        } else {
            return null;
        }
    }
}




