package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.GetValueUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.dto.RawMaterialReportDTO;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.WmsStorageReceiptPageDTO;
import com.steccn.wms.mapper.WmsDeliveryReceiptInfoMapper;
import com.steccn.wms.service.WmsDeliveryReceiptInfoService;
import com.steccn.wms.service.WmsStorageReceiptInfoService;
import com.steccn.wms.mapper.WmsStorageReceiptInfoMapper;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import com.steccn.wms.vo.WmsStorageReceiptInfoVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author W10
 * @description 针对表【wms_storage_receipt_info】的数据库操作Service实现
 * @createDate 2022-12-09 11:18:09
 */
@Service
@RequiredArgsConstructor
public class WmsStorageReceiptInfoServiceImpl extends ServiceImpl<WmsStorageReceiptInfoMapper, WmsStorageReceiptInfo>
        implements WmsStorageReceiptInfoService {

    private final WmsStorageReceiptInfoMapper wmsStorageReceiptInfoMapper;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final WmsDeliveryReceiptInfoMapper wmsDeliveryReceiptInfoMapper;
    private final RemoteSpareFeignApi remoteSpareFeignApi;

    @Override
    public Map<Long, WmsStorageReceiptInfo> infoMap(List<Long> updateIds) {
        Map<Long, WmsStorageReceiptInfo> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(updateIds)) {
            List<WmsStorageReceiptInfo> list = list(new LambdaQueryWrapper<WmsStorageReceiptInfo>().in(WmsStorageReceiptInfo::getId, updateIds));
            list.forEach(wmsStorageReceiptInfo -> {
                map.put(wmsStorageReceiptInfo.getId(), wmsStorageReceiptInfo);
            });
        }
        return map;
    }

    @Override
    public WmsStorageReceiptInfo getByCaseNo(String caseNo) {
       return wmsStorageReceiptInfoMapper.getByCaseNo(caseNo);
    }

    @Override
    public Map<String, BigDecimal> purchaseQuantity(RawMaterialReportDTO dto) {
        Map<String,BigDecimal>  resMap = Maps.newHashMap();
        //获取采购类型
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode("1", "warehousing_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<WmsStorageReceiptInfo> infoList = this.baseMapper.purchaseQuantity(sysDictVO.getId(),dto.getStartTime(),dto.getEndTime(),dto.getMaterialName());
        if (CollUtil.isNotEmpty(infoList)){
            infoList.forEach( info->{
                resMap.put(info.getMaterialName(),info.getQuantity());
            });
        }
        return resMap;
    }

    @Override
    public Map<LocalDate, BigDecimal> produceConsumeQuantity(Long materialId, LocalDate start, LocalDate end) {
        Map<LocalDate, BigDecimal> resMap = Maps.newHashMap();
        SysDictVO sysDictVO1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode("4", "warehousing_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDictVO1, "[生产退料]字典不存在");
        SysDictVO sysDictVO2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode("0", "outbound_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDictVO2, "[生产领料]字典不存在");
        //获取物料确认日期在[start,end]中的消耗数量
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end).plusDays(1);
        List<WmsStorageReceiptInfo> list1 = wmsStorageReceiptInfoMapper.produceReturnQuantity(sysDictVO1.getId(), materialId, startLdt, endLdt);
        List<WmsStorageReceiptInfo> list2 = wmsDeliveryReceiptInfoMapper.produceConsumeQuantity(sysDictVO2.getId(), materialId, startLdt, endLdt);
        if (CollUtil.isNotEmpty(list1) || CollUtil.isNotEmpty(list2)) {
            while (LocalDateUtil.le(start, end)) {
                LocalDate finalStart = start;
                List<BigDecimal> collect1 = list1.stream().filter(ele -> finalStart.isEqual(ele.getValidTime())).map(WmsStorageReceiptInfo::getQuantity).collect(Collectors.toList());
                List<BigDecimal> collect2 = list2.stream().filter(ele -> finalStart.isEqual(ele.getValidTime())).map(WmsStorageReceiptInfo::getQuantity).collect(Collectors.toList());
                BigDecimal value1 = BigDecimalUtil.sum(collect1);
                BigDecimal value2 = BigDecimalUtil.sum(collect2);
                BigDecimal subtract = BigDecimalUtil.subtract(value2, value1);
                if (Objects.nonNull(subtract)) {
                    resMap.put(start, subtract);
                }
                start = start.plusDays(1);
            }
        }
        return resMap;
    }

    @Override
    public BigDecimal purchaseInventoryRawGlass(TupleDTO<LocalDateTime, LocalDateTime> tupleDTO) {
        //获取物料类型为原片以及下级的物料id
        List<EqpSpareVO> eqpSpareVOS = FeignOptional.of(() -> remoteSpareFeignApi.listByTypeId(Sets.newHashSet("1551432406889791488")), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Long> materialIds  = eqpSpareVOS.stream().map(EqpSpareVO::getId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(materialIds)){
           return this.baseMapper.purchaseInventoryRawGlass(tupleDTO.getField1(),tupleDTO.getField2(),materialIds);
        }
        return null;
    }

    @Override
    public Page<WmsStorageReceiptInfoVO> infoPageList(WmsStorageReceiptPageDTO dto) {
        Page<WmsStorageReceiptInfo> page =   page(new Page(dto.getCurrent(), dto.getSize()),new LambdaQueryWrapper<WmsStorageReceiptInfo>()
                .eq(WmsStorageReceiptInfo::getStorageReceiptId,dto.getId()));
        Page<WmsStorageReceiptInfoVO> resPage = new Page<>();
        if (CollUtil.isNotEmpty(page.getRecords())){
            Set<Long> dictSet = Sets.newHashSet();
            List<Long> materialIds = Lists.newArrayList();
            page.getRecords().forEach(wmsStorageReceiptInfo -> {
                dictSet.add(wmsStorageReceiptInfo.getUnit());
                materialIds.add(wmsStorageReceiptInfo.getMaterialId());
            });
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            BeanUtil.copyProperties(page,resPage);
            List<WmsStorageReceiptInfoVO> wmsStorageReceiptInfoVOS = BeanUtil.copyToList(page.getRecords(), WmsStorageReceiptInfoVO.class);
            wmsStorageReceiptInfoVOS.forEach(wmsStorageReceiptInfoVO -> {
                wmsStorageReceiptInfoVO.setUnitName(dictMap.get(wmsStorageReceiptInfoVO.getUnit()));
                wmsStorageReceiptInfoVO.setSpecification(materialMap.get(wmsStorageReceiptInfoVO.getMaterialId()).getSpecifications());
            });
            resPage.setRecords(wmsStorageReceiptInfoVOS);
        }
        return resPage;
    }

    @Override
    public void excelExportData(HttpServletResponse response, Long id) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = URLEncoder.encode("出库明细", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), WmsStorageReceiptInfoVO.class).sheet("模板").doWrite(getExcelData(id));
    }


    /**
     * 获取excel数据
     *
     * @return
     */
    private List<WmsStorageReceiptInfoVO> getExcelData(Long id) {
        //获取数
        List<WmsStorageReceiptInfo> list = list(new LambdaQueryWrapper<WmsStorageReceiptInfo>().eq(WmsStorageReceiptInfo::getStorageReceiptId, id));
//        List<WmsDeliveryReceiptInfoVO> wmsDeliveryReceiptInfoVOS = wmsStorageReceiptInfoMapper.infoList(id);
        //处理数据
        List<WmsStorageReceiptInfoVO> wmsDeliveryReceiptInfoVOS = BeanUtil.copyToList(list, WmsStorageReceiptInfoVO.class);
        Set<Long> dictIds = Sets.newHashSet();
        List<Long> materialIds = Lists.newArrayList();
        wmsDeliveryReceiptInfoVOS.forEach( info->{
            dictIds.add(info.getUnit());
            materialIds.add(info.getMaterialId());
        });
        Map<Long, String> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        wmsDeliveryReceiptInfoVOS.forEach(info->{
            info.setUnitName(dictMap.get(info.getUnit()));
            info.setSpecification(materialMap.get(info.getMaterialId()).getSpecifications());
        });
        return  wmsDeliveryReceiptInfoVOS;
    }
}




