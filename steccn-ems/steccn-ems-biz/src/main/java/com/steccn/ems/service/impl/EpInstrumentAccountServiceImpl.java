package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.config.FeignConfig;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.dto.*;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.mapper.EpInstrumentAccountMapper;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.vo.*;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.vo.QtSpcReportDetailVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.summary.Product;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author yequan
* @description 针对表【ep_instrument_account(仪表台账)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@Service
@Slf4j
public class EpInstrumentAccountServiceImpl extends ServiceImpl<EpInstrumentAccountMapper, EpInstrumentAccount>
    implements EpInstrumentAccountService{

    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private EpPartitionService epPartitionService;
    @Autowired
    private EpEnergyTypeService epEnergyTypeService;
    @Autowired
    private EpMeterReadService epMeterReadService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrEdit(EpInstrumentAccountDTO dto) {
        long count = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                //所属分区节点
                .eq(EpInstrumentAccount::getPartitionNode, dto.getPartitionNode())
                .eq(EpInstrumentAccount::getMeterType,dto.getMeterType())
                //可展示的未删除的
                .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES));
        getRepeatJudgment(dto.getId(), count,"该节点下已经有仪表,请勿多次创建");

        if (StringUtils.isNotBlank(dto.getDistantMapCode())){
            List<EpInstrumentAccount> list = list(new LambdaQueryWrapper<EpInstrumentAccount>()
                    .eq(EpInstrumentAccount::getDistantMapCode, dto.getDistantMapCode())
                    .eq(EpInstrumentAccount::getMeterType, dto.getMeterType())
                    .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES));
            getRepeatJudgment(dto.getId(), list.size(),"远传表映射编码不能重复");
        }

        long sizeMeterName = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getMeterName, dto.getMeterName())
                .eq(EpInstrumentAccount::getMeterType,dto.getMeterType())
                .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES));
       getRepeatJudgment(dto.getId(),sizeMeterName,"仪表名称不能重复");

        if (StringUtils.isNotBlank(dto.getSiteName())&&dto.getMeterType()!=1){
            //查询电表下的所有安装位置有无重复
            long sizeSiteName = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                    .eq(EpInstrumentAccount::getMeterType,dto.getMeterType())
                    .eq(EpInstrumentAccount::getSiteName,dto.getSiteName())
                    .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES));
        getRepeatJudgment(dto.getId(),sizeSiteName,"安装位置不能重复");
        }
        if (StringUtils.isNotBlank(dto.getAssetsCode())){
            //查询数据库中的资产编码
            long sizeAssetsCode = count(new LambdaQueryWrapper<EpInstrumentAccount>()
                    .eq(EpInstrumentAccount::getAssetsCode, dto.getAssetsCode())
                    .eq(EpInstrumentAccount::getMeterType,dto.getMeterType())
                    .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES));
            getRepeatJudgment(dto.getId(),sizeAssetsCode,"资产编码不能重复");
        }
        EpInstrumentAccount epInstrumentAccount = new EpInstrumentAccount();
        BeanUtils.copyProperties(dto,epInstrumentAccount);
        if (Objects.isNull(dto.getId())){
            save(epInstrumentAccount);
        }else {
            EpInstrumentAccount instrumentAccount = getById(dto.getId());
            Assert.notNull(instrumentAccount,"编辑的仪表台账不存在");
            if (MpConstant.NO==epInstrumentAccount.getShowFlag()){
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
                .in(CollUtil.isNotEmpty(dto.getPartitionNode()),EpInstrumentAccount::getPartitionNode, dto.getPartitionNode())
                //模糊匹配仪表名称
                .like(Objects.nonNull(dto.getMeterName()),EpInstrumentAccount::getMeterName, dto.getMeterName())
                //能源类型
                .eq(EpInstrumentAccount::getMeterType,dto.getEnergyId())
                //查询所有可展示的
                .eq(EpInstrumentAccount::getShowFlag,MpConstant.YES);

        //3.根据条件查询数据库数据
        List<EpInstrumentAccount> instrumentAccounts = list(condition);
        //4.拷贝返回数据
        List<EpInstrumentAccountPageVO> instrumentAccountVOS = BeanUtil.copyToList(instrumentAccounts, EpInstrumentAccountPageVO.class);
        //5.处理返回数据
        //自定义分页对象
        Page<EpInstrumentAccountPageVO> page = new Page<>();
        //先筛选掉不合条件的数据
        instrumentAccountVOS = instrumentAccountVOS.stream().filter(s -> Objects.nonNull(s)
                && Objects.nonNull(s.getPartitionNode())&&Objects.nonNull(s.getMeterType())).collect(Collectors.toList());
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
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictSet)).get();
        //去能源分区表中找到所有的分区节点
        Map<Long, EpPartitionVO> epPartitionNameListVO = epPartitionService.getEpPartitionNameListVO(partitionIds);

        //获取该类型下所有下一级子节点
        Map<Long, List<EpPartitionVO>> partitionLinkChildMap = epPartitionService.allNextChildren(dto.getEnergyId());
        //找到该类型下根据节点分组的全量数据
        Map<Long,EpInstrumentAccountPageVO> partitionInstrumentVOList = Maps.newHashMap();
        instrumentAccountVOS.forEach(s->{
            partitionInstrumentVOList.put(s.getPartitionNode(),s);
        });

        //先lou出仪表台账里面所有仪表id以及远传映射编码
        List<Integer> instrumentIds = instrumentAccountVOS.stream().map(EpInstrumentAccountPageVO::getId).collect(Collectors.toList());
        List<String> distantCodeList = instrumentAccountVOS.stream().map(EpInstrumentAccountPageVO::getDistantMapCode).collect(Collectors.toList());
        //查询最后抄表时间
        Map<Integer, Map<String, List<EpMeterRead>>> groupByMeter = epMeterReadService.list(new LambdaQueryWrapper<EpMeterRead>()
                        .eq(EpMeterRead::getDelFlag, MpConstant.NO)
                        .in(CollUtil.isNotEmpty(distantCodeList), EpMeterRead::getDistantMapCode, distantCodeList)
                        .in(CollUtil.isNotEmpty(instrumentIds), EpMeterRead::getMeterId, instrumentIds))
                .stream()
                .collect(Collectors.groupingBy(EpMeterRead::getMeterId, Collectors.groupingBy(EpMeterRead::getDistantMapCode)));


        //遍历集合插入数据
        instrumentAccountVOS.forEach(s -> {
            if (CollUtil.isNotEmpty(dictMap)&&Objects.nonNull(s.getSite())) {
                s.setSiteName(dictMap.get(s.getSite()));
            }
            if (CollUtil.isNotEmpty(epPartitionNameListVO)&&Objects.nonNull(s.getPartitionNode())){
                s.setPartitionNodeName(Objects.isNull(epPartitionNameListVO.get(s.getPartitionNode()))
                        ?null:epPartitionNameListVO.get(s.getPartitionNode()).getOfficeName());
            }
            EpPartitionChildTypeDTO epPartitionChildTypeDTO = new EpPartitionChildTypeDTO();
            epPartitionChildTypeDTO.setType(s.getMeterType());
            epPartitionChildTypeDTO.setChildId(s.getPartitionNode());
            EpPartitionParentVO partitionParent = epPartitionService.getParentList(epPartitionChildTypeDTO);
            if (Objects.nonNull(partitionParent)) {
                s.setAllPartitionNodeName(partitionParent.getJoinOfficeNames());
            }

            //设置最后抄表时间
            if (CollUtil.isNotEmpty(groupByMeter)){
                //先判断仪表的所属类型
                if (EpManagerConstant.InstrumentType.REMOTE.getCode()==s.getEntityTable()
                        ||EpManagerConstant.InstrumentType.NON_REMOTE.getCode()==s.getEntityTable()){
                    Map<String, List<EpMeterRead>> stringListMap = groupByMeter.get(s.getId());
                    List<EpMeterRead> epMeterReads = new ArrayList<>();
                    if (CollUtil.isNotEmpty(stringListMap)){
                        epMeterReads = stringListMap.get(s.getDistantMapCode());
                    }
                    if (Objects.nonNull( getLastReadTime(epMeterReads))){
                        s.setLastReadingTime( getLastReadTime(epMeterReads));
                    }
                } else if (EpManagerConstant.InstrumentType.VIRTUAL.getCode()==s.getEntityTable()) {
                    List<EpPartitionVO> partitionChildrenList = partitionLinkChildMap.get(s.getPartitionNode());
                    if (CollUtil.isNotEmpty(partitionChildrenList)){
                        //找到绑定该节点下子节点的仪表的id
                        List<Integer> instrumentChildIds = Lists.newArrayList();
                        partitionChildrenList.stream().forEach(p->{
                            //子节点绑定的仪表
                            EpInstrumentAccountPageVO epInstrumentAccountPageVO = partitionInstrumentVOList.get(p.getId());
                            if (Objects.nonNull(epInstrumentAccountPageVO)){
                                instrumentChildIds.add(epInstrumentAccountPageVO.getId());
                            }
                        });
                        if (CollUtil.isNotEmpty(instrumentChildIds)){
                            //如果该节点的子节点绑定了仪表设置最后抄表时间(取每个子节点最后抄表时间最早的)
                            //保证每个子节点仪表的最后抄表时间都不能有空
                            List<LocalDateTime> timeList = Lists.newArrayList();
                            instrumentChildIds.forEach(i->{
                                Map<String, List<EpMeterRead>> stringListMap = groupByMeter.get(s.getId());
                                List<EpMeterRead> epMeterReads = new ArrayList<>();
                                if (CollUtil.isNotEmpty(stringListMap)){
                                    epMeterReads = stringListMap.get(s.getDistantMapCode());
                                }
                                if (Objects.nonNull(getLastReadTime(epMeterReads))){
                                    timeList.add(getLastReadTime(epMeterReads));
                                }
                            });
                            //如果查询到的子表最后抄表时间的个数等于绑定仪表的个数设置虚拟仪表的最后抄表时间
                            if (instrumentChildIds.size()==timeList.size()){
                                //从timeList集合中找到时间最早的
                                LocalDateTime realLastReadTime = LocalDateTime.now();
                                for (int i = 0; i < timeList.size(); i++) {
                                    realLastReadTime=timeList.get(i);
                                    if ((i+1)<timeList.size() && realLastReadTime.isAfter(timeList.get(i+1))){
                                        realLastReadTime=timeList.get(i+1);
                                    }
                                }
                                s.setLastReadingTime(realLastReadTime);
                            }
                        }
                    }
                }

            }

        });

        //排序+逻辑分页
        Integer skip = (dto.getCurrent() - 1) * dto.getSize();
        //排序
        List<EpInstrumentAccountPageVO> collect = instrumentAccountVOS.stream().skip(skip).limit(dto.getSize()).collect(Collectors.toList());
        if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_METER_READING_TIME.equals(dto.getSortField())){
            collect = collect.stream().sorted(Comparator.comparing(EpInstrumentAccountPageVO::getLastReadingTime).reversed()).collect(Collectors.toList());
        }else if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_DISTANT_MAPCODE.equals(dto.getSortField())){
           collect= collect.stream().sorted(Comparator.comparing(EpInstrumentAccountPageVO::getDistantMapCode).reversed()).collect(Collectors.toList());
        }else if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_METER_NAME.equals(dto.getSortField())){
            collect=collect.stream().sorted(Comparator.comparing(EpInstrumentAccountPageVO::getMeterName).reversed()).collect(Collectors.toList());
        }else if (EpManagerConstant.EP_INSTRUMENT_ACCOUNT_SORT_FIELD_STATUS.equals(dto.getSortField())){
            collect=collect.stream().sorted(Comparator.comparing(EpInstrumentAccountPageVO::getStatus).reversed()).collect(Collectors.toList());
        }

        page.setCurrent(dto.getCurrent());
        page.setTotal(instrumentAccountVOS.size());
        page.setSize(dto.getSize());
        page.setRecords(collect);

        return page;
    }

    private List<String> getParentNameList(List<EpPartitionParentVO> parentList,List<String> partitionName) {
            if (CollUtil.isNotEmpty(parentList)){
                for (EpPartitionParentVO epPartitionParentVO : parentList) {
                    partitionName.add(epPartitionParentVO.getOfficeName());
                    if (CollUtil.isNotEmpty(epPartitionParentVO.getParentList())){
                        for (EpPartitionParentVO partitionParentVO : epPartitionParentVO.getParentList()) {
                            getParentNameList(partitionParentVO.getParentList(),partitionName);
                        }
                    }
                }
            }
            return partitionName;
    }

    private LocalDateTime getLastReadTime( List<EpMeterRead> epMeterReads) {
            if (CollUtil.isEmpty(epMeterReads)){
                return null;
            }else if (!epMeterReads.stream().filter(m->Objects.isNull(m.getReadTime()))
                    .findAny().isPresent()){
                LocalDateTime lastReadTime = LocalDateTime.now();
                for (int i = 0; i < epMeterReads.size(); i++) {
                    lastReadTime =epMeterReads.get(i).getReadTime();
                    if ((i+1)<epMeterReads.size()){
                        if (lastReadTime.isBefore(epMeterReads.get(i+1).getReadTime())){
                            lastReadTime=epMeterReads.get(i+1).getReadTime();
                        }
                    }
                }
                return lastReadTime;
            }
            return null;
    }

    @Override
    public List<Long> getPartitionNodes(Integer energyType) {
        List<Long> partitionNodesVO = Lists.newArrayList();
        List<Long> partitionNodes = list(new LambdaQueryWrapper<EpInstrumentAccount>()
                .eq(EpInstrumentAccount::getMeterType,energyType)
                .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES))
                .stream().map(EpInstrumentAccount::getPartitionNode).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(partitionNodes)){
            partitionNodes.forEach(v->{
                partitionNodesVO.add(v);
            });
        }
        return partitionNodesVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reviseStatus(EpInstrumentAccountStatusDTO dto) {
        EpInstrumentAccount epInstrumentAccount = getById(dto.getId());
        Assert.notNull(epInstrumentAccount,"该仪表台账不存在");
        Assert.isTrue(MpConstant.YES==epInstrumentAccount.getShowFlag(),"该仪表台账已删除");
        epInstrumentAccount.setStatus(dto.getStatus());
        updateById(epInstrumentAccount);
        return Boolean.TRUE;
    }

    @Override
    public EpInstrumentAccountVO getEpInstrumentAccountVO(Integer id) {
       Assert.notNull(id,"查询id不能为空");
        EpInstrumentAccount epInstrumentAccount = getById(id);
        if (Objects.isNull(epInstrumentAccount)){
            return null;
        }
        Assert.isTrue(MpConstant.YES==epInstrumentAccount.getShowFlag(),"该仪表台账已删除");
        //设置返回实体类对象
        EpInstrumentAccountVO epInstrumentAccountVO = new EpInstrumentAccountVO();
        BeanUtils.copyProperties(epInstrumentAccount,epInstrumentAccountVO);
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
        if (Objects.nonNull(energyTypeVO)){
            //设置仪表名称
            epInstrumentAccountVO.setMeterTypeName(energyTypeVO.getMeasureMeterName());
            //设置计量单位
            epInstrumentAccountVO.setMeasureUnitName(energyTypeVO.getMeasureUnitName());
        }
        if (Objects.nonNull(epInstrumentAccountVO.getSite())){
            Set<Long> dictSet = Sets.newHashSet();
            dictSet.add(epInstrumentAccountVO.getSite());
            Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictSet)).get();
            if (CollUtil.isNotEmpty(dictMap)){
                epInstrumentAccountVO.setSiteName(dictMap.get(epInstrumentAccountVO.getSite()));
            }
        }
        if (Objects.nonNull(epInstrumentAccountVO.getPartitionNode())&&Objects.nonNull(epInstrumentAccountVO.getMeterType())){
            EpPartitionChildTypeDTO epPartitionChildTypeDTO = new EpPartitionChildTypeDTO();
            epPartitionChildTypeDTO.setChildId(epInstrumentAccountVO.getPartitionNode());
            epPartitionChildTypeDTO.setType(epInstrumentAccountVO.getMeterType());
            EpPartitionParentVO parentVO = epPartitionService.getParentList(epPartitionChildTypeDTO);
            if (Objects.nonNull(parentVO)&&Objects.nonNull(parentVO.getJoinOfficeNames())){
                epInstrumentAccountVO.setAllPartitionNodeName(parentVO.getJoinOfficeNames());
            }
        }
        return epInstrumentAccountVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        EpInstrumentAccount epInstrumentAccount = getById(id);
        Assert.notNull(epInstrumentAccount,"该仪表台账不存在,无法删除");
        epInstrumentAccount.setShowFlag(MpConstant.NO);
        updateById(epInstrumentAccount);
        return Boolean.TRUE;
    }

