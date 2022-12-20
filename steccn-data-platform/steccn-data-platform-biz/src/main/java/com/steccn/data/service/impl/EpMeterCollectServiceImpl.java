package com.steccn.data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.dto.DwConditionDTO;
import com.steccn.data.dto.DwEpMeterCollectVO;
import com.steccn.data.service.EpMeterCollectService;
import com.steccn.data.mapper.EpMeterCollectMapper;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yequan
 * @description 针对表【ep_meter_collect(能源绩效-远程仪表采样表)】的数据库操作Service实现
 * @createDate 2022-12-16 13:23:55
 */
@Service
public class EpMeterCollectServiceImpl extends ServiceImpl<EpMeterCollectMapper, EpMeterCollect>
        implements EpMeterCollectService {

    @Override
    public List<DwEpMeterCollectVO> meterRead(List<DwConditionDTO> dwList) {
        List<DwEpMeterCollectVO> resList = Lists.newArrayList();
        LambdaQueryWrapper<EpMeterCollect> lqw = new LambdaQueryWrapper<>();
        if (CollUtil.isNotEmpty(dwList)) {
            for (DwConditionDTO ele : dwList) {
                lqw = lqw.or(l -> {
                    l.eq(EpMeterCollect::getCode, ele.getCode()).eq(EpMeterCollect::getRecordTime, ele.getStart());
                });
            }
        }
        List<EpMeterCollect> dbList = list(lqw);
        if (CollUtil.isNotEmpty(dbList)) {
            resList = BeanUtil.copyToList(dbList, DwEpMeterCollectVO.class);
        }
        return resList;
    }
}
