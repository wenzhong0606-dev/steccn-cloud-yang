package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.config.FeignConfig;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.dto.*;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.mapper.EpInstrumentAccountMapper;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.util.PinyinUtils;
import com.steccn.ems.vo.*;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.vo.QtSpcReportDetailVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.CollationKey;
import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【ep_instrument_account(仪表台账)】的数据库操作Service实现
 * @createDate 2022-11-29 19:27:28
 */
@Service
@Slf4j
public class EpInstrumentAccountServiceImpl extends ServiceImpl<EpInstrumentAccountMapper, EpInstrumentAccount>
        implements EpInstrumentAccountService {

    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private EpPartitionService epPartitionService;
    @Autowired
    private EpEnergyTypeService epEnergyTypeService;
    @Autowired
    private EpMeterReadService epMeterReadService;
    @Autowired
    private RemoteDataPlatformFeignApi remoteDataPlatformFeignApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrEdit(EpInstrumentAccountDTO dto) {
        long count = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                //所属分区节点
                .eq(EpInstrumentAccount::getPartitionNode, dto.getPartitionNode())
                //所属能源类型
                .eq(EpInstrumentAccount::getMeterType, dto.getMeterType())
                //可展示的未删除的
                .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES)
                //编辑时去除对自身的判断
                .ne(Objects.nonNull(dto.getId()), EpInstrumentAccount::getId, dto.getId()));
        getRepeatJudgment(dto.getId(), count, "该节点下已经有仪表,请勿多次创建");

        if (StringUtils.isNotBlank(dto.getDistantMapCode())) {
            List<EpInstrumentAccount> list = list(new LambdaQueryWrapper<EpInstrumentAccount>()
                    .eq(EpInstrumentAccount::getDistantMapCode, dto.getDistantMapCode())
                    .eq(EpInstrumentAccount::getMeterType, dto.getMeterType())
                    .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES)
                    .ne(Objects.nonNull(dto.getId()), EpInstrumentAccount::getId, dto.getId()));
            getRepeatJudgment(dto.getId(), list.size(), "远传表映射编码不能与其他仪表重复");
        }

        long sizeMeterName = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getMeterName, dto.getMeterName())
                .eq(EpInstrumentAccount::getMeterType, dto.getMeterType())
                .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES)
                .ne(Objects.nonNull(dto.getId()), EpInstrumentAccount::getId, dto.getId()));
        getRepeatJudgment(dto.getId(), sizeMeterName, "仪表名称不能与其他仪表重复");

        if (StringUtils.isNotBlank(dto.getSiteName()) && dto.getMeterType() != 1) {
            //查询电表下的所有安装位置有无重复
            long sizeSiteName = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                    .eq(EpInstrumentAccount::getMeterType, dto.getMeterType())
                    .eq(EpInstrumentAccount::getSiteName, dto.getSiteName())
                    .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES)
                    .ne(Objects.nonNull(dto.getId()), EpInstrumentAccount::getId, dto.getId()));
            getRepeatJudgment(dto.getId(), sizeSiteName, "安装位置不能与其他仪表重复");
        }
        if (StringUtils.isNotBlank(dto.getAssetsCode())) {
            //查询数据库中的资产编码
            long sizeAssetsCode = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                    .eq(EpInstrumentAccount::getAssetsCode, dto.getAssetsCode())
                    .eq(EpInstrumentAccount::getMeterType, dto.getMeterType())
                    .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES)
                    .ne(Objects.nonNull(dto.getId()), EpInstrumentAccount::getId, dto.getId()));
            getRepeatJudgment(dto.getId(), sizeAssetsCode, "资产编码不能与其他仪表重复");
        }
        EpInstrumentAccount epInstrumentAccount = new EpInstrumentAccount();
        BeanUtils.copyProperties(dto, epInstrumentAccount);
        if (Objects.isNull(dto.getId())) {
            save(epInstrumentAccount);
        } else {
            EpInstrumentAccount instrumentAccount = getById(dto.getId());
            Assert.notNull(instrumentAccount, "编辑的仪表台账不存在");
            if (MpConstant.NO == epInstrumentAccount.getShowFlag()) {
                throw new BusinessException("该仪表台账已删除无法编辑");
            }
            updateById(epInstrumentAccount);
        }
        return Boolean.TRUE;
    }

    @Override
    public Page<EpInstrumentAccountPageVO> getEpInstrumentAccountVOPage(EpInstrumentAccountPageDTO dto) {
        //自定义查询条件
        LambdaQueryWrapper<EpInstrumentAccount> condition = new LambdaQueryWrapper<EpInstrumentAccount>()
                //所属分区节点id数组
                .in(CollUtil.isNotEmpty(dto.getPartitionNode()), EpInstrumentAccount::getPartitionNode, dto.getPartitionNode())
                //模糊匹配仪表名称
                .like(Objects.nonNull(dto.getMeterName()), EpInstrumentAccount::getMeterName, dto.getMeterName())
                //能源类型
                .eq(EpInstrumentAccount::getMeterType, dto.getEnergyId())
                //查询所有可展示的
                .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES);

        //3.根据条件查询数据库数据
        List<EpInstrumentAccount> instrumentAccounts = list(condition);
        //4.拷贝返回数据
        List<EpInstrumentAccountPageVO> instrumentAccountVOS = BeanUtil.copyToList(instrumentAccounts, EpInstrumentAccountPageVO.class);
        //5.处理返回数据
        //自定义分页对象
        Page<EpInstrumentAccountPageVO> page = new Page<>();
        if (CollUtil.isEmpty(instrumentAccountVOS)) {
            //如果为空返回空对象
            return page;
        }
        //能源分区节点
        Set<Long> partitionIds = Sets.newHashSet();
        //数据字典
        Set<Long> dictSet = Sets.newHashSet();
        instrumentAccountVOS.forEach(s -> {
            partitionIds.add(s.getPartitionNode());
            if (Objects.nonNull(s.getSite())) {
                dictSet.add(s.getSite());
            }
        });

        //去数据字典中查找到所有位置
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //去能源分区表中找到所有的分区节点
        Map<Long, EpPartitionVO> epPartitionNameListVOMap = epPartitionService.getEpPartitionNameListVO(partitionIds);

        //获取该类型下所有下一级子节点
        Map<Long, List<EpPartitionVO>> partitionLinkChildMap = epPartitionService.allNextChildren(dto.getEnergyId());
        //找到该类型下根据节点分组的全量数据
        Map<Long, EpInstrumentAccountPageVO> partitionInstrumentVOList = Maps.newHashMap();
        //远传仪表编码
        Set<String> distantMapCodeSet = Sets.newHashSet();
        instrumentAccountVOS.forEach(s -> {
            partitionInstrumentVOList.put(s.getPartitionNode(), s);
            distantMapCodeSet.add(s.getDistantMapCode());
        });
        //避免dataplatform服务不可用
        Map<String, LocalDateTime> lastReadTimeMap;
        try {
            lastReadTimeMap = FeignOptional.of(() -> remoteDataPlatformFeignApi.lastReadTime(distantMapCodeSet), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
        }catch (Exception e){
            log.error("{}",e.getLocalizedMessage());
            lastReadTimeMap = Maps.newHashMap();
        }

        //查询所有非远传仪表编码的最近抄表时间
        Set<Integer> meterIdSet = instrumentAccountVOS.stream().filter(v -> EpManagerConstant.InstrumentType.NON_REMOTE.getCode().equals(v.getEntityTable()))
                .map(EpInstrumentAccountPageVO::getId).collect(Collectors.toSet());

        Map<Integer, LocalDateTime> readTimeMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(meterIdSet)) {
            List<EpMeterRead> epMeterReadList = epMeterReadService.list(new QueryWrapper<EpMeterRead>().select("meter_id , max(read_time) as readTime").lambda()
                    .in(EpMeterRead::getMeterId, meterIdSet).groupBy(EpMeterRead::getMeterId));
            if (CollUtil.isNotEmpty(epMeterReadList)) {
                epMeterReadList.forEach(ele -> readTimeMap.put(ele.getMeterId(), ele.getReadTime()));
            }
        }
        //查询allPartitionNodeName
        Map<Long, String> allPartitionNameMap = epPartitionService.allPartitionNameMap(dto.getEnergyId(), partitionInstrumentVOList.keySet());
        //遍历集合插入数据
        Map<String, LocalDateTime> finalLastReadTimeMap = lastReadTimeMap;
        instrumentAccountVOS.forEach(s -> {
            if (CollUtil.isNotEmpty(dictMap) && Objects.nonNull(s.getSite())) {
                s.setSiteName(dictMap.get(s.getSite()));
            }
            EpPartitionVO epPartitionVO = epPartitionNameListVOMap.get(s.getPartitionNode());
            s.setPartitionNodeName(Objects.isNull(epPartitionVO) ? null : epPartitionVO.getOfficeName());
            s.setAllPartitionNodeName(allPartitionNameMap.get(s.getPartitionNode()));

            //设置最后抄表时间
            if (EpManagerConstant.InstrumentType.REMOTE.getCode().equals(s.getEntityTable())) {
                //远传仪表-从数仓获取
                s.setLastReadingTime(getLastReadTime(s.getDistantMapCode(), finalLastReadTimeMap));
            } else if (EpManagerConstant.InstrumentType.NON_REMOTE.getCode().equals(s.getEntityTable())) {
                //非远传仪表-从抄表管理获取
                s.setLastReadingTime(readTimeMap.get(s.getId()));
            } else if (EpManagerConstant.InstrumentType.VIRTUAL.getCode().equals(s.getEntityTable())) {
                //虚拟仪表的最后抄表时间：所有下一级子表的最后抄表时间中最早的一个【下一级子表中最后抄表时间为空，则当前虚拟仪表的最后抄表时间也为空】
                List<EpPartitionVO> partitionChildrenList = partitionLinkChildMap.get(s.getPartitionNode());
                LocalDateTime vmLastTime = findVMLastTime(partitionLinkChildMap, partitionChildrenList, finalLastReadTimeMap, readTimeMap);
                s.setLastReadingTime(vmLastTime);
            }
        });

        //排序+逻辑分页
        if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_METER_READING_TIME.equals(dto.getSortField())) {
            List<EpInstrumentAccountPageVO> havaDataCollect = instrumentAccountVOS.stream().filter(v -> Objects.nonNull(v.getLastReadingTime())).collect(Collectors.toList());
            List<EpInstrumentAccountPageVO> noDataCollect = instrumentAccountVOS.stream().filter(v -> Objects.isNull(v.getLastReadingTime())).collect(Collectors.toList());
            if (dto.getAscFlag() == Boolean.TRUE) {
                instrumentAccountVOS.clear();
                if (CollUtil.isNotEmpty(havaDataCollect)) {
                    havaDataCollect = havaDataCollect.stream().sorted(Comparator.comparing(EpInstrumentAccountPageVO::getLastReadingTime)).collect(Collectors.toList());
                    for (EpInstrumentAccountPageVO epInstrumentAccountPageVO : havaDataCollect) {
                        instrumentAccountVOS.add(epInstrumentAccountPageVO);
                    }
                }
                if (CollUtil.isNotEmpty(noDataCollect)) {
                    for (EpInstrumentAccountPageVO epInstrumentAccountPageVO : noDataCollect) {
                        instrumentAccountVOS.add(epInstrumentAccountPageVO);
                    }
                }
            } else {
                instrumentAccountVOS.clear();
                if (CollUtil.isNotEmpty(havaDataCollect)) {
                    havaDataCollect = havaDataCollect.stream().sorted(Comparator.comparing(EpInstrumentAccountPageVO::getLastReadingTime).reversed()).collect(Collectors.toList());
                    for (EpInstrumentAccountPageVO epInstrumentAccountPageVO : havaDataCollect) {
                        instrumentAccountVOS.add(epInstrumentAccountPageVO);
                    }
                }
                if (CollUtil.isNotEmpty(noDataCollect)) {
                    for (EpInstrumentAccountPageVO epInstrumentAccountPageVO : noDataCollect) {
                        instrumentAccountVOS.add(epInstrumentAccountPageVO);
                    }
                }
            }
        } else if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_DISTANT_MAPCODE.equals(dto.getSortField())) {
            instrumentAccountVOS = listToSortBySubName(instrumentAccountVOS, dto.getSortField(), dto.getAscFlag());
        } else if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_METER_NAME.equals(dto.getSortField())) {
            instrumentAccountVOS = listToSortBySubName(instrumentAccountVOS, dto.getSortField(), dto.getAscFlag());
        } else if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_SITE_NAME.equals(dto.getSortField())) {
            instrumentAccountVOS = listToSortBySubName(instrumentAccountVOS, dto.getSortField(), dto.getAscFlag());
        } else if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_STATUS.equals(dto.getSortField())) {
            if (dto.getAscFlag() == Boolean.TRUE) {
                instrumentAccountVOS = instrumentAccountVOS.stream().sorted(Comparator.comparing(EpInstrumentAccountPageVO::getStatus).reversed()).collect(Collectors.toList());
            } else {
                instrumentAccountVOS = instrumentAccountVOS.stream().sorted(Comparator.comparing(EpInstrumentAccountPageVO::getStatus)).collect(Collectors.toList());
            }

        }
        page = LogicalPageUtil.page(instrumentAccountVOS, dto.getCurrent(), dto.getSize());
        //根据partitionNode排序
        if (CollUtil.isNotEmpty(page.getRecords())) {
            page.getRecords().sort(Comparator.comparing(EpInstrumentAccountPageVO::getPartitionNode));
        }
        return page;
    }

    /**
     * 获取虚拟仪表的最后抄表时间
     *
     * @param
     * @param children
     * @param distantMap
     * @param readTimeMap
     * @return
     */
    private LocalDateTime findVMLastTime(Map<Long, List<EpPartitionVO>> map, List<EpPartitionVO> children, Map<String, LocalDateTime> distantMap, Map<Integer, LocalDateTime> readTimeMap) {
        LocalDateTime min = null;
        if (CollUtil.isNotEmpty(children)) {
            for (EpPartitionVO child : children) {
                LocalDateTime date = null;
                if (EpManagerConstant.InstrumentType.REMOTE.getCode().equals(child.getEntityTable())) {
                    //远传仪表-从数仓获取
                    date = getLastReadTime(child.getDistantMapCode(), distantMap);
                } else if (EpManagerConstant.InstrumentType.NON_REMOTE.getCode().equals(child.getEntityTable())) {
                    //非远传仪表-从抄表管理获取
                    date = readTimeMap.get(child.getId());
                } else if (EpManagerConstant.InstrumentType.VIRTUAL.getCode().equals(child.getEntityTable())) {
                    //虚拟仪表的最后抄表时间：所有下一级子表的最后抄表时间中最早的一个【下一级子表中最后抄表时间为空，则当前虚拟仪表的最后抄表时间也为空】
                    List<EpPartitionVO> curChildren = map.get(child.getId());
                    date = findVMLastTime(map, curChildren, distantMap, readTimeMap);
                }
                if (Objects.isNull(date)) {
                    //孩子有一个为空，则直接返回
                    min = null;
                    break;
                } else if (Objects.isNull(min) || min.isAfter(date)) {
                    //孩子不为空，第一个孩子或者是比前面的抄表时间要小
                    min = date;
                }
            }
        }
        return min;
    }

    private List<String> getParentNameList(List<EpPartitionParentVO> parentList, List<String> partitionName) {
        if (CollUtil.isNotEmpty(parentList)) {
            for (EpPartitionParentVO epPartitionParentVO : parentList) {
                partitionName.add(epPartitionParentVO.getOfficeName());
                if (CollUtil.isNotEmpty(epPartitionParentVO.getParentList())) {
                    for (EpPartitionParentVO partitionParentVO : epPartitionParentVO.getParentList()) {
                        getParentNameList(partitionParentVO.getParentList(), partitionName);
                    }
                }
            }
        }
        return partitionName;
    }

    private LocalDateTime getLastReadTime(String code, Map<String, LocalDateTime> map) {
        return map.get(code);
    }

    @Override
    public List<Long> getPartitionNodes(Integer energyType) {
        List<Long> partitionNodesVO = Lists.newArrayList();
        List<Long> partitionNodes = list(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getMeterType, energyType)
                .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES))
                .stream().map(EpInstrumentAccount::getPartitionNode).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(partitionNodes)) {
            partitionNodes.forEach(v -> {
                partitionNodesVO.add(v);
            });
        }
        return partitionNodesVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reviseStatus(EpInstrumentAccountStatusDTO dto) {
        EpInstrumentAccount epInstrumentAccount = getById(dto.getId());
        Assert.notNull(epInstrumentAccount, "该仪表台账不存在");
        Assert.isTrue(MpConstant.YES == epInstrumentAccount.getShowFlag(), "该仪表台账已删除");
        epInstrumentAccount.setStatus(dto.getStatus());
        updateById(epInstrumentAccount);
        return Boolean.TRUE;
    }

    @Override
    public EpInstrumentAccountVO getEpInstrumentAccountVO(Integer id) {
        Assert.notNull(id, "查询id不能为空");
        EpInstrumentAccount epInstrumentAccount = getById(id);
        if (Objects.isNull(epInstrumentAccount)) {
            return null;
        }
        Assert.isTrue(MpConstant.YES == epInstrumentAccount.getShowFlag(), "该仪表台账已删除");
        //设置返回实体类对象
        EpInstrumentAccountVO epInstrumentAccountVO = new EpInstrumentAccountVO();
        BeanUtils.copyProperties(epInstrumentAccount, epInstrumentAccountVO);
        Set<Long> partitionIds = Sets.newHashSet();
        partitionIds.add(epInstrumentAccount.getPartitionNode());
        Map<Long, EpPartitionVO> epPartitionVOMap = epPartitionService.getEpPartitionNameListVO(partitionIds);
        if (CollUtil.isNotEmpty(epPartitionVOMap)) {
            EpPartitionVO epPartitionVO = epPartitionVOMap.get(epInstrumentAccountVO.getPartitionNode());
            if (Objects.nonNull(epPartitionVO)) {
                //设置所属分区节点
                epInstrumentAccountVO.setPartitionNodeName(epPartitionVO.getOfficeName());
            }
        }
        EpEnergyTypeVO energyTypeVO = epEnergyTypeService.getEpAddEnergyTypeVO(epInstrumentAccountVO.getMeterType());
        if (Objects.nonNull(energyTypeVO)) {
            //设置仪表名称
            epInstrumentAccountVO.setMeterTypeName(energyTypeVO.getMeasureMeterName());
            //设置计量单位
            epInstrumentAccountVO.setMeasureUnitName(energyTypeVO.getMeasureUnitName());
        }
        if (Objects.nonNull(epInstrumentAccountVO.getSite())) {
            Set<Long> dictSet = Sets.newHashSet();
            dictSet.add(epInstrumentAccountVO.getSite());
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (CollUtil.isNotEmpty(dictMap)) {
                epInstrumentAccountVO.setSiteName(dictMap.get(epInstrumentAccountVO.getSite()));
            }
        }
        if (Objects.nonNull(epInstrumentAccountVO.getPartitionNode()) && Objects.nonNull(epInstrumentAccountVO.getMeterType())) {
            EpPartitionChildTypeDTO epPartitionChildTypeDTO = new EpPartitionChildTypeDTO();
            epPartitionChildTypeDTO.setChildId(epInstrumentAccountVO.getPartitionNode());
            epPartitionChildTypeDTO.setType(epInstrumentAccountVO.getMeterType());
            EpPartitionParentVO parentVO = epPartitionService.getParentList(epPartitionChildTypeDTO);
            if (Objects.nonNull(parentVO) && Objects.nonNull(parentVO.getJoinOfficeNames())) {
                epInstrumentAccountVO.setAllPartitionNodeName(parentVO.getJoinOfficeNames());
            }
        }
        return epInstrumentAccountVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        EpInstrumentAccount epInstrumentAccount = getById(id);
        Assert.notNull(epInstrumentAccount, "该仪表台账不存在或已删除");
        epInstrumentAccount.setShowFlag(MpConstant.NO);
        updateById(epInstrumentAccount);
        return Boolean.TRUE;
    }

    @Override
    public Map<Long, EpInstrumentAccount> listById(Set<Long> partitionNodeIdSet) {
        Map<Long, EpInstrumentAccount> resMap = Maps.newHashMap();
        List<EpInstrumentAccount> dbList = list(new LambdaQueryWrapper<EpInstrumentAccount>().in(EpInstrumentAccount::getPartitionNode, partitionNodeIdSet));
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> resMap.put(ele.getPartitionNode(), ele));
        }
        return resMap;
    }

    @Override
    public Map<Long, EpInstrumentAccountDTO> queryNodeLinkMeter(Integer energyType) {
        List<EpInstrumentAccount> accountList = list(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES)
                .eq(EpInstrumentAccount::getMeterType, energyType));
        //获取仪表id集合
        Map<Long, EpInstrumentAccountDTO> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(accountList)) {
            accountList.forEach(ele -> {
                EpInstrumentAccountDTO accountDTO = new EpInstrumentAccountDTO();
                BeanUtils.copyProperties(ele, accountDTO);
                resMap.put(ele.getPartitionNode(), accountDTO);
            });
        }
        return resMap;
    }

    @Override
    public Map<Long, EpInstrumentAccountDTO> queryNodeLinkMeterFilter(Integer energyType,Set<Long> partitionIdSet) {
        List<EpInstrumentAccount> accountList = list(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES)
                .eq(EpInstrumentAccount::getMeterType, energyType)
                .in(EpInstrumentAccount::getPartitionNode, partitionIdSet));
        //获取仪表id集合
        Map<Long, EpInstrumentAccountDTO> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(accountList)) {
            accountList.forEach(ele -> {
                EpInstrumentAccountDTO accountDTO = new EpInstrumentAccountDTO();
                BeanUtils.copyProperties(ele, accountDTO);
                resMap.put(ele.getPartitionNode(), accountDTO);
            });
        }
        return resMap;
    }

    @Override
    public List<String> remoteAccountList() {
        List<String> accountList = Lists.newArrayList();
        List<EpInstrumentAccount> dbList = list(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getEntityTable, 0)
                .isNotNull(EpInstrumentAccount::getDistantMapCode));
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                accountList.add(ele.getDistantMapCode());
            });
        }
        return accountList;
    }

    /**
     * 根据条件判断是否抛出异常
     *
     * @param id
     * @param size
     * @param mistake
     */
    private void getRepeatJudgment(Integer id, long size, String mistake) {
        Assert.isTrue(size == 0, mistake);
    }

    /**
     * 根据某字段首字母排序
     *
     * @param list
     * @param sort
     * @param isOrder
     * @return
     */
    private List<EpInstrumentAccountPageVO> listToSortBySubName(List<EpInstrumentAccountPageVO> list, String sort, Boolean isOrder) {
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        Map<String, EpInstrumentAccountPageVO> map = new HashMap<String, EpInstrumentAccountPageVO>();
        List<String> names = new ArrayList<>();
        //储存该字段不为空的集合
        List<EpInstrumentAccountPageVO> haveDataList = new ArrayList<>();
        //储存该字段为空的集合
        List<EpInstrumentAccountPageVO> noDataList = new ArrayList<>();
        if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_DISTANT_MAPCODE.equals(sort)) {
            list.stream().filter(s -> Objects.nonNull(s)).forEach(s -> {
                if (Objects.nonNull(s.getDistantMapCode()) && StringUtils.isNotBlank(s.getDistantMapCode())) {
                    haveDataList.add(s);
                } else {
                    noDataList.add(s);
                }
            });

        } else if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_METER_NAME.equals(sort)) {
            list.stream().filter(s -> Objects.nonNull(s)).forEach(s -> {
                if (Objects.nonNull(s.getMeterName()) && StringUtils.isNotBlank(s.getMeterName())) {
                    haveDataList.add(s);
                } else {
                    noDataList.add(s);
                }
            });

        } else if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_SITE_NAME.equals(sort)) {
            list.stream().filter(s -> Objects.nonNull(s)).forEach(s -> {
                if (Objects.nonNull(s.getSiteName()) && StringUtils.isNotBlank(s.getSiteName())) {
                    haveDataList.add(s);
                } else {
                    noDataList.add(s);
                }
            });
        }
        //创建返回数据集合
        List<EpInstrumentAccountPageVO> voList = new ArrayList<>();

        if (CollUtil.isNotEmpty(haveDataList)) {
            getSort(haveDataList, isOrder, sort);
            haveDataList.forEach(s -> {
                voList.add(s);
            });
        }
        //封装该字段为空情况的集合
        if (CollUtil.isNotEmpty(noDataList)) {
            noDataList.forEach(s -> {
                voList.add(s);
            });
        }

        return voList;
    }

    public static void getSort(List<EpInstrumentAccountPageVO> list, Boolean isOrder, String sort) {
        Comparator comparator = Collator.getInstance(Locale.CHINA);
        if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_DISTANT_MAPCODE.equals(sort)) {
            if (isOrder) {
                Collections.sort(list, (p1, p2) -> comparator.compare(PinyinUtils.converterToFirstSpell(p1.getDistantMapCode().substring(0, 1)).toLowerCase(),
                        PinyinUtils.converterToFirstSpell(p2.getDistantMapCode().substring(0, 1)).toLowerCase()));

            } else {
                Collections.sort(list, (p1, p2) -> comparator.compare(PinyinUtils.converterToFirstSpell(p2.getDistantMapCode().substring(0, 1)).toLowerCase(),
                        PinyinUtils.converterToFirstSpell(p1.getDistantMapCode().substring(0, 1)).toLowerCase()));
            }
        } else if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_METER_NAME.equals(sort)) {
            if (isOrder) {
                Collections.sort(list, (p1, p2) -> comparator.compare(PinyinUtils.converterToFirstSpell(p1.getMeterName().substring(0, 1)).toLowerCase(),
                        PinyinUtils.converterToFirstSpell(p2.getMeterName().substring(0, 1)).toLowerCase()));

            } else {
                Collections.sort(list, (p1, p2) -> comparator.compare(PinyinUtils.converterToFirstSpell(p2.getMeterName().substring(0, 1)).toLowerCase(),
                        PinyinUtils.converterToFirstSpell(p1.getMeterName().substring(0, 1)).toLowerCase()));
            }
        } else if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_SITE_NAME.equals(sort)) {
            if (isOrder) {
                Collections.sort(list, (p1, p2) -> comparator.compare(PinyinUtils.converterToFirstSpell(p1.getSiteName().substring(0, 1)).toLowerCase(),
                        PinyinUtils.converterToFirstSpell(p2.getSiteName().substring(0, 1)).toLowerCase()));

            } else {
                Collections.sort(list, (p1, p2) -> comparator.compare(PinyinUtils.converterToFirstSpell(p2.getSiteName().substring(0, 1)).toLowerCase(),
                        PinyinUtils.converterToFirstSpell(p1.getSiteName().substring(0, 1)).toLowerCase()));
            }
        }

    }
}





