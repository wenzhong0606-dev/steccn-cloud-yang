package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmProcess;
import com.steccn.pms.service.PmProcessService;
import com.steccn.pms.mapper.PmProcessMapper;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.service.PmProcessStationService;
import com.steccn.pms.vo.PmProcessVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.util.IdGenerator;

import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【pm_process(生产工序表)】的数据库操作Service实现
 * @createDate 2022-09-02 17:01:20
 */
@Service
@RequiredArgsConstructor
public class PmProcessServiceImpl extends ServiceImpl<PmProcessMapper, PmProcess>
        implements PmProcessService {

    private final PmProcessStationService processStationService;

    private final PmProcessMapper processMapper;

    @Override
    public R<Boolean> saveProcess(PmProcessDTO dto) {
        PmProcess model = new PmProcess();
        BeanUtil.copyProperties(dto, model);
        model.setCreateBy(SecurityUtils.getUser().getUserId());
        model.setCreateName(SecurityUtils.getUser().getName());
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        save(model);
        if (!Objects.isNull(dto.getList())) {
            dto.setId(Math.toIntExact(model.getId()));
            return R.success(processStationService.saveStations(dto));
        }
        return R.success(true);
    }

    @Override
    public Page<PmProcessVO> pageList(PmProcessDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<PmProcess> eq = new LambdaQueryWrapper<PmProcess>()
                .eq(Objects.nonNull(dto.getCode()), PmProcess::getCode, dto.getCode())
                .like(Objects.nonNull(dto.getName()), PmProcess::getName, dto.getName())
                .eq(Objects.nonNull(dto.getType()), PmProcess::getType, dto.getType());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, eq);
        //4.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<PmProcessVO> res = BeanUtil.copyToList(resPage.getRecords(), PmProcessVO.class);
        //6.设置数据
        return page.setRecords(res);
    }

    @Override
    public Boolean delete(Integer id) {
        if (Objects.isNull(getById(id))) {
            throw new BusinessException("删除的数据不存在");
        }
        return removeById(id);
    }

    @Override
    public R<Boolean> edit(PmProcessDTO dto) {
        //校验是否存在
        PmProcess model = getById(dto.getId());
        if (Objects.isNull(model)) {
            throw new BusinessException("编辑的工序不存在");
        }
        model.setName(dto.getName());
        model.setCode(dto.getCode());
        model.setType(dto.getType());
        model.setStatus(dto.getStatus());
        model.setFileId(dto.getFileId());
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        if (!Objects.isNull(dto.getList())) {
            processStationService.deleteByProcessId(dto.getId());
            Boolean a = processStationService.saveStations(dto);
            if (a == false) {
                throw new BusinessException("执行工位数据保存失败");
            }
        }
        //保存数据
        return R.success(updateById(model));
    }


}




