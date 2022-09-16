package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
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
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmBillOfMaterial;
import com.steccn.pms.domain.PmCraftRoute;
import com.steccn.pms.domain.PmMaterial;
import com.steccn.pms.dto.PmBillOfMaterialDTO;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmMaterialDTO;
import com.steccn.pms.dto.PmsBillExcelDTO;
import com.steccn.pms.service.PmBillOfMaterialService;
import com.steccn.pms.mapper.PmBillOfMaterialMapper;
import com.steccn.pms.service.PmMaterialService;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.PmBillOfMaterialVO;
import com.steccn.pms.vo.PmMaterialVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pm_bill_of_material(物料清单表)】的数据库操作Service实现
 * @createDate 2022-09-02 17:01:20
 */
@Service
@RequiredArgsConstructor
public class PmBillOfMaterialServiceImpl extends ServiceImpl<PmBillOfMaterialMapper, PmBillOfMaterial>
        implements PmBillOfMaterialService {

    private final PmMaterialService pmMaterialService;

    @Autowired
    private RemoteDictFeignApi remoteDictFeign;

    @Autowired
    private RemoteSpareFeignApi remoteSpareFeignApi;


    /**
     * 新增物料清单信息
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Boolean> add(PmBillOfMaterialDTO dto) {
        PmBillOfMaterial model = new PmBillOfMaterial();
        //拷贝数据，设置创建人时间
        BeanUtil.copyProperties(dto, model);
        model.setCreateBy(SecurityUtils.getUser().getUserId());
        model.setCreateName(SecurityUtils.getUser().getName());
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        //生产编码
        model.setCode(PmsCodeUtil.getNextCode(PmsCodeUtil.PREFIX_B, PmsCodeUtil.NUM_BIT_8, currentMaxCode()));

        //物料版本是否已存在
        if (existVersion(dto.getMaterialId(), dto.getVersion())) {
            throw new BusinessException("物料版本已存在");
        }
        //是否是当前版本的限制
        if (MpConstant.CURRENT_VERSION_YES.equals(dto.getIsCurrentVersion())) {
            //是当前版本，判断物料是否存在是当前版本标识
            PmBillOfMaterial currentVersion = currentVersion(dto.getMaterialId());
            if (Objects.nonNull(currentVersion)) {
                //修改新增数据为当前版本，更改之前数据当前版本标识
                currentVersion.setIsCurrentVersion(MpConstant.CURRENT_VERSION_NOT);
                updateById(currentVersion);
//                throw new BusinessException("版本" + currentVersion.getVersion() + "为当前版本");
            }
        }
        //保存数据
        save(model);
        if (CollUtil.isNotEmpty((dto.getList()))) {
            dto.setId(model.getId());
            return R.success(pmMaterialService.saveMaterial(dto));
        }
        return R.success(true);
    }

    /**
     * 物料版本是否已存在
     *
     * @param materialId 物料id
     * @param version    版本
     * @return
     */
    public Boolean existVersion(Long materialId, String version) {
        if (Objects.isNull(materialId) || StrUtil.isBlank(version)) {
            return Boolean.FALSE;
        }
        long count = count(new LambdaQueryWrapper<PmBillOfMaterial>()
                .eq(PmBillOfMaterial::getMaterialId, materialId)
                .eq(PmBillOfMaterial::getVersion, version));
        return count > 0;
    }

    @Override
    public void download(HttpServletResponse response) throws IOException{
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = URLEncoder.encode("物料清单模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        List<PmsBillExcelDTO> dtoList = new ArrayList<>();
        EasyExcel.write(response.getOutputStream(), PmsBillExcelDTO.class).sheet("模板").doWrite(dtoList);
    }

    /**
     * 物料是否存在当前版本
     *
     * @param materialId 物料id
     * @return 是否
     */
    private PmBillOfMaterial currentVersion(Long materialId) {
        if (Objects.isNull(materialId)) {
            return null;
        }
        PmBillOfMaterial one = getOne(new LambdaQueryWrapper<PmBillOfMaterial>()
                .eq(PmBillOfMaterial::getMaterialId, materialId)
                .eq(PmBillOfMaterial::getIsCurrentVersion, MpConstant.CURRENT_VERSION_YES)
                .last(MpConstant.STR_LIMIT_1));
        return one;
    }

    /**
     * 获取物料清单分页列表
     *
     * @param dto
     * @return
     */
    @Override
    public Page<PmBillOfMaterialVO> pageList(PmBillOfMaterialDTO dto) {
        //1.构建条件
        LambdaQueryWrapper<PmBillOfMaterial> eq = new LambdaQueryWrapper<PmBillOfMaterial>()
                .eq(Objects.nonNull(dto.getMaterialCode()) && !dto.getMaterialCode().equals(""), PmBillOfMaterial::getMaterialCode, dto.getMaterialCode())
                .like(Objects.nonNull(dto.getMaterialName()), PmBillOfMaterial::getMaterialName, dto.getMaterialName())
                .eq(Objects.nonNull(dto.getStatus()), PmBillOfMaterial::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getIsCurrentVersion()), PmBillOfMaterial::getIsCurrentVersion, dto.getIsCurrentVersion())
                .eq(Objects.nonNull(dto.getCode()) && !dto.getCode().equals(""), PmBillOfMaterial::getCode, dto.getCode())
                .eq(Objects.nonNull(dto.getVersion()) && !dto.getVersion().equals(""), PmBillOfMaterial::getVersion, dto.getVersion());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, eq);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<PmBillOfMaterialVO> res = BeanUtil.copyToList(resPage.getRecords(), PmBillOfMaterialVO.class);
        //6.处理数据
        List<Long> ids = Lists.newArrayList();
        res.forEach(billOfMaterial -> {
            ids.add(billOfMaterial.getUnitId());
            LambdaQueryWrapper<PmMaterial> wrapper = new LambdaQueryWrapper<PmMaterial>().eq(PmMaterial::getBomId, billOfMaterial.getId());
            List<PmMaterial> list = pmMaterialService.list(wrapper);
            billOfMaterial.setList(BeanUtil.copyToList(list, PmMaterialVO.class));
            billOfMaterial.getList().forEach(pmMaterialVO -> {
                ids.add(pmMaterialVO.getUnitId());
            });
        });
        //获取字典数据
        R<Map<Long, SysDictVO>> dict = remoteDictFeign.listById(ids);
        //将字典数据处理返回
        res.forEach(pmBillOfMaterialVO -> {
            try {
                pmBillOfMaterialVO.setUnit(dict.getData().get(pmBillOfMaterialVO.getUnitId()).getName());
            } catch (Exception e) {
//                throw new BusinessException(pmBillOfMaterialVO.getMaterialName() +"的单位字典不存在");
            }
            pmBillOfMaterialVO.getList().forEach(pmMaterialVO -> {
                try {
                    pmMaterialVO.setUnit(dict.getData().get(pmMaterialVO.getUnitId()).getName());
                } catch (Exception e) {
//                    throw  new BusinessException(pmMaterialVO.getName() +"的单位字典不存在");
                }
            });
        });
        //7.返回数据
        return page.setRecords(res);
    }

    /**
     * 编辑物料清单
     *
     * @param dto
     * @return
     */
    @Override
    public R<Boolean> edit(PmBillOfMaterialDTO dto) {
        //校验是否存在
        PmBillOfMaterial pmBillOfMaterial = getById(dto.getId());
        if (Objects.isNull(pmBillOfMaterial)) {
            throw new BusinessException("编辑的物料清单不存在");
        }
        //物料版本是否已存在
        if (existVersion(dto.getMaterialId(), dto.getVersion())) {
            throw new BusinessException("物料版本已存在");
        }
        //是否是当前版本的限制
        if (MpConstant.CURRENT_VERSION_YES.equals(dto.getIsCurrentVersion())) {
            //是当前版本，判断物料是否存在是当前版本标识，如果存在则抛异常
            PmBillOfMaterial currentVersion = currentVersion(dto.getMaterialId());
            if (Objects.nonNull(currentVersion)) {
                if (!currentVersion.getId().equals(dto.getId())) {
                    currentVersion.setIsCurrentVersion(MpConstant.CURRENT_VERSION_NOT);
                    updateById(currentVersion);
                }
            }
        }
        PmBillOfMaterial model = new PmBillOfMaterial();
        BeanUtils.copyProperties(dto, model);
        model.setUpdateBy(SecurityUtils.getUser().getUserId());
        model.setUpdateName(SecurityUtils.getUser().getName());
        pmMaterialService.deleteByMaterialId(dto.getId());
        if (CollUtil.isNotEmpty(dto.getList())) {
            Boolean a = pmMaterialService.saveMaterial(dto);
            if (a == false) {
                throw new BusinessException("物料组件数据保存失败");
            }
        }
        //保存数据
        return R.success(updateById(model));
    }

    @Override
    public Boolean delete(Integer id) {
        return removeById(id);
    }

    /**
     * excel 导入
     *
     * @param dto
     * @return
     * @throws IOException
     */
    @Override
