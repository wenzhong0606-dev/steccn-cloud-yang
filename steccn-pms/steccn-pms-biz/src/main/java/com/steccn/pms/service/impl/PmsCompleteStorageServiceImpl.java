package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsBarcodePrint;
import com.steccn.pms.domain.PmsCompleteStorage;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.steccn.pms.domain.PmsProduceOrder;
import com.steccn.pms.dto.CompleteStoragePageDTO;
import com.steccn.pms.dto.CompleteStorageScanDTO;
import com.steccn.pms.mapper.PmsCompleteStorageMapper;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.service.PmsCompleteStorageInfoService;
import com.steccn.pms.service.PmsCompleteStorageService;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.CompleteStoragePageVO;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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
        //5.箱状态
        resDTO.setCaseState(pmsBarcodePrint.getCaseState());
        return resDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generate(List<CompleteStorageScanDTO> dtoList) {
        Assert.notEmpty(dtoList, "请选择箱子");
        //校验箱子不能重复出现在不同的入库单
        List<PmsCompleteStorageInfo> modelInfoList = new ArrayList<>(dtoList.size());
        //创建主表
        BigDecimal sumQuantity = BigDecimal.ZERO;
        PmsCompleteStorage pmsCompleteStorage = new PmsCompleteStorage();
        pmsCompleteStorage.setId(IdUtil.getSnowflake().nextId());
        pmsCompleteStorage.setUnitId(dtoList.get(0).getUnitId());

        Long produceOrderId = dtoList.get(0).getProduceOrderId();
        pmsCompleteStorage.setProduceOrderId(produceOrderId);
        PmsProduceOrder pmsProduceOrder = pmsProduceOrderService.getById(produceOrderId);
        Assert.notNull(pmsProduceOrder, "生产订单不存在");
        pmsCompleteStorage.setProductLineId(pmsProduceOrder.getProductionLineId());
        for (CompleteStorageScanDTO caseInfo : dtoList) {
            PmsCompleteStorageInfo modelInfo = new PmsCompleteStorageInfo();
            BeanUtils.copyProperties(caseInfo, modelInfo);
            modelInfo.setCompleteStorageId(pmsCompleteStorage.getId());
            modelInfo.setPackTime(caseInfo.getCreateTime());
            sumQuantity = sumQuantity.add(caseInfo.getQuantity());
            modelInfoList.add(modelInfo);
        }
        completeStorageInfoService.saveBatch(modelInfoList);
        //生成code 规则：SI-20220905-0001
        pmsCompleteStorage.setCode(maxCompleteCode());
        pmsCompleteStorage.setQuantity(sumQuantity);
        save(pmsCompleteStorage);
        return pmsCompleteStorage.getCode();
    }

    @Override
    public Page<CompleteStoragePageVO> pageList(CompleteStoragePageDTO dto) {
        //定义返回page对象
        Page<CompleteStoragePageVO> resPage = new Page<>();
        //根据条件获取筛选数据
        Page<PmsCompleteStorage> pageList = page(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<PmsCompleteStorage>()
                .eq(PmsCompleteStorage::getProductLineId, dto.getProductLineId())
                .ge(Objects.nonNull(dto.getStart()), PmsCompleteStorage::getCreateTime, LocalDateTime.of(dto.getStart(), LocalTime.MIN))
                .le(Objects.nonNull(dto.getEnd()), PmsCompleteStorage::getCreateTime, LocalDateTime.of(dto.getEnd(), LocalTime.MAX)));
        //拷贝分页对象
        BeanUtils.copyProperties(pageList, resPage);
        //如果查询结果不为空，拷贝返回的数据
        if (CollUtil.isNotEmpty(pageList.getRecords())) {
            List<PmsCompleteStorage> modelList = pageList.getRecords();
            List<CompleteStoragePageVO> voList = BeanUtil.copyToList(modelList, CompleteStoragePageVO.class);
            resPage.setRecords(voList);
        }
        return resPage;
    }

    /**
     * 最大完工code
     *
     * @return
     */
    private String maxCompleteCode() {
        String prefix = PmsCodeUtil.PREFIX_SI + LocalDate.now().toString().replaceAll(PmsCodeUtil.SHORT_LINE, Strings.EMPTY) + PmsCodeUtil.SHORT_LINE;
        PmsCompleteStorage one = getOne(new LambdaQueryWrapper<PmsCompleteStorage>()
                .likeRight(PmsCompleteStorage::getCode, prefix)
                .orderByDesc(PmsCompleteStorage::getCode)
                .last(MpConstant.STR_LIMIT_1));
        long currentMax = PmsCodeUtil.DEFAULT_PADDING;
        if (Objects.nonNull(one)) {
            String substring = one.getCode().substring(one.getCode().length() - PmsCodeUtil.NUM_BIT_4);
            try {
                currentMax = Long.parseLong(substring);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return PmsCodeUtil.getNextCode(prefix, PmsCodeUtil.NUM_BIT_4, currentMax);
    }

}