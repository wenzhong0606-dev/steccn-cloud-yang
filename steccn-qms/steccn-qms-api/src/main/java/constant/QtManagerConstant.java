package constant;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Set;

/**
 * @desc 质量管理的常量接口
 */
public interface QtManagerConstant {

    /**
     * @desc 检验项 | 缺陷项类别枚举
     */
    @Getter
    @AllArgsConstructor
    enum QtItemCategoryEnum {
        /**
         * 检验项
         */
        CHECK(0, "检验项"),
        /**
         * 缺陷项
         */
        DEFECTIVE(1, "缺陷项");

        private Integer type;
        private String name;

        /**
         * 所有类别
         *
         * @return 一个set集合【底层是hashmap组成，检索效率为o(1)】
         */
        public static Set<Integer> categoryAll() {
            Set<Integer> categorySet = Sets.newHashSet();
            for (QtItemCategoryEnum ele : QtItemCategoryEnum.values()) {
                categorySet.add(ele.getType());
            }
            return categorySet;
        }

        /**
         * 获取类别-可能会返回空
         *
         * @param type 类别id
         * @return 类别元素
         */
        public static QtItemCategoryEnum getEleByType(Integer type) {
            for (QtItemCategoryEnum ele : QtItemCategoryEnum.values()) {
                if (ele.type.equals(type)) {
                    return ele;
                }
            }
            return null;
        }
    }
}
