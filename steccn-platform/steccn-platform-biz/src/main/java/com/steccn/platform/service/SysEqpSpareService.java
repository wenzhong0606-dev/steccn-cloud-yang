package com.steccn.platform.service;

import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.domain.SysEqpSpare;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import io.swagger.models.auth.In;

import java.util.List;
import java.util.Map;

/**
* @author W10
* @description 针对表【sys_eqp_spare(Mes备品备件表)】的数据库操作Service
* @createDate 2022-09-02 16:22:51
*/
public interface SysEqpSpareService extends IService<SysEqpSpare> {

    List<EqpSpareVO> listSpares(String name);

    List<EqpSpareVO> listSpares(String name,String code);

    Map<Long, EqpSpareVO> searchSpares(String name, String code);

    EqpSpareVO getByCode(String code);

    Map<String,EqpSpareVO> listByCode(List<String> codeList);

    Map<Long,EqpSpareVO>  listById(List<Long> materialIds);
}
