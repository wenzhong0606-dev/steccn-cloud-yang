package com.steccn.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.domain.Hrmjobtitles;
import com.steccn.oa.vo.OaJobTitleVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 根据id集合查询岗位数据
     *
     * @param idSet 集合
     * @return
     */
    Map<Integer, String> listByIdCh(Set<Integer> idSet);

    /**
     * 获取所有岗位信息
     * @param dto
     * @return
     */
    List<OaJobTitleVO> getAllJobTitle(TupleDTO<LocalDateTime,LocalDateTime> dto);
}