//    @Override
//    public void downloadSubstrate(HttpServletResponse response, HttpServletRequest request)throws Exception {
//        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
//        String line;
//        StringBuilder sb = new StringBuilder();
//        while ((line = br.readLine()) != null) {
//            sb.append(line);
//        }
//        br.close();
//        String base = sb.toString();
//        if (!StringUtils.isEmpty(base)) {
//            OutputStream out = null;
//            try {
//                String base64 = base.split(",")[1];
//                byte[] bytes = new BASE64Decoder().decodeBuffer(base64);
//                for (int i = 0; i < bytes.length; ++i) {
//                    if (bytes[i] < 0) {
//                        bytes[i] += 256;
//                    }
//                }
//                // 这里URLEncoder.encode可以防止中文乱码
//                String fileName = "资产卡片".replaceAll("-", "")+".jpg";
//               String realPath = request.getSession().getServletContext().getRealPath("")+ "/normalFiles/workOrderAttach/";
//                File file = new File(realPath);
//
//                if (!file.exists() || !file.isDirectory()) {
//                    file.mkdirs();
//                }
//                out = new FileOutputStream(realPath + "/" + fileName);
//                out.write(bytes);
//                out.flush();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }finally {
//                if (out != null) {
//                    out.close();
//                }
//            }
//        }
//    }


    /**
     * 根据条件判断是否抛出异常
     * @param id
     * @param size
     * @param mistake
     */
    private void getRepeatJudgment(Integer id, long size,String mistake) {
        if (Objects.isNull(id)){
            Assert.isTrue(size ==0 , mistake);
        }else {
            //编辑时允许与自身重复
            Assert.isTrue(size <=1 , mistake);
        }
    }
}




