package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.ems.domain.CeElectricSetting;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.CeElectricSettingDTO;
import com.steccn.ems.dto.EpDiscountFactorPageDTO;
import com.steccn.ems.vo.CeElectricSettingVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【ce_electric_setting(电力设置表)】的数据库操作Service
 * @createDate 2024-04-18 09:27:24
 */
public interface CeElectricSettingService extends IService<CeElectricSetting> {

    /**
     * 查询电力设置分页列表
     *
     * @param dto 分页对象
     * @return
     */
    Page<CeElectricSettingVO> getPage(BasePageDTO dto);

    /**
     * 根据id查询电力设置
     *
     * @param id 主键
     * @return
     */
    CeElectricSettingVO info(Integer id);

    /**
     * 新增电力设置
     *
     * @param dto 请求对象
     * @return
     */
    Boolean add(CeElectricSettingDTO dto);

    /**
     * 编辑电力设置
     *
     * @param dto 请求对象
     * @return
     */
    Boolean edit(CeElectricSettingDTO dto);

    /**
     * 碳排放总量报表-按日为粒度
     * @param start 开始日期
     * @param end 结束日期
     * @return
     */
    List<BigDecimal> carbonSumReportDay(LocalDate start, LocalDate end);
}
