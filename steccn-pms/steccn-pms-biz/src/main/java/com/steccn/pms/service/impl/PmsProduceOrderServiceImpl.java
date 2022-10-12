package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.mapper.PmsProduceOrderMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_produce_order(生产订单)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsProduceOrderServiceImpl extends ServiceImpl<PmsProduceOrderMapper, PmsProduceOrder>
        implements PmsProduceOrderService {

    @Autowired
    private PmsProduceOrderCraftService pmsProduceOrderCraftService;
    @Autowired
    private PmsWorkOrderBomService pmsWorkOrderBomService;
    @Autowired
    private PmsWorkOrderCraftStationService pmsWorkOrderCraftStationService;
    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private PmsWorkOrderBatchService pmsWorkOrderBatchService;

    /**
     * 新增生产订单信息
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public Boolean add(PmsProduceOrderDTO dto) {
        //1.校验数据是否存在
        if (Objects.isNull(dto)) {
            throw new BusinessException("订单信息为空");
        }
        //2.定义数据模型
        PmsProduceOrder model = new PmsProduceOrder();
        BeanUtil.copyProperties(dto, model);
        //3.给当前订单生成编码
        model.setCode(PmsCodeUtil.getProduceOrderCode(PmsCodeUtil.PREFIX_PMS_PRODUCE_ORDER, PmsCodeUtil.NUM_BIT_4, currentMaxCode()));
        //4.保存订单信息
        //4.1.保存基础信息
        save(model);
        //4.2保存工序信息
        if (CollUtil.isNotEmpty(dto.getProcessList())) {
            pmsProduceOrderCraftService.saveCrafts(dto.getProcessList(), model.getId());
        }
        //4.3保存物料信息
        if (CollUtil.isNotEmpty(dto.getMaterialList())) {
            pmsWorkOrderBomService.saveMaterial(dto.getMaterialList(), model.getId());
        }
        return true;
    }

    @Override
    public Page<PmsProduceOrderVO> pageList(PmsProduceOrderPageDTO dto) {
        //1.将日期拼接最大时间和最小时间
        //计划开始时间-开始
        if (Objects.nonNull(dto.getPlanStartDateStart())) {
            dto.setPlanStartTimeStart(LocalDateTime.of(dto.getPlanStartDateStart(), LocalTime.MIN));
        }
        //计划开始时间-结束
        if (Objects.nonNull(dto.getPlanStartDateEnd())) {
            dto.setPlanStartTimeEnd(LocalDateTime.of(dto.getPlanStartDateEnd(), LocalTime.MAX));
        }
        //计划结束时间-开始
        if (Objects.nonNull(dto.getPlanEndDateStart())) {
            dto.setPlanEndTimeStart(LocalDateTime.of(dto.getPlanEndDateStart(), LocalTime.MIN));
        }
        //计划结束时间-结束
        if (Objects.nonNull(dto.getPlanEndDateEnd())) {
            dto.setPlanEndTimeEnd(LocalDateTime.of(dto.getPlanEndDateEnd(), LocalTime.MAX));
        }
        //实际开始时间-开始
        if (Objects.nonNull(dto.getActualStartDateStart())) {
            dto.setActualStartTimeStart(LocalDateTime.of(dto.getActualStartDateStart(), LocalTime.MIN));
        }
        //实际开始时间-结束
        if (Objects.nonNull(dto.getActualStartDateEnd())) {
            dto.setActualStartTimeEnd(LocalDateTime.of(dto.getActualStartDateEnd(), LocalTime.MAX));
        }
        //实际结束时间-开始
        if (Objects.nonNull(dto.getActualEndDateStart())) {
            dto.setActualEndTimeStart(LocalDateTime.of(dto.getActualEndDateStart(), LocalTime.MIN));
        }
        //实际结束时间-结束
        if (Objects.nonNull(dto.getActualEndDateEnd())) {
            dto.setActualEndTimeEnd(LocalDateTime.of(dto.getActualEndDateEnd(), LocalTime.MAX));
        }
        //2.自定义sql查询订单信息列表
        Page<PmsProduceOrderVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        //3.处理数据
        page.getRecords().forEach(
                pmsProduceOrderVO -> {
                    //获取工序信息
                    pmsProduceOrderVO.setProcessList(pmsProduceOrderCraftService.getCrafts(pmsProduceOrderVO.getId()));
                    //获取物料信息
                    pmsProduceOrderVO.setMaterialList(pmsWorkOrderBomService.getMaterialList(pmsProduceOrderVO.getId()));
                }
        );
        return page;
    }

    /**
     * 编辑物料清单
     *
     * @return
     */
    @Override
    public Boolean edit(PmsProduceOrderDTO dto) {
        if (Objects.nonNull(dto)) {
            PmsProduceOrder pmsProduceOrder = new PmsProduceOrder();
            BeanUtil.copyProperties(dto, pmsProduceOrder);
            updateById(pmsProduceOrder);
            // 获取工序信息
            if (CollUtil.isNotEmpty(dto.getProcessList())) {
                dto.getProcessList().forEach(
                        pmsProduceOrderCraftDTO -> {
                            PmsProduceOrderCraft craft = new PmsProduceOrderCraft();
                            BeanUtil.copyProperties(pmsProduceOrderCraftDTO, craft);
                            craft.setProduceOrderId(dto.getId());
                            if (Objects.nonNull(craft.getId())) {
                                //修改工序信息
                                pmsProduceOrderCraftService.updateById(craft);
                                //处理工位数据
                                if (CollUtil.isNotEmpty(pmsProduceOrderCraftDTO.getStationList())) {
                                    pmsProduceOrderCraftDTO.getStationList().forEach(station -> {
                                        PmsWorkOrderCraftStation pmsWorkOrderCraftStation = new PmsWorkOrderCraftStation();
                                        BeanUtil.copyProperties(station, pmsWorkOrderCraftStation);
                                        pmsWorkOrderCraftStation.setProduceOrderCraftId(craft.getId());
                                        if (Objects.nonNull(pmsWorkOrderCraftStation.getId())) {
                                            pmsWorkOrderCraftStationService.updateById(pmsWorkOrderCraftStation);
                                        } else {
                                            pmsWorkOrderCraftStationService.save(pmsWorkOrderCraftStation);
                                        }
                                    });
                                }
                            } else {
                                //新增工序信息
                                craft.setProduceOrderId(dto.getId());
                                pmsProduceOrderCraftService.save(craft);
                                //处理工位数据
                                if (CollUtil.isNotEmpty(pmsProduceOrderCraftDTO.getStationList())) {
                                    List<PmsWorkOrderCraftStation> stations = Lists.newArrayList();
                                    pmsProduceOrderCraftDTO.getStationList().forEach(station -> {
                                        PmsWorkOrderCraftStation pmsWorkOrderCraftStation = new PmsWorkOrderCraftStation();
                                        pmsWorkOrderCraftStation.setId(craft.getId());
                                        BeanUtil.copyProperties(station, pmsWorkOrderCraftStation);
                                        stations.add(pmsWorkOrderCraftStation);
                                    });
                                    //保存工位数据
                                    pmsWorkOrderCraftStationService.saveBatch(stations);
                                }
                            }
                        }
                );
            }
            //获取物料信息
            List<PmsWorkOrderBom> bomList = Lists.newArrayList();
            if (CollUtil.isNotEmpty(dto.getMaterialList())) {
                dto.getMaterialList().forEach(material -> {
                    PmsWorkOrderBom pmsWorkOrderBom = new PmsWorkOrderBom();
                    BeanUtil.copyProperties(material, pmsWorkOrderBom);
                    pmsWorkOrderBom.setWorkProduceId(dto.getId());
                    if (Objects.nonNull(material.getId())) {
                        pmsWorkOrderBomService.updateById(pmsWorkOrderBom);
                    } else {
                        bomList.add(pmsWorkOrderBom);
                    }
                });
            }
            pmsWorkOrderBomService.saveBatch(bomList);
        }
        return true;
    }
    /**
     * 获取订单批次信息
     *
     * @param dto
     * @return
     */
    @Override
    public Page<PmsWorkOrderBatchVO> batchPageList(PmsProduceOrderBatchDTO dto) {
        //1.定义查询条件
        LambdaQueryWrapper<PmsWorkOrderBatch> eq = new LambdaQueryWrapper<PmsWorkOrderBatch>().eq(PmsWorkOrderBatch::getProduceOrderId, dto.getProduceOrderId());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = pmsWorkOrderBatchService.page(page, eq);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<PmsWorkOrderBatchVO> res = BeanUtil.copyToList(resPage.getRecords(), PmsWorkOrderBatchVO.class);
        //6.处理数据
        Set<Long> dictIds = Sets.newHashSet();
        res.forEach(pmsWorkOrderBatchVO -> {
            dictIds.add(pmsWorkOrderBatchVO.getUnitId());
        });
        //查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
        res.forEach(pmsWorkOrderBatchVO -> pmsWorkOrderBatchVO.setUnit(dictMap.get(pmsWorkOrderBatchVO.getUnitId())));
        return page.setRecords(res);
    }

    @Override
    public ReportWorkOrderVO reportWorkOrder(Integer productLineId, Integer stationId) {
        //1.根据产线查询订单
        PmsProduceOrder order = getOne(new LambdaQueryWrapper<PmsProduceOrder>()
                .eq(PmsProduceOrder::getProductionLineId, productLineId)
                .orderByDesc(PmsProduceOrder::getCreateTime)
                .last(MpConstant.STR_LIMIT_1));
        Assert.notNull(order, "该产线暂无订单");
        //2.根据订单查询当前批次
        PmsWorkOrderBatch currentBatch = pmsWorkOrderBatchService.getOne(new LambdaQueryWrapper<PmsWorkOrderBatch>()
                .eq(PmsWorkOrderBatch::getProduceOrderId, order.getId())
                .eq(PmsWorkOrderBatch::getCurrentBatchFlag, MpConstant.YES));
        Assert.notNull(currentBatch, "订单" + order.getCode() + "无当前批次");
        //3.查询工位
        PmsWorkOrderCraftStation station = pmsWorkOrderCraftStationService.getOne(new LambdaQueryWrapper<PmsWorkOrderCraftStation>()
                .eq(PmsWorkOrderCraftStation::getExecuteStationId, stationId)
                .last(MpConstant.STR_LIMIT_1));
        Assert.notNull(station,"该工位暂无工序配置");
        //4.查询工序
        PmsProduceOrderCraft craft = pmsProduceOrderCraftService.getById(station.getProduceOrderCraftId());
        Assert.notNull(craft,"工序不存在");

        //5.定义返回数据
        ReportWorkOrderVO vo = new ReportWorkOrderVO();
        BeanUtils.copyProperties(order,vo);
        vo.setProduceOrderId(order.getId());
        vo.setProduceOrderCode(order.getCode());
        vo.setBatch(currentBatch.getId());
        vo.setBatchNo(currentBatch.getCode());
        vo.setProcessId(craft.getId());
        vo.setProcessName(craft.getName());
        vo.setStationId(station.getExecuteStationId());
        vo.setStationName(station.getExecuteStationName());
        return vo;
    }


    /**
     * 当前最大的code值
     *
     * @return 数值类型，可能为null
     */
    private Long currentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        PmsProduceOrder maxCodeModel = getOne(new LambdaQueryWrapper<PmsProduceOrder>().orderByDesc(PmsProduceOrder::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(PmsCodeUtil.PREFIX_PMS_PRODUCE_ORDER.length() + PmsCodeUtil.NUM_BIT_8);
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }
}




