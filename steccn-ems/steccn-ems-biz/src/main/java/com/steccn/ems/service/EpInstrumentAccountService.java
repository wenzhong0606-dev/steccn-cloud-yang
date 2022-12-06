package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpInstrumentAccountDTO;
import com.steccn.ems.dto.EpInstrumentAccountPageDTO;
import com.steccn.ems.vo.EpInstrumentAccountPageVO;
import com.steccn.ems.vo.EpInstrumentAccountVO;

import java.util.List;

/**
* @author yequan
* @description 针对表【ep_instrument_account(仪表台账)】的数据库操作Service
* @createDate 2022-11-29 19:27:28
*/
public interface EpInstrumentAccountService extends IService<EpInstrumentAccount> {

    /**
     * 新增仪表台账
     * @param dto
     * @return
     */
    Boolean addOrEdit(EpInstrumentAccountDTO dto);

    /**
     * 根据仪表名称模糊查询以及所属分区节点id查询分页列表
     * @param dto
     * @return
     */
    Page<EpInstrumentAccountPageVO> getEpInstrumentAccountVOPage(EpInstrumentAccountPageDTO dto);

    /**
     * 获取所有未删除仪表所属分区节点id
     * @return
     */
    List<Long> getPartitionNodes ();
}
