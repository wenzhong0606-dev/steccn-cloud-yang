package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.pms.domain.PmsReportWorkConsume;
import com.steccn.pms.dto.ProcessReportWorkConsumeDTO;
import com.steccn.pms.service.PmsReportWorkConsumeService;
import com.steccn.pms.mapper.PmsReportWorkConsumeMapper;
import com.steccn.pms.vo.EnergyGroupListVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【pms_report_work_consume(报工能耗表)】的数据库操作Service实现
 * @createDate 2022-11-02 15:32:08
 */
@Service
public class PmsReportWorkConsumeServiceImpl extends ServiceImpl<PmsReportWorkConsumeMapper, PmsReportWorkConsume>
        implements PmsReportWorkConsumeService {

    @Override
    public void saveConsume(Long id, List<ProcessReportWorkConsumeDTO> consumeDTOList) {
        if (CollUtil.isNotEmpty(consumeDTOList)) {
            List<PmsReportWorkConsume> insertList = Lists.newArrayList();
            for (ProcessReportWorkConsumeDTO consume : consumeDTOList) {
                PmsReportWorkConsume model = new PmsReportWorkConsume();
                BeanUtils.copyProperties(consume, model);
                model.setReportWorkId(id);
                insertList.add(model);
            }
            saveBatch(insertList);
        }
    }

    @Override
    public List<EnergyGroupListVO> energyGroup(List<Integer> productLineIdList) {
        List<EnergyGroupListVO> groupList = this.baseMapper.energyGroup(productLineIdList);
        return Objects.isNull(groupList) ? Lists.newArrayList() : groupList;
    }
}




