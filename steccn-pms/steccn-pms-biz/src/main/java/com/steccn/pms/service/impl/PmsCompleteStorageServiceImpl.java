package com.steccn.pms.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsBarcodePrint;
import com.steccn.pms.domain.PmsCompleteStorage;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.steccn.pms.domain.PmsProduceOrder;
import com.steccn.pms.dto.CompleteStorageScanDTO;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.service.PmsCompleteStorageInfoService;
import com.steccn.pms.service.PmsCompleteStorageService;
import com.steccn.pms.mapper.PmsCompleteStorageMapper;
import com.steccn.pms.service.PmsProduceOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【pms_complete_storage(生产订单-完工入库)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsCompleteStorageServiceImpl extends ServiceImpl<PmsCompleteStorageMapper, PmsCompleteStorage>
        implements PmsCompleteStorageService {
    @Autowired
    private PmsCompleteStorageInfoService completeStorageInfoService;
    @Autowired
    private PmsBarcodePrintService pmsBarcodePrintService;
    @Autowired
    private PmsProduceOrderService pmsProduceOrderService;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public CompleteStorageScanDTO scan(String caseNo) {
        //1.判断装箱单（PP-XXXXX）已扫描，入库单为SI-XXXXX
        PmsCompleteStorageInfo pmsCompleteStorageInfo = completeStorageInfoService.getOne(new LambdaQueryWrapper<PmsCompleteStorageInfo>()
                .eq(PmsCompleteStorageInfo::getCaseNo, caseNo)
                .last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(pmsCompleteStorageInfo)) {
            PmsCompleteStorage pmsCompleteStorage = getById(pmsCompleteStorageInfo.getCompleteStorageId());
            throw new BusinessException("装箱单" + caseNo + "已扫描，入库单为" + pmsCompleteStorage.getCode());
        }
        //2.根据箱号查询箱信息
        PmsBarcodePrint pmsBarcodePrint = pmsBarcodePrintService.getOne(new LambdaQueryWrapper<PmsBarcodePrint>()
                .eq(PmsBarcodePrint::getCaseNo, caseNo)
                .last(MpConstant.STR_LIMIT_1));
        Assert.notNull(pmsBarcodePrint, "箱号不存在");
        CompleteStorageScanDTO resDTO = new CompleteStorageScanDTO();
        BeanUtils.copyProperties(pmsBarcodePrint, resDTO);
        //3.存放生产订单中
        PmsProduceOrder pmsProduceOrder = pmsProduceOrderService.getById(pmsBarcodePrint.getProduceOrderId());
        Assert.notNull(pmsProduceOrder, "生产订单不能为空");
        resDTO.setProduceOrderNo(pmsProduceOrder.getCode());
        resDTO.setWarehouseId(pmsProduceOrder.getDefaultWarehouseId());
        resDTO.setShelfId(pmsProduceOrder.getDefaultShelfId());
        //4.仓库和货架信息
        HashSet<Integer> storageSet = Sets.newHashSet(pmsProduceOrder.getDefaultWarehouseId(), pmsProduceOrder.getDefaultShelfId());
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(remoteOfficeFeign.listById(storageSet)).get();
        resDTO.setWarehouseName(sysOfficeMap.getOrDefault(pmsProduceOrder.getDefaultWarehouseId(), new SysOfficeVO()).getOfficeName());
        resDTO.setShelfName(sysOfficeMap.getOrDefault(pmsProduceOrder.getDefaultShelfId(), new SysOfficeVO()).getOfficeName());
        return resDTO;
    }
}




