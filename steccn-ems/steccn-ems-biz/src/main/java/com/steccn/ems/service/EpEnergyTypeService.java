package com.steccn.ems.service;

import com.steccn.ems.domain.EpEnergyType;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpEnergyTypeDTO;
import com.steccn.ems.vo.EpEnergyTypeVO;

import java.util.List;

/**
* @author yequan
* @description 针对表【ep_energy_type(能源类型表)】的数据库操作Service
* @createDate 2022-11-29 19:27:28
*/
public interface EpEnergyTypeService extends IService<EpEnergyType> {

    /**
     * 新增或编辑能源类型
     * @param dto
     * @return
     */
    Boolean addOrEdit(EpEnergyTypeDTO dto);

    /**
     *根据id查询
     * @param id
     * @return
     */
    EpEnergyTypeVO getEpAddEnergyTypeVO(Integer id);

    /**
     * 查询能源列表
     * @return
     */
    List<EpEnergyTypeVO> getEpEnergyTypeVOList();

    /**
     *根据id逻辑删除
     * @param id
     * @return
     */
    Boolean delete(Integer id);
}
