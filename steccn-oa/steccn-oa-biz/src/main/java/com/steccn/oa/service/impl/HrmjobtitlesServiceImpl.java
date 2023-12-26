package com.steccn.oa.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.domain.Hrmjobtitles;
import com.steccn.oa.service.HrmjobtitlesService;
import com.steccn.oa.mapper.HrmjobtitlesMapper;
import com.steccn.oa.vo.OaJobTitleVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
* @author Admin
* @description 针对表【hrmjobtitles】的数据库操作Service实现
* @createDate 2023-05-30 16:48:30
*/
@Service
public class HrmjobtitlesServiceImpl extends ServiceImpl<HrmjobtitlesMapper, Hrmjobtitles>
    implements HrmjobtitlesService{

    @Override
    public OaJobTitleVO getJobInfoById(Integer id) {
        OaJobTitleVO vo = new OaJobTitleVO();
        Hrmjobtitles hrmjobtitles = getById(id);
        if(Objects.isNull(hrmjobtitles)){
            return vo;
        }
        vo.setJobTitleCode(hrmjobtitles.getJobtitlecode());
        vo.setId(hrmjobtitles.getId());
        vo.setJobTitleName(hrmjobtitles.getJobtitlename());
        return vo;
    }

    @Override
    public List<OaJobTitleVO> getAllJobTitle(TupleDTO<LocalDateTime, LocalDateTime> dto) {
        List<OaJobTitleVO> results = Lists.newArrayList();
        List<Hrmjobtitles> list = list(new LambdaQueryWrapper<Hrmjobtitles>()
                .le(dto.getField2() != null, Hrmjobtitles::getModified, dto.getField2())
                .ge(dto.getField1() != null, Hrmjobtitles::getModified, dto.getField1())
                .orderByDesc(Hrmjobtitles::getModified));
        if(CollUtil.isNotEmpty(list)){
            list.forEach(tmp->{
                OaJobTitleVO oaJobTitleVO = new OaJobTitleVO();
                oaJobTitleVO.setId(tmp.getId());
                oaJobTitleVO.setJobTitleName(tmp.getJobtitlename());
                oaJobTitleVO.setJobTitleCode(tmp.getJobtitlecode());
                oaJobTitleVO.setUpdateTime(tmp.getModified());
                results.add(oaJobTitleVO);
            });
        }
        return results;
    }
}




