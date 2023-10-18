package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.TupleDTO;
import com.steccn.qms.domain.QtDefectiveItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtBaseIdNameDTO;
import com.steccn.qms.dto.QtDefectiveItemPageDTO;
import com.steccn.qms.dto.QtDefectiveItemSaveDTO;
import com.steccn.qms.dto.QtItemStatusDTO;
import com.steccn.qms.vo.QtDefectiveItemTreeVo;
import com.steccn.qms.vo.QtDefectiveItemVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【qt_defective_item(缺陷项表)】的数据库操作Service
 * @createDate 2022-09-02 08:54:09
 */
public interface QtDefectiveItemService extends IService<QtDefectiveItem> {

    /**
     * 缺陷项分页列表
     *
     * @param dto dto
     * @return 分页数据
     */
    Page<QtDefectiveItemVO> pageList(QtDefectiveItemPageDTO dto);

    /**
     * 新建缺陷项
     *
     * @param dto dto
     * @return 是否
     */
    Boolean add(QtDefectiveItemSaveDTO dto);

    /**
     * 编辑缺陷项
     *
     * @param dto dto
     * @return 是否成功
     */
    Boolean edit(QtDefectiveItemSaveDTO dto);

    /**
     * 停用启用
     *
     * @param dto dto
     * @return 是否成功
     */
    Boolean status(QtItemStatusDTO dto);

    /**
     * 删除缺陷项
     *
     * @param id id
     * @return 是否成功
     */
    Boolean delete(Integer id);

    /**
     * 启用状态缺陷项列表
     *
     * @return list
     */
    List<QtDefectiveItemTreeVo> selectItemList();

    /**
     * 缺陷项快照
     *
     * @param defectiveItemIdList 检验项id集合
     * @return map key为旧检验项id value为新检验项id
     */
    Map<Integer, Integer> defectiveItemSnapshot(List<Integer> defectiveItemIdList);

    /**
     *
     * @param id
     * @return
     */
    List<QtBaseIdNameDTO> selectItemIds(Integer id);

    /**
     * 根据缺陷项，获取id名字
     * @param defectIdSet 缺陷项id集合
     * @return map
     */
    Map<Integer, String> listByIdCh(Set<Integer> defectIdSet);

    /**
     * 根据缺陷项，获取id对象
     * @param defectIdSet 缺陷项id集合
     * @return map
     */
    Map<Integer, QtDefectiveItem> listById(Set<Integer> defectIdSet);

    /**
     * 根据缺陷项id，查询该缺陷项id的 第一个快照id和类别id
     * @param defectiveCodeSet 缺陷code集合
     * @return map
     */
    Map<String, TupleDTO<Integer, Integer>> firstSnapshotData(Set<String> defectiveCodeSet);

    /**
     * 缺陷快照的所有集合
     * @param defectIdSet 缺陷快照
     * @return map
     */
    Map<Integer,Set<Integer>> defectSnapshot(Set<Integer> defectIdSet);

    /**
     * 获取机器采样的缺陷项 对应的缺陷项id
     * @param defectTypeSet 缺陷名称集合
     * @return map
     */
    Map<String, QtDefectiveItem> collectDefect(Set<String> defectTypeSet);

    /**
     * 根据类别获取缺陷项集合
     * @param categoryName 类别名称
     * @return
     */
    List<QtDefectiveItemVO> getDefectItemByCategory(String categoryName);
}
