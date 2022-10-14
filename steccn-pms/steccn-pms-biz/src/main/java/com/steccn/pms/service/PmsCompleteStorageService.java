package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsCompleteStorage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.CompleteStoragePageDTO;
import com.steccn.pms.dto.CompleteStorageScanDTO;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.dto.PmsCompleteStorageNumDTO;
import com.steccn.pms.vo.CompleteStorageListVO;
import com.steccn.pms.vo.CompleteStoragePageVO;
import com.steccn.pms.vo.PmsCompleteStorageVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_complete_storage(生产订单-完工入库)】的数据库操作Service
 * @createDate 2022-09-28 15:18:51
 */
public interface PmsCompleteStorageService extends IService<PmsCompleteStorage> {

    /**
     * 摄像头扫描箱号
     *
     * @param caseNo 箱号
     * @return 完工入库扫描vo
     */
    CompleteStorageScanDTO scan(String caseNo);

    /**
     * 生成入库单
     *
     * @param list 入库单
     * @return 入库单号
     */
    String generate(List<CompleteStorageScanDTO> list);

    /**
     * 入库单列表
     *
     * @param dto dto
     * @return page
     */
    Page<CompleteStoragePageVO> pageList(CompleteStoragePageDTO dto);

    /**
     * 入库单明细
     * @param id 主键id
     * @return vo
     */
    CompleteStorageListVO info(Long id);

    /**
     * 根据订单id 获取完工入库信息
     * @param dto
     * @return
     */
    Page<PmsCompleteStorageVO> completeStoragePageList(PmsCompleteStorageDTO dto);
}
