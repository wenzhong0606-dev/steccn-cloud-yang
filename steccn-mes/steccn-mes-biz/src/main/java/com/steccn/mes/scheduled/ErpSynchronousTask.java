package com.steccn.mes.scheduled;

import com.steccn.mes.dto.ErpCustomerDTO;
import com.steccn.mes.service.JsSysOfficeService;
import com.steccn.mes.service.MesEqpSpareService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * erp同步数据
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ErpSynchronousTask {
    private final JsSysOfficeService jsSysOfficeService;
    private final MesEqpSpareService mesEqpSpareService;
    /**
     * @desc 每天 2点同步昨天数据
     *
     */
    @XxlJob("synchronousErpCustomer")
    public void synchronousErpCustomer() {
        XxlJobHelper.log("同步客户-任务开始");
        LocalDateTime now = LocalDateTime.now();
        ErpCustomerDTO dto = new ErpCustomerDTO();
        dto.setStopfmodifydate(now.minusDays(1).withHour(0).withMinute(0).withSecond(0));
        dto.setStopfmodifydate(now.minusDays(1).withHour(23).withMinute(59).withSecond(59));
        jsSysOfficeService.synchronousCustomer(dto);
        XxlJobHelper.log("同步客户-任务结束");
    }

    /**
     * @desc 每天 2点10分同步昨天数据
     *
     */
    @XxlJob("synchronousErpMaterial")
    public void synchronousErpMaterial() {
        XxlJobHelper.log("同步物料-任务开始");
        LocalDateTime now = LocalDateTime.now();
        ErpCustomerDTO dto = new ErpCustomerDTO();
        dto.setStopfmodifydate(now.minusDays(1).withHour(0).withMinute(0).withSecond(0));
        dto.setStopfmodifydate(now.minusDays(1).withHour(23).withMinute(59).withSecond(59));
        mesEqpSpareService.synchronousSpare(dto);
        XxlJobHelper.log("同步物料-任务结束");
    }

    /**
     * @desc 每天 2点20分同步昨天数据
     *
     */
    @XxlJob("synchronousErpSupplier")
    public void synchronousErpSupplier() {
        XxlJobHelper.log("同步供应商-任务开始");
        LocalDateTime now = LocalDateTime.now();
        ErpCustomerDTO dto = new ErpCustomerDTO();
        dto.setStopfmodifydate(now.minusDays(1).withHour(0).withMinute(0).withSecond(0));
        dto.setStopfmodifydate(now.minusDays(1).withHour(23).withMinute(59).withSecond(59));
        jsSysOfficeService.synchronousSupplier(dto);
        XxlJobHelper.log("同步供应商-任务结束");
    }
}
