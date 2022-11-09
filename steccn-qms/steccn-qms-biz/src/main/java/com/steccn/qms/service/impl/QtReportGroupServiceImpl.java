package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.qms.domain.QtReport;
import com.steccn.qms.domain.QtReportGroup;
import com.steccn.qms.dto.QtReportGroupDTO;
import com.steccn.qms.service.QtReportGroupService;
import com.steccn.qms.mapper.QtReportGroupMapper;
import com.steccn.qms.service.QtReportService;
import com.steccn.qms.vo.QtReportChartVO;
import com.steccn.qms.vo.QtReportGroupVO;
import com.steccn.qms.vo.QtReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

/**
* @author 86178
* @description 针对表【qt_report_sort】的数据库操作Service实现
* @createDate 2022-11-08 11:29:13
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class QtReportGroupServiceImpl extends ServiceImpl<QtReportGroupMapper, QtReportGroup>
    implements QtReportGroupService {
        private final QtReportService qtReportService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(QtReportGroupDTO dto) {
        QtReportGroup reportGroup = new QtReportGroup();
        BeanUtils.copyProperties(dto, reportGroup);
        QtReportGroup qtReportGroup = getOne(new LambdaQueryWrapper<QtReportGroup>().eq(QtReportGroup::getReportGroupName, dto.getReportGroupName()));
        Assert.isNull(qtReportGroup, "该报表卡片类型已存在,请勿重复创建!");
        this.save(reportGroup);
        return Boolean.TRUE;
    }

    /**
     * 根据分组id查询报表卡片列表
     * @param id
     * @return
     */
    @Override
    public List<QtReportChartVO> getQtReportList(Integer id) {
        List<QtReportChartVO> qtReportChartVOS = qtReportService.getQtReportList(id);

        return qtReportChartVOS;
    }

    @Override
    public List<QtReportGroupVO> getReportGroupVOlist() {
        List<QtReportGroup> groups = list();
        if (CollUtil.isEmpty(groups)){
            throw new BusinessException("暂无分组请先创建");
        }
        List<QtReportGroupVO> qtReportGroupVOS=Lists.newArrayList();
        groups.stream().forEach(g->{
            QtReportGroupVO qtReportGroupVO = new QtReportGroupVO();
            BeanUtils.copyProperties(g,qtReportGroupVO);
            qtReportGroupVOS.add(qtReportGroupVO);
        });
        return qtReportGroupVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deletQtReportGroup(Integer id) {
        if (Objects.isNull(id)){
            throw new BusinessException("分组id不能为空");
        }
        QtReportGroup reportGroup = getById(id);
        if (Objects.isNull(reportGroup)){
            throw new BusinessException("该分组不存在");
        }
        removeById(id);
        //查询报表卡片列表如果改分组下有卡片才去删卡片
        List<QtReport> qtReports = qtReportService.list(new LambdaQueryWrapper<QtReport>().eq(QtReport::getReportGroupId, id));
        if (Objects.nonNull(qtReports)){
            qtReportService.deleteByGroupId(id);
        }

        return Boolean.TRUE;
    }
}




