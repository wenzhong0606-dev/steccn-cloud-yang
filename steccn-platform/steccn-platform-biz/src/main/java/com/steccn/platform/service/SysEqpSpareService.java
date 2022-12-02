package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.domain.SysEqpSpare;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SparePageDTO;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import io.swagger.models.auth.In;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author W10
 * @description 针对表【sys_eqp_spare(Mes备品备件表)】的数据库操作Service
 * @createDate 2022-09-02 16:22:51
 */
public interface SysEqpSpareService extends IService<SysEqpSpare> {

    /**
     * 根据名称获取备件列表
     *
     * @param name
     * @return
     */
    List<EqpSpareVO> listSpares(String name);

    /**
     * 根据名称编码 筛选返回List
     *
     * @param name
     * @param code
     * @return
     */
    List<EqpSpareVO> listSpares(String name, String code);

    /**
     * 根据名称编码条件筛选返回Map
     *
     * @param name
     * @param code
     * @return
     */
    Map<Long, EqpSpareVO> searchSpares(String name, String code);

    /**
     * 根据编码获取备件
     *
     * @param code
     * @return
     */
    EqpSpareVO getByCode(String code);

    /**
     * 根据编码批量查询
     *
     * @param codeList
     * @return
     */
    Map<String, EqpSpareVO> listByCode(List<String> codeList);

    /**
     * 根据物料id批量查询
     *
     * @param materialIds
     * @return
     */
    Map<Long, EqpSpareVO> listById(List<Long> materialIds);

    Map<Long, String> listByIdCh(Set<Long> materialIds);

    /**
     * 根据编码批量查询,返回id为key
     *
     * @param codeList
     * @return
     */
    Map<Long, EqpSpareVO> mapByCode(List<String> codeList);


    /**
     * 备件分页列表
     *
     * @param sparePageDTO
     * @return
     */
    Page<EqpSpareVO> listPage(SparePageDTO sparePageDTO);

    /**
     * 根据条件筛选
     * @param spareDTO
     * @return
     */
    List<EqpSpareVO> listCondition(SpareDTO spareDTO);
}
