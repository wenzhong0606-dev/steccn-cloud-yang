package com.steccn.oa.service;

import com.steccn.oa.domain.FormtableMain441;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.vo.OaFormTableMain441VO;

import java.util.List;

/**
* @author Admin
* @description 针对表【formtable_main_441】的数据库操作Service
* @createDate 2024-12-11 10:11:25
*/
public interface FormtableMain441Service extends IService<FormtableMain441> {

    /**
     *
     * @param yearMonth yyyy-MM
     * @return
     */
    List<OaFormTableMain441VO> synPmsPlan(String yearMonth);
}
