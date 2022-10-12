package com.steccn.pms.service.impl;

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
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
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
import com.steccn.pms.vo.CompleteStorageListVO;
import com.steccn.pms.vo.CompleteStorageMaterialVO;
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
import java.util.stream.Collectors;

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
    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;

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
        //数据字典
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(Sets.newHashSet(resDTO.getUnitId()))).get();
        resDTO.setUnitName(dictMap.get(resDTO.getUnitId()));

        //3.存放生产订单中
        PmsProduceOrder pmsProduceOrder = pmsProduceOrderService.getById(pmsBarcodePrint.getProduceOrderId());
        Assert.notNull(pmsProduceOrder, "生产订单不能为空");
        resDTO.setProduceOrderNo(pmsProduceOrder.getCode());
        resDTO.setWarehouseId(pmsProduceOrder.getDefaultWarehouseId());
        resDTO.setShelfId(pmsProduceOrder.getDefaultShelfId());
        //4.仓库和货架信息
        HashSet<Integer> storageSet = Sets.newHashSet(pmsProduceOrder.getDefaultWarehouseId(), pmsProduceOrder.getDefaultShelfId());
        Map<Integer, String> sysOfficeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(storageSet)).get();
        resDTO.setWarehouseName(sysOfficeMap.get(pmsProduceOrder.getDefaultWarehouseId()));
        resDTO.setShelfName(sysOfficeMap.get(pmsProduceOrder.getDefaultShelfId()));
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
        LambdaQueryWrapper<PmsCompleteStorage> lqw = new LambdaQueryWrapper<PmsCompleteStorage>()
                .eq(PmsCompleteStorage::getProductLineId, dto.getProductLineId());
        if (Objects.nonNull(dto.getStart())) {
            lqw.ge(PmsCompleteStorage::getCreateTime, LocalDateTime.of(dto.getStart(), LocalTime.MIN));
        }
        if (Objects.nonNull(dto.getEnd())) {
            lqw.le(PmsCompleteStorage::getCreateTime, LocalDateTime.of(dto.getEnd(), LocalTime.MAX));
        }
        Page<PmsCompleteStorage> pageList = page(new Page<>(dto.getCurrent(), dto.getSize()), lqw);

        //拷贝分页对象
        BeanUtils.copyProperties(pageList, resPage);
        //如果查询结果不为空，拷贝返回的数据
        if (CollUtil.isNotEmpty(pageList.getRecords())) {
            List<PmsCompleteStorage> modelList = pageList.getRecords();
            Set<Long> dictSet = modelList.stream().map(PmsCompleteStorage::getUnitId).collect(Collectors.toSet());
            Map<Long, String> sysDictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictSet)).get();
            List<CompleteStoragePageVO> voList = new ArrayList<>(modelList.size());
            for (PmsCompleteStorage model : modelList) {
                CompleteStoragePageVO vo = new CompleteStoragePageVO();
                BeanUtils.copyProperties(model, vo);
                vo.setUnitName(sysDictMap.get(vo.getUnitId()));
                voList.add(vo);
            }
            resPage.setRecords(voList);
        }
        return resPage;
    }

    @Override
    public CompleteStorageListVO info(Long id) {
        //查询入库单信息
        PmsCompleteStorage pmsCompleteStorage = getById(id);
        Assert.notNull(pmsCompleteStorage, "未查询到入库单");
        CompleteStorageListVO completeStorageListVO = new CompleteStorageListVO();
        BeanUtils.copyProperties(pmsCompleteStorage, completeStorageListVO);
        //查询所有的箱信息
        List<PmsCompleteStorageInfo> infoList = completeStorageInfoService.list(new LambdaQueryWrapper<PmsCompleteStorageInfo>().eq(PmsCompleteStorageInfo::getCompleteStorageId, id));
        //查询订单信息
        Long produceOrderId = pmsCompleteStorage.getProduceOrderId();
        PmsProduceOrder produceOrder = pmsProduceOrderService.getById(produceOrderId);
        Assert.notNull(produceOrder, "生产订单不存在");

        //定义字典集合和箱号集合
        Set<Long> dictList = Sets.newHashSet();
        Set<String> caseNoList = Sets.newHashSet();
        for (PmsCompleteStorageInfo info : infoList) {
            dictList.add(info.getUnitId());
            caseNoList.add(info.getCaseNo());
        }

        dictList.add(pmsCompleteStorage.getUnitId());
        Map<Long, String> sysDictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictList)).get();

        Map<String, PmsBarcodePrint> barcodePrintMap = pmsBarcodePrintService.caseMap(caseNoList);
        PmsBarcodePrint defaultPmsBarcodePrint = new PmsBarcodePrint();
        Set<Integer> storageSet = Sets.newHashSet();
        for (Map.Entry<String, PmsBarcodePrint> entry : barcodePrintMap.entrySet()) {
            PmsBarcodePrint pbp = entry.getValue();
            storageSet.add(pbp.getWarehouseId());
            storageSet.add(pbp.getShelfId());
        }
        Map<Integer, String> sysOfficeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(storageSet)).get();

        List<CompleteStorageMaterialVO> materialVOList = new ArrayList<>(infoList.size());
        for (PmsCompleteStorageInfo info : infoList) {
            CompleteStorageMaterialVO materialVO = new CompleteStorageMaterialVO();
            BeanUtils.copyProperties(info, materialVO);
            materialVO.setProduceOrderId(produceOrderId);
            materialVO.setProduceOrderNo(produceOrder.getCode());
            materialVO.setMaterialId(produceOrder.getMaterialId());
            materialVO.setMaterialCode(produceOrder.getMaterialCode());
            materialVO.setMaterialName(produceOrder.getMaterialName());
            materialVO.setUnitName(sysDictMap.get(materialVO.getUnitId()));
            materialVO.setWarehouseId(barcodePrintMap.getOrDefault(info.getCaseNo(), defaultPmsBarcodePrint).getWarehouseId());
            materialVO.setWarehouseName(sysOfficeMap.get(materialVO.getWarehouseId()));
            materialVO.setShelfId(barcodePrintMap.getOrDefault(info.getCaseNo(), defaultPmsBarcodePrint).getShelfId());
            materialVO.setShelfName(sysOfficeMap.get(materialVO.getShelfId()));
            materialVOList.add(materialVO);
        }
        completeStorageListVO.setList(materialVOList);
        completeStorageListVO.setUnitName(sysDictMap.get(completeStorageListVO.getUnitId()));
        return completeStorageListVO;
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