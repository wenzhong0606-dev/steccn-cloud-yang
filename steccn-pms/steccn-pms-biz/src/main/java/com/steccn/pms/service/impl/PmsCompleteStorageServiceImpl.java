package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsBarcodePrint;
import com.steccn.pms.domain.PmsCompleteStorage;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.steccn.pms.domain.PmsProduceOrder;
import com.steccn.pms.dto.*;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.mapper.PmsCompleteStorageMapper;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.service.PmsCompleteStorageInfoService;
import com.steccn.pms.service.PmsCompleteStorageService;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.*;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    @Autowired
    private RemoteSpareFeignApi remoteSpareFeignApi;
    @Autowired
    private RemoteMessFeignApi remoteMessFeignApi;


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
        //校验一箱货只能存在于一个入库单中
        Set<String> caseNoSet = dtoList.stream().map(CompleteStorageScanDTO::getCaseNo).collect(Collectors.toSet());
        Assert.isFalse(completeStorageInfoService.existCaseNo(caseNoSet), "部分箱已生成入库单了");

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
        List<MessInStorageVoucherListDTO> materialList = Lists.newArrayList();

        Set<Integer> storageSet = Sets.newHashSet();
        List<Long> eqpList = Lists.newArrayList();
        for (CompleteStorageScanDTO caseInfo : dtoList) {
            storageSet.add(caseInfo.getWarehouseId());
            storageSet.add(caseInfo.getShelfId());
        }
        Map<Integer, SysOfficeVO> sysOfficeVOMap = FeignOptional.of(remoteOfficeFeign.listById(storageSet)).get();
        SysOfficeVO defaultSysOfficeVO = new SysOfficeVO();

        Map<Long, EqpSpareVO> eqpSpareVOMap = FeignOptional.of(remoteSpareFeignApi.listById(eqpList)).get();
        EqpSpareVO defaultEqpSpareVO = new EqpSpareVO();
        for (CompleteStorageScanDTO caseInfo : dtoList) {
            PmsCompleteStorageInfo modelInfo = new PmsCompleteStorageInfo();
            BeanUtils.copyProperties(caseInfo, modelInfo);
            modelInfo.setCompleteStorageId(pmsCompleteStorage.getId());
            modelInfo.setPackTime(caseInfo.getCreateTime());
            sumQuantity = sumQuantity.add(caseInfo.getQuantity());
            modelInfoList.add(modelInfo);

            MessInStorageVoucherListDTO mess = new MessInStorageVoucherListDTO();
            BeanUtils.copyProperties(caseInfo, mess);
            mess.setQuantity(sumQuantity);
            mess.setUnit(caseInfo.getUnitName());
            mess.setSpecifications(eqpSpareVOMap.getOrDefault(caseInfo.getMaterialId(), defaultEqpSpareVO).getName());
            mess.setWarehouse(sysOfficeVOMap.getOrDefault(caseInfo.getWarehouseId(), defaultSysOfficeVO).getOfficeCode());
            mess.setShelf(sysOfficeVOMap.getOrDefault(caseInfo.getShelfId(), defaultSysOfficeVO).getOfficeCode());
            materialList.add(mess);
        }
        completeStorageInfoService.saveBatch(modelInfoList);
        //生成code 规则：SI-20220905-0001
        pmsCompleteStorage.setCode(maxCompleteCode());
        pmsCompleteStorage.setQuantity(sumQuantity);
        pmsCompleteStorage.setInStorageVoucher(pmsCompleteStorage.getCode());

        SysDictVO sysDictVO = FeignOptional.of(remoteDictFeignApi.getByCode(SysDictConstant.CODE_0, SysDictConstant.TYPE_WAREHOUSING_TYPE)).get();
        pmsCompleteStorage.setTypeId(sysDictVO.getId());
        pmsCompleteStorage.setState(PmManagerConstant.InVouchEnum.CREATE.getCode());
        save(pmsCompleteStorage);
        //生成入库单
        MessInStorageVoucherDTO messDto = new MessInStorageVoucherDTO();
        messDto.setCreateBy(SecurityUtils.getUser().getName());
        messDto.setCode(pmsCompleteStorage.getCode());
        messDto.setType(sysDictVO.getCode());
        messDto.setMaterialList(materialList);
        MessInStorageVoucherVO messInStorageVoucherVO = remoteMessFeignApi.generateInStorageVoucher(messDto);
        //入库必定要成功
        if (Objects.nonNull(messInStorageVoucherVO) && messInStorageVoucherVO.getResult()) {
            log.info("入库单 -> " + messInStorageVoucherVO.getMessage());
        }
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
        Map<String, CaseSiteDTO> caseSiteMap = pmsBarcodePrintService.caseSite(barcodePrintMap.keySet());
        CaseSiteDTO defaultCaseSiteDTO = new CaseSiteDTO();

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
            CaseSiteDTO caseSite = caseSiteMap.getOrDefault(info.getCaseNo(), defaultCaseSiteDTO);
            materialVO.setWarehouseId(caseSite.getWarehouseId());
            materialVO.setWarehouseName(caseSite.getWarehouseName());
            materialVO.setShelfId(caseSite.getShelfId());
            materialVO.setShelfName(caseSite.getShelfName());
            materialVOList.add(materialVO);
        }
        completeStorageListVO.setList(materialVOList);
        completeStorageListVO.setUnitName(sysDictMap.get(completeStorageListVO.getUnitId()));
        return completeStorageListVO;
    }

    @Override
    public Page<PmsCompleteStorageVO> completeStoragePageList(PmsCompleteStorageDTO dto) {
        //1.查询分页数据
        Page<PmsCompleteStorageVO> page = completeStorageInfoService.pageByOrderId(dto);
        //2.处理数据
        List<Long> ids = Lists.newArrayList();
        Set<Integer> warehouseIds = Sets.newHashSet();
        //获取订单的仓库信息
        PmsProduceOrder order = pmsProduceOrderService.getOne(new LambdaQueryWrapper<PmsProduceOrder>().eq(PmsProduceOrder::getId, dto.getProduceOrderId()));
        List<PmsCompleteStorageVO> records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            records.forEach(pmsCompleteStorageVO -> {
                ids.add(pmsCompleteStorageVO.getUnitId());
                warehouseIds.add(order.getDefaultShelfId());
                warehouseIds.add(order.getDefaultWarehouseId());
                pmsCompleteStorageVO.setWarehouseId(order.getDefaultWarehouseId());
                pmsCompleteStorageVO.setShelfId(order.getDefaultShelfId());
            });
            //获取单位信息
            R<Map<Long, SysDictVO>> mapR = remoteDictFeignApi.listById(ids);
            //获取仓库以及货架信息
            R<Map<Integer, SysOfficeVO>> warehouses = remoteOfficeFeign.listById(warehouseIds);
            records.forEach(pmsCompleteStorageVO -> {
                pmsCompleteStorageVO.setUnit(mapR.getData().get(pmsCompleteStorageVO.getUnitId()).getName());
                pmsCompleteStorageVO.setWarehouseName(warehouses.getData().get(pmsCompleteStorageVO.getWarehouseId()).getOfficeName());
                pmsCompleteStorageVO.setShelfName(warehouses.getData().get(pmsCompleteStorageVO.getShelfId()).getOfficeName());
            });
        }
        return page;
    }

    @Override
    public Set<String> existInVouch(Set<String> caseNoSet) {
        Set<String> resSet = Sets.newHashSet();
        List<PmsCompleteStorageInfo> dbList = completeStorageInfoService.list(new LambdaQueryWrapper<PmsCompleteStorageInfo>()
                .select(PmsCompleteStorageInfo::getCaseNo)
                .in(PmsCompleteStorageInfo::getCaseNo, caseNoSet));
        if (CollUtil.isNotEmpty(dbList)) {
            resSet = dbList.stream().map(PmsCompleteStorageInfo::getCaseNo).collect(Collectors.toSet());
        }
        return resSet;
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