package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.platform.domain.SysScreenConfig;
import com.steccn.platform.domain.SysScreenConfigDetail;
import com.steccn.platform.dto.SysScreenConfigDTO;
import com.steccn.platform.mapper.SysScreenConfigMapper;
import com.steccn.platform.service.SysScreenConfigDetailService;
import com.steccn.platform.service.SysScreenConfigService;
import com.steccn.platform.vo.SysScreenConfigDetailVO;
import com.steccn.platform.vo.SysScreenConfigVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
* @author Admin
* @description 针对表【sys_screen_config】的数据库操作Service实现
* @createDate 2024-04-22 15:28:35
*/
@Service
@RequiredArgsConstructor
public class SysScreenConfigServiceImpl extends ServiceImpl<SysScreenConfigMapper, SysScreenConfig>
    implements SysScreenConfigService{
    private final SysScreenConfigDetailService sysScreenConfigDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SysScreenConfigDTO dto) {
        SysScreenConfig model = new SysScreenConfig();
        BeanUtil.copyProperties(dto,model);
        save(model);
        List<SysScreenConfigDetail> detailModels = Lists.newArrayList();
        dto.getList().forEach(tmp->{
            SysScreenConfigDetail detailModel = new SysScreenConfigDetail();
            BeanUtil.copyProperties(tmp,detailModel);
            detailModel.setConfigId(model.getId());
            detailModels.add(detailModel);
        });
        sysScreenConfigDetailService.saveBatch(detailModels);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateData(SysScreenConfigDTO dto) {
        SysScreenConfig model = new SysScreenConfig();
        BeanUtil.copyProperties(dto,model);
        updateById(model);
        sysScreenConfigDetailService.remove(new LambdaQueryWrapper<SysScreenConfigDetail>()
                .eq(SysScreenConfigDetail::getConfigId,model.getId()));
        List<SysScreenConfigDetail> detailModels = Lists.newArrayList();
        dto.getList().forEach(tmp->{
            SysScreenConfigDetail detailModel = new SysScreenConfigDetail();
            BeanUtil.copyProperties(tmp,detailModel);
            detailModel.setConfigId(model.getId());
            detailModels.add(detailModel);
        });
        sysScreenConfigDetailService.saveBatch(detailModels);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteData(Integer id) {
        removeById(id);
        sysScreenConfigDetailService.remove(new LambdaQueryWrapper<SysScreenConfigDetail>()
                .eq(SysScreenConfigDetail::getConfigId,id));
        return true;
    }

    @Override
    public Page<SysScreenConfigVO> pageList(BasePageDTO dto) {
        LambdaQueryWrapper<SysScreenConfig> condition = new LambdaQueryWrapper<SysScreenConfig>()
                .orderByAsc(SysScreenConfig::getPriority);
        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.查询分页数据
        IPage<SysScreenConfig> resPage = page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的信息
        List<SysScreenConfigVO> resRecords = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resPage.getRecords())) {
            for (SysScreenConfig sysScreenConfig : resPage.getRecords()) {
                SysScreenConfigVO vo = new SysScreenConfigVO();
                BeanUtils.copyProperties(sysScreenConfig, vo);
                resRecords.add(vo);
            }
        }
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }

    @Override
    public SysScreenConfigVO info(Integer id) {
        SysScreenConfigVO result = new SysScreenConfigVO();
        SysScreenConfig model = getById(id);
        if(Objects.nonNull(model)){
            BeanUtil.copyProperties(model,result);
            List<SysScreenConfigDetail> details = sysScreenConfigDetailService.list(new LambdaQueryWrapper<SysScreenConfigDetail>()
                    .eq(SysScreenConfigDetail::getConfigId, id));
            List<SysScreenConfigDetailVO> vos = Lists.newArrayList();
            if(CollUtil.isNotEmpty(details)){
                details.forEach(tmp->{
                    SysScreenConfigDetailVO vo = new SysScreenConfigDetailVO();
                    BeanUtil.copyProperties(tmp,vo);
                    vos.add(vo);
                });
            }
            result.setList(vos);
        }
        return result;
    }
}




