package com.steccn.qms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.qms.domain.QtImproveMember;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtImproveMemberDTO;
import com.steccn.qms.vo.QtImproveMemberVO;

import java.util.List;

/**
 * @author yequan
 * @description 针对表【qms_improve_member(整改成员)】的数据库操作Service
 * @createDate 2023-03-15 10:16:54
 */
public interface QtImproveMemberService extends IService<QtImproveMember> {

    /**
     * 整改成员列表
     *
     * @return
     */
    List<QtImproveMemberVO> memberList();

    /**
     * 整改成员-添加
     *
     * @param dto 请求参数
     * @return
     */
    Boolean addMember(QtImproveMemberDTO dto);

    /**
     * 整改成员-移除
     *
     * @param id 主键
     * @return
     */
    Boolean delete(Integer id);
}