//    @Transactional(readOnly = false)
    public Boolean excelImportData(PmExcelDTO dto) throws IOException {
        //文件类型校验
        if (Objects.nonNull(dto.getFile())) {
            //获取文件拓展名
            String fileExtension = dto.getFile().getOriginalFilename().substring(dto.getFile().getOriginalFilename().lastIndexOf("."));
            if (!fileExtension.equals(PmManagerConstant.FILE_EXTENSION_XLSX)) {
                throw new BusinessException("文件类型不支持,请使用模板进行导入");
            }
        }
        //1.定义数据集合
        List<PmBillOfMaterialDTO> pmBillOfMaterialList = Lists.newArrayList();
        List<PmMaterialDTO> pmMaterialList = Lists.newArrayList();
        List<String> spareCodes = Lists.newArrayList();
        List<String> dictCodes = Lists.newArrayList();
        List<String> bomCodes = Lists.newArrayList();
        //2.读取文件信息
        EasyExcel.read(dto.getFile().getInputStream(), PmsBillExcelDTO.class, new PageReadListener<PmsBillExcelDTO>(pmsBillExcelDTOS -> {
            pmsBillExcelDTOS.forEach(
                    pmsBillExcelDTO -> {
                        //获取主数据
                        PmBillOfMaterialDTO pmBillOfMaterialDTO = new PmBillOfMaterialDTO();
                        BeanUtils.copyProperties(pmsBillExcelDTO, pmBillOfMaterialDTO);
                        pmBillOfMaterialList.add(pmBillOfMaterialDTO);
                        spareCodes.add(pmsBillExcelDTO.getMaterialCode());
                        bomCodes.add(pmBillOfMaterialDTO.getCode());
                        //物料版本是否已存在
                        if (existVersion(pmBillOfMaterialDTO.getMaterialId(), pmBillOfMaterialDTO.getVersion())) {
                            throw new BusinessException(pmBillOfMaterialDTO.getCode() + "的物料版本已存在,请检查修改");
                        }
                        //是否是当前版本的限制
                        if (MpConstant.CURRENT_VERSION_YES.equals(pmBillOfMaterialDTO.getIsCurrentVersion())) {
                            //是当前版本，判断物料是否存在是当前版本标识，如果存在则抛异常
                            PmBillOfMaterial currentVersion = currentVersion(pmBillOfMaterialDTO.getMaterialId());
                            if (Objects.nonNull(currentVersion)) {
                                throw new BusinessException(pmBillOfMaterialDTO.getMaterialCode() + "的当前版本有存在版本,请检查修改");
                            }
                        }
                        //获取从数据
                        PmMaterialDTO pmMaterialDTO = new PmMaterialDTO();
                        BeanUtils.copyProperties(pmsBillExcelDTO, pmMaterialDTO);
                        pmMaterialDTO.setBomCode(pmBillOfMaterialDTO.getCode());
                        pmMaterialDTO.setCode(pmsBillExcelDTO.getModuleCode());
                        spareCodes.add(pmsBillExcelDTO.getModuleCode());
                        pmMaterialList.add(pmMaterialDTO);
                        dictCodes.add(pmsBillExcelDTO.getUnit());
                    }
            );
        })).sheet().doRead();
        //3.处理数据
        //3.1根据物料编码查询物料信息
        R<Map<String, EqpSpareVO>> spareMap = remoteSpareFeignApi.listByCode(spareCodes);
        //3.2根据字典编码查询字典信息
        SysDictDTO sysDictDTO = new SysDictDTO();
        sysDictDTO.setCodeList(dictCodes);
        sysDictDTO.setType(PmManagerConstant.MEASURE_UNIT);
        R<Map<String, SysDictVO>> dictMap = remoteDictFeign.listByCode(sysDictDTO);
        //3.3处理主数据
        ArrayList<PmBillOfMaterialDTO> pmBillOfMaterials = pmBillOfMaterialList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(
                        Comparator.comparing(
                                PmBillOfMaterialDTO::getMaterialCode))), ArrayList::new));
        pmBillOfMaterials.forEach(
                pmBillOfMaterialDTO -> {
                    //根据物料的id获取物料的名称和id
                    EqpSpareVO eqpSpareVO = spareMap.getData().get(pmBillOfMaterialDTO.getMaterialCode());
                    if (Objects.isNull(eqpSpareVO)) {
                        throw new BusinessException("主物料不存在,请检查编码是否正确:" + pmBillOfMaterialDTO.getMaterialCode());
                    }
                    pmBillOfMaterialDTO.setMaterialName(eqpSpareVO.getName());
                    pmBillOfMaterialDTO.setMaterialId(eqpSpareVO.getId());
                });
        //保存主数据
        List<PmBillOfMaterial> billOfMaterials = BeanUtil.copyToList(pmBillOfMaterials, PmBillOfMaterial.class);
        saveBatch(billOfMaterials);
        //3.3处理从数据
        //根据code查询主数据的id
        Map<String, Long> ids = listByCodes(bomCodes);
        pmMaterialList.forEach(
                pmMaterialDTO -> {
                    pmMaterialDTO.setBomId(ids.get(pmMaterialDTO.getBomCode()));
                    pmMaterialDTO.setUnitId(dictMap.getData().get(pmMaterialDTO.getUnit()).getId());
                    if (Objects.isNull(spareMap.getData().get(pmMaterialDTO.getCode()))) {
                        throw new BusinessException("组件物料不存在,请检查编码是否正确:" + pmMaterialDTO.getCode());
                    }
                    pmMaterialDTO.setMaterialId(spareMap.getData().get(pmMaterialDTO.getCode()).getId());
                    pmMaterialDTO.setName(spareMap.getData().get(pmMaterialDTO.getCode()).getName());
                }
        );
        //保存从数据
        List<PmMaterial> pmMaterials = BeanUtil.copyToList(pmMaterialList, PmMaterial.class);
        if (CollUtil.isNotEmpty(pmMaterials)) {
            pmMaterialService.saveBatch(pmMaterials);
        }
        return true;
        /**
         *
         //读取文件
         EasyExcel.read(dto.getFile().getInputStream(), PmsBillExcelDTO.class, new PageReadListener<PmsBillExcelDTO>(pmsBillExcelDTOS -> {
         pmsBillExcelDTOS.forEach(pmsBillExcelDTO -> {
         //处理主数据
         PmBillOfMaterial pmBillOfMaterial = new PmBillOfMaterial();
         BeanUtils.copyProperties(pmsBillExcelDTO, pmBillOfMaterial);
         pmBillOfMaterial.setCode(PmsCodeUtil.getNextCode(PmsCodeUtil.PREFIX_B, PmsCodeUtil.NUM_BIT_8, currentMaxCode()));
         //根据物料的编码查询物料的信息
         R<EqpSpareVO> eqpSpareVOR = remoteSpareFeignApi.getByCode(pmsBillExcelDTO.getMaterialCode());
         if (Objects.isNull(eqpSpareVOR.getData())) {
         throw new BusinessException("主物料不存在,请检查编码是否正确:" + pmsBillExcelDTO.getMaterialCode());
         }
         pmBillOfMaterial.setMaterialId(eqpSpareVOR.getData().getId());
         pmBillOfMaterial.setMaterialName(eqpSpareVOR.getData().getName());
         if (!save(pmBillOfMaterial)) {
         throw new BusinessException("编码为" + pmBillOfMaterial.getCode() + "数据异常");
         }
         //处理从数据 根据组件编码查询组件的数据
         PmMaterial pmMaterial = new PmMaterial();
         BeanUtils.copyProperties(pmsBillExcelDTO, pmMaterial);
         pmMaterial.setBomId(pmBillOfMaterial.getId());
         pmMaterial.setCode(pmsBillExcelDTO.getModuleCode());
         //查询单位数据
         R<SysDictVO> unit = remoteDictFeign.getByCode(pmsBillExcelDTO.getUnit(), PmManagerConstant.MEASURE_UNIT);
         if (Objects.nonNull(unit.getData())) {
         pmMaterial.setUnitId(unit.getData().getId());
         } else {
         throw new BusinessException("单位数据不存在,请检查单位是否正确:从数据组件编码：" + pmsBillExcelDTO.getModuleCode());
         }
         R<EqpSpareVO> spare = remoteSpareFeignApi.getByCode(pmsBillExcelDTO.getModuleCode());
         if (Objects.nonNull(spare.getData())) {
         pmMaterial.setMaterialId(spare.getData().getId());
         pmMaterial.setName(spare.getData().getName());
         } else {
         throw new BusinessException("主物料不存在,请检查编码是否正确:" + pmsBillExcelDTO.getModuleCode());
         }
         if (!pmMaterialService.save(pmMaterial)) {
         throw new BusinessException("编码为" + pmBillOfMaterial.getCode() + "数据异常");
         }
         });
         })).sheet().doRead();
         return true;
         */
    }

    /**
     * excel数据导出
     *
     * @param response
     * @throws IOException
     */
    @Override
    public void excelExportData(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = URLEncoder.encode("物料清单", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), PmsBillExcelDTO.class).sheet("模板").doWrite(getExcelData());
    }

    /**
     * 获取excel数据
     *
     * @return
     */
    private List<PmsBillExcelDTO> getExcelData() {
        //定义返回数据集合
        List<PmsBillExcelDTO> excelDTOList = Lists.newArrayList();
        //获取数据
        List<PmBillOfMaterial> pmBillOfMaterialList = list();
        List<PmMaterial> pmMaterialList = pmMaterialService.list();
        List<Long> dictIds = Lists.newArrayList();
        //处理数据
        pmBillOfMaterialList.forEach(pmBillOfMaterial -> {
                    PmsBillExcelDTO pmsBillExcelDTO = new PmsBillExcelDTO();
                    BeanUtils.copyProperties(pmBillOfMaterial, pmsBillExcelDTO);
                    pmMaterialList.forEach(
                            pmMaterial -> {
                                if (pmMaterial.getBomId().equals(pmBillOfMaterial.getId())) {
                                    BeanUtils.copyProperties(pmMaterial, pmsBillExcelDTO);
                                    pmsBillExcelDTO.setModuleCode(pmMaterial.getCode());
                                    pmsBillExcelDTO.setModuleQuantity(pmMaterial.getQuantity());
                                    dictIds.add(pmMaterial.getUnitId());
                                    pmsBillExcelDTO.setUnitId(pmMaterial.getUnitId());
                                }
                            }
                    );
                    excelDTOList.add(pmsBillExcelDTO);
                }
        );
        //获取字典数据
        R<Map<Long, SysDictVO>> mapR = remoteDictFeign.listById(dictIds);

        excelDTOList.forEach(pmsBillExcelDTO -> {
            if (Objects.nonNull(mapR.getData().get(pmsBillExcelDTO.getUnitId()))) {
                pmsBillExcelDTO.setUnit(mapR.getData().get(pmsBillExcelDTO.getUnitId()).getName());
            }else {
                pmsBillExcelDTO.setUnit(null);
            }
        });
        return excelDTOList;
    }


    /**
     * 根据物料code获取当前物料下的物料清单版本
     *
     * @param code
     * @return
     */
    @Override
    public R<List<String>> getVersion(String code) {
        LambdaQueryWrapper<PmBillOfMaterial> eq = new LambdaQueryWrapper<PmBillOfMaterial>().eq(PmBillOfMaterial::getMaterialCode, code);
        List<PmBillOfMaterial> list = list(eq);
        List<String> versions = Lists.newArrayList();
        if (CollUtil.isEmpty(list)) {
            return R.success(versions);
        }
        for (PmBillOfMaterial pmBillOfMaterial : list) {
            versions.add(pmBillOfMaterial.getVersion());
        }
        return R.success(versions);
    }


    /**
     * 根据code查询id
     *
     * @param codeList
     * @return
     */
    private Map<String, Long> listByCodes(List<String> codeList) {
        Map<String, Long> map = new HashMap<>();
        List<PmBillOfMaterial> pmBillOfMaterials = baseMapper.listByCodes(codeList);
        if (CollUtil.isEmpty(pmBillOfMaterials)) {
            return map;
        }
        for (PmBillOfMaterial pmBillOfMaterial : pmBillOfMaterials) {
            map.put(pmBillOfMaterial.getCode(), pmBillOfMaterial.getId());
        }
        return map;
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
        PmBillOfMaterial maxCodeModel = getOne(new LambdaQueryWrapper<PmBillOfMaterial>().orderByDesc(PmBillOfMaterial::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(PmsCodeUtil.PREFIX_B.length());
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




