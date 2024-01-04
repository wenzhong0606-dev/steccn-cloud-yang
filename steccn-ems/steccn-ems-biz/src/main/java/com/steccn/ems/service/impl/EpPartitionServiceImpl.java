package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.dto.*;
import com.steccn.ems.mapper.EpPartitionMapper;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.vo.EpPartitionNameListVO;
import com.steccn.ems.vo.EpPartitionParentVO;
import com.steccn.ems.vo.EpPartitionVO;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author yequan
* @description 针对表【ep_partition(能耗分区)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@Service
@Slf4j
public class EpPartitionServiceImpl extends ServiceImpl<EpPartitionMapper, EpPartition>
    implements EpPartitionService{

    @Autowired
    private EpInstrumentAccountService epInstrumentAccountService;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;

    /**
     * 默认能源类型
     */
    private final Set<Integer> DEFAULT_ENERGY_SET = Sets.newHashSet(1,2,3,4,5);


    @Override
    public EpPartitionNameListVO getEpEnergyTypeVOList(Integer energyType) {
        EpPartitionNameListVO epPartitionNameListVO = new EpPartitionNameListVO();
        if (DEFAULT_ENERGY_SET.contains(energyType)) {
            epPartitionNameListVO.setName(EpManagerConstant.EnergyPartitioningTree.getByEnergyType(energyType).getName());
        } else {
            epPartitionNameListVO.setName(EpManagerConstant.EnergyPartitioningTree.ENERGY_PARTITION.getName());
        }

        //查询所有的仪表
        Map<Long, EpInstrumentAccountDTO> nodeLinkMeterMap = epInstrumentAccountService.queryNodeLinkMeter(energyType);
        EpInstrumentAccountDTO defaultEpInstrumentAccountDTO = new EpInstrumentAccountDTO();
        //根据传过来的能源类型先获取数据库的全量数据
        List<EpPartition> dataList = list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, energyType)
                .eq(EpPartition::getDelFlag,MpConstant.NO));

        List<EpPartitionVO> rootVOList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dataList)) {
            //复制结果到新的集合
            List<EpPartitionVO> epPartitionVOS = BeanUtil.copyToList(dataList, EpPartitionVO.class);
            epPartitionVOS.forEach(ele -> ele.setEntityTable(nodeLinkMeterMap.getOrDefault(ele.getId(), defaultEpInstrumentAccountDTO).getEntityTable()));

            //根据能源类型去仪表里面查询所有的可展示的仪表判断是否绑定所属分区节点
            List<Long> partitionNodes = epInstrumentAccountService.getPartitionNodes(energyType);
            rootVOList = searchRoot(epPartitionVOS);
            rootVOList = rootVOList.stream().sorted(Comparator.comparing(EpPartitionVO::getSort)).collect(Collectors.toList());
            for (EpPartitionVO root : rootVOList) {
                List<EpPartitionVO> children = getChildren(epPartitionVOS, root,partitionNodes);
                if (CollUtil.isNotEmpty(partitionNodes)&&partitionNodes.contains(root.getId())){
                    root.setIsLinkTable(MpConstant.YES);
                }else {
                    root.setIsLinkTable(MpConstant.NO);
                }
                root.setChildren(children);
            }
            //去platform中组织机构树取officeName
            Set<Integer> officeIdSet = Sets.newHashSet();
            rootVOList.forEach(ele ->{
                officeIdSet.add(ele.getOfficeId());
                officeIdSet.add(ele.getParentOfficeId());
            });
            Map<Integer, String> officeIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            rootVOList.forEach(ele -> {
                ele.setOfficeName(officeIdNameMap.get(ele.getOfficeId()));
                ele.setParentOfficeName(officeIdNameMap.get(ele.getParentOfficeId()));
            });
            epPartitionNameListVO.setEpPartitionVOList(rootVOList);
        }
        return epPartitionNameListVO;
    }

    private List<EpPartitionVO> getChildren(List<EpPartitionVO> childrenList, EpPartitionVO root,List<Long> partitionNodes) {
        List<EpPartitionVO> result=Lists.newArrayList();
        childrenList = childrenList.stream().sorted(Comparator.comparing(EpPartitionVO::getSort)).collect(Collectors.toList());
        for (EpPartitionVO epPartitionVO : childrenList) {
            if (Objects.nonNull(epPartitionVO.getParentId())&& epPartitionVO.getParentId().equals(root.getId())){
                epPartitionVO.setParentId(root.getId());
                epPartitionVO.setParentOfficeId(root.getOfficeId());
                epPartitionVO.setParentOfficeName(root.getOfficeName());
                epPartitionVO.setEntityTable(epPartitionVO.getEntityTable());
                if (CollUtil.isNotEmpty(partitionNodes)&&partitionNodes.contains(epPartitionVO.getId())){
                    epPartitionVO.setIsLinkTable(MpConstant.YES);
                }else {
                    epPartitionVO.setIsLinkTable(MpConstant.NO);
                }
                //递归方法找到子节点
                List<EpPartitionVO> children = getChildren(childrenList, epPartitionVO,partitionNodes);
                epPartitionVO.setChildren(children);
                result.add(epPartitionVO);
            }
        }
        return result;
    }

    /**
     * 根据一堆元素 ，自己去查询根节点
     * @param dbList 元素集合
     * @return
     */
    private List<EpPartitionVO> searchRoot(List<EpPartitionVO> dbList) {
        ArrayList<EpPartitionVO> rootList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dbList)){
            for (EpPartitionVO epPartitionVO : dbList) {
                long count = dbList.stream().filter(v -> v.getId().equals(epPartitionVO.getParentId())).count();
                if (count==0){
                    rootList.add(epPartitionVO);
                }
            }
        }
        return rootList;
    }

    @Override
    public  Map<Long,EpPartitionVO> getEpPartitionNameListVO(Set<Long> ids) {
       Map<Long,EpPartitionVO> epPartitionVOMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(ids)){
            List<EpPartition> epPartitions = list(new LambdaQueryWrapper<EpPartition>().in(EpPartition::getId, ids));
         List<EpPartitionVO> epPartitionVOS = BeanUtil.copyToList(epPartitions, EpPartitionVO.class);
            if (CollUtil.isNotEmpty(epPartitionVOS)){
                epPartitionVOS.forEach(s->{
                    epPartitionVOMap.put(s.getId(),s);
                });
            }
        }
        return epPartitionVOMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(EpPartitionDTO dto) {
        //构造成树形结构
        EpPartitionTreeDTO root = convertTree(dto.getEpPartitionListDTO());
        //定义需要插入的集合
        List<EpPartition> insertList = Lists.newArrayList();
        //定义需要更新的集合
        List<EpPartition> editList = Lists.newArrayList();
        //定义需要删除的集合
        List<EpPartition> deleteList = Lists.newArrayList();

        //officeId -> id
        Map<Integer, Long> officeIdMap = existOfficeIdMap(dto.getType());
        //获取已经存在的officeId
        Set<Integer> existOfficeSet = officeIdMap.keySet();
        //获取本次编辑后，所有的officeId
        Set<Integer> newOfficeIdSet = Sets.newHashSet(root.getId());
        //自上向下遍历数据
        List<EpPartitionTreeDTO> dataList = Arrays.asList(root);
        do {
            List<EpPartitionTreeDTO> resList = Lists.newArrayList();
            for (EpPartitionTreeDTO child : dataList) {
                resList.addAll(child.getChildren());
                //最后存在的officeId集合，不在该集合的元素都需要被删除掉
                newOfficeIdSet.add(child.getId());
                EpPartition model = copyTreeDTO(dto.getType(), child);
                //节点已经存在了，加入到更新集合
                if (existOfficeSet.contains(child.getId())) {
                    model.setId(officeIdMap.get(child.getId()));
                    editList.add(model);
                } else {
                    model.setId(IdUtil.getSnowflake().nextId());
                    //构造父子关系
                    Long parentId = officeIdMap.get(child.getParentId());
                    model.setParentId(parentId);
                    officeIdMap.put(model.getOfficeId(), model.getId());
                    insertList.add(model);
                }
            }
            dataList = resList;
        } while (!dataList.isEmpty());
        //插入元素
        if (!insertList.isEmpty()) {
            saveBatch(insertList);
        }
        //编辑元素
        if (!editList.isEmpty()) {
            updateBatchById(editList);
        }
        //existOfficeSet-编辑之前的所有元素id集合
        //newOfficeIdSet-编辑之后的所有元素id集合
        delOldNode(dto.getType(), officeIdMap, existOfficeSet, newOfficeIdSet);
        return Boolean.TRUE;
    }

    /**
     * 删除旧的节点
     * @param type 能源类型
     * @param officeIdMap officeId -> id map
     * @param existOfficeSet 编辑之前的所有元素id集合
     * @param newOfficeIdSet 编辑之后的所有元素id集合
     */
    private void delOldNode(Integer type, Map<Integer, Long> officeIdMap, Set<Integer> existOfficeSet, Set<Integer> newOfficeIdSet) {
        existOfficeSet.removeAll(newOfficeIdSet);
        //如果existOfficeSet还存在元素，说明需要删除元素
        if (CollUtil.isNotEmpty(existOfficeSet)) {
            Set<Long> partitionIdSet = Sets.newHashSet();
            for (Integer officeId : existOfficeSet) {
                partitionIdSet.add(officeIdMap.get(officeId));
            }
            List<EpInstrumentAccount> epInstrumentAccounts = epInstrumentAccountService.list(new LambdaQueryWrapper<EpInstrumentAccount>()
                    .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES)
                    .eq(EpInstrumentAccount::getMeterType, type)
                    .in(EpInstrumentAccount::getPartitionNode, partitionIdSet));
            if (CollUtil.isNotEmpty(epInstrumentAccounts)) {
                StringBuilder sb = new StringBuilder();
                Set<Long> linkInstrumentSet = epInstrumentAccounts.stream().map(v -> v.getPartitionNode()).collect(Collectors.toSet());
                List<EpPartition> epPartitions = listByIds(linkInstrumentSet);
                if (CollUtil.isNotEmpty(epPartitions)) {
                    for (EpPartition epPartition : epPartitions) {
                        sb.append(epPartition.getOfficeName()).append(",");
                    }
                }
                throw new BusinessException("已有仪表绑定节点:" + sb + "请重新编辑");
            }
            removeByIds(partitionIdSet);
        }
    }

    /**
     * 获取能源类型已经存在的officeId -> Id 的map
     *
     * @param type 能源类型
     * @return
     */
    private Map<Integer, Long> existOfficeIdMap(Integer type) {
        Map<Integer, Long> resMap = Maps.newHashMap();
        List<EpPartition> dbList = list(new LambdaQueryWrapper<EpPartition>()
                .eq(EpPartition::getType, type)
                .eq(EpPartition::getDelFlag, MpConstant.NO));
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                resMap.put(ele.getOfficeId(), ele.getId());
            });
        }
        return resMap;
    }

    /**
     * 拷贝树的vo
     *
     *
     * @param type
     * @param treeDTO 树vo
     * @return
     */
    private EpPartition copyTreeDTO(Integer type, EpPartitionTreeDTO treeDTO) {
        EpPartition model = new EpPartition();
        model.setOfficeName(treeDTO.getOfficeName());
        model.setOfficeId(treeDTO.getId());
        model.setSort(treeDTO.getSort());
        model.setType(type);
        return model;
    }

    /**
     * 将平铺结构转换成一棵树
     *
     * @return root节点的一棵树
     */
    private EpPartitionTreeDTO convertTree(List<EpPartitionListDTO> sourceDataList) {
        //校验是否officeId重复
        Set<Integer> officeIdSet = Sets.newHashSet();
        //定义父子关系的map
        Map<Integer, Integer> idParentMap = Maps.newHashMap();
        for (EpPartitionListDTO ele : sourceDataList) {
            officeIdSet.add(ele.getId());
            idParentMap.put(ele.getId(), ele.getParentId());
        }
        Assert.isTrue(officeIdSet.size() == sourceDataList.size(), "存在组织机构重复的情况");

        //从下往上校验，是否成环，如果成环，则构不成tree
        Assert.isTrue(!isRing(sourceDataList, idParentMap), "树成环了");

        List<EpPartitionTreeDTO> dataList = BeanUtil.copyToList(sourceDataList, EpPartitionTreeDTO.class);

        List<EpPartitionTreeDTO> rootList = dataList.stream().filter(v -> Objects.isNull(v.getParentId())).collect(Collectors.toList());
        Assert.notEmpty(rootList, "根节点为空");
        Assert.isTrue(rootList.size() == 1, "存在多个根节点");
        EpPartitionTreeDTO root = rootList.get(0);

        List<EpPartitionTreeDTO> childList = dataList.stream().filter(v -> root.getId().equals(v.getParentId())).collect(Collectors.toList());
        Integer sumNode = 1;
        if (CollUtil.isNotEmpty(childList)) {
            root.setChildren(childList);
            //构造孩子的树形节点结构
            sumNode += recycle(dataList, childList);
        }
        Assert.isTrue(sumNode == sourceDataList.size(), "存在节点没有父节点");
        return root;
    }

    /**
     * 判断树是否成环了
     *
     * @param sourceDataList 原始平铺数据
     * @param parentIdMap    父节点map
     * @return 是否成环
     */
    private boolean isRing(List<EpPartitionListDTO> sourceDataList, Map<Integer, Integer> parentIdMap) {
        //遍历一颗树，把已经遍历过得节点存放到set中，如果重复了，则成环
        for (EpPartitionListDTO out : sourceDataList) {
            //定义该节点的父路径集合
            Set<Integer> pathSet = Sets.newHashSet();
            //获取节点的父亲
            Integer parentId = out.getParentId();
            //如果父节点不为空
            while (Objects.nonNull(parentId)) {
                //加入父节点
                if (!pathSet.add(parentId)) {
                    return Boolean.TRUE;
                }
                parentId = parentIdMap.get(parentId);
            }
        }
        return Boolean.FALSE;
    }

    /**
     * 递归构造树形节点结构
     * @param dataList 数据集合
     * @param children 孩子节点
     */
    private Integer recycle(List<EpPartitionTreeDTO> dataList, List<EpPartitionTreeDTO> children) {
        Integer sum = 0;
        if (CollUtil.isNotEmpty(children)) {
            for (EpPartitionTreeDTO child : children) {
                //获取孩子的孩子节点 -》 孙子节点
                List<EpPartitionTreeDTO> sunList = dataList.stream().filter(v -> child.getId().equals(v.getParentId())).collect(Collectors.toList());
                Integer childSum = recycle(dataList, sunList);
                child.setChildren(sunList);
                sum += (childSum + 1);
            }
        }
        return sum;
    }

    /**
     * 过滤子树信息
     * @param dto
     * @return
     */
    @Override
    public EpEnergyTreeDTO getChildTree(EpEnergyParamDTO dto) {
        //1、根据能源类型返回树状数据
        EpPartitionNameListVO epEnergyTypeVOList = this.getEpEnergyTypeVOList(dto.getType());
        List<EpPartitionVO> treeList = epEnergyTypeVOList.getEpPartitionVOList();
        EpEnergyTreeDTO result = new EpEnergyTreeDTO();
        //根据子树信息返回详情
        Set<Long> partitionIdResult = Sets.newHashSet();
        //准备数据
        List<EpPartitionVO> childTree = Lists.newArrayList();
        if(CollUtil.isNotEmpty(treeList)){
            //查询每个节点的孩子节点
            dto.getPartitionId().forEach(partitionTmp -> {
                //过滤子树信息
                EpPartitionVO resTmp = new EpPartitionVO();
                EpPartitionVO child = getChild(treeList.get(0), partitionTmp,resTmp);
                childTree.add(child);
            });

            //获取每一个节点上挂的仪表
            Map<Long, EpInstrumentAccount> meterMap = Maps.newHashMap();
            List<EpInstrumentAccount> meterList = epInstrumentAccountService.list(new LambdaQueryWrapper<EpInstrumentAccount>().eq(EpInstrumentAccount::getMeterType, dto.getType()));
            if (CollUtil.isNotEmpty(meterList)) {
                meterList.forEach(ele -> meterMap.put(ele.getPartitionNode(), ele));
            }
            for (EpPartitionVO tmp : childTree) {
                tmp = setTreeDetail(tmp, meterMap);
                Set<Long> partitionTmp = Sets.newHashSet();
                partitionTmp = getRealMeter(tmp, partitionTmp);
                partitionIdResult.addAll(partitionTmp);
            }
        }
        result.setTree(childTree);
        result.setPartitionIds(partitionIdResult);
        return result;
    }

    @Override
    public Map<Long, String> listByIdCh(List<Long> partitionIdList) {
        Map<Long, String> resMap = Maps.newLinkedHashMap();
        if (CollUtil.isNotEmpty(partitionIdList)) {
            List<EpPartition> epPartitions = listByIds(partitionIdList);
            epPartitions.sort(Comparator.comparing(EpPartition::getSort));
            if (CollUtil.isNotEmpty(epPartitions)) {
                epPartitions.forEach(ele -> {
                    resMap.put(ele.getId(), ele.getOfficeName());
                });
            }
        }
        return resMap;
    }

    @Override
    public Map<Long, EpPartition> listById(Set<Long> partitionIdList) {
        Map<Long, EpPartition> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(partitionIdList)) {
            List<EpPartition> epPartitions = listByIds(partitionIdList);
            if (CollUtil.isNotEmpty(epPartitions)) {
                epPartitions.forEach(ele -> {
                    resMap.put(ele.getId(), ele);
                });
            }
        }
        return resMap;
    }

    @Override
    public Map<Integer, EpInstrumentAccount> listByOfficeId(Integer energyType, Set<Integer> officeIdSet) {
        Map<Integer, EpInstrumentAccount> resMap = Maps.newHashMap();

        if (Objects.nonNull(energyType) && CollUtil.isNotEmpty(officeIdSet)) {
            List<EpPartition> list = list(new LambdaQueryWrapper<EpPartition>()
                    .eq(EpPartition::getType, energyType)
                    .in(EpPartition::getOfficeId, officeIdSet));
            if (CollUtil.isNotEmpty(list)) {
                Set<Long> partitionIdSet = list.stream().map(v -> v.getId()).collect(Collectors.toSet());

                Map<Long, Integer> officePartitionNodeMap = Maps.newHashMap();
                list.forEach(ele -> officePartitionNodeMap.put(ele.getId(), ele.getOfficeId()));

                List<EpInstrumentAccount> accountList = epInstrumentAccountService.list(new LambdaQueryWrapper<EpInstrumentAccount>()
                        .eq(EpInstrumentAccount::getMeterType, energyType)
                        .in(EpInstrumentAccount::getPartitionNode, partitionIdSet));
                if (CollUtil.isNotEmpty(accountList)) {
                    accountList.forEach(ele -> {
                        Integer officeId = officePartitionNodeMap.get(ele.getPartitionNode());
                        if (Objects.nonNull(officeId)) {
                            resMap.put(officeId, ele);
                        }
                    });
                }
            }
        }
        return resMap;
    }

    @Override
    public Map<Long, List<EpPartitionVO>> allNextChildren(Integer type) {
        Map<Long, List<EpPartitionVO>> linkChildren = Maps.newHashMap();
        //先找出该能源类型下的所有分区节点
        List<EpPartition> epPartitions = list(new LambdaQueryWrapper<EpPartition>()
                .eq(EpPartition::getType, type));
        if (CollUtil.isNotEmpty(epPartitions)) {
            //查询所有的节点
            Set<Long> partitionNodeIdSet = epPartitions.stream().map(EpPartition::getId).collect(Collectors.toSet());
            Map<Long,EpInstrumentAccount> instrumentAccountMap =  epInstrumentAccountService.listById(partitionNodeIdSet);
            List<EpPartitionVO> epPartitionVOS = BeanUtil.copyToList(epPartitions, EpPartitionVO.class);
            epPartitionVOS.forEach(ele ->{
                EpInstrumentAccount account = instrumentAccountMap.get(ele.getId());
                if(Objects.nonNull(account)){
                    ele.setMeterId(account.getId());
                    ele.setMeterStatus(account.getStatus());
                    ele.setEntityTable(account.getEntityTable());
                    ele.setDistantMapCode(account.getDistantMapCode());
                }
            });
            epPartitionVOS.forEach(s -> {
                //查询当前节点的孩子节点
                List<EpPartitionVO> children = epPartitionVOS.stream().filter(v -> s.getId().equals(v.getParentId())).collect(Collectors.toList());
                //获取该类型下每个节点及其下一级子节点的集合
                linkChildren.put(s.getId(), children);
            });
        }
        return linkChildren;
    }

    @Override
    public Map<Integer, EpPartition> rootMap(List<Integer> energyType) {
        Map<Integer, EpPartition> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(energyType)) {
            //获取能源的所有根节点
            List<EpPartition> rootList = list(new LambdaQueryWrapper<EpPartition>()
                    .in(EpPartition::getType, energyType)
                    .isNull(EpPartition::getParentId));
            if (CollUtil.isNotEmpty(rootList)) {
                rootList.forEach(ele -> resMap.put(ele.getType(), ele));
            }
        }
        return resMap;
    }

    @Override
    public Set<Long> otherPartition(Set<Long> allPartitionIdSet) {
        Set<Long> resSet = Sets.newHashSet();
        if (CollUtil.isNotEmpty(allPartitionIdSet)) {
            //获取所有的节点
            List<EpPartition> epPartitions = listByIds(allPartitionIdSet);
            if (CollUtil.isNotEmpty(epPartitions)) {
                Set<Integer> officeIdSet = epPartitions.stream().map(EpPartition::getOfficeId).collect(Collectors.toSet());
                //查询所有节点的产线父节点
                Map<Integer, Integer> parentLineMap = FeignOptional.of(() -> remoteOfficeFeign.parentLine(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                //如果是产线父节点，则留下，不然视为其余节点
                Set<Integer> newOfficeIdSet = officeIdSet.stream().filter(v -> Objects.isNull(parentLineMap.get(v))).collect(Collectors.toSet());
                if(CollUtil.isNotEmpty(newOfficeIdSet)){
                    //获取剩下的节点数据
                    List<EpPartition> other = list(new LambdaQueryWrapper<EpPartition>().in(EpPartition::getId, allPartitionIdSet)
                            .in(EpPartition::getOfficeId, newOfficeIdSet).select(EpPartition::getId));
                    if(CollUtil.isNotEmpty(other)){
                        Set<Long> otherPartitionIdSet = other.stream().map(EpPartition::getId).collect(Collectors.toSet());
                        resSet.addAll(otherPartitionIdSet);
                    }
                }
            }
        }
        return resSet;
    }

    @Override
    public Map<Long, String> allPartitionNameMap(Integer energyType, Set<Long> partitionIdSet) {
        Map<Long, String> resMap = Maps.newHashMap();
        Map<Long, EpPartition> epPartitionMap = Maps.newHashMap();
        List<EpPartition> epPartitionList = list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, energyType));
        if (CollUtil.isNotEmpty(epPartitionList)) {
            epPartitionList.forEach(ele -> {
                epPartitionMap.put(ele.getId(), ele);
            });
        }
        partitionIdSet.forEach(ele -> {
            EpPartition cur = epPartitionMap.get(ele);
            if(Objects.nonNull(cur)){
                cur.setCreateName(Strings.EMPTY);
                EpPartition parent = parent(epPartitionMap, cur);
                //获取到当前节点
                resMap.put(ele, parent.getCreateName());
            }
        });
        return resMap;
    }

    @Override
    public Map<Long, EpChildTypeDTO> childTypeMap(Integer energyType) {
        //保证有序
        Map<Long, EpChildTypeDTO> resMap = Maps.newLinkedHashMap();
        //获取能源的分区树
        EpPartitionNameListVO partitionTree = this.getEpEnergyTypeVOList(energyType);
        if (CollUtil.isNotEmpty(partitionTree.getEpPartitionVOList())) {
            //根节点
            EpPartitionVO root = partitionTree.getEpPartitionVOList().get(0);
            //获取所有的孩子节点
            List<EpPartitionVO> allNodeList = getChildren(root);
            allNodeList.add(root);

            //获取孩子节点的类型
            for (EpPartitionVO node : allNodeList) {
                Integer type = childType(node);
                EpChildTypeDTO epChildTypeDTO = new EpChildTypeDTO();
                epChildTypeDTO.setType(type);
                List<EpPartitionVO> childrenList = node.getChildren();
                if(CollUtil.isNotEmpty(childrenList)){
                    Set<Long> kidIdSet = childrenList.stream().map(EpPartitionVO::getId).collect(Collectors.toSet());
                    epChildTypeDTO.setChildren(kidIdSet);
                }
                resMap.put(node.getId(), epChildTypeDTO);
            }
        }
        return resMap;
    }

    @Override
    public Map<Integer, List<TripleDTO<Long, Integer, String>>> dayReportCountDefaultNode() {
        //电
        Map<String, List<SysConfigurationDTO>> eleConfMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_ELEC, EpManagerConstant.ELECConfContentEnum.allContentConf())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysConfigurationDTO> eleConfList = eleConfMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        //水
        Map<String, List<SysConfigurationDTO>> waterConfMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_WATER, Sets.newHashSet(EpManagerConstant.CONF_EMS_WATER_CONTENT))), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysConfigurationDTO> waterConfList = waterConfMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        //气
        Map<String, List<SysConfigurationDTO>> gasConfMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_GAS, Sets.newHashSet(EpManagerConstant.CONF_EMS_GAS_CONTENT))), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysConfigurationDTO> gasConfList = gasConfMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        //油
        Map<String, List<SysConfigurationDTO>> oilConfMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_OIL, Sets.newHashSet(EpManagerConstant.CONF_EMS_OIL_CONTENT))), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysConfigurationDTO> oilConfList = oilConfMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        //站外电
        Map<String, List<SysConfigurationDTO>> eleOtherConfMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_ELEC_OTHER, EpManagerConstant.ELECOtherConfContentEnum.allContentConf())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysConfigurationDTO> eleOtherConfList = Lists.newArrayList();
        for (EpManagerConstant.ELECOtherConfContentEnum elecOtherConfContentEnum : EpManagerConstant.ELECOtherConfContentEnum.values()) {
            List<SysConfigurationDTO> tmpList = eleOtherConfMap.get(elecOtherConfContentEnum.toString());
            if (CollUtil.isNotEmpty(tmpList)) {
                eleOtherConfList.addAll(tmpList);
            }
        }
        Map<Integer, List<TripleDTO<Long, Integer, String>>> resMap = Maps.newHashMap();
        resMap.put(EpManagerConstant.ENERGY_ELECTRIC, queryNodeByNameList(EpManagerConstant.ENERGY_ELECTRIC, eleConfList));
        resMap.put(EpManagerConstant.ENERGY_WATER, queryNodeByNameList(EpManagerConstant.ENERGY_WATER, waterConfList));
        resMap.put(EpManagerConstant.ENERGY_GAS, queryNodeByNameList(EpManagerConstant.ENERGY_GAS, gasConfList));
        resMap.put(EpManagerConstant.ENERGY_OIL, queryNodeByNameList(EpManagerConstant.ENERGY_OIL, oilConfList));
        resMap.put(EpManagerConstant.ENERGY_ELECTRIC_OTHER, queryNodeByNameList(EpManagerConstant.ENERGY_ELECTRIC_OTHER, eleOtherConfList));
        return resMap;
    }

    @Override
    public Map<Integer, List<TripleDTO<Long, Integer, String>>> jpCountReportHeatPowerDefaultNode() {
        //站外电
        Map<String, List<SysConfigurationDTO>> eleOtherConfMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_ELEC_OTHER, EpManagerConstant.ELECOtherConfContentEnum.allContentConf())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysConfigurationDTO> eleOtherConfList = Lists.newArrayList();
        for (EpManagerConstant.ELECOtherConfContentEnum elecOtherConfContentEnum : EpManagerConstant.ELECOtherConfContentEnum.values()) {
            List<SysConfigurationDTO> tmpList = eleOtherConfMap.get(elecOtherConfContentEnum.toString());
            if (CollUtil.isNotEmpty(tmpList)) {
                eleOtherConfList.addAll(tmpList);
            }
        }
        //水
        Map<String, List<SysConfigurationDTO>> waterConfMap = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>(EpManagerConstant.CONF_EMS_DAY_COUNT_WATER, Sets.newHashSet(EpManagerConstant.CONF_EMS_WATER_CONTENT))), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysConfigurationDTO> waterConfList = waterConfMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        Map<Integer, List<TripleDTO<Long, Integer, String>>> resMap = Maps.newHashMap();
        resMap.put(EpManagerConstant.ENERGY_ELECTRIC_OTHER, queryNodeByNameList(EpManagerConstant.ENERGY_ELECTRIC_OTHER, eleOtherConfList));
        resMap.put(EpManagerConstant.ENERGY_WATER, queryNodeByNameList(EpManagerConstant.ENERGY_WATER, waterConfList));
        return resMap;
    }

    /**
     * 根据officeName获取指定能源类型的节点id和节点名称
     *
     * @param energyType           能源类型
     * @param configurationDTOList 组织结构名称集合
     * @return
     */
    private List<TripleDTO<Long, Integer, String>> queryNodeByNameList(Integer energyType, List<SysConfigurationDTO> configurationDTOList) {
        List<TripleDTO<Long, Integer, String>> resList = Lists.newArrayList();
        if(CollUtil.isNotEmpty(configurationDTOList)){
            Map<Integer,String> idNameMap = Maps.newLinkedHashMap();
            for (SysConfigurationDTO configurationDTO : configurationDTOList) {
                try {
                    int id = Integer.parseInt(configurationDTO.getRefContent());
                    idNameMap.put(id, configurationDTO.getOtherContent());
                }catch (Exception e){
                    log.error("{}",e.getLocalizedMessage());
                }
            }
            List<EpPartition> dbList = list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, energyType).in(EpPartition::getOfficeId, idNameMap.keySet()));
            if (CollUtil.isNotEmpty(dbList)) {
                Map<Integer,EpPartition> epMap = Maps.newHashMap();
                dbList.forEach(ele -> epMap.put(ele.getOfficeId(), ele));
                idNameMap.forEach((k,v) ->{
                    EpPartition epPartition = epMap.get(k);
                    if(Objects.nonNull(epPartition)){
                        String officeName = v;
                        resList.add(new TripleDTO<>(epPartition.getId(), epPartition.getOfficeId(), StrUtil.isNotBlank(officeName) ? officeName : epPartition.getOfficeName()));
                    }
                });
            }
        }
        return resList;
    }


    /**
     * 获取当前节点的所有孩子节点
     * @param cur
     * @return
     */
    List<EpPartitionVO> getChildren(EpPartitionVO cur){
        List<EpPartitionVO> nodeList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(cur.getChildren())){
            for (EpPartitionVO child : cur.getChildren()) {
                //先添加所有孩子，再添加自己，如此可以保证 底层节点在下面
                nodeList.addAll(getChildren(child));
                nodeList.add(child);
            }
        }
        return nodeList;
    }

    /**
     * 当前节点 孩子节点的类型
     * @param cur 当前节点
     * @return  0 ， 1 ， 2 ,null
     */
    private Integer childType(EpPartitionVO cur) {
        if (Objects.nonNull(cur.getEntityTable()) && cur.getEntityTable() == 0) {
            return 0;
        } else if (Objects.nonNull(cur.getEntityTable()) && cur.getEntityTable() == 1) {
            return 1;
        }
        List<EpPartitionVO> children = cur.getChildren();
        List<Integer> typeList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(children)) {
            for (EpPartitionVO child : children) {
                typeList.add(childType(child));
            }
        }
        //①typeList为空，或者孩子都为空 ， 返回null
        //②孩子空 + 0 返回0
        //③孩子空 + 1 返回1
        //④孩子空 +0 + 1 返回2
        //⑤孩子有2 返回2
        typeList = typeList.stream().filter(v -> Objects.nonNull(v)).collect(Collectors.toList());
        if(typeList.contains(2)){
            return 2;
        } else if (typeList.contains(1) && typeList.contains(0)) {
            return 2;
        } else if (typeList.contains(1)) {
            return 1;
        } else if (typeList.contains(0)) {
            return 0;
        }
        return null;
    }

    /**
     * 获取节点的全路径
     * @param epPartitionMap
     * @param cur
     * @return
     */
    private EpPartition parent(Map<Long, EpPartition> epPartitionMap, EpPartition cur) {
        if (Strings.isBlank(cur.getCreateName())) {
            cur.setCreateName(cur.getOfficeName());
        } else {
            cur.setCreateName(cur.getOfficeName() + "/" + cur.getCreateName());
        }
        if (Objects.nonNull(cur) && Objects.nonNull(cur.getParentId())) {
            EpPartition parent = epPartitionMap.get(cur.getParentId());
            parent.setCreateName(cur.getCreateName());
            return parent(epPartitionMap, parent);
        }
        return cur;
    }

    @Override
    public List<EpPartitionVO> getAllChildrenById(Long partitionId) {
        List<EpPartitionVO> resList = null;
        List<EpPartition> epPartitions = list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getParentId, partitionId));
        if (Objects.nonNull(epPartitions)) {
            resList = BeanUtil.copyToList(epPartitions, EpPartitionVO.class);
        }
        return Objects.isNull(resList) ? Lists.newArrayList() : resList;
    }

    @Override
    public Map<Long, List<EpPartitionVO>> getAllChildren(Set<Long> partitionIdSet) {
        Map<Long, List<EpPartitionVO>> resMap = null;
        if (CollUtil.isNotEmpty(partitionIdSet)) {
            List<EpPartition> dbList = list(new LambdaQueryWrapper<EpPartition>().in(EpPartition::getParentId, partitionIdSet));
            if (Objects.nonNull(dbList)) {
                List<EpPartitionVO> voList = BeanUtil.copyToList(dbList, EpPartitionVO.class);
                resMap = voList.stream().collect(Collectors.groupingBy(EpPartitionVO::getParentId));
            }
        }
        return Objects.nonNull(resMap) ? resMap : Maps.newHashMap();
    }

    @Override
    public EpPartitionParentVO getParentList(EpPartitionChildTypeDTO dto) {
        //机构名称集合
        List<String> joinPartitions = new ArrayList<>();
        //先获取该能源类型下的全量数据
        List<EpPartition> epPartitions = list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, dto.getType()));
        //获取当前节点数据
        EpPartition childPartition = getById(dto.getChildId());
        if (Objects.isNull(childPartition)||CollUtil.isEmpty(epPartitions)){
            return null;
        }
        //当前子节点的返回数据
        EpPartitionParentVO childPartitionVO = new EpPartitionParentVO();
        BeanUtils.copyProperties(childPartition,childPartitionVO);
        //获取所有父节点
        EpPartitionParentVO parentPartitionVO = getParentListByChildId(childPartitionVO, epPartitions);
        //获取拼接名称
        joinPartitions.add(parentPartitionVO.getOfficeName());
        getJoinPartitionName( parentPartitionVO ,parentPartitionVO.getParentList(),joinPartitions);
        Collections.reverse(joinPartitions);
        String officeNames = "";
        for (String vo: joinPartitions) {
            officeNames=officeNames+vo+"/";
        }
        if(StringUtils.isNotBlank(officeNames)) officeNames = officeNames.substring(0,officeNames.length()-1);
        parentPartitionVO.setJoinOfficeNames(officeNames);
        return parentPartitionVO;
    }


    public void getJoinPartitionName(EpPartitionParentVO menu ,List<EpPartitionParentVO> menus,List<String> joinPartitions){
       if (CollUtil.isNotEmpty(menus)){
           for (EpPartitionParentVO mu : menus) {
               //遍历出父id等于参数的id，add进子节点集合
               if (menu.getParentId().equals(mu.getId())) {
                   //递归遍历下一级
                   joinPartitions.add(mu.getOfficeName());
                   if(Objects.nonNull(mu.getParentList()))   getJoinPartitionName(mu,mu.getParentList(),joinPartitions);
               }
           }
       }
    }
    private EpPartitionParentVO getParentListByChildId(EpPartitionParentVO childPartitionVO, List<EpPartition> epPartitions) {
        //当前能源类型下所有节点的id集合
        List<Long> partitionIds = epPartitions.stream().map(EpPartition::getId).collect(Collectors.toList());
        if (!partitionIds.contains(childPartitionVO.getId())){
            //该能源类型下没有任何树结构或者不包含子id返回当前本身数据
            return childPartitionVO;
        }
        if (Objects.isNull(childPartitionVO.getParentId())){
            //如果父id为空则自己就是最终父级
            return childPartitionVO;
        }else {
            //如果孩子id的排序不为0则表明其至少有一个父类
            //子挂父
            getAllParentByParentId(childPartitionVO,epPartitions);
        }
        return childPartitionVO;
    }

    private void getAllParentByParentId(EpPartitionParentVO childPartitionVO, List<EpPartition> epPartitions) {
        //过滤出父级
        List<EpPartition> parentList = epPartitions.stream().filter(s -> Objects.nonNull(childPartitionVO.getParentId())&&
                childPartitionVO.getParentId().equals(s.getId())).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(parentList)){
            List<EpPartitionParentVO> epPartitionParentVOS = BeanUtil.copyToList(parentList, EpPartitionParentVO.class);
            childPartitionVO.setParentList(epPartitionParentVOS);
            epPartitions.removeAll(parentList);
            for (EpPartitionParentVO partitionParentVO : epPartitionParentVOS) {
                getAllParentByParentId(partitionParentVO,epPartitions);
            }
        }

    }

    @Override
    public Map<Long, EpPartitionVO> parentInfo(Set<Long> childList) {
        Map<Long,EpPartitionVO> resMap = Maps.newHashMap();
        List<EpPartitionVO> epPartitionVOS = baseMapper.listParentInfo(childList);
        if (CollUtil.isNotEmpty(epPartitionVOS)){
            for (EpPartitionVO epPartitionVO : epPartitionVOS) {
                resMap.put(epPartitionVO.getChildrenId(),epPartitionVO);
            }
        }
        return resMap;
    }

    /**
     * 获取子树
     * @param vo
     * @param partitionId
     * @return
     */
    private EpPartitionVO getChild(EpPartitionVO vo,Long partitionId,EpPartitionVO res){
        if(CollUtil.isEmpty(vo.getChildren())){
            if(vo.getId().equals(partitionId)){
                res = vo;
            }
            return res;
        }
        for(int i = 0; i < vo.getChildren().size(); i++) {
            if (vo.getId().equals(partitionId)) {
                res = vo;
            }
            res = this.getChild(vo.getChildren().get(i), partitionId,res);
        }
        return res;
    }

    /**
     * 获取所有实体的节点id
     * @param vo
     * @param vo
     * @return
     */
    private Set<Long> getRealMeter(EpPartitionVO vo,Set<Long> ids){
        //没有子节点且或者是虚拟表则停止递归
        if(CollUtil.isEmpty(vo.getChildren())){
            Set<Long> results = Sets.newHashSet();
            if(vo.getEntityTable()!=null && !vo.getEntityTable().equals(EpManagerConstant.InstrumentType.VIRTUAL.getCode())){
                results.add(vo.getId());
            }
            ids.addAll(results);
            return ids;
        }
        for(int i = 0; i < vo.getChildren().size(); i++){
            Set<Long> results = Sets.newHashSet();
            if(vo.getEntityTable()!=null && !vo.getEntityTable().equals(EpManagerConstant.InstrumentType.VIRTUAL.getCode())){
                results.add(vo.getId());
            }
            ids.addAll(results);
            this.getRealMeter(vo.getChildren().get(i),ids);
        }
        return ids;
    }

    /**
     * 设置树节点数据
     * @param vo
     * @param vo
     * @return
     */
    private EpPartitionVO setTreeDetail(EpPartitionVO vo, Map<Long, EpInstrumentAccount> meterMap) {
        if (CollUtil.isEmpty(vo.getChildren())) {
            //获取表详情
            EpInstrumentAccount meter = meterMap.get(vo.getId());
            if (meter != null) {
                vo.setEntityTable(meter.getEntityTable());
                vo.setMeterStatus(meter.getStatus());
                vo.setMeterId(meter.getId());
            }
            return vo;
        }
        for (int i = 0; i < vo.getChildren().size(); i++) {
            //获取表详情
            EpInstrumentAccount meter = meterMap.get(vo.getId());
            if (meter != null) {
                vo.setEntityTable(meter.getEntityTable());
                vo.setMeterStatus(meter.getStatus());
                vo.setMeterId(meter.getId());
            }
            this.setTreeDetail(vo.getChildren().get(i), meterMap);
        }
        return vo;
    }
}




