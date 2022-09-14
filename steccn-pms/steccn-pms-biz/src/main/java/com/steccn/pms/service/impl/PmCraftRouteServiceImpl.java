package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.PmCraftRouteDTO;
import com.steccn.pms.dto.PmCraftRouteProcessDTO;
import com.steccn.pms.service.PmCraftRouteModuleService;
import com.steccn.pms.service.PmCraftRouteProcessService;
import com.steccn.pms.service.PmCraftRouteService;
import com.steccn.pms.mapper.PmCraftRouteMapper;
import com.steccn.pms.service.PmProcessService;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author yequan
 * @description 针对表【pm_craft_route(生产工艺路线
 * )】的数据库操作Service实现
 * @createDate 2022-09-02 17:01:20
 */
@Service
@RequiredArgsConstructor
public class PmCraftRouteServiceImpl extends ServiceImpl<PmCraftRouteMapper, PmCraftRoute>
        implements PmCraftRouteService {

    private final PmCraftRouteModuleService pmCraftRouteModuleService;

    private final PmCraftRouteProcessService pmCraftRouteProcessService;

    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;

    @Override
    public R<Boolean> add(PmCraftRouteDTO dto) {
        PmCraftRoute model = new PmCraftRoute();
        //拷贝数据，设置创建人时间
        BeanUtil.copyProperties(dto, model);
        model.setCreateBy(SecurityUtils.getUser().getUserId());
        model.setCreateName(SecurityUtils.getUser().getName());
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        //保存数据
        save(model);
        if (!Objects.isNull(dto.getList())) {
            //保存物料组件信息
            try {
                dto.setId(model.getId());
                pmCraftRouteModuleService.saveMaterial(dto);
            } catch (Exception e) {
                throw new BusinessException("保存物料组件信息失败");
            }
        }
        if (!Objects.isNull(dto.getProcessDTOList())) {
            //保存工序信息
            try {
                pmCraftRouteProcessService.saveProcess(dto);
            } catch (Exception e) {
                throw new BusinessException("保存工序信息失败");
            }
        }
        return R.success(true);
    }

    @Override
    public Page<PmCraftRouteVO> pageList(PmCraftRouteDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<PmCraftRoute> eq = new LambdaQueryWrapper<PmCraftRoute>()
                .eq(Objects.nonNull(dto.getMaterialCode()) && !dto.getMaterialCode().equals(""),PmCraftRoute::getMaterialCode, dto.getMaterialCode())
                .like(Objects.nonNull(dto.getMaterialName()), PmCraftRoute::getMaterialName, dto.getMaterialName())
                .eq(Objects.nonNull(dto.getStatus()), PmCraftRoute::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getIsCurrentVersion()), PmCraftRoute::getIsCurrentVersion, dto.getIsCurrentVersion())
                .eq(Objects.nonNull(dto.getCode()) && !dto.getCode().equals(""), PmCraftRoute::getCode, dto.getCode());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, eq);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<PmCraftRouteVO> res = BeanUtil.copyToList(resPage.getRecords(), PmCraftRouteVO.class);
        //6.处理数据
        List<Long> ids = Lists.newArrayList();
        List<Long> processIds = Lists.newArrayList();
        res.forEach(craftRouteVO -> {
            LambdaQueryWrapper<PmCraftRouteModule> wrapper = new LambdaQueryWrapper<PmCraftRouteModule>().eq(PmCraftRouteModule::getCraftRouteId, craftRouteVO.getId());
            List<PmCraftRouteModule> list = pmCraftRouteModuleService.list(wrapper);
            craftRouteVO.setList(BeanUtil.copyToList(list, PmCraftRouteModuleVO.class));
            craftRouteVO.getList().forEach(craftRouteModuleVO -> {
                ids.add(craftRouteModuleVO.getUnitId());
                processIds.add(craftRouteModuleVO.getProcessId());
            });
        });
        //获取字典数据
        R<Map<Long, SysDictVO>> dict = remoteDictFeignApi.listById(ids);
        //将数据处理返回
        res.forEach(craftRouteVO ->
                craftRouteVO.getList().forEach(moduleVO -> {
                    try {
                        SysDictVO sysDictVO = dict.getData().get(moduleVO.getUnitId());
                        if (Objects.isNull(sysDictVO)){
                            moduleVO.setUnit(null);
                        }else {
                            moduleVO.setUnit(sysDictVO.getName());
                        }
                    } catch (Exception e) {
                        throw new BusinessException("数据异常");
                    }
                })
        );
        //7.返回数据
        return page.setRecords(res);
    }

    @Override
    public R<Boolean> edit(PmCraftRouteDTO dto) {
        //校验是否存在
        PmCraftRoute pmCraftRoute = getById(dto.getId());
        if (Objects.isNull(pmCraftRoute)) {
            throw new BusinessException("编辑的工艺路线不存在");
        }
        PmCraftRoute model = new PmCraftRoute();
        BeanUtils.copyProperties(dto, model);
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        //生成编码
        model.setCode(PmsCodeUtil.getNextCode(PmsCodeUtil.PREFIX_R,PmsCodeUtil.NUM_BIT_8,currentMaxCode()));
        pmCraftRouteModuleService.deleteByRoteId(dto.getId());
        if (CollUtil.isNotEmpty(dto.getList())) {
            try {
                pmCraftRouteModuleService.saveMaterial(dto);
            } catch (Exception e) {
                throw new BusinessException("物料组件数据保存失败");
            }
        }
        //保存数据
        return R.success(updateById(model));
    }

    @Override
    public R<Boolean> delete(Integer id) {
        return R.success(removeById(id));
    }


    /**
     * 当前最大的code值
     *
     * @return 数值类型，可能为null
     */
    private Long currentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        PmCraftRoute maxCodeModel = getOne(new LambdaQueryWrapper<PmCraftRoute>().orderByDesc(PmCraftRoute::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(PmsCodeUtil.PREFIX_B.length());
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }
}




