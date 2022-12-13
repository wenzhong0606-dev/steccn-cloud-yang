package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.common.constant.MpConstant;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.dto.*;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.mapper.EpPartitionMapper;
import com.steccn.ems.vo.EpPartitionNameListVO;
import com.steccn.ems.vo.EpPartitionVO;
import com.steccn.platform.vo.SysOfficeVO;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
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
            //根据传过来的能源类型先获取全量数据
        List<EpPartition> dataList = list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, energyType));
        if (CollUtil.isNotEmpty(dataList)) {
            //复制结果到新的集合
            List<EpPartitionVO> epPartitionVOS = recursiveTree(dataList);
               epPartitionNameListVO.setEpPartitionVOList(epPartitionVOS);
            }
        return epPartitionNameListVO;
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
        //需要新增的list
        List<EpPartition> addPartitionList = Lists.newArrayList();
        //需要删除的list
        List<Long> deletePartitionList = Lists.newArrayList();
        //根据能源类型查询出该类型的所有分区树的集合
        List<EpPartition> epPartitions = list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, dto.getType()));
       //获取数据库中所有officeId
        List<Integer> officeIds = epPartitions.stream().map(EpPartition::getOfficeId).collect(Collectors.toList());
        Map<Integer,Long> partitionMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(epPartitions)){
            epPartitions.forEach(v->{
                //根据officeId分组
                partitionMap.put(v.getOfficeId(),v.getId());
            });
        }

        if (CollUtil.isNotEmpty(dto.getEpPartitionListDTO())){
            for (EpPartitionListDTO partitionDTO : dto.getEpPartitionListDTO()) {
                EpPartition epPartition = new EpPartition();
                //设置能源类型
                epPartition.setType(dto.getType());
                epPartition.setOfficeId(partitionDTO.getId());
                epPartition.setOfficeName(partitionDTO.getOfficeName());
                //如果数据库中没有包含该机构的id则新增
                if (!officeIds.contains(partitionDTO.getId())){
                    long mainId = IdUtil.getSnowflake().nextId();
                    epPartition.setId(mainId);
                    //设置排序字段
                    epPartition.setSort(partitionDTO.getTreeLevel());
                    partitionMap.put(partitionDTO.getId(),epPartition.getId());
                    addPartitionList.add(epPartition);
                }
            }
            //获取dto中office的所有id
            List<Integer> officeDtoIds = dto.getEpPartitionListDTO().stream().map(EpPartitionListDTO::getId).collect(Collectors.toList());
            //判断officeDtoIds是否包含数据库中所有officeid 如果不是则要删除数据库中多余的
            if (!officeDtoIds.containsAll(officeIds)){
                //去除刚刚新增的officeid,比对剩下的
                List<Integer> addOfficeIds = addPartitionList.stream().map(EpPartition::getOfficeId).collect(Collectors.toList());
                officeDtoIds.removeAll(addOfficeIds);
                officeIds.removeAll(officeDtoIds);
                officeIds.forEach(v->{
                    deletePartitionList.add(partitionMap.get(v));
                });
            }
        }
        if (CollUtil.isNotEmpty(addPartitionList)){
            addPartitionList.forEach(s->{
                //设置能源分区树里的父id
                if (Objects.nonNull(s)&&Objects.nonNull(s.getParentId())){
                    if (officeIds.contains(s.getParentId())){
                        s.setParentId(partitionMap.get(s.getParentId()));
                    }
                }
            });

            saveBatch(addPartitionList);
        }
        if (CollUtil.isNotEmpty(deletePartitionList)){
            removeByIds(deletePartitionList);
        }
    return Boolean.TRUE;
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
        //准备数据
        List<EpPartitionVO> childTree = Lists.newArrayList();
        EpEnergyTreeDTO result = new EpEnergyTreeDTO();
        dto.getPartitionId().forEach(partitionTmp -> {
            //过滤子树信息
            EpPartitionVO resTmp = new EpPartitionVO();
            EpPartitionVO child = getChild(treeList.get(0), partitionTmp,resTmp);
            childTree.add(child);
        });
        //根据子树信息返回详情
        Set<Long> partitionIdResult = Sets.newHashSet();
        for(EpPartitionVO tmp:childTree){
            tmp = setTreeDetail(tmp);
            Set<Long> partitionTmp = Sets.newHashSet();
            partitionTmp = getRealMeter(tmp,partitionTmp);
            partitionIdResult.addAll(partitionTmp);
        }
        result.setTree(childTree);
        result.setPartitionIds(partitionIdResult);
        return result;
    }

    @Override
    public Map<Long, String> listByIdCh(List<Long> partitionIdList) {
        Map<Long, String> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(partitionIdList)) {
            List<EpPartition> epPartitions = listByIds(partitionIdList);
            if (CollUtil.isNotEmpty(epPartitions)) {
                epPartitions.forEach(ele -> {
                    resMap.put(ele.getId(), ele.getOfficeName());
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
     * 根据全量数据递归构造一棵树
     * @param dataList
     * @return
     */
    private List<EpPartitionVO> recursiveTree(List<EpPartition> dataList){
        //定义返回数据集合
        List<EpPartitionVO> epPartitionVOS = Lists.newArrayList();
        //去仪表里面查询所有的可展示的仪表判断是否绑定所属分区节点
        List<Long> partitionNodes = epInstrumentAccountService.getPartitionNodes();
        //获取所有根节点
        List<EpPartition> rootList = dataList.stream().filter(v -> Objects.isNull(v.getParentId()) || v.getParentId() == 0).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(rootList)){
            rootList.sort(Comparator.comparing(EpPartition::getSort));
            //把根节点从全量数据集合中删除
            dataList.removeAll(rootList);
            //遍历根节点
            for (EpPartition root : rootList) {
                EpPartitionVO parent = new EpPartitionVO();
                BeanUtils.copyProperties(root,parent);
                if (CollUtil.isNotEmpty(partitionNodes)&&partitionNodes.contains(root.getId())){
                    parent.setIsLinkTable(MpConstant.YES);
                }else {
                    parent.setIsLinkTable(MpConstant.NO);
                }
                epPartitionVOS.add(parent);
                //查询vo的子级,并将子级放到vo
                searchSysDictCh(parent, dataList,partitionNodes);

            }
        }
        return epPartitionVOS;
    }

    /**
     * 递归查询
     * @param parentVO
     * @param dataList
     */
    private void searchSysDictCh(EpPartitionVO parentVO, List<EpPartition> dataList,List<Long> partitionNodes) {
        //获取当前节点的子级
        List<EpPartition> children = dataList.stream().filter(v -> Objects.nonNull(parentVO.getId())
                && parentVO.getId() == v.getParentId()).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(children)){
            children.sort(Comparator.comparing(EpPartition::getSort));
            List<EpPartitionVO> childrenVoList = BeanUtil.copyToList(children, EpPartitionVO.class);
            childrenVoList.forEach(v->{
                if (CollUtil.isNotEmpty(partitionNodes)&&partitionNodes.contains(v.getId())){
                    v.setIsLinkTable(MpConstant.YES);
                }else {
                    v.setIsLinkTable(MpConstant.NO);
                }
            });
            //父亲结点挂孩子
            parentVO.setChildren(childrenVoList);
            //从集合中删除孩子
            dataList.removeAll(children);
            //孩子结点继续去挂自己的孩子
            for (EpPartitionVO item : childrenVoList) {
                searchSysDictCh(item, dataList,partitionNodes);
            }
        }
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
    private EpPartitionVO setTreeDetail(EpPartitionVO vo){
        if(CollUtil.isEmpty(vo.getChildren())){
            //获取表详情
            EpInstrumentAccount meter = epInstrumentAccountService.getOne(new LambdaQueryWrapper<EpInstrumentAccount>().
                    eq(EpInstrumentAccount::getPartitionNode,vo.getId()),false);
            if(meter != null){
                vo.setEntityTable(meter.getEntityTable());
                vo.setMeterStatus(meter.getStatus());
                vo.setMeterId(meter.getId());
            }
            return vo;
        }
        for(int i = 0; i < vo.getChildren().size(); i++){
            //获取表详情
            EpInstrumentAccount meter = epInstrumentAccountService.getOne(new LambdaQueryWrapper<EpInstrumentAccount>().
                    eq(EpInstrumentAccount::getPartitionNode,vo.getId()),false);
            if(meter != null){
                vo.setEntityTable(meter.getEntityTable());
                vo.setMeterStatus(meter.getStatus());
                vo.setMeterId(meter.getId());
            }
            this.setTreeDetail(vo.getChildren().get(i));
        }
        return vo;
    }
}




