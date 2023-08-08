package com.steccn.mes.service;

import com.steccn.mes.domain.MesEqpSpare;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.mes.vo.MesSpareDownloadVO;

import java.util.List;

/**
* @author Admin
* @description 针对表【mes_eqp_spare(备品备件表)】的数据库操作Service
* @createDate 2023-08-04 13:15:46
*/
public interface MesEqpSpareService extends IService<MesEqpSpare> {
    /**
     * 下载备品备件
     */
    List<MesSpareDownloadVO> downloadSpare();

}
