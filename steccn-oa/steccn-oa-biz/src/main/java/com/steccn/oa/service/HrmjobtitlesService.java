package com.steccn.oa.service;

import com.steccn.oa.domain.Hrmjobtitles;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.vo.OaJobTitleVO;

/**
* @author Admin
* @description 针对表【hrmjobtitles】的数据库操作Service
* @createDate 2023-05-30 16:48:30
*/
public interface HrmjobtitlesService extends IService<Hrmjobtitles> {
    /**
     * 根据岗位id查询数据
     * @param id
     * @return
     */
    OaJobTitleVO getJobInfoById(Integer id);
}
