package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.pms.domain.PmsBarcodePrint;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.steccn.pms.dto.BarcodePrintDTO;
import com.steccn.pms.dto.BarcodePrintRecordDTO;
import com.steccn.pms.dto.CaseSiteDTO;
import com.steccn.pms.dto.PmsCompleteStorageNumDTO;
import com.steccn.pms.vo.BarcodePrintRecordVO;
import com.steccn.pms.vo.WorkOrderMaterialVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_barcode_print(条码打印)】的数据库操作Service
 * @createDate 2022-09-28 15:18:51
 */
public interface PmsBarcodePrintService extends IService<PmsBarcodePrint> {

    /**
     * 打印条码
     *
     * @param dto dto
     * @return 是否成功
     */
    Boolean print(BarcodePrintDTO dto);

    /**
     * 保存一个箱子
     *
     * @param dto dto
     * @return 箱子id caseId
     */
    Long saveOneCase(BarcodePrintDTO dto);


    /**
     * 打印记录
     *
     * @param dto dto
     * @return 分页数据
     */
    Page<BarcodePrintRecordVO> printRecord(BarcodePrintRecordDTO dto);

    /**
     * 删除箱号
     *
     * @param idList 主键id集合
     * @return 删除
     */
    Boolean deleteBatch(List<Long> idList);

    /**
     * 箱号set
     *
     * @param caseNoSet 箱号set
     * @return map
     */
    Map<String, PmsBarcodePrint> caseMap(Set<String> caseNoSet);

    /**
     * 根据主键获取详细信息
     *
     * @param id 主键id
     * @return vo
     */
    BarcodePrintRecordVO info(Long id);

    /**
     * 批次生产总数量
     *
     * @param batchNoSet 批次set
     * @return map
     */
    Map<String, PmsCompleteStorageNumDTO> batchSum(Set<String> batchNoSet);

    /**
     * 订单总数量
     *
     * @param orderSet 订单set
     * @return map
     */
    Map<Long, PmsCompleteStorageNumDTO> orderSum(Set<Long> orderSet);

    /**
     * 根据完成箱号编码修改状态
     *
     * @param caseNoList
     * @param code 状态
     * @return
     */
    Boolean reviseAlreadyPutInStatue(List<String> caseNoList, Integer code);

    /**
     * 箱子位置
     *
     * @param caseNoSet 箱号集合
     * @return 箱号-位置
     */
    Map<String, CaseSiteDTO> caseSite(Set<String> caseNoSet);


    /**
     * 根据箱号获取物料信息
     * @param code
     * @return
     */
    List<WorkOrderMaterialVO> getMaterialByCaseNo(String code);


    /**
     * 根据批次号获取物料信息
     * @param batchNo
     * @return
     */
    List<WorkOrderMaterialVO> getMaterialByBatchNo(String batchNo);

    /**
     * 获取箱号是否存在
     * @param caseNo
     * @return
     */
    Boolean getCaseNoFlag(String caseNo);
}
