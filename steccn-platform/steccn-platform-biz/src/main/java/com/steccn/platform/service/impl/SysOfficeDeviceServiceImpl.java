package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.SysOfficeDevice;
import com.steccn.platform.dto.SysOfficeDeviceAddDTO;
import com.steccn.platform.dto.SysOfficeDevicePageDTO;
import com.steccn.platform.dto.SysOfficeDeviceUpdateDTO;
import com.steccn.platform.mapper.SysOfficeDeviceMapper;
import com.steccn.platform.service.SysOfficeDeviceService;
import com.steccn.platform.vo.SysOfficeDeviceVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【sys_office_device(工位设备配置)】的数据库操作Service实现
 * @createDate 2023-05-18 18:02:43
 */
@Service
public class SysOfficeDeviceServiceImpl extends ServiceImpl<SysOfficeDeviceMapper, SysOfficeDevice>
        implements SysOfficeDeviceService {

    @Override
    public Page<SysOfficeDeviceVO> pageList(SysOfficeDevicePageDTO dto) {
        //查询条件
        LambdaQueryWrapper<SysOfficeDevice> condition = new LambdaQueryWrapper<SysOfficeDevice>()
                .eq(Objects.nonNull(dto.getOfficeType()), SysOfficeDevice::getOfficeType, dto.getOfficeType())
                .eq(Objects.nonNull(dto.getOfficeId()), SysOfficeDevice::getOfficeId, dto.getOfficeId())
                .eq(StringUtils.isNotBlank(dto.getCode()), SysOfficeDevice::getCode, dto.getCode())
                .eq(StringUtils.isNotBlank(dto.getName()), SysOfficeDevice::getName, dto.getName());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, condition);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<SysOfficeDeviceVO> res = BeanUtil.copyToList(resPage.getRecords(), SysOfficeDeviceVO.class);
        return page.setRecords(res);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SysOfficeDeviceAddDTO dto) {
        SysOfficeDevice sysOfficeDevice = new SysOfficeDevice();
        BeanUtils.copyProperties(dto, sysOfficeDevice);
        return save(sysOfficeDevice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(SysOfficeDeviceUpdateDTO dto) {
        SysOfficeDevice sysOfficeDevice = new SysOfficeDevice();
        BeanUtils.copyProperties(dto, sysOfficeDevice);
        return updateById(sysOfficeDevice);
    }
}




