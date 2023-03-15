package com.steccn.qms.mapper;

import com.steccn.qms.domain.QtComplainDefectImproveMan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【qms_complain_defect_improve_man(客诉,不合格品，整改审批人，抄送人)】的数据库操作Mapper
* @createDate 2023-03-15 10:16:53
* @Entity com.steccn.qms.domain.QmsComplainDefectImproveMan
*/
@Mapper
public interface QmsComplainDefectImproveManMapper extends BaseMapper<QtComplainDefectImproveMan> {

}




