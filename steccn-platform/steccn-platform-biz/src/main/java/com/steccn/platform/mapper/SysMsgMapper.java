package com.steccn.platform.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.platform.domain.SysMsg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.platform.dto.SlidePageDTO;
import com.steccn.platform.vo.SysMsgPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author yequan
 * @description 针对表【sys_msg(消息表)】的数据库操作Mapper
 * @createDate 2023-02-16 15:21:15
 * @Entity com.steccn.platform.domain.SysMsg
 */
@Mapper
public interface SysMsgMapper extends BaseMapper<SysMsg> {

    /**
     * 获取每个tab的数字
     *
     * @param typeList 消息分类
     * @param userId   用户id
     * @return 数字
     */
    Integer tabCount(@Param("typeList") List<Integer> typeList, @Param("userId") Long userId);

    /**
     * 分页获取消息列表
     *
     * @param page     分页参数
     * @param typeList 类型
     * @return page
     */
    List<SysMsgPageVO> msgPage(@Param("page") SlidePageDTO page, @Param("typeList") List<Integer> typeList, @Param("userId") Long userId);
}




