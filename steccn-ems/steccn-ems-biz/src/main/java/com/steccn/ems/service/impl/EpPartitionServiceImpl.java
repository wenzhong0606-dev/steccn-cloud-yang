package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.constant.MpConstant;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
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
            //根据传过来的能源类型先获取数据库的全量数据
        List<EpPartition> dataList = list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, energyType)
                .eq(EpPartition::getDelFlag,MpConstant.NO));

        List<EpPartitionVO> rootVOList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(dataList)) {
            //复制结果到新的集合
//            List<EpPartitionVO> epPartitionVOS = recursiveTree(dataList,energyType);
//               epPartitionNameListVO.setEpPartitionVOList(epPartitionVOS);
            //复制结果到新的集合
            List<EpPartitionVO> epPartitionVOS = BeanUtil.copyToList(dataList, EpPartitionVO.class);
            //根据能源类型去仪表里面查询所有的可展示的仪表判断是否绑定所属分区节点
            List<Long> partitionNodes = epInstrumentAccountService.getPartitionNodes(energyType);
            rootVOList= searchRoot(epPartitionVOS);
            for (EpPartitionVO root : rootVOList) {
                List<EpPartitionVO> children = getChildren(epPartitionVOS, root,partitionNodes);
                if (CollUtil.isNotEmpty(partitionNodes)&&partitionNodes.contains(root.getId())){
                    root.setIsLinkTable(MpConstant.YES);
                }else {
                    root.setIsLinkTable(MpConstant.NO);
                }
                root.setChildren(children);
            }
            epPartitionNameListVO.setEpPartitionVOList(rootVOList);
        }
        return epPartitionNameListVO;
    }

    private List<EpPartitionVO> getChildren(List<EpPartitionVO> childrenList, EpPartitionVO root,List<Long> partitionNodes) {
        List<EpPartitionVO> result=Lists.newArrayList();
        for (EpPartitionVO epPartitionVO : childrenList) {
            if (Objects.nonNull(epPartitionVO.getParentId())&& epPartitionVO.getParentId().equals(root.getId())){
                epPartitionVO.setParentId(root.getId());
                epPartitionVO.setParentOfficeId(root.getOfficeId());
                epPartitionVO.setParentOfficeName(root.getOfficeName());
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

        //用来处理新增的List
        List<EpPartitionVO> dealAddPartitionList = Lists.newArrayList();

        //需要删除的list
        List<Long> deletePartitionList = Lists.newArrayList();
        //根据能源类型查询出该类型的所有分区树的集合
        List<EpPartition> epPartitions = list(new LambdaQueryWrapper<EpPartition>()
                .eq(EpPartition::getType, dto.getType())
                .eq(EpPartition::getDelFlag,MpConstant.NO));
       //获取数据库中所有officeId
        List<Integer> officeIds = epPartitions.stream().map(EpPartition::getOfficeId).collect(Collectors.toList());
       //机构的id以及分区树idMap集合
        Map<Integer,Long> partitionMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(epPartitions)){
            epPartitions.forEach(v->{
                //根据officeId分组
                partitionMap.put(v.getOfficeId(),v.getId());
            });
        }

        if (CollUtil.isNotEmpty(dto.getEpPartitionListDTO())){
            //先过滤掉前端传过来不符合要求的数据
            List<EpPartitionListDTO> dtoList = dto.getEpPartitionListDTO().stream().filter(s -> Objects.nonNull(s.getId())).collect(Collectors.toList());
            for (EpPartitionListDTO partitionDTO : dtoList) {
                //如果数据库中没有包含该机构的id则新增
                if (!officeIds.contains(partitionDTO.getId())){
                    EpPartitionVO epPartitionVO = new EpPartitionVO();
                    //设置能源分区树主键id
                    long mainId = IdUtil.getSnowflake().nextId();
                    epPartitionVO.setId(mainId);
                    //设置能源类型
                    epPartitionVO.setType(dto.getType());
                    //设置机构id
                    epPartitionVO.setOfficeId(partitionDTO.getId());
                    //设置机构父id
                    epPartitionVO.setParentOfficeId(partitionDTO.getParentId());
                    //设置机构名称
                    epPartitionVO.setOfficeName(partitionDTO.getOfficeName());
                    //设置排序字段
                    epPartitionVO.setSort(partitionDTO.getSort());
                    partitionMap.put(partitionDTO.getId(),mainId);
                    dealAddPartitionList.add(epPartitionVO);
                }
            }
            //获取dto中office的所有id
            List<Integer> officeDtoIds = dtoList.stream().map(EpPartitionListDTO::getId).collect(Collectors.toList());
            //判断officeDtoIds是否包含数据库中所有officeid 如果不是则要删除数据库中多余的
            if (!officeDtoIds.containsAll(officeIds)){
                //去除刚刚新增的officeid,比对剩下的
                List<Integer> addOfficeIds = dealAddPartitionList.stream().map(EpPartitionVO::getOfficeId).collect(Collectors.toList());
                officeDtoIds.removeAll(addOfficeIds);
                officeIds.removeAll(officeDtoIds);
                officeIds.forEach(v->{
                    deletePartitionList.add(partitionMap.get(v));
                });
            }
        }
        if (CollUtil.isNotEmpty(dealAddPartitionList)){
            dealAddPartitionList.stream().filter(s->Objects.nonNull(s)
                    &&Objects.nonNull(s.getParentOfficeId())).forEach(s->{
                //设置能源分区树里的父id
                s.setParentId(partitionMap.get(s.getParentOfficeId()));
            });
            List<EpPartition> addPartitionList = BeanUtil.copyToList(dealAddPartitionList, EpPartition.class);
            saveOrUpdateBatch(addPartitionList);
        }
        if (CollUtil.isNotEmpty(deletePartitionList)) {
            List<EpInstrumentAccount> epInstrumentAccounts = epInstrumentAccountService.list(new LambdaQueryWrapper<EpInstrumentAccount>()
                    .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES)
                    .eq(EpInstrumentAccount::getMeterType, dto.getType()));
            //lou出仪表里面某个类型的所有节点
            List<Long> partitionList = epInstrumentAccounts.stream().map(EpInstrumentAccount::getPartitionNode).collect(Collectors.toList());
            Map<Long, String> partionIdNames = Maps.newHashMap();
            epPartitions.stream().forEach(s -> {
                partionIdNames.put(s.getId(), s.getOfficeName());
            });
            //分区树表中
            List<Long> partitionIds = epPartitions.stream().map(EpPartition::getId).collect(Collectors.toList());
            //如果有仪表绑定了该节点无法删除
            deletePartitionList.forEach(s -> {
                if (partitionList.contains(s) && partitionIds.contains(s)) {
                    throw new BusinessException("已有仪表绑定节点:" + partionIdNames.get(s) + ",请重新编辑");
                }
            });
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
        EpEnergyTreeDTO result = new EpEnergyTreeDTO();
        //根据子树信息返回详情
        Set<Long> partitionIdResult = Sets.newHashSet();
        //准备数据
        List<EpPartitionVO> childTree = Lists.newArrayList();
        if(CollUtil.isNotEmpty(treeList)){

            dto.getPartitionId().forEach(partitionTmp -> {
                //过滤子树信息
                EpPartitionVO resTmp = new EpPartitionVO();
                EpPartitionVO child = getChild(treeList.get(0), partitionTmp,resTmp);
                childTree.add(child);
            });
            for(EpPartitionVO tmp:childTree){
                tmp = setTreeDetail(tmp);
                Set<Long> partitionTmp = Sets.newHashSet();
                partitionTmp = getRealMeter(tmp,partitionTmp);
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
        List<EpPartitionVO> epPartitionVOS = BeanUtil.copyToList(epPartitions, EpPartitionVO.class);
        if (CollUtil.isEmpty(epPartitionVOS)){
            return linkChildren;
        }

        epPartitionVOS.stream().filter(s->Objects.nonNull(s)).forEach(s->{
            List<EpPartitionVO> epPartitionVOList = Lists.newArrayList();
            epPartitionVOS.stream().filter(v->Objects.nonNull(v)).forEach(v->{
                if (v.getParentId()==s.getId()){
                    epPartitionVOList.add(v);
                }
            });
            //获取该类型下每个节点及其下一级子节点的集合
            linkChildren.put(s.getId(),epPartitionVOList);
        });
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
    public List<EpPartitionVO> getAllChildrenById(Long partitionId) {
        List<EpPartition> epPartitions = list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getParentId, partitionId));
        List<EpPartitionVO> epPartitionVOList = BeanUtil.copyToList(epPartitions, EpPartitionVO.class);
        return epPartitionVOList;
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
     * 根据全量数据递归构造一棵树
     * @param dataList
     * @return
     */
//    private List<EpPartitionVO> recursiveTree(List<EpPartition> dataList,Integer energyType){
//        //定义返回数据集合
//        List<EpPartitionVO> epPartitionVOS = Lists.newArrayList();
//        //根据能源类型去仪表里面查询所有的可展示的仪表判断是否绑定所属分区节点
//        List<Long> partitionNodes = epInstrumentAccountService.getPartitionNodes(energyType);
//        //获取所有根节点
//        List<EpPartition> rootList = dataList.stream().filter(v -> Objects.isNull(v.getParentId())).collect(Collectors.toList());
//        if (CollUtil.isNotEmpty(rootList)){
//            rootList.sort(Comparator.comparing(EpPartition::getSort));
//            //把根节点从全量数据集合中删除
//            dataList.removeAll(rootList);
//            //遍历根节点
//            for (EpPartition root : rootList) {
//                EpPartitionVO parent = new EpPartitionVO();
//                BeanUtils.copyProperties(root,parent);
//                if (CollUtil.isNotEmpty(partitionNodes)&&partitionNodes.contains(root.getId())){
//                    parent.setIsLinkTable(MpConstant.YES);
//                }else {
//                    parent.setIsLinkTable(MpConstant.NO);
//                }
//                epPartitionVOS.add(parent);
//                //查询vo的子级,并将子级放到vo
//                searchSysDictCh(parent, dataList,partitionNodes);
//
//            }
//        }
//        return epPartitionVOS;
//    }

    /**
     * 递归查询
     * @param parentVO
     * @param dataList
     */
//    private void searchSysDictCh(EpPartitionVO parentVO, List<EpPartition> dataList,List<Long> partitionNodes) {
//        //获取当前节点的子级
//        List<EpPartition> children = dataList.stream().filter(v -> Objects.nonNull(parentVO.getId())
//                && parentVO.getId() .equals(v.getParentId()) ).collect(Collectors.toList());
//        if (CollUtil.isNotEmpty(children)){
//            children.sort(Comparator.comparing(EpPartition::getSort));
//            List<EpPartitionVO> childrenVoList = BeanUtil.copyToList(children, EpPartitionVO.class);
//            childrenVoList.forEach(v->{
//                if (CollUtil.isNotEmpty(partitionNodes)&&partitionNodes.contains(v.getId())){
//                    v.setIsLinkTable(MpConstant.YES);
//                }else {
//                    v.setIsLinkTable(MpConstant.NO);
//                }
//            });
//            //父亲结点挂孩子
//            parentVO.setChildren(childrenVoList);
//            //从集合中删除孩子
//            dataList.removeAll(children);
//            //孩子结点继续去挂自己的孩子
//            for (EpPartitionVO item : childrenVoList) {
//                searchSysDictCh(item, dataList,partitionNodes);
//            }
//        }
//    }

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




