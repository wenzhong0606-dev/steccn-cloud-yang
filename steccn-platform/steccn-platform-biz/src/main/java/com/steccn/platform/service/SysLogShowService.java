package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.TupleDTO;
import com.steccn.platform.domain.SysLogShow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysLogShowPageDTO;
import com.steccn.platform.vo.SysLogShowPageVO;

import java.util.List;

/**
 * @author yequan
 * @description 针对表【sys_log_show】的数据库操作Service
 * @createDate 2024-03-27 15:39:38
 */
public interface SysLogShowService extends IService<SysLogShow> {

    /**
     * 访问日志-列表
     *
     * @param dto
     * @return
     */
    Page<SysLogShowPageVO> pageList(SysLogShowPageDTO dto);

    /**
     * 访问日志-所有用户
     *
     * @param name
     * @return
     */
    List<TupleDTO<Long, String>> userList(String name);
}
