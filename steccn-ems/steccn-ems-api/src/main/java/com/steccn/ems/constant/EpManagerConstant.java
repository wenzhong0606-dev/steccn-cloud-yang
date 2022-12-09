package com.steccn.ems.constant;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

public interface EpManagerConstant {
    //产品单耗排序字段
    String  EP_ENERGY_TYPE_SORT_FIELD_STANDARD_NAME = "name";
    String  EP_ENERGY_TYPE_SORT_FIELD_ENERGY_TYPE = "energyTypeName";
    String  EP_ENERGY_TYPE_SORT_FIELD_STANDARD_TYPE="standardTypeName";
    String  EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_TYPE ="productTypeName";
    String  EP_ENERGY_TYPE_SORT_FIELD_PRODUCT_THICKNESS  ="productThickName" ;
    String  EP_ENERGY_TYPE_SORT_FIELD_UNIT_CONSUMPTION_STANDARD = "unitValue";
    String  EP_ENERGY_TYPE_SORT_FIELD_INDATE  = "validStart";
    //仪表名称排序字段
    String EP_INSTRUMENT_ACCOUNT_SORT_FIELD_DISTANT_MAPCODE = "distantMapCode";
    String EP_INSTRUMENT_ACCOUNT_SORT_FIELD_METER_NAME = "meterName";
    String EP_INSTRUMENT_ACCOUNT_SORT_FIELD_STATUS = "status";
    String EP_INSTRUMENT_ACCOUNT_SORT_METER_READING_TIME = "meterReadingTime";

    String EP_UNIT_CONSUME_ALARM_SORT_ALARM_DATE = "alarmDate";
    String EP_UNIT_CONSUME_ALARM_SORT_ENERGY_TYPE = "energyType";
    String EP_UNIT_CONSUME_ALARM_SORT_OFFICE_ID = "officeId";
    String EP_UNIT_CONSUME_ALARM_SORT_ALARM_TYPE = "alarmType";

    //下载模版名称
    String EP_DOWNLOAD_MODEL_NAME_FGPDB = "fgpdb.xlsx";


    /**
     * 产品单耗标准的产品类型
     */
    @Getter
    @AllArgsConstructor
    enum EpProductTypes{
        SUBSTRATE(0L,"基片"),
        DEEP_PROCESSING(1L,"深加工");
        private Long id;
        private String name;
        //根据code查找值
        public static EpProductTypes getProductType(Long id) {
            for (EpProductTypes value : EpProductTypes.values()) {
                if (value.getId().equals(id)){
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 关联图表
     */
    @Getter
    @AllArgsConstructor
    enum EpEnergyTypes {
        ENERGY_PARTITION(0, "能耗分区对比"),
        ENERGY_HISTORY(1, "能耗历史对比"),
        CONSUMPTION_PARTITION(2, "单耗分区对比"),
        CONSUMPTION_SHIFT(3, "单耗历史对比"),
        ENERGY_FLOW(4, "能源流向"),
        CONSUMPTION_HISTORY(5, "单耗班组对比"),
        PEAK_PARTITION(6, "峰谷平分区对比"),
        PEAK_HISTORY(7, "峰谷平历史对比");

        private Integer code;
        private String name;
        //根据code查找值
        public static EpEnergyTypes getByCode(Integer code) {
            for (EpEnergyTypes value : EpEnergyTypes.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }

        //获取code的集合  新建或编辑时必须在这里面选
        public static Set<Integer> codeSet() {
            Set<Integer> set = Sets.newHashSet();
            for (EpEnergyTypes value : EpEnergyTypes.values()) {
                set.add(value.getCode());
            }
            return set;
        }
    }

    /**
     * 能耗分区
     */
    @Getter
    @AllArgsConstructor
    enum EnergyPartitioningTree {
        POWER_CONSUMPTION_PARTITION(1,"电耗分区"),
        WATER_CONSUMPTION_PARTITION(2,"水耗分区"),
        GAS_CONSUMPTION_PARTITION(3,"气耗分区"),
        FUEL_CONSUMPTION_PARTITION(4,"油耗分区"),
        GAS_FUEL_CONSUMPTION_PARTITION(5,"气耗/油耗分区"),
        ENERGY_PARTITION(6,"能耗分区");


        private Integer energyType;
        private String name;

        //根据energyType查找能耗分区
        public static EnergyPartitioningTree getByEnergyType(Integer energyType) {
            for (EnergyPartitioningTree value : EnergyPartitioningTree.values()) {
                if (value.energyType.equals(energyType)) {
                    return value;
                }
            }
            return ENERGY_PARTITION;
        }
    }

//    /**
//     * 时间粒度-天、周、月、无粒度
//     */
//    @Getter
//    @AllArgsConstructor
//    enum EpGranularity {
//        DAY(0, "天"),
//        WEEK(1, "周"),
//        MONTH(2, "月"),
//        NONE(3, "无粒度");
//        private Integer code;
//        private String name;
//
//        /**
//         * 根据
//         *
//         * @param code
//         * @return
//         */
//        public static EpGranularity getProductType(Long code) {
//            for (EpGranularity value : EpGranularity.values()) {
//                if (value.getCode().equals(code)) {
//                    return value;
//                }
//            }
//            return NONE;
//        }
//    }

    /**
     * 仪表类型
     */
    @Getter
    @AllArgsConstructor
    enum InstrumentType {
        //远传仪表
        REMOTE(0, "实体远传仪表"),
        //非远传仪表
        NON_REMOTE(1, "实体非远传仪表"),
        //虚拟仪表
        VIRTUAL(2, "虚拟仪表");
        private Integer code;
        private String name;

        /**
         * 根据
         *
         * @param code
         * @return
         */
        public static InstrumentType getInstrumentType(Integer code) {
            for (InstrumentType value : InstrumentType.values()) {
                if (value.getCode().equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    @Getter
    @AllArgsConstructor
    enum OperationType {
        //移动端上传
        APP(0, "移动端上传"),
        //电脑端上传
        PC(1, "电脑端上传"),
        //管理员修改
        ADMIN(2, "管理员修改");
        private final Integer code;
        private final String name;

        /**
         * 根据
         *
         * @param code
         * @return
         */
        public static String getNameByCode(Integer code) {
            for (OperationType value : OperationType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getName();
                }
            }
            return null;
        }
    }


    /**
     * 峰 平 谷
     */
    @Getter
    @AllArgsConstructor
    enum ElectricType {
        //峰
        PEAK(1, "峰"),
        //平
        FLAT(2, "平"),
        //谷
        VALLEY(3, "谷");
        private final Integer code;
        private final String name;

        /**
         * 根据
         *
         * @param code
         * @return
         */
        public static String getNameByCode(Integer code) {
            for (ElectricType value : ElectricType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getName();
                }
            }
            return null;
        }
    }

    @Getter
    @AllArgsConstructor
    enum EnergyPriceType {
        //峰时段
        PEAK_PERIOD(1, "峰时段"),
        //平时段
        USUALLY_PERIOD(2, "平时段"),
        //谷时段
        VALLEY_PERIOD(3, "谷时段");
        private final Integer code;
        private final String name;

        /**
         * 根据code获取名称
         *
         * @param code
         * @return
         */
        public static String getNameByCode(Integer code) {
            for (OperationType value : OperationType.values()) {
                if (value.getCode().equals(code)) {
                    return value.getName();
                }
            }
            return null;
        }
    }
}
