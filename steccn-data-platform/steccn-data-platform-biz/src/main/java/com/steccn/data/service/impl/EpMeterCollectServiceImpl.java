package com.steccn.data.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.data.dto.DwMeterReadDTO;
import com.steccn.data.service.EpMeterCollectService;
import com.steccn.data.mapper.EpMeterCollectMapper;
import org.apache.commons.compress.utils.Lists;
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
    public List<DwEpMeterCollectVO> meterRead(List<DwMeterReadDTO> dwList) {
        List<DwEpMeterCollectVO> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dwList)) {
            //批量查询
            List<EpMeterCollect> dbList = this.baseMapper.batchSearch(dwList);
            if (CollUtil.isNotEmpty(dbList)) {
                resList = BeanUtil.copyToList(dbList, DwEpMeterCollectVO.class);
            }
        }
        return resList;
    }
}
