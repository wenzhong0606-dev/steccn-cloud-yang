package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.ems.domain.EpDiscountFactor;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpDiscountFactorDTO;
import com.steccn.ems.dto.EpDiscountFactorPageDTO;
import com.steccn.ems.vo.EpDiscountFactorVO;

import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【ep_discount_factor(折标系数表)】的数据库操作Service
 * @createDate 2024-03-08 09:10:12
 */
public interface EpDiscountFactorService extends IService<EpDiscountFactor> {

    /**
     * 折标系数-分页列表
     *
     * @param dto 请求dto
     * @return
     */
    Page<EpDiscountFactorVO> getPage(EpDiscountFactorPageDTO dto);

    /**
     * 根据id查询折标系数
     *
     * @param id 主键
     * @return
     */
    EpDiscountFactorVO info(Integer id);

    /**
     * 新增折标系数
     *
     * @param dto 请求对象
     * @return
     */
    Boolean add(EpDiscountFactorDTO dto);

    /**
     * 编辑折标系数
     * @param dto 请求对象
     * @return
     */
    Boolean edit(EpDiscountFactorDTO dto);

    /**
     * 获取指定类型下，有效的折标系数集合
     *
     * @param type  系数类型 当量折标系数，等价折标系数，窑龄系数
     * @param start 开始日期
     * @param end   结束日期
     * @return
     */
    List<EpDiscountFactorVO> getValidDiscountList(Long type, LocalDate start, LocalDate end);
}
