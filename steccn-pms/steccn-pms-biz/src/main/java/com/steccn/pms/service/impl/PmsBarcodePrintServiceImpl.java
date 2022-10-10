package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.common.constant.MpConstant;
import com.steccn.pms.domain.PmsBarcodePrint;
import com.steccn.pms.dto.BarcodePrintDTO;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.mapper.PmsBarcodePrintMapper;
import com.steccn.pms.util.PmsCodeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【pms_barcode_print(条码打印)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsBarcodePrintServiceImpl extends ServiceImpl<PmsBarcodePrintMapper, PmsBarcodePrint>
        implements PmsBarcodePrintService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean print(BarcodePrintDTO dto) {
        Integer printStrip = dto.getPrintStrip();
        List<PmsBarcodePrint> modelList = new ArrayList<>(printStrip);

        //获取当前订单最大的
        long currentMaxNo = orderMaxCaseNo(dto.getProduceOrderId());
        //遍历打印条数
        for (int count = 0; count < printStrip; count++) {
            PmsBarcodePrint printModel = new PmsBarcodePrint();
            BeanUtils.copyProperties(dto, printModel);
            printModel.setCaseNumber(dto.getProduceOrderNo());
            printModel.setCaseNumber(PmsCodeUtil.getNextCode(dto.getProduceOrderNo() + PmsCodeUtil.SHORT_LINE, PmsCodeUtil.NUM_BIT_4, currentMaxNo++));
            modelList.add(printModel);
        }
        saveBatch(modelList);
        return Boolean.TRUE;
    }

    /**
     * 生产订单最大箱号
     *
     * @param produceOrderId 生产订单id
     * @return 最大箱号
     */
    private long orderMaxCaseNo(Long produceOrderId) {
        PmsBarcodePrint one = getOne(new LambdaQueryWrapper<PmsBarcodePrint>()
                .eq(PmsBarcodePrint::getProduceOrderId, produceOrderId)
                .orderByDesc(PmsBarcodePrint::getCaseNumber)
                .last(MpConstant.STR_LIMIT_1));
        long currentMax = PmsCodeUtil.DEFAULT_PADDING;
        if (Objects.nonNull(one)) {
            String substring = one.getCaseNumber().substring(one.getCaseNumber().length() - PmsCodeUtil.NUM_BIT_4);
            try {
                currentMax = Long.parseLong(substring);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }

}




