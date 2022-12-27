package com.steccn.data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.common.util.SplitListUtil;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.data.dto.DwMeterReadDTO;
import com.steccn.data.service.EpMeterCollectService;
import com.steccn.data.mapper.EpMeterCollectMapper;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【ep_meter_collect(能源绩效-远程仪表采样表)】的数据库操作Service实现
 * @createDate 2022-12-16 13:23:55
 */
@Service
public class EpMeterCollectServiceImpl extends ServiceImpl<EpMeterCollectMapper, EpMeterCollect>
        implements EpMeterCollectService {

    @Override
    public List<DwEpMeterCollectVO> meterRead(List<DwMeterReadDTO> dwList) {
        Set<DwEpMeterCollectVO> dataCollect = Sets.newHashSet();
        if (CollUtil.isNotEmpty(dwList)) {
            List<List<DwMeterReadDTO>> splitList = SplitListUtil.split(dwList, 5);
            for (List<DwMeterReadDTO> split : splitList) {
                //批量查询
                List<EpMeterCollect> dbList = this.baseMapper.batchSearch(split);
                if (CollUtil.isNotEmpty(dbList)) {
                    dataCollect.addAll(BeanUtil.copyToList(dbList, DwEpMeterCollectVO.class));
                }
            }
        }
        return new ArrayList<>(dataCollect);
    }
}
