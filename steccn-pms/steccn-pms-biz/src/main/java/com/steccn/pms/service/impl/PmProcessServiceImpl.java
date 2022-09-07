package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.vo.R;
import com.steccn.minio.fegin.RemoteFileFeignApi;
import com.steccn.minio.vo.FileInfoVO;
import com.steccn.pms.domain.PmProcess;
import com.steccn.pms.domain.PmProcessStation;
import com.steccn.pms.service.PmProcessService;
import com.steccn.pms.mapper.PmProcessMapper;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.service.PmProcessStationService;
import com.steccn.pms.vo.PmProcessStationVO;
import com.steccn.pms.vo.PmProcessVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.hutool.core.bean.BeanUtil;

import java.util.List;
import java.util.Map;
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

    @Autowired
    private RemoteFileFeignApi remoteFileFeignApi;


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
        //6.处理数据
        List<Long> ids = Lists.newArrayList();
        res.forEach(pmProcessVO -> {
            ids.add(pmProcessVO.getFileId());
            LambdaQueryWrapper<PmProcessStation> wrapper = new LambdaQueryWrapper<PmProcessStation>().eq(PmProcessStation::getProcessId, pmProcessVO.getId().intValue());
            List<PmProcessStationVO> pmProcessStationVOS = BeanUtil.copyToList(processStationService.list(wrapper), PmProcessStationVO.class);
            pmProcessVO.setList(pmProcessStationVOS);
        });
        //获取文件信息
        R<Map<Long, FileInfoVO>> fileLists = remoteFileFeignApi.listById(ids);
        res.forEach(pmProcessVO -> {
            if (!Objects.isNull(pmProcessVO.getFileId())){
                try {
                    pmProcessVO.setFileUrl(fileLists.getData().get(pmProcessVO.getFileId()).getFileUrl());
                } catch (Exception e) {
                    throw new BusinessException( pmProcessVO.getName() +":文件信息不存在");
                }
            }
        });
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
        PmProcess pmProcess = getById(dto.getId());
        if (Objects.isNull(pmProcess)) {
            throw new BusinessException("编辑的工序不存在");
        }
        PmProcess model = new PmProcess();
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




