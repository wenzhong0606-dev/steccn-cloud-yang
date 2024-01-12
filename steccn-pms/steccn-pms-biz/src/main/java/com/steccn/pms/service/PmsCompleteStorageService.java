package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsCompleteDelivery;
import com.steccn.pms.domain.PmsCompleteDeliveryInfo;
import com.steccn.pms.domain.PmsCompleteStorage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.*;
import com.steccn.pms.vo.CompleteStorageListVO;
import com.steccn.pms.vo.CompleteStoragePageVO;
import com.steccn.pms.vo.PmsCompleteStorageVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    String generateInfo(List<CompleteStorageScanDTO> list);

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

    /**
     * 根据入库单编码找到入库凭证修改完成状态（用于仓储对完工入库单冲销后 修改完工入库单状态：已冲销 以及箱状态 已完工）
     *
     * @param code
     * @return
     */
    R<Boolean> completeStorageStatue(String code,String confirmName);


    /**
     * 连线入库
     * @param dto 请求参数
     * @return 入库单编码
     */
    String connectLine(ConnectLineStorageDTO dto);

    /**
     * 通过出库单生成入库单
     * @param dto 提交报工信息
     * @param infoList 明细集合
     * @return 入库单id
     */
    Long generateByDelivery(ProcessReportWorkSubmitDTO dto, List<PmsCompleteDeliveryInfo> infoList);


    /**
     * 生产入库冲销
     * @param id
     * @return
     */
    Boolean chargeAgainst(Long id);

    /**
     * 根据入库单编码查询是否存在生产入库凭证
     * @param code
     * @return
     */
    Boolean getCompleteStorageByCode(String code);

    /**
     * 根据入库单编码查询是否存在生产入库
     * @param code
     * @return
     */
    PmsCompleteStorageVO getCompleteStorage(String code);


    /**
     * 根据wms 的出库单创建对应的出库单以及修改入库单的状态
     * @param wmsDeliveryReceiptDTO
     * @return
     */
    Boolean wmsChargeAgainst(WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO);

    /**
     * 完工入库单据状态变更
     * @param dto
     * @return
     */
    Boolean changeReceiptState(ReceiptStatusDTO dto);

    /**
     * 运营驾驶舱 - 深加工入库
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param lineIdSet   产线主键集合
     * @param bit         保留位数
     * @return
     */
    Map<String, BigDecimal> sjgInStorage(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> lineIdSet, Integer bit);

    /**
     * 运营驾驶舱 - 基片入库
     *
     * @param granularity 粒度
     * @param start       开始日期
     * @param end         结束日期
     * @param lineIdSet   产线主键集合
     * @param bit         保留位数
     * @return
     */
    Map<String, BigDecimal> jpInStorage(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> lineIdSet, int bit);

    /**
     * 入库汇总
     * @param dto
     * @return
     */
    Map<String,BigDecimal> sjgInStorageSum(PmsOperateDriverDTO dto);
}
