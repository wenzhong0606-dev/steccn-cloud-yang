package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmBillOfMaterial;
import com.steccn.pms.domain.PmMaterial;
import com.steccn.pms.dto.PmBillOfMaterialDTO;
import com.steccn.pms.service.PmBillOfMaterialService;
import com.steccn.pms.mapper.PmBillOfMaterialMapper;
import com.steccn.pms.service.PmMaterialService;
import com.steccn.pms.vo.PmBillOfMaterialVO;
import com.steccn.pms.vo.PmMaterialVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【pm_bill_of_material(物料清单表)】的数据库操作Service实现
 * @createDate 2022-09-02 17:01:20
 */
@Service
@RequiredArgsConstructor
public class PmBillOfMaterialServiceImpl extends ServiceImpl<PmBillOfMaterialMapper, PmBillOfMaterial>
        implements PmBillOfMaterialService {

    private final PmMaterialService pmMaterialService;

    @Override
    public R<Boolean> add(PmBillOfMaterialDTO dto) {
        PmBillOfMaterial model = new PmBillOfMaterial();
        //拷贝数据，设置创建人时间
        BeanUtil.copyProperties(dto, model);
        model.setCreateBy(SecurityUtils.getUser().getUserId());
        model.setCreateName(SecurityUtils.getUser().getName());
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        //保存数据
        save(model);
        if (!Objects.isNull(dto.getList())) {
            dto.setId(model.getId());
            return R.success(pmMaterialService.saveMaterial(dto));
        }
        return R.success(true);
    }

    @Override
    public Page<List<PmBillOfMaterialVO>> pageList(PmBillOfMaterialDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<PmBillOfMaterial> eq = new LambdaQueryWrapper<PmBillOfMaterial>()
                .eq(Objects.nonNull(dto.getMaterialCode()), PmBillOfMaterial::getMaterialCode, dto.getMaterialCode())
                .like(Objects.nonNull(dto.getMaterialName()), PmBillOfMaterial::getMaterialName, dto.getMaterialName())
                .eq(Objects.isNull(dto.getStatus()), PmBillOfMaterial::getStatus, dto.getStatus())
                .eq(Objects.isNull(dto.getIsCurrentVersion()), PmBillOfMaterial::getIsCurrentVersion, dto.getIsCurrentVersion())
                .eq(Objects.isNull(dto.getCode()), PmBillOfMaterial::getCode, dto.getCode());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, eq);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<PmBillOfMaterialVO> res = BeanUtil.copyToList(resPage.getRecords(), PmBillOfMaterialVO.class);
        //6.处理数据
        res.forEach(billOfMaterial -> {
            LambdaQueryWrapper<PmMaterial> wrapper = new LambdaQueryWrapper<PmMaterial>().eq(PmMaterial::getBomId, billOfMaterial.getId());
            List<PmMaterial> list = pmMaterialService.list(wrapper);
            billOfMaterial.setList(BeanUtil.copyToList(list, PmMaterialVO.class));
        });
        //7.返回数据
        return page.setRecords(res);
    }

    @Override
    public R<Boolean> edit(PmBillOfMaterialDTO dto) {
        //校验是否存在
        PmBillOfMaterial pmBillOfMaterial = getById(dto.getId());
        if (Objects.isNull(pmBillOfMaterial)) {
            throw new BusinessException("编辑的物料清单不存在");
        }
        PmBillOfMaterial model = new PmBillOfMaterial();
        BeanUtils.copyProperties(dto, model);
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        if (!Objects.isNull(dto.getList())) {
            pmMaterialService.deleteByMaterialId(dto.getId());
            Boolean a = pmMaterialService.saveMaterial(dto);
            if (a == false) {
                throw new BusinessException("物料组件数据保存失败");
            }
        }
        //保存数据
        return R.success(updateById(model));
    }

    @Override
    public Boolean delete(Integer id) {
        return removeById(id);
    }

}




