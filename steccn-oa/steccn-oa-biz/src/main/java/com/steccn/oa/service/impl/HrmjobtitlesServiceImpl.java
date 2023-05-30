package com.steccn.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.oa.domain.Hrmjobtitles;
import com.steccn.oa.service.HrmjobtitlesService;
import com.steccn.oa.mapper.HrmjobtitlesMapper;
import com.steccn.oa.vo.OaJobTitleVO;
import org.springframework.stereotype.Service;

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
}




