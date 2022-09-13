package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.domain.QtCheckTaskDetail;
import com.steccn.qms.domain.QtRecheckTask;
import com.steccn.qms.dto.*;
import com.steccn.qms.service.*;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.vo.QtCheckPlanDetailsVO;
import com.steccn.qms.vo.QtCheckTaskDetailVO;
import com.steccn.qms.vo.QtCheckTaskListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author AndrewLuo
* @description 针对表【qt_check_task(巡检任务表)】的数据库操作Service实现
* @createDate 2022-09-02 08:54:09
*/
@Service
@RequiredArgsConstructor
public class QtCheckTaskServiceImpl extends ServiceImpl<QtCheckTaskMapper, QtCheckTask>
    implements QtCheckTaskService{

    private final QtCheckTaskMapper qtCheckTaskMapper;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final QtMaterialService qtMaterialService;

    private final QtCheckPlanService qtCheckPlanService;

    private final QtCheckTaskDetailService qtCheckTaskDetailService;

    private final QtRecheckTaskService qtRecheckTaskService;

    /**
     * 新增
     * @param dto
     * @return
     */
    @Override
    public Boolean add(QtAddCheckTaskDTO dto) {
        QtCheckTask qtCheckTask = new QtCheckTask();
        BeanUtils.copyProperties(dto, qtCheckTask);
        //任务类型,原始任务
        qtCheckTask.setType(0);
        //状态待检验
        qtCheckTask.setStatus(0);
        return save(qtCheckTask);
    }

    /**
     * 任务列表
     * @param dto
     * @return
     */
    @Override
    public List<QtCheckTaskListVO> checkTaskList(QtCheckTaskListDTO dto) {
        List<QtCheckTask> qtCheckTasks = qtCheckTaskMapper.selectByTaskType(dto.getCheckTypeCode(),dto.getStatus());
        List<QtCheckTaskListVO> vos = new ArrayList<>();
        if(qtCheckTasks!=null && qtCheckTasks.size() > 0){
            qtCheckTasks.forEach(tmp -> {
                QtCheckTaskListVO qtCheckTaskListVo = new QtCheckTaskListVO();
                qtCheckTaskListVo.setId(tmp.getId());
                qtCheckTaskListVo.setStatus(tmp.getStatus());
                //返回名字
                qtCheckTaskListVo.setMaterialName(qtMaterialService.getById(tmp.getMaterialId()).getName());
                qtCheckTaskListVo.setWorkshopName(remoteOfficeFeign.getNameById(tmp.getWorkshopId()).getMsg());
                qtCheckTaskListVo.setProductLineName(remoteOfficeFeign.getNameById(tmp.getProductLineId()).getMsg());
                vos.add(qtCheckTaskListVo);
            });
        }
        return vos;
    }

    /**
     * 任务详情
     * @param id
     * @return
     */
    @Override
    public QtCheckTaskDetailVO taskDetail(Long id) {
        QtCheckTask qtCheckTask = getById(id);
        QtCheckTaskListVO qtCheckTaskListVo = new QtCheckTaskListVO();
        if(qtCheckTask!=null){
            qtCheckTaskListVo.setId(qtCheckTask.getId());
            qtCheckTaskListVo.setStatus(qtCheckTask.getStatus());
            //返回名字
            qtCheckTaskListVo.setMaterialName(qtMaterialService.getById(qtCheckTask.getMaterialId()).getName());
            qtCheckTaskListVo.setWorkshopName(remoteOfficeFeign.getNameById(qtCheckTask.getWorkshopId()).getMsg());
            qtCheckTaskListVo.setProductLineName(remoteOfficeFeign.getNameById(qtCheckTask.getProductLineId()).getMsg());
        }
        QtCheckPlanDetailsVO qtCheckPlanDetailsVO = qtCheckPlanService.info(qtCheckTask.getCheckPlanId(),id);
        QtCheckTaskDetailVO qtCheckTaskDetailVo = new QtCheckTaskDetailVO();
        qtCheckTaskDetailVo.setQtCheckTaskListVo(qtCheckTaskListVo);
        qtCheckTaskDetailVo.setQtCheckPlanDetailsVO(qtCheckPlanDetailsVO);
        return qtCheckTaskDetailVo;
    }

    /**
     * 修改任务状态
     * @param dto
     * @return
     */
    @Override
    public Boolean updateStatus(QtCheckTaskStatusDTO dto) {
        QtCheckTask qtCheckTask = new QtCheckTask();
        qtCheckTask.setId(dto.getId());
        qtCheckTask.setStatus(dto.getStatus());
        return updateById(qtCheckTask);
    }

    /**
     * 添加值
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean itemValue(QtCheckTaskSubmitDTO dto) {
        List<QtCheckTaskDetail> valueModels = new ArrayList<>();
        //插入值
        if(dto.getValues()!=null && dto.getValues().size() > 0){
            dto.getValues().forEach(tmp->{
                Map<String, Object> condition = new HashMap<>();
                QtCheckTaskDetail qtCheckTaskDetail = new QtCheckTaskDetail();
                qtCheckTaskDetail.setCheckResult(tmp.getCheckResult());
                qtCheckTaskDetail.setTaskId(dto.getId());
                condition.put("task_id",dto.getId());
                qtCheckTaskDetail.setItemId(tmp.getItemId());
                condition.put("item_id",tmp.getItemId());
                qtCheckTaskDetail.setGroupId(tmp.getGroupId());
                condition.put("group_id",tmp.getGroupId());
                //需要先删除之前保存的值
                qtCheckTaskDetailService.removeByMap(condition);
                valueModels.add(qtCheckTaskDetail);
            });
            qtCheckTaskDetailService.saveBatch(valueModels);
        }
        QtCheckTaskStatusDTO statusDTO = new QtCheckTaskStatusDTO();
        statusDTO.setId(dto.getId());
        statusDTO.setStatus(dto.getStatus());
        return updateStatus(statusDTO);
    }

    /**
     * 完成任务
     * @param dto
     * @return
     */
    @Override
    public Boolean finished(QtCheckResultDTO dto) {
        QtCheckTask qtCheckTask = new QtCheckTask();
        BeanUtils.copyProperties(dto,qtCheckTask);
        qtCheckTask.setStatus(2);
        return updateById(qtCheckTask);
    }

    /**
     * 复检任务
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long retask(Long id) {
        QtCheckTask res = getById(id);
        QtCheckTask model = new QtCheckTask();
        if(res == null){
            throw new BusinessException("任务信息不存在");
        }
        BeanUtils.copyProperties(res,model);
        //复制值后置空
        model.setBadNum(null);
        model.setCheckNum(null);
        model.setGoodNum(null);
        model.setResult(null);
        model.setNote(null);
        model.setStatus(0);
        model.setType(1);
        model.setId(null);
        save(model);
        QtRecheckTask recheckModel = new QtRecheckTask();
        recheckModel.setOriginalId(id);
        recheckModel.setRecheckId(model.getId());
        qtRecheckTaskService.save(recheckModel);
        return model.getId();
    }
}




