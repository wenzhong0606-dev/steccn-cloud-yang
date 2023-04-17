package com.steccn.oa.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/04/14
 */
@Mapper
public interface OaWorkflowDeleteDetailMapper {
    /**
     * 根据requestId删除不合格品详情表
     * @param requestId
     * @return
     */
    Integer deleteUnqualified(Integer requestId);

    /**
     * 根据requestId删除客诉详情表1
     * @param requestId
     * @return
     */
    Integer deleteComplain1(Integer requestId);

    /**
     * 根据requestId删除不合格品详情表2
     * @param requestId
     * @return
     */
    Integer deleteComplain2(Integer requestId);
}
