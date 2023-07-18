package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.qms.domain.QtCheckTaskDestoryInfo;
import com.steccn.qms.dto.QtCheckTaskDestoryInfoDTO;
import com.steccn.qms.dto.QtDestoryInfoFallDTO;
import com.steccn.qms.mapper.QtCheckTaskDestoryInfoMapper;
import com.steccn.qms.service.QtCheckTaskDestoryInfoService;
import com.steccn.qms.vo.QtCheckTaskDestoryInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author 86187
* @description 针对表【qt_check_task_destory_info】的数据库操作Service实现
* @createDate 2022-11-03 10:29:18
*/
@Service
@RequiredArgsConstructor
public class QtCheckTaskDestoryInfoServiceImpl extends ServiceImpl<QtCheckTaskDestoryInfoMapper, QtCheckTaskDestoryInfo>
    implements QtCheckTaskDestoryInfoService {

    private final QtCheckTaskDestoryInfoMapper qtCheckTaskDestoryInfoMapper;

    /**
     * 新增落板记录
     * @param dto
     * @return
     */
    @Override
    public Boolean add(QtCheckTaskDestoryInfoDTO dto) {
        Assert.notNull(dto.getTaskId(),"新增落板记录请求参数任务id为空");
        QtCheckTaskDestoryInfo qtCheckTaskDestoryInfo = new QtCheckTaskDestoryInfo();
        BeanUtils.copyProperties(dto, qtCheckTaskDestoryInfo);
        if (dto.getDestoryReason() == null || dto.getDestoryDetail() == null ||dto.getDestoryStartTime() == null){
            throw new BusinessException("落板原因,落板时段不能为空");
        }
        QtCheckTaskDestoryInfo byTaskId = getById(qtCheckTaskDestoryInfo.getTaskId());
        //判断是否有重复的落板记录
        if (byTaskId != null){
            if (dto.getDestoryReason().equals(byTaskId.getDestoryReason()) && dto.getDestoryStartTime().equals(byTaskId.getDestoryStartTime()) && dto.getDestoryEndTime().equals(byTaskId.getDestoryEndTime())){
                throw new BusinessException("该条落板记录已经存在，不可重复添加");
            }
        }
        return save(qtCheckTaskDestoryInfo);

    }

    /**
     * 新增落板记录列表
     * @param dtoList
     * @return
     */
    @Override
    public Boolean addList(List<QtCheckTaskDestoryInfoDTO> dtoList) {
        Assert.notNull(dtoList.isEmpty(),"新增落板记录列表请求参数为空");
        QtCheckTaskDestoryInfo qtCheckTaskDestoryInfo = new QtCheckTaskDestoryInfo();
        for (QtCheckTaskDestoryInfoDTO dto:dtoList) {
            if (dto.getDestoryReason() == null || dto.getDestoryDetail() == null ||dto.getDestoryStartTime() == null){
                throw new BusinessException("落板原因,落板时段不能为空");
            }
            BeanUtils.copyProperties(dto, qtCheckTaskDestoryInfo);
            QtCheckTaskDestoryInfo byTaskId = getById(qtCheckTaskDestoryInfo.getTaskId());
            //判断是否有重复的落板记录
            if (byTaskId != null){
                if (dto.getDestoryReason().equals(byTaskId.getDestoryReason()) && dto.getDestoryStartTime().equals(byTaskId.getDestoryStartTime()) && dto.getDestoryEndTime().equals(byTaskId.getDestoryEndTime())){
                    throw new BusinessException("该条落板记录已经存在，不可重复添加");
                }
            }
            save(qtCheckTaskDestoryInfo);

        }
        return Boolean.TRUE;
    }

    @Override
    public List<QtDestoryInfoFallDTO> falling(List<Integer> productLineList, LocalDateTime start, LocalDateTime end) {
        return baseMapper.falling(productLineList, start, end);
    }

    /**
     * 删除落板记录
     * @param dto
     * @return
     */
    @Override
    public Boolean delete(QtCheckTaskDestoryInfoDTO dto) {
        Assert.notNull(dto,"删除落板记录不能为空");
        boolean delete = Boolean.FALSE;
        QtCheckTaskDestoryInfo qtCheckTaskDestoryInfo = new QtCheckTaskDestoryInfo();
        BeanUtils.copyProperties(dto,qtCheckTaskDestoryInfo);
        //如果根据下面条件可以找到该条落板记录，则修改删除标志为已删除
        if (dto.getTaskId() != null && dto.getDestoryDetail() != null && dto.getSampleNum() != null){
           QtCheckTaskDestoryInfo qtCheckTaskDestoryInfo1 = getOne(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>()
                   .eq(QtCheckTaskDestoryInfo::getTaskId,qtCheckTaskDestoryInfo.getTaskId())
                   .eq(QtCheckTaskDestoryInfo::getDestoryDetail,qtCheckTaskDestoryInfo.getDestoryDetail())
                   .eq(QtCheckTaskDestoryInfo::getSampleNum,qtCheckTaskDestoryInfo.getSampleNum()));
           qtCheckTaskDestoryInfo1.setDelFlag(1);
            delete = updateById(qtCheckTaskDestoryInfo1);
        }
        return delete;
    }

    @Override
    public List<QtCheckTaskDestoryInfoVO> select(QtCheckTaskDestoryInfoDTO dto) {
        Assert.notNull(dto.getTaskId(),"查询落板记录任务号不能为空");
        QtCheckTaskDestoryInfo qtCheckTaskDestoryInfo = new QtCheckTaskDestoryInfo();
        List<QtCheckTaskDestoryInfo> qtCheckTaskDestoryInfoVoList = list();
        List<QtCheckTaskDestoryInfoVO> qtCheckTaskDestoryInfoVOList = Lists.newArrayList();
        if (CollUtil.isEmpty(qtCheckTaskDestoryInfoVoList)) {
            return qtCheckTaskDestoryInfoVOList;
        }
        BeanUtils.copyProperties(dto,qtCheckTaskDestoryInfo);
        if (dto.getSampleNum()==null){
            throw new BusinessException("样本号不能为空");
        }
        qtCheckTaskDestoryInfoVoList = list(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>()
                .eq(QtCheckTaskDestoryInfo::getTaskId,qtCheckTaskDestoryInfo.getTaskId())
                .eq(QtCheckTaskDestoryInfo::getSampleNum,qtCheckTaskDestoryInfo.getSampleNum()));
        BeanUtil.copyToList(qtCheckTaskDestoryInfoVoList,QtCheckTaskDestoryInfoVO.class);
        return qtCheckTaskDestoryInfoVOList;
    }

}




