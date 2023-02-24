package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.domain.QtCheckItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtCheckItemSaveDTO;
import com.steccn.qms.dto.QtCheckItemPageDTO;
import com.steccn.qms.dto.QtItemStatusDTO;
import com.steccn.qms.vo.QtCheckItemListVO;
import com.steccn.qms.vo.QtRangeArrayVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【qt_check_item(检验项表)】的数据库操作Service
 * @createDate 2022-09-02 08:54:09
 */
public interface QtCheckItemService extends IService<QtCheckItem> {

    /**
     * 检验项分页列表
     *
     * @param dto 筛选条件，排序条件
     * @return 列表
     */
    Page<QtCheckItemListVO> pageList(QtCheckItemPageDTO dto);

    /**
     * 新建检验项
     *
     * @param dto
     * @return
     */
    Boolean add(QtCheckItemSaveDTO dto);

    /**
     * 根据id获取详情
     *
     * @param id 主键id
     * @return
     */
    QtCheckItemListVO info(Integer id);

    /**
     * 编辑检验项
     *
     * @param dto dto
     * @return 是否成功
     */
    Boolean edit(QtCheckItemSaveDTO dto);

    /**
     * 停用启用
     *
     * @param dto dto
     * @return 是否成功
     */
    Boolean status(QtItemStatusDTO dto);

    /**
     * 删除检验项
     *
     * @param id 主键id
     * @return 是否
     */
    Boolean delete(Integer id);

    /**
     * 启用状态的下拉选项
     *
     * @return
     */
    List<QtCheckItemListVO> selectItemList();

    /**
     * 检验项快照
     *
     * @param checkIdList 检验项id集合
     * @return map key为旧检验项id value为新检验项id
     */
    Map<Integer, Integer> checkItemSnapshot(List<Integer> checkIdList);

    /**
     * 检验项快照集合
     * @param checkItemIdList
     * @return
     */
    Map<Integer, Set<Integer>> checkItemSnapshotSet(List<Integer> checkItemIdList);

    R<List<SysOfficeVO>> selectStationList(List<String> typeList);

    /**
     * 获取检验项
     * @param ids
     * @return
     */
    Map<Integer,QtCheckItem> mapCheckItem(List<Integer> ids);
}
