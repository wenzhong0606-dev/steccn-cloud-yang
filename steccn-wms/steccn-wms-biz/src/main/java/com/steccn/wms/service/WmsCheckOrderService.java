package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsCheckOrder;
import com.steccn.wms.dto.WmsCheckOrderDTO;
import com.steccn.wms.vo.WmsCheckOrderAreaVO;
import com.steccn.wms.vo.WmsCheckOrderInfoVO;
import com.steccn.wms.vo.WmsCheckOrderVO;
import reactor.util.function.Tuple3;

import java.util.List;
import java.util.Map;

/**
* @author W10
* @description 针对表【wms_check_order(盘点单)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsCheckOrderService extends IService<WmsCheckOrder> {

    /***
     * 获取盘点管理列表
     * @param dto
     * @return
     */
    Page<WmsCheckOrderVO> getCheckOrderList(WmsCheckOrderDTO dto);

    /***
     * 新增或者编辑盘点管理
     * @param dto
     * @return
     */
    String addOrUpdateCheckOrder(WmsCheckOrder dto);

    /***
     * 删除盘点管理
     * @param dto
     * @return
     */
    Boolean delCheckOrder( WmsCheckOrder dto);

    /***
     * 盘点管理详情
     * @param id
     * @return
     */
    Map getByIdCheckOrderDetail(Long id,Integer current, Integer size);

    /****
     * 盘点差异调整
     * @param id
     * @return
     */
    String varianceAdjustmentCheckOrder(Long id);

    /****
     * 手机端 获取盘点单列表
     * @param id
     * @return
     */
    Page<WmsCheckOrderVO> getApiCheckOrderList(Integer current,Integer size,Integer status, String beginTime,
                                               String endTime,Integer post);

    /****
     * 手机端 获取盘点单明细列表
     * @param checkOrderId
     * @return
     */
    R<Page<WmsCheckOrderInfoVO>> getApiCheckOrderDetailList(Long checkOrderId, Integer current, Integer size,Integer status);

    /****
     * 手机端 盘点单操作正常明细状态变更
     * @param wmsCheckOrderInfoDTO
     * @return
     */
    R<Boolean> adjNormalOrDifferenceCheckOrderItem(  Map<String, Object> paramMap);

    List<WmsCheckOrderAreaVO> areaCountList(Long checkOrderId);

    Boolean affirmCheck(WmsCheckOrderDTO dto);

    Boolean newVarianceAdjustmentCheckOrder(Long id);

    Boolean updateStatus(WmsCheckOrderDTO wmsCheckOrderDTO);
}
