package com.steccn.mes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.mes.domain.JsSysOffice;
import com.steccn.mes.domain.MesEqpServicePlan;
import com.steccn.mes.domain.MesEquipment;
import com.steccn.mes.dto.MesEqpCountDTO;
import com.steccn.mes.mapper.JsSysOfficeMapper;
import com.steccn.mes.mapper.MesEqpInsTaskMapper;
import com.steccn.mes.service.MesEqpServicePlanService;
import com.steccn.mes.service.MesEquipmentService;
import com.steccn.mes.mapper.MesEquipmentMapper;
import com.steccn.mes.vo.MesEqpPartVO;
import com.steccn.mes.vo.MesEqpVO;
import com.steccn.mes.vo.YlEqpDashboardVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【mes_equipment(设备表
)】的数据库操作Service实现
* @createDate 2023-09-01 16:29:57
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class MesEquipmentServiceImpl extends ServiceImpl<MesEquipmentMapper, MesEquipment>
    implements MesEquipmentService {
    private final MesEqpServicePlanService mesEqpServicePlanService;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;
    private final JsSysOfficeMapper jsSysOfficeMapper;
    private final MesEquipmentMapper mesEquipmentMapper;
    private final MesEqpInsTaskMapper mesEqpInsTaskMapper;

    @Override
    public List<MesEqpVO> getEqpInfo() {
        //获取所有检修作业计划
        List<MesEqpServicePlan> servicePlan = mesEqpServicePlanService.list(new LambdaQueryWrapper<MesEqpServicePlan>()
                .select(MesEqpServicePlan::getEqpId, MesEqpServicePlan::getPart, MesEqpServicePlan::getMaintainMethod));
        List<MesEqpVO> results = Lists.newArrayList();
        List<MesEquipment> mesEquipments = list(new LambdaQueryWrapper<MesEquipment>()
                .select(MesEquipment::getId, MesEquipment::getEquipmentCode, MesEquipment::getEquipmentName, MesEquipment::getRemarks, MesEquipment::getModel)
                .eq(MesEquipment::getStatus, MpConstant.NO.toString())
                .eq(MesEquipment::getIsEqp, MpConstant.NO.toString()));
        mesEquipments.forEach(tmp -> {
            MesEqpVO vo = new MesEqpVO();
            BeanUtil.copyProperties(tmp, vo);
            List<MesEqpServicePlan> collect = servicePlan.stream().filter(v -> v.getEqpId().equals(tmp.getId().toString())).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(collect)) {
                List<MesEqpPartVO> part = Lists.newArrayList();
                collect.forEach(collectTmp -> {
                    MesEqpPartVO partVO = new MesEqpPartVO();
                    BeanUtil.copyProperties(collectTmp, partVO);
                    part.add(partVO);
                });
                vo.setParts(part);
            }
            results.add(vo);
        });
        return results;
    }

    @Override
    public MesEqpVO getEqpByCode(String eqpCode) {
        MesEquipment one = getOne(new LambdaQueryWrapper<MesEquipment>()
                .eq(MesEquipment::getEquipmentCode, eqpCode)
                .eq(MesEquipment::getStatus, MpConstant.NO.toString())
                .last("limit 1"));
        MesEqpVO vo = new MesEqpVO();
        BeanUtil.copyProperties(one, vo);
        return vo;
    }

    @Override
    public Boolean changeEqpState(TupleDTO<String, String> dto) {
        MesEquipment equipment = new MesEquipment();
        //找到设备code信号
        Set<String> signalCode = FeignOptional.of(() -> remoteSysConfigurationFeignApi.contentByType("EQP_SIGNAL"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //修改设备启停
        equipment.setState(dto.getField2());
        //需要修改的id
        Set<Long> idSet = Sets.newHashSet();
        //获取设备自身信息和上级信息
        MesEquipment eqp = getOne(new LambdaQueryWrapper<MesEquipment>()
                .eq(MesEquipment::getEquipmentCode, dto.getField1())
                .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
                .last("limit 1"));
        if (Objects.isNull(eqp)) {
            log.error(dto.getField1() + "设备不存在");
            throw new BusinessException(dto.getField1() + "设备不存在");
        }
        idSet.add(eqp.getId());
        MesEquipment parent = getOne(new LambdaQueryWrapper<MesEquipment>()
                .eq(MesEquipment::getEquipmentCode, eqp.getEquipmentCode())
                .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
                .last("limit 1"));
        //设备停止时，1)下级设备全部停止 2)同级是否是否都停止，若都停止则上级也停止
        if (equipment.getState().equals(OaConstant.NUM_1.toString())) {
            //1)下级设备全部停止
            List<MesEquipment> childrenList = list(new LambdaQueryWrapper<MesEquipment>()
                    .eq(MesEquipment::getParentCode, dto.getField1())
                    .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
            if (CollUtil.isNotEmpty(childrenList)) {
                //仅对设备信号外的设备有效
                childrenList.forEach(tmp -> {
                    if (!signalCode.contains(tmp.getEquipmentCode())) {
                        idSet.add(tmp.getId());
                    }
                });
            }
            //2)同级是否是否都停止，若都停止则上级也停止
            List<MesEquipment> platList = list(new LambdaQueryWrapper<MesEquipment>()
                    .eq(MesEquipment::getParentCode, eqp.getParentCode())
                    .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
            if (CollUtil.isNotEmpty(platList)) {
                //判断同级设备是否都停止
                int size = platList.size();
                platList = platList.stream().filter(v -> v.getState().equals(OaConstant.NUM_1.toString())).collect(Collectors.toList());
                if (size == platList.size() && !signalCode.contains(parent.getEquipmentCode())) {
                    idSet.add(parent.getId());
                }
            }

        }
        return update(equipment, new LambdaQueryWrapper<MesEquipment>()
                .in(MesEquipment::getId, idSet)
                .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
    }

    @Override
    public YlEqpDashboardVO dashBoard(TupleDTO<LocalDate, LocalDate> dto) {
        YlEqpDashboardVO result = new YlEqpDashboardVO();
        //获取原料车间下的所有节点
        List<JsSysOffice> sysOffices = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                .like(JsSysOffice::getParentCodes, "STEC001003")
                .eq(JsSysOffice::getStatus, OaConstant.NO));
        Set<String> departments = sysOffices.stream().map(JsSysOffice::getOfficeCode).collect(Collectors.toSet());
        departments.add("STEC001003");
        //获取设备信息
        List<MesEqpCountDTO> eqpCount = mesEquipmentMapper.findEqpCount(departments);
        if (CollUtil.isNotEmpty(eqpCount)) {
            result.setEqpAllNum(eqpCount.stream().mapToInt(MesEqpCountDTO::getNum).sum());
            Map<Integer, Integer> stateNumMap = eqpCount.stream()
                    .collect(Collectors.toMap(MesEqpCountDTO::getState, MesEqpCountDTO::getNum, Integer::sum));
            result.setEqpRunningPercent(BigDecimalUtil.percent(new BigDecimal(stateNumMap.getOrDefault(0, 0) + stateNumMap.getOrDefault(2, 0)),
                    new BigDecimal(result.getEqpAllNum()), 2));
            //组图表
            List<StackGraphDTO<String, Integer>> dataList = Lists.newArrayList();
            List<TupleDTO<String, Integer>> datList1 = Lists.newArrayList();
            StackGraphDTO<String, Integer> stackGraphDTO1 = new StackGraphDTO<>();
            stackGraphDTO1.setXAxis("待机数");
            datList1.add(new TupleDTO<>("数量", stateNumMap.getOrDefault(2, 0)));
            stackGraphDTO1.setData(datList1);
            dataList.add(stackGraphDTO1);

            List<TupleDTO<String, Integer>> datList2 = Lists.newArrayList();
            StackGraphDTO<String, Integer> stackGraphDTO2 = new StackGraphDTO<>();
            stackGraphDTO2.setXAxis("运行数");
            datList2.add(new TupleDTO<>("数量", stateNumMap.getOrDefault(0, 0)));
            stackGraphDTO2.setData(datList2);
            dataList.add(stackGraphDTO2);

            StackGraphDTO<String, Integer> stackGraphDTO3 = new StackGraphDTO<>();
            stackGraphDTO3.setXAxis("停机数");
            List<TupleDTO<String, Integer>> datList3 = Lists.newArrayList();
            datList3.add(new TupleDTO<>("数量", stateNumMap.getOrDefault(3, 0)));
            stackGraphDTO3.setData(datList3);
            dataList.add(stackGraphDTO3);

            StackGraphDTO<String, Integer> stackGraphDTO4 = new StackGraphDTO<>();
            stackGraphDTO4.setXAxis("故障数");
            List<TupleDTO<String, Integer>> datList4 = Lists.newArrayList();
            datList4.add(new TupleDTO<>("数量", stateNumMap.getOrDefault(1, 0)));
            stackGraphDTO4.setData(datList4);
            dataList.add(stackGraphDTO4);
            result.setEqpDashBoard(StackGraphUtil.convertData(dataList, Boolean.TRUE));
        }
        //获取点检任务信息
        List<MesEqpCountDTO> taskCount = mesEqpInsTaskMapper.findTaskCount2(departments, dto.getField1().atTime(8, 0, 0), dto.getField2().plusDays(1).atTime(8, 0, 0));
        if (CollUtil.isNotEmpty(taskCount)) {
            result.setInsNum(taskCount.stream().mapToInt(MesEqpCountDTO::getNum).sum());
            result.setInsFinishedPercent(BigDecimalUtil.percent(new BigDecimal(taskCount.stream().filter(v -> v.getState().equals(0)).findFirst().orElse(new MesEqpCountDTO(0, 0)).getNum()),
                    new BigDecimal(result.getInsNum()), 2));
        }
        return result;
    }

    @Override
    public Boolean test(TupleDTO<String, String> dto) {
        //测试设备启停状态变更
        //获取设备自身信息和上级信息
        MesEquipment eqp = getOne(new LambdaQueryWrapper<MesEquipment>()
                .eq(MesEquipment::getEquipmentCode, dto.getField1())
                .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
                .last("limit 1"));
        if (Objects.isNull(eqp)) {
            log.error(dto.getField1() + "设备不存在");
            throw new BusinessException(dto.getField1() + "设备不存在");
        }
        //写死原料车间设备
        if (eqp.getParentCodes().contains("1501738403312599040")) {
            if (dto.getField2().equals("true")) {
                eqp.setState(OaConstant.NO.toString());
            } else {
                eqp.setState(OaConstant.NUM_3.toString());
            }
            //注:eqp是传过来的设备信息,equipment代表设备的改变状态
            //修改当前设备状态
            updateById(eqp);
            //判断当前设备是否末节点
            if (!eqp.getTreeLeaf().equals("1")) {
                //不是末节点，需要同时考虑向上和向下
                //向下，获取设备下级设备，并修改所有下级设备状态
                //递归更新所有子节点的状态
                updateChildrenStatus(eqp, dto.getField2());
            }
            //向上
            List<MesEquipment> list = list(new LambdaQueryWrapper<MesEquipment>().eq(MesEquipment::getParentCode, eqp.getParentCode()));
            if (CollUtil.isNotEmpty(list)) {
                //检查设备同一层级节点状态一致性
                if (checkSiblingsStatusConsistency(list, eqp)) {
                    MesEquipment one = getOne(new LambdaQueryWrapper<MesEquipment>().eq(MesEquipment::getId, eqp.getParentCode()));
                    if (Objects.nonNull(one)) {
                        updateParentStatus(one, dto.getField2());
                    }
                }else {
                    //向上，确保当前设备开时，父设备也在开启状态，直到根状态也为开启状态，如果当前设备状态为关闭，则不用变更状态
                    if (eqp.getState().equals(OaConstant.NO.toString())){
                        MesEquipment one = getOne(new LambdaQueryWrapper<MesEquipment>().eq(MesEquipment::getId, eqp.getParentCode()));
                        //不需要考虑父状态一致性
                        updateParentStatusTree(one, dto.getField2());
                    }
                }
            }
        }
        return true;
    }


    //更新设备父节点的状态，不考虑父状态一致性，直到根节点
    private void updateParentStatusTree(MesEquipment equipment, String status) {
        if (equipment.getIsEqp().equals("0")) {
            if (status.equals("true")) {
                equipment.setState(OaConstant.NO.toString());
            } else {
                equipment.setState(OaConstant.NUM_3.toString());
            }
            //修改设备状态
            updateById(equipment);
            //一致， 继续向上检查
            updateParentStatusTree(findParent(equipment), status);
        }
    }

    //更新设备父节点的状态
    private void updateParentStatus(MesEquipment equipment, String status) {
        if (equipment.getIsEqp().equals("0")) {
            if (status.equals("true")) {
                equipment.setState(OaConstant.NO.toString());
            } else {
                equipment.setState(OaConstant.NUM_3.toString());
            }
            //修改设备状态
            updateById(equipment);
            List<MesEquipment> list = list(new LambdaQueryWrapper<MesEquipment>().eq(MesEquipment::getParentCode, equipment.getParentCode()));
            //判断父节点是否存在兄弟节点并且判断状态一致性
            if (checkSiblingsStatusConsistency(list, equipment)) {
                //一致， 继续向上检查
                updateParentStatus(findParent(equipment), status);
            }
        }
    }
    //检查设备同一层级节点状态一致性
    private boolean checkSiblingsStatusConsistency(List<MesEquipment> nodes, MesEquipment equipment) {
        for (MesEquipment node : nodes) {
            if (!node.getEquipmentCode().equals(equipment.getEquipmentCode()) && !node.getState().equals(equipment.getState())) {
                //存在状态不一致的兄弟节点
                return false;
            }
        }
        return true;
    }

    // 查找设备父节点
    private MesEquipment findParent(MesEquipment equipment) {
        return getOne(new LambdaQueryWrapper<MesEquipment>().eq(MesEquipment::getId,equipment.getParentCode()));
    }


    // 递归更新子节点的状态
    private void updateChildrenStatus(MesEquipment equipment, String status) {
        if(status.equals("true")) {
            equipment.setState(OaConstant.NO.toString());
        }else {
            equipment.setState(OaConstant.NUM_3.toString());
        }
        updateById(equipment);
        List<MesEquipment> list = list(new LambdaQueryWrapper<MesEquipment>().eq(MesEquipment::getParentCode, equipment.getId()));
        for (MesEquipment mesEquipment : list) {
            //修改设备状态
            updateChildrenStatus(mesEquipment, status);
        }
    }
}




