package com.steccn.qms.mapper;

import com.steccn.qms.domain.QmsComplainDefectLink;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【qms_complain_defect_link(客诉，不合格品关联整改表)】的数据库操作Mapper
* @createDate 2023-03-15 10:16:53
* @Entity com.steccn.qms.domain.QmsComplainDefectLink
*/
@Mapper
public interface QmsComplainDefectLinkMapper extends BaseMapper<QmsComplainDefectLink> {

}




