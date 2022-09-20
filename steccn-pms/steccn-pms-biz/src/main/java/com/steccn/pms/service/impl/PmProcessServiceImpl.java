package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.vo.R;
import com.steccn.minio.fegin.RemoteFileFeignApi;
import com.steccn.minio.vo.FileInfoVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmBillOfMaterial;
import com.steccn.pms.domain.PmMaterial;
import com.steccn.pms.domain.PmProcess;
import com.steccn.pms.domain.PmProcessStation;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmProcessExcelDTO;
import com.steccn.pms.dto.PmsBillExcelDTO;
import com.steccn.pms.service.PmProcessService;
import com.steccn.pms.mapper.PmProcessMapper;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.service.PmProcessStationService;
import com.steccn.pms.vo.PmProcessStationVO;
import com.steccn.pms.vo.PmProcessVO;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * @author yequan
 * @description 针对表【pm_process(生产工序表)】的数据库操作Service实现
 * @createDate 2022-09-02 17:01:20
 */
@Service
@RequiredArgsConstructor
public class PmProcessServiceImpl extends ServiceImpl<PmProcessMapper, PmProcess>
        implements PmProcessService {

    private final PmProcessStationService processStationService;

    @Autowired
    private RemoteFileFeignApi remoteFileFeignApi;


    @Override
    public R<Boolean> saveProcess(PmProcessDTO dto) {
        PmProcess model = new PmProcess();
        BeanUtil.copyProperties(dto, model);
        SteccnUser user = SecurityUtils.getUser();
        long userId = ObjectUtil.isNull(user) ? 0 : user.getUserId();
        String name = ObjectUtil.isNull(user) ? Strings.EMPTY : user.getName();
        model.setCreateBy(userId);
        model.setCreateName(name);
        model.setUpdateBy(userId);
        model.setUpdateName(name);
        save(model);
        if (CollUtil.isNotEmpty(dto.getList())) {
            dto.setId(Math.toIntExact(model.getId()));
            processStationService.saveStations(dto);
        }
        return R.success(true);
    }

    @Override
    public Page<PmProcessVO> pageList(PmProcessDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<PmProcess> eq = new LambdaQueryWrapper<PmProcess>()
                .eq(Objects.nonNull(dto.getCode()) && !dto.getCode().equals(""), PmProcess::getCode, dto.getCode())
                .like(Objects.nonNull(dto.getName()), PmProcess::getName, dto.getName())
                .eq(Objects.nonNull(dto.getType()), PmProcess::getType, dto.getType());
        //排序按创建时间排序
        eq.orderBy(true, false, PmProcess::getCreateTime);
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, eq);
        //4.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<PmProcessVO> res = BeanUtil.copyToList(resPage.getRecords(), PmProcessVO.class);
        //6.处理数据
        List<Long> ids = Lists.newArrayList();
        res.forEach(pmProcessVO -> {
            ids.add(pmProcessVO.getFileId());
            LambdaQueryWrapper<PmProcessStation> wrapper = new LambdaQueryWrapper<PmProcessStation>().eq(PmProcessStation::getProcessId, pmProcessVO.getId().intValue());
            List<PmProcessStationVO> pmProcessStationVOS = BeanUtil.copyToList(processStationService.list(wrapper), PmProcessStationVO.class);
            pmProcessVO.setList(pmProcessStationVOS);
        });
        //获取文件信息
        R<Map<Long, FileInfoVO>> fileLists = remoteFileFeignApi.listById(ids);
        res.forEach(pmProcessVO -> {
            if (!Objects.isNull(pmProcessVO.getFileId())) {
                try {
                    FileInfoVO fileInfoVO = fileLists.getData().get(pmProcessVO.getFileId());
                    if (Objects.isNull(fileInfoVO)) {
                        pmProcessVO.setFileUrl(null);
                    } else {
                        pmProcessVO.setFileUrl(fileInfoVO.getFileUrl());
                        pmProcessVO.setFileName(fileInfoVO.getFileName());
                    }
                } catch (Exception e) {
                    throw new BusinessException(pmProcessVO.getName() + ":文件信息不存在");
                }
            }
        });
        return page.setRecords(res);
    }

    @Override
    public Boolean delete(Integer id) {
        if (Objects.isNull(getById(id))) {
            throw new BusinessException("删除的数据不存在");
        }
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> edit(PmProcessDTO dto) {
        //校验是否存在
        PmProcess model = getById(dto.getId());
        if (Objects.isNull(model)) {
            throw new BusinessException("编辑的工序不存在");
        }
        SteccnUser user = SecurityUtils.getUser();
        long userId = ObjectUtil.isNull(user) ? 0 : user.getUserId();
        String name = ObjectUtil.isNull(user) ? Strings.EMPTY : user.getName();
        model.setUpdateName(name);
        model.setUpdateBy(userId);
        model.setName(dto.getName());
        model.setCode(dto.getCode());
        model.setType(dto.getType());
        model.setStatus(dto.getStatus());
        model.setFileId(dto.getFileId());
        model.setNote(dto.getNote());
        processStationService.deleteByProcessId(dto.getId());
        if (CollUtil.isNotEmpty(dto.getList())) {
            Boolean a = processStationService.saveStations(dto);
            if (a == false) {
                throw new BusinessException("执行工位数据保存失败");
            }
        }
        //保存数据
        return R.success(updateById(model));
    }

    @Override
    public Map<Long, PmProcessVO> listById(List<Long> processIds) {
        Map<Long, PmProcessVO> resMap = Maps.newHashMap();
        Collection<PmProcess> processList = listByIds(processIds);
        if (CollUtil.isEmpty(processList)) {
            return resMap;
        }
        List<PmProcessVO> pmProcessVOList = BeanUtil.copyToList(processList, PmProcessVO.class);
        for (PmProcessVO pmProcessVO : pmProcessVOList) {
            resMap.put(pmProcessVO.getId(), pmProcessVO);
        }
        return resMap;
    }

    /**
     * 获取所有数据
     *
     * @return
     */
    @Override
    public List<PmProcessVO> listData() {
        List<PmProcessVO> pmProcessVOList = Lists.newArrayList();
        List<PmProcess> list = list();
        if (CollUtil.isEmpty(list)) {
            return pmProcessVOList;
        }
        pmProcessVOList = BeanUtil.copyToList(list, PmProcessVO.class);
        return pmProcessVOList;
    }

    @Override
    public Boolean excelImportData(PmExcelDTO dto) throws IOException {
        //文件类型校验
        if (Objects.isNull(dto.getFile())) {
            //获取文件拓展名
            String fileExtension = dto.getFile().getOriginalFilename().substring(dto.getFile().getOriginalFilename().lastIndexOf("."));
            if (!fileExtension.equals(PmManagerConstant.FILE_EXTENSION_XLSX)) {
                throw new BusinessException("文件类型不支持,请使用模板进行导入");
            }
        }
        //定义数据集合
        List<PmProcess> processList = Lists.newArrayList();
        //读取文件信息
        EasyExcel.read(dto.getFile().getInputStream(), PmProcessExcelDTO.class, new PageReadListener<PmProcessExcelDTO>(pmProcessExcelDTOS -> {
            pmProcessExcelDTOS.forEach(pmProcessExcelDTO -> {
                PmProcess pmProcess = new PmProcess();
                BeanUtils.copyProperties(pmProcessExcelDTO, pmProcess);
                processList.add(pmProcess);
            });
        }
        )).sheet().doRead();
        //保存数据
        if (CollUtil.isNotEmpty(processList)) {
            saveBatch(processList);
        }
        return true;
    }


    /**
     * 下载模板
     *
     * @param response
     */
    @Override
    public void download(HttpServletResponse response) throws IOException {
        downloadExcel(response, new ArrayList<>());
    }


    /**
     * 下载物料清单excel
     *
     * @param response
     * @param list
     * @throws IOException
     */
    private void downloadExcel(HttpServletResponse response, List<PmProcessExcelDTO> list) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = URLEncoder.encode("工序信息", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), PmProcessExcelDTO.class).sheet("模板").doWrite(list);
    }

    /**
     * 导出数据
     *
     * @param response
     */
    @Override
    public void excelExportData(HttpServletResponse response) throws IOException {
        downloadExcel(response, getExcelData());
    }


    /**
     * 获取要导出的数据
     * @return
     */
    private List<PmProcessExcelDTO> getExcelData() {
        List<PmProcess> list = list();
        List<PmProcessExcelDTO> pmProcessExcelDTOS = Lists.newArrayList();
        if (CollUtil.isNotEmpty(list)) {
            pmProcessExcelDTOS = BeanUtil.copyToList(list, PmProcessExcelDTO.class);
        }
        return pmProcessExcelDTOS;
    }


}




