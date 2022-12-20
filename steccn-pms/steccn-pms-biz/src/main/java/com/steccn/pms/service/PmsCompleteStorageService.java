package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsCompleteStorage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.CompleteStoragePageDTO;
import com.steccn.pms.dto.CompleteStorageScanDTO;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.vo.CompleteStorageListVO;
import com.steccn.pms.vo.CompleteStoragePageVO;
import com.steccn.pms.vo.PmsCompleteStorageVO;

import java.util.List;
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
     * @param warehouseId 货架id
     * @return 完工入库扫描vo
     */
    CompleteStorageScanDTO scan(Integer warehouseId,String caseNo);

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

    /**
     * 箱号是否已生成入库单
     * @param caseNoSet 箱号集合
     * @return 已生成入库单的箱号
     */
    Set<String> existInVouch(Set<String> caseNoSet);

    /**
     * 根据入库单编码找到入库凭证修改状态 pms_complete_storage中statue 0已创建 1已完成 pms_barcode_print 箱状态0:已完工 1:待入库 2：已入库
     * @param code
     * @param confirmName
     * @return R
     */
    R<Boolean> completeStorageReviseStatue(String code,String confirmName);


}
