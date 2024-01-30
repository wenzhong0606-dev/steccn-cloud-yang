package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.*;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.dp.vo.DpWeightVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.dto.OaGroupCountDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.dto.*;
import com.steccn.pms.mapper.PmsPullForceMapper;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.vo.PmsPullForceClassQuantity;
import com.steccn.pms.vo.PmsPullForceDataVO;
import com.steccn.pms.vo.PmsPullForceDetailsVO;
import com.steccn.pms.vo.PmsPullForceVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_DATE;

/**
 * @author yequan
 * @description 针对表【pms_pull_force(拉引力表)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@RequiredArgsConstructor
@Service
public class PmsPullForceServiceImpl extends ServiceImpl<PmsPullForceMapper, PmsPullForce> implements PmsPullForceService {

    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RedissonClient redissonClient;
    private final PmsPullForceMapper pmsPullForceMapper;
    private final OaInfoService oaInfoService;

    @Override
    public List<PmsPullForceVO> list(Integer workshopId, String monthDate) {
        LocalDate date = LocalDate.parse(monthDate + "-01", ISO_DATE);
        LocalDate startDate = date.minusWeeks(1);
        LocalDate endDate = date.plusMonths(1).plusWeeks(1);

        List<PmsPullForce> pmsPullForceList = list(new LambdaQueryWrapper<PmsPullForce>().eq(PmsPullForce::getWorkshopId, workshopId).ge(PmsPullForce::getPullForceDate, startDate).le(PmsPullForce::getPullForceDate, endDate));

        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(startDate, endDate);


        //根据日期分组
        Map<LocalDate, List<PmsPullForce>> groupBy = pmsPullForceList.stream().collect(Collectors.groupingBy(PmsPullForce::getPullForceDate));
        //获取白班和夜班的id集合
        Set<Long> classIdSet = pmsPullForceList.stream().map(PmsPullForce::getClassesId).collect(Collectors.toSet());
        Map<Long, SysDictVO> classIdMap = remoteDictFeignApi.listById(new ArrayList<>(classIdSet)).getData();


        List<PmsPullForceVO> pmsPullForceVOList = new ArrayList<>();
        //组装返回参数
        for (Map.Entry<LocalDate, List<PmsPullForce>> entry : groupBy.entrySet()) {
            PmsPullForceVO pullForceVO = new PmsPullForceVO();
            pullForceVO.setPullForceDate(entry.getKey());
            pullForceVO.setWorkshopId(entry.getValue().get(0).getWorkshopId());

            List<PmsPullForce> localDateGroupList = entry.getValue();
            //再按班次分组
            Map<Long, List<PmsPullForce>> classMap = localDateGroupList.stream().collect(Collectors.groupingBy(PmsPullForce::getClassesId));
            for (Map.Entry<Long, List<PmsPullForce>> classEntry : classMap.entrySet()) {
                //获取白班夜班总量
                List<BigDecimal> dayClassTotalList = classEntry.getValue().stream().map(PmsPullForce::getQuantity).collect(Collectors.toList());
                BigDecimal dayClassTotal = BigDecimalUtil.sum(dayClassTotalList);
                String classCode = classIdMap.get(classEntry.getKey()).getCode();

                if (SysDictConstant.CODE_0.equals(classCode)) {
                    pullForceVO.setDayClassTotal(dayClassTotal);
                } else {
                    pullForceVO.setNightClassTotal(dayClassTotal);
                }
            }
            //获取总量
            List<BigDecimal> totalList = localDateGroupList.stream().map(PmsPullForce::getQuantity).collect(Collectors.toList());
            BigDecimal total = BigDecimalUtil.sum(totalList);
            pullForceVO.setTotal(total);
            pmsPullForceVOList.add(pullForceVO);
        }

        List<PmsPullForceVO> pullForceVOList = new ArrayList<>();
        for (LocalDate localDate : localDateList) {
            PmsPullForceVO pmsPullForceVO = new PmsPullForceVO();
            Optional<PmsPullForceVO> present = pmsPullForceVOList.stream().filter(pullForceVO -> pullForceVO.getPullForceDate().equals(localDate)).findAny();
            if (present.isPresent()) {
                BeanUtils.copyProperties(present.get(), pmsPullForceVO);
                pmsPullForceVO.setUnitName(PmManagerConstant.UNIT_T);
            } else {
                pmsPullForceVO.setPullForceDate(localDate);
            }
            pullForceVOList.add(pmsPullForceVO);
        }

        return pullForceVOList;
    }

    @Override
    public List<PmsPullForceDetailsVO> details(Integer workshopId, String pullForceDate) {
        List<PmsPullForce> pmsPullForceList = list(new LambdaQueryWrapper<PmsPullForce>().eq(PmsPullForce::getWorkshopId, workshopId).eq(PmsPullForce::getPullForceDate, pullForceDate));

        //根据产线分组
        Map<Integer, List<PmsPullForce>> groupBy = pmsPullForceList.stream().collect(Collectors.groupingBy(PmsPullForce::getProductLineId));
        Set<Integer> productLineIdSet = pmsPullForceList.stream().map(PmsPullForce::getProductLineId).collect(Collectors.toSet());

        Map<Integer, SysOfficeVO> map = remoteOfficeFeign.listById(productLineIdSet).getData();
        List<SysDictVO> sysDictVOList = remoteDictFeignApi.listByType("sys_shift").getData();
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(workshopId);
        sysOfficeParentDTO.setOfficeType(PmManagerConstant.STATE_9);
        List<SysOfficeVO> sysOfficeVOList = remoteOfficeFeign.listByParentId(sysOfficeParentDTO).getData();
        Assert.notNull(sysOfficeVOList, "车间下暂无产线");


        List<PmsPullForceDetailsVO> pullForceDetailsVOList = new ArrayList<>();
        for (Map.Entry<Integer, List<PmsPullForce>> entry : groupBy.entrySet()) {
            List<BigDecimal> totalList = entry.getValue().stream().map(PmsPullForce::getQuantity).collect(Collectors.toList());
            BigDecimal total = BigDecimalUtil.sum(totalList);
            PmsPullForceDetailsVO pullForceDetailsVO = new PmsPullForceDetailsVO();
            pullForceDetailsVO.setProductLineId(entry.getKey());
            pullForceDetailsVO.setProductLineName(map.get(entry.getKey()).getOfficeName());
            pullForceDetailsVO.setTotal(total);
            pullForceDetailsVO.setUnitName(PmManagerConstant.UNIT_T);

            //获取白班夜班数量
            List<PmsPullForceClassQuantity> pullForceClassQuantityList = new ArrayList<>();
            for (SysDictVO sysDictVO : sysDictVOList) {
                PmsPullForceClassQuantity pullForceClassQuantity = new PmsPullForceClassQuantity();

                Optional<PmsPullForce> present = entry.getValue().stream().filter(pmsPullForce -> pmsPullForce.getClassesId().equals(sysDictVO.getId())).findAny();
                if (present.isPresent()) {
                    pullForceClassQuantity.setId(present.get().getId());
                    pullForceClassQuantity.setClassId(present.get().getClassesId());
                    pullForceClassQuantity.setQuantity(present.get().getQuantity());
                } else {
                    pullForceClassQuantity.setClassId(sysDictVO.getId());
                }
                pullForceClassQuantityList.add(pullForceClassQuantity);
            }

            pullForceDetailsVO.setPullForceClassQuantityList(pullForceClassQuantityList);
            pullForceDetailsVOList.add(pullForceDetailsVO);
        }

        List<PmsPullForceDetailsVO> pmsPullForceDetailsVOList = new ArrayList<>();
        for (SysOfficeVO sysOfficeVO : sysOfficeVOList) {
            PmsPullForceDetailsVO pmsPullForceDetailsVO = new PmsPullForceDetailsVO();
            Optional<PmsPullForceDetailsVO> present = pullForceDetailsVOList.stream().filter(pullForceDetailsVO -> pullForceDetailsVO.getProductLineId().equals(sysOfficeVO.getId())).findAny();
            if (present.isPresent()) {
                pmsPullForceDetailsVOList.add(present.get());
            } else {
                pmsPullForceDetailsVO.setProductLineId(sysOfficeVO.getId());
                pmsPullForceDetailsVO.setProductLineName(sysOfficeVO.getOfficeName());
                List<PmsPullForceClassQuantity> pullForceClassQuantityList = new ArrayList<>();
                pmsPullForceDetailsVO.setPullForceClassQuantityList(pullForceClassQuantityList);
                for (SysDictVO sysDictVO : sysDictVOList) {
                    PmsPullForceClassQuantity pullForceClassQuantity = new PmsPullForceClassQuantity();
                    pullForceClassQuantity.setClassId(sysDictVO.getId());
                    pullForceClassQuantityList.add(pullForceClassQuantity);
                }
                pmsPullForceDetailsVOList.add(pmsPullForceDetailsVO);
            }
        }

        return pmsPullForceDetailsVOList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean editBatch(List<PmsPullForceEditDTO> dtoList) {
        //防止出现重复数据
        RLock lock = redissonClient.getLock(LockConstant.TABLE_PMS_PULL_FORCE);
        lock.lock();
        try {
            List<PmsPullForce> addList = new ArrayList<>();
            List<PmsPullForce> updateList = new ArrayList<>();
            SysDictVO sysDictVO = remoteDictFeignApi.getByCode(PmManagerConstant.UNIT_T, PmManagerConstant.ENERGY_CONSUME).getData();
            for (PmsPullForceEditDTO pmsPullForceEditDTO : dtoList) {
                PmsPullForce pmsPullForce = new PmsPullForce();
                BeanUtils.copyProperties(pmsPullForceEditDTO, pmsPullForce);
                pmsPullForce.setUnitId(sysDictVO.getId());
                if (Objects.isNull(pmsPullForceEditDTO.getId())) {
                    addList.add(pmsPullForce);
                } else {
                    updateList.add(pmsPullForce);
                }
            }
            if (CollectionUtils.isNotEmpty(addList)) {
                if (!saveBatch(addList)) {
                    throw new BusinessException("拉引量新增失败");
                }
            }
            if (CollectionUtils.isNotEmpty(updateList)) {
                if (!updateBatchById(updateList)) {
                    throw new BusinessException("拉引量编辑失败");
                }
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    public Map<Long, PmsPullForce> pullForceDay(Integer productLineId, LocalDate date, List<Long> classGroupIdList) {
        Map<Long, PmsPullForce> resMap = Maps.newHashMap();
        if (Objects.nonNull(productLineId) && Objects.nonNull(date)) {
            List<PmsPullForce> list = list(new LambdaQueryWrapper<PmsPullForce>().eq(PmsPullForce::getProductLineId, productLineId).in(CollUtil.isNotEmpty(classGroupIdList), PmsPullForce::getClassesId, classGroupIdList).eq(PmsPullForce::getPullForceDate, date));
            if (CollUtil.isNotEmpty(list)) {
                for (PmsPullForce pmsPullForce : list) {
                    resMap.put(pmsPullForce.getClassesId(), pmsPullForce);
                }
            }
        }
        return resMap;
    }

    @Override
    public Map<Integer, List<PmsPullForce>> pullForceDayByWorkshop(LocalDate date, List<Integer> productWorkshopIds) {
        Map<Integer, List<PmsPullForce>> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(productWorkshopIds)) {
            List<PmsPullForce> list = list(new LambdaQueryWrapper<PmsPullForce>().in(PmsPullForce::getWorkshopId, productWorkshopIds).eq(PmsPullForce::getPullForceDate, date));

            map = list.stream().collect(Collectors.groupingBy(PmsPullForce::getProductLineId));
        }
        return map;
    }

    @Override
    public Map<String, BigDecimal> getPullForce(Set<String> pfSet) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(pfSet)) {
            List<PmsPullForce> conditionList = Lists.newArrayList();
            pfSet.forEach(ele -> {
                String[] split = ele.split(",");
                PmsPullForce pmsPullForce = new PmsPullForce();
                pmsPullForce.setPullForceDate(LocalDate.parse(split[0]));
                pmsPullForce.setProductLineId(Integer.parseInt(split[1]));
                pmsPullForce.setClassesId(Long.parseLong(split[2]));
                conditionList.add(pmsPullForce);
            });
            //构造条件 () or ()
            LambdaQueryWrapper<PmsPullForce> lqw = new LambdaQueryWrapper<PmsPullForce>().select(PmsPullForce::getPullForceDate, PmsPullForce::getProductLineId, PmsPullForce::getClassesId, PmsPullForce::getQuantity).and(q -> {
                for (int i = 0; i < conditionList.size(); i++) {
                    PmsPullForce pmsPullForce = conditionList.get(i);
                    if (i != 0) {
                        q.or();
                    }
                    q.eq(PmsPullForce::getPullForceDate, pmsPullForce.getPullForceDate()).eq(PmsPullForce::getProductLineId, pmsPullForce.getProductLineId()).eq(PmsPullForce::getClassesId, pmsPullForce.getClassesId());
                }
            });
            List<PmsPullForce> dbList = list(lqw);
            dbList.forEach(ele -> {
                resMap.put(ele.getPullForceDate() + "," + ele.getProductLineId() + "," + ele.getClassesId(), ele.getQuantity());
            });
        }
        return resMap;
    }

    @Override
    public Boolean edit(List<PmsPullForceEditDTO> list) {
        SysDictVO sysDictVO = remoteDictFeignApi.getByCode(PmManagerConstant.UNIT_T, PmManagerConstant.ENERGY_CONSUME).getData();
        List<PmsPullForce> forces = list.stream().map(dto -> {
            PmsPullForce force = getOne(new LambdaQueryWrapper<PmsPullForce>().in(PmsPullForce::getWorkshopId, dto.getWorkshopId()).in(PmsPullForce::getProductLineId, dto.getProductLineId()).in(PmsPullForce::getClassesId, dto.getClassesId()).in(PmsPullForce::getPullForceDate, dto.getPullForceDate()));
            if (Objects.nonNull(force)) {
                removeById(force.getId());
            }
            PmsPullForce pmsPullForce = new PmsPullForce();
            BeanUtils.copyProperties(dto, pmsPullForce);
            pmsPullForce.setUnitId(sysDictVO.getId());
            return pmsPullForce;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(forces)) {
            saveBatch(forces);
        }
        return true;
    }

    @Override
    public List<PmsPullForce> monthlyData(LocalDate date, List<Integer> productWorkshop) {
        LocalDate startDate = date.withDayOfMonth(1);
        return list(new LambdaQueryWrapper<PmsPullForce>().in(PmsPullForce::getWorkshopId, productWorkshop).ge(PmsPullForce::getPullForceDate, startDate).le(PmsPullForce::getPullForceDate, date));
    }

    @Override
    public List<PmsPullForceDataVO> getDataFromPeriods(PmsPullForceQueryDTO dto) {
        List<PmsPullForceDataVO> result = Lists.newArrayList();
        List<PmsPullForce> list = list(new LambdaQueryWrapper<PmsPullForce>()
                .ge(PmsPullForce::getPullForceDate, dto.getStartDate())
                .le(PmsPullForce::getPullForceDate, dto.getEndDate())
                .in(CollUtil.isNotEmpty(dto.getProductLineIds()),PmsPullForce::getProductLineId,dto.getProductLineIds()));
        if(CollUtil.isNotEmpty(list)){
            result = BeanUtil.copyToList(list,PmsPullForceDataVO.class);
        }
        return result;
    }

    @Override
    public Map<String, BigDecimal> datePullForce(List<Integer> lineIdList, LocalDate start, LocalDate end) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        List<PmsPullForce> dbList = list(new LambdaQueryWrapper<PmsPullForce>()
                .ge(PmsPullForce::getPullForceDate, start)
                .le(PmsPullForce::getPullForceDate, end)
                .in(CollUtil.isNotEmpty(lineIdList), PmsPullForce::getProductLineId, lineIdList));
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele ->{
                MapUtil.add(resMap, ele.getPullForceDate() + "", ele.getQuantity(), (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> dateClassPullForce(List<Integer> lineIdList, LocalDate start, LocalDate end) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        List<PmsPullForce> dbList = list(new LambdaQueryWrapper<PmsPullForce>()
                .ge(PmsPullForce::getPullForceDate, start)
                .le(PmsPullForce::getPullForceDate, end)
                .in(CollUtil.isNotEmpty(lineIdList), PmsPullForce::getProductLineId, lineIdList));
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                MapUtil.add(resMap, ele.getPullForceDate() + "_" + ele.getClassesId(), ele.getQuantity(), (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> dateClassLinePmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        List<PmsPullForce> dbList = list(new LambdaQueryWrapper<PmsPullForce>()
                .ge(PmsPullForce::getPullForceDate, start)
                .le(PmsPullForce::getPullForceDate, end)
                .in(CollUtil.isNotEmpty(lineIdList), PmsPullForce::getProductLineId, lineIdList));
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getPullForceDate() + "_" + ele.getClassesId(), ele.getQuantity(), (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> dateLinePmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        List<PmsPullForce> dbList = list(new LambdaQueryWrapper<PmsPullForce>()
                .ge(PmsPullForce::getPullForceDate, start)
                .le(PmsPullForce::getPullForceDate, end)
                .in(CollUtil.isNotEmpty(lineIdList), PmsPullForce::getProductLineId, lineIdList));
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getPullForceDate(), ele.getQuantity(), (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        return resMap;
    }

    @Override
    public CommonReportDTO jpPfDateGranularity(Integer workShopId, LocalDate start, LocalDate end, Integer granularityInt) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(granularityInt);
        Assert.notNull(granularity, "粒度不存在");
        CommonReportDTO res = new CommonReportDTO();
        List<PmsPullForce> dbList = list(new LambdaQueryWrapper<PmsPullForce>()
                .ge(PmsPullForce::getPullForceDate, start)
                .le(PmsPullForce::getPullForceDate, end)
                .eq(PmsPullForce::getWorkshopId, workShopId));
        Map<String, BigDecimal> map = new HashMap<>();
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                String convert = MysqlDateKeyUtil.convert(granularity, ele.getPullForceDate());
                MapUtil.add(map, convert, ele.getQuantity(), (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        res.setName("拉引量");
        res.setUnit("T");
        res.setValueMap(map);
        return res;
    }

    @Override
    public Map<String, BigDecimal> pullForceSum(PullForceSumDTO dto) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        cn.hutool.core.lang.Assert.notNull(granularity, "粒度不存在");
        List<PmsPullForce> dataList = this.list(new QueryWrapper<PmsPullForce>()
                .select("pull_force_date, sum(quantity) as quantity").lambda()
                .in(CollUtil.isNotEmpty(dto.getWorkshopIdSet()), PmsPullForce::getWorkshopId, dto.getWorkshopIdSet())
                .in(CollUtil.isNotEmpty(dto.getLineIdSet()), PmsPullForce::getProductLineId, dto.getLineIdSet())
                .ge(Objects.nonNull(dto.getStart()), PmsPullForce::getPullForceDate, dto.getStart())
                .le(Objects.nonNull(dto.getEnd()), PmsPullForce::getPullForceDate, dto.getEnd())
                .groupBy(PmsPullForce::getPullForceDate));
        if (CollUtil.isNotEmpty(dataList)) {
            List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
            Assert.notEmpty(dataList, "日期区间有误");
            for (int i = 1; i < dateList.size(); i++) {
                LocalDate left = dateList.get(i - 1);
                LocalDate right = dateList.get(i);
                String convert = MysqlDateKeyUtil.convert(granularity, left);
                List<BigDecimal> valueList = dataList.stream().filter(xx -> LocalDateUtil.isRange(xx.getPullForceDate(), left, right, Boolean.TRUE, Boolean.FALSE)).map(PmsPullForce::getQuantity).collect(Collectors.toList());
                resMap.put(convert, BigDecimalUtil.sum(valueList));
            }
        }
        return resMap;
    }

    @Override
    public SteccnTableDataDTO<Map<String, String>> pullForceReport(PmsPullForceDashboardDTO dto) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        List<PmsPullForceDayCountDTO> data = Lists.newArrayList();
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            data = pmsPullForceMapper.selectPullForceByDay2(dto.getStart(), dto.getEnd(), dto.getProductLineId().stream().collect(Collectors.toSet()));
        }else if(QtManagerConstant.QtGranularityEnum.CLASS.equals(granularity)){
            data = pmsPullForceMapper.selectPullForceByClass(dto.getStart(), dto.getEnd(), dto.getProductLineId().stream().collect(Collectors.toSet()));
        }else if(QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)){
            data = pmsPullForceMapper.selectPullForceByMonth2(dto.getStart(), dto.getEnd(), dto.getProductLineId().stream().collect(Collectors.toSet()));
        }
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> result = new SteccnTableDataDTO<>();
        //组表头
        List<LocalDate> dateList;
        if(QtManagerConstant.QtGranularityEnum.CLASS.equals(granularity)){
            dateList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), dto.getStart(), dto.getEnd());
        }else {
            dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        }
        if(QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)){
            List<SteccnTableColumn> columns = Lists.newArrayList();
            columns.add(new SteccnTableColumn("productLine","产线",null));
            for (int i = 1; i < dateList.size(); i++) {
                LocalDate start = dateList.get(i - 1);
                columns.add(new SteccnTableColumn(start.toString(),start.getMonthValue()+"月"+start.getDayOfMonth()+"日",null));
            }
            columns.add(new SteccnTableColumn("count","小计",null));
            result.setColumns(columns);
        }else if(QtManagerConstant.QtGranularityEnum.CLASS.equals(granularity)){
            List<SteccnTableColumn> columns = Lists.newArrayList();
            columns.add(new SteccnTableColumn("productLine","产线",null));
            columns.add(new SteccnTableColumn("classOrder","班次",null));
            columns.add(new SteccnTableColumn("classGroup","班组",null));
            for (int i = 1; i < dateList.size(); i++) {
                LocalDate start = dateList.get(i - 1);
                columns.add(new SteccnTableColumn("head"+i,start.getMonthValue()+"月"+start.getDayOfMonth()+"日",
                        Lists.newArrayList(new SteccnTableColumn("classGroup","班组",null),new SteccnTableColumn(start.toString(),"拉引量(吨)",null))));
            }
            columns.add(new SteccnTableColumn("count","小计",null));
            result.setColumns(columns);
        }else if(QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)){
            List<SteccnTableColumn> columns = Lists.newArrayList();
            columns.add(new SteccnTableColumn("productLine","产线",null));
            for (int i = 1; i < dateList.size(); i++) {
                LocalDate start = dateList.get(i - 1);
                columns.add(new SteccnTableColumn(start.toString(),start.getYear()+"年"+start.getMonthValue()+"月",null));
            }
            columns.add(new SteccnTableColumn("count","小计",null));
            result.setColumns(columns);
        }
        //组数据
        List<Map<String,String>> tableLists = Lists.newArrayList();
        Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(dto.getProductLineId().stream().collect(Collectors.toSet())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysOfficeVO> officeList = officeMap.values().stream().sorted(Comparator.comparingInt(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
        //循环list循环
        Map<String,BigDecimal> countMap = Maps.newHashMap();
        //班次
        if(QtManagerConstant.QtGranularityEnum.CLASS.equals(granularity)){
            //获取班组班次关系
            Map<String, OaGroupCountDTO> groupMap = FeignOptional.of(() -> oaInfoService.getGroupInfoJp(new TripleDTO<>(dto.getStart(), dto.getEnd(), OaConstant.NO)), FeignConstant.OA_SERVICE_NAME).get();
            for(SysOfficeVO officeTmp:officeList){
                //产线合计
                Map<String,BigDecimal> productLineCountMap = Maps.newHashMap();
                for(int i = 0; i < 2; i++){
                    Map<String,String> line = Maps.newHashMap();
                    line.put("productLine",officeTmp.getOfficeName());
                    line.put("type","0");
                    String classOrder;
                    if(i==0){
                        classOrder = "早班";
                    }else {
                        classOrder = "晚班";
                    }
                    line.put("classOrder",classOrder);
                    //小计
                    BigDecimal count = BigDecimal.ZERO;
                    for (int j = 1; j < dateList.size(); j++) {
                        LocalDate date = dateList.get(j - 1);
                        line.put("classGroup",groupMap.getOrDefault(date.toString()+"-"+ classOrder,new OaGroupCountDTO()).getGroupName());
                        int finalI = i;
                        PmsPullForceDayCountDTO dataTmp = data.stream().filter(v -> v.getProductLineId().equals(officeTmp.getId())
                                && v.getPullForceDateStr().equals(date.toString())
                                && v.getClassId().equals(finalI ==0 ? 1587279237069422592L: 1587279280585326592L))
                                .findFirst().orElse(new PmsPullForceDayCountDTO());
                        line.put(date.toString(),Objects.isNull(dataTmp.getQuantity()) ? null : dataTmp.getQuantity().toString());
                        //小计
                        count = BigDecimalUtil.add(count,dataTmp.getQuantity());
                        BigDecimalUtil.accumulate(productLineCountMap,date.toString(),dataTmp.getQuantity());
                        BigDecimalUtil.accumulate(countMap,date.toString(),dataTmp.getQuantity());
                        productLineCountMap.put("count",dataTmp.getQuantity());
                    }
                    line.put("count",Objects.isNull(count) ? null : count.toString());
                    BigDecimalUtil.accumulate(productLineCountMap,"count",count);
                    BigDecimalUtil.accumulate(countMap,"count",count);
                    tableLists.add(line);
                }
                //产线合计
                Map<String,String> countLine = Maps.newHashMap();
                countLine.put("type","1");
                for(int i = 1; i < dateList.size(); i++){
                    LocalDate date = dateList.get(i - 1);
                    countLine.put(date.toString(),Objects.isNull(productLineCountMap.get(date.toString())) ? null : productLineCountMap.get(date.toString()).toString());
                }
                countLine.put("count",Objects.isNull(productLineCountMap.get("count")) ? null : countMap.get("count").toString());
                tableLists.add(countLine);
            }
        }else {
            for(SysOfficeVO officeTmp:officeList){
                Map<String,String> line = Maps.newHashMap();
                line.put("productLine",officeTmp.getOfficeName());
                line.put("type","0");
                for (int i = 1; i < dateList.size(); i++) {
                    LocalDate date = dateList.get(i - 1);
                    PmsPullForceDayCountDTO dataTmp = data.stream().filter(v -> v.getProductLineId().equals(officeTmp.getId())
                            && v.getPullForceDateStr().equals(QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity) ? date.format(DateTimeFormatter.ofPattern("yyyy-MM")) : date.toString())).findFirst().orElse(new PmsPullForceDayCountDTO());
                    line.put(date.toString(),Objects.isNull(dataTmp.getQuantity()) ? null : dataTmp.getQuantity().toString());
                    //累加日期和产线
                    BigDecimalUtil.accumulate(countMap,date.toString(),dataTmp.getQuantity());
                    BigDecimalUtil.accumulate(countMap,officeTmp.getId().toString(),dataTmp.getQuantity());
                }
                //累加小计
                BigDecimalUtil.accumulate(countMap,"count",countMap.get(officeTmp.getId().toString()));
                line.put("count",Objects.isNull(countMap.get(officeTmp.getId().toString())) ? null : countMap.get(officeTmp.getId().toString()).toString());
                tableLists.add(line);
            }
        }
        //合计
        Map<String,String> countLine = Maps.newHashMap();
        countLine.put("type","2");
        for(int i = 1; i < dateList.size(); i++){
            LocalDate date = dateList.get(i - 1);
            countLine.put(date.toString(),Objects.isNull(countMap.get(date.toString())) ? null : countMap.get(date.toString()).toString());
        }
        countLine.put("count",Objects.isNull(countMap.get("count")) ? null : countMap.get("count").toString());
        tableLists.add(countLine);
        result.setTableList(tableLists);
        return result;
    }
}




