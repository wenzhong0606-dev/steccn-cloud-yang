package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsBarcodePrint;
import com.steccn.pms.domain.PmsCompleteStorage;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.steccn.pms.domain.PmsProduceOrder;
import com.steccn.pms.dto.BarcodePrintDTO;
import com.steccn.pms.dto.BarcodePrintRecordDTO;
import com.steccn.pms.dto.CaseSiteDTO;
import com.steccn.pms.dto.PmsCompleteStorageNumDTO;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.mapper.PmsBarcodePrintMapper;
import com.steccn.pms.service.PmsCompleteStorageService;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.BarcodePrintRecordVO;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_barcode_print(条码打印)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsBarcodePrintServiceImpl extends ServiceImpl<PmsBarcodePrintMapper, PmsBarcodePrint>
        implements PmsBarcodePrintService {

    @Autowired
    private PmsCompleteStorageService pmsCompleteStorageService;
    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private RemoteMessFeignApi remoteMessFeignApi;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private PmsProduceOrderService pmsProduceOrderService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean print(BarcodePrintDTO dto) {
        Integer printStrip = dto.getPrintStrip();
        List<PmsBarcodePrint> modelList = new ArrayList<>(printStrip);

        //获取当前订单最大的
        long currentMaxNo = orderMaxCaseNo(dto.getProduceOrderId());
        //遍历打印条数
        for (int count = 0; count < printStrip; count++) {
            PmsBarcodePrint printModel = new PmsBarcodePrint();
            BeanUtils.copyProperties(dto, printModel);
            printModel.setCaseState(PmManagerConstant.CaseStateEnum.FINISH.getCode());
            printModel.setCaseNo(PmsCodeUtil.getNextCode(dto.getProduceOrderNo() + PmsCodeUtil.SHORT_LINE, PmsCodeUtil.NUM_BIT_4, currentMaxNo++));
            modelList.add(printModel);
        }
        saveBatch(modelList);
        return Boolean.TRUE;
    }

    @Override
    public Page<BarcodePrintRecordVO> printRecord(BarcodePrintRecordDTO dto) {
        Page<BarcodePrintRecordVO> resPage = new Page<>();
        Page<PmsBarcodePrint> page = page(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<PmsBarcodePrint>()
                .eq(PmsBarcodePrint::getProduceOrderId, dto.getProduceOrderId())
                .eq(PmsBarcodePrint::getBatchNo, dto.getBatchNo())
                .eq(Objects.nonNull(dto.getCaseState()), PmsBarcodePrint::getCaseState, dto.getCaseState())
                .orderByDesc(PmsBarcodePrint::getCreateTime));
        BeanUtils.copyProperties(page, resPage);
        if (CollUtil.isNotEmpty(page.getRecords())) {
            List<BarcodePrintRecordVO> voList = BeanUtil.copyToList(page.getRecords(), BarcodePrintRecordVO.class);
            //处理单位数据
            Set<Long> orderSet = page.getRecords().stream().map(PmsBarcodePrint::getProduceOrderId).collect(Collectors.toSet());
            Map<Long, BaseSelectVO<Long>> orderUnitMap = pmsProduceOrderService.listUnit(orderSet);
            voList.forEach(barcodePrintRecordVO -> {
                BaseSelectVO<Long> baseSelectVO = orderUnitMap.get(barcodePrintRecordVO.getProduceOrderId());
                if(Objects.nonNull(baseSelectVO)){
                    barcodePrintRecordVO.setUnitId(baseSelectVO.getId());
                    barcodePrintRecordVO.setUnit(baseSelectVO.getName());
                }
            });
            resPage.setRecords(voList);
        }
        return resPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteBatch(List<Long> idList) {
        Assert.notEmpty(idList, "删除集合不能为空");
        List<PmsBarcodePrint> printList = listByIds(idList);
        Assert.notEmpty(printList, "删除的数据集合不能为空");
        Set<String> caseNoSet = printList.stream().map(PmsBarcodePrint::getCaseNo).collect(Collectors.toSet());
        Set<String> existInVouchSet = pmsCompleteStorageService.existInVouch(caseNoSet);
        if (CollUtil.isNotEmpty(existInVouchSet)) {
            throw new BusinessException("箱号：" + existInVouchSet + "已生成入库单");
        }
        return removeByIds(idList);
    }

    @Override
    public Map<String, PmsBarcodePrint> caseMap(Set<String> caseNoSet) {
        Map<String, PmsBarcodePrint> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(caseNoSet)) {
            List<PmsBarcodePrint> dbList = list(new LambdaQueryWrapper<PmsBarcodePrint>().in(PmsBarcodePrint::getCaseNo, caseNoSet));
            if (CollUtil.isNotEmpty(dbList)) {
                dbList.forEach(ele -> {
                    resMap.put(ele.getCaseNo(), ele);
                });
            }
        }
        return resMap;
    }

    @Override
    public BarcodePrintRecordVO info(Long id) {
        PmsBarcodePrint model = getById(id);
        Assert.notNull(model, "打印详细信息不存在");
        BarcodePrintRecordVO vo = new BarcodePrintRecordVO();
        BeanUtils.copyProperties(model, vo);
        Long produceOrderId = vo.getProduceOrderId();
        if(Objects.nonNull(produceOrderId)){
            Map<Long, BaseSelectVO<Long>> orderUnitMap = pmsProduceOrderService.listUnit(Sets.newHashSet(produceOrderId));
            if(CollUtil.isNotEmpty(orderUnitMap)){
                BaseSelectVO<Long> baseSelectVO = orderUnitMap.get(produceOrderId);
                vo.setUnitId(baseSelectVO.getId());
                vo.setUnit(baseSelectVO.getName());
            }
        }
        return vo;
    }

    @Override
    public Map<String, PmsCompleteStorageNumDTO> batchSum(Set<String> batchNoSet) {
        Map<String, PmsCompleteStorageNumDTO> resMap = Maps.newHashMap();
        if (CollUtil.isEmpty(batchNoSet)) {
            return resMap;
        }
        List<PmsBarcodePrint> dbList = list(new QueryWrapper<PmsBarcodePrint>()
                .select("batch_no , sum(quantity) as quantity , any_value(unit_id) as unit_id, any_value(produce_order_id) as produce_order_id").lambda()
                .in(PmsBarcodePrint::getBatchNo, batchNoSet)
                .groupBy(PmsBarcodePrint::getBatchNo));
        if (CollUtil.isNotEmpty(dbList)) {
            for (PmsBarcodePrint pmsBarcodePrint : dbList) {
                PmsCompleteStorageNumDTO numDTO = new PmsCompleteStorageNumDTO();
                numDTO.setQuantity(pmsBarcodePrint.getQuantity());
                numDTO.setUnitId(pmsBarcodePrint.getUnitId());
                numDTO.setProduceOrderId(pmsBarcodePrint.getProduceOrderId());
                resMap.put(pmsBarcodePrint.getBatchNo(), numDTO);
            }
        }
        return resMap;
    }

    @Override
    public Map<Long, PmsCompleteStorageNumDTO> orderSum(Set<Long> orderSet) {
        Map<Long, PmsCompleteStorageNumDTO> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(orderSet)) {
            List<PmsBarcodePrint> dbList = list(new QueryWrapper<PmsBarcodePrint>()
                    .select("produce_order_id , sum(quantity) as quantity , any_value(unit_id) as unit_id ").lambda()
                    .in(PmsBarcodePrint::getProduceOrderId, orderSet)
                    .groupBy(PmsBarcodePrint::getProduceOrderId));
            if (CollUtil.isNotEmpty(dbList)) {
                for (PmsBarcodePrint pmsBarcodePrint : dbList) {
                    PmsCompleteStorageNumDTO numDTO = new PmsCompleteStorageNumDTO();
                    numDTO.setQuantity(pmsBarcodePrint.getQuantity());
                    numDTO.setUnitId(pmsBarcodePrint.getUnitId());
                    numDTO.setProduceOrderId(pmsBarcodePrint.getProduceOrderId());
                    resMap.put(pmsBarcodePrint.getProduceOrderId(), numDTO);
                }
            }
        }
        return resMap;
    }

    @Override
    public Boolean reviseAlreadyPutInStatue(List<String> caseNoList,Integer code) {
        if (CollUtil.isNotEmpty(caseNoList)) {
            update(new LambdaUpdateWrapper<PmsBarcodePrint>()
                    .set(PmsBarcodePrint::getCaseState, code)
                    .in(PmsBarcodePrint::getCaseNo, caseNoList));
        }
        return Boolean.TRUE;
    }

    @Override
    public Map<String, CaseSiteDTO> caseSite(Set<String> caseNoSet) {
        Map<String, CaseSiteDTO> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(caseNoSet)) {
            Map<String, CaseSiteDTO> messMap = remoteMessFeignApi.caseSite(caseNoSet);
            if (CollUtil.isNotEmpty(messMap)) {
                Set<String> siteSet = Sets.newHashSet();
                for (Map.Entry<String, CaseSiteDTO> entry : messMap.entrySet()) {
                    siteSet.add(entry.getValue().getShelfCode());
                    siteSet.add(entry.getValue().getWarehouseCode());
                }
                Map<String, SysOfficeVO> sysOfficeMap = FeignOptional.of(remoteOfficeFeign.listByCodeSet(siteSet)).get();
                SysOfficeVO defaultSysOfficeVO = new SysOfficeVO();
                for (Map.Entry<String, CaseSiteDTO> entry : messMap.entrySet()) {
                    CaseSiteDTO value = entry.getValue();
                    SysOfficeVO warehouse = sysOfficeMap.getOrDefault(value.getWarehouseCode(), defaultSysOfficeVO);
                    value.setWarehouseId(warehouse.getId());
                    value.setWarehouseName(warehouse.getOfficeName());
                    SysOfficeVO shelf = sysOfficeMap.getOrDefault(value.getShelfCode(), defaultSysOfficeVO);
                    value.setShelfId(shelf.getId());
                    value.setShelfName(shelf.getOfficeName());
                }
                resMap = messMap;
            }
        }
        return resMap;
    }

    /**
     * 生产订单最大箱号
     *
     * @param produceOrderId 生产订单id
     * @return 最大箱号
     */
    private long orderMaxCaseNo(Long produceOrderId) {
        PmsBarcodePrint one = getOne(new LambdaQueryWrapper<PmsBarcodePrint>()
                .eq(PmsBarcodePrint::getProduceOrderId, produceOrderId)
                .orderByDesc(PmsBarcodePrint::getCaseNo)
                .last(MpConstant.STR_LIMIT_1));
        long currentMax = PmsCodeUtil.DEFAULT_PADDING;
        if (Objects.nonNull(one)) {
            String substring = one.getCaseNo().substring(one.getCaseNo().length() - PmsCodeUtil.NUM_BIT_4);
            try {
                currentMax = Long.parseLong(substring);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }
}




