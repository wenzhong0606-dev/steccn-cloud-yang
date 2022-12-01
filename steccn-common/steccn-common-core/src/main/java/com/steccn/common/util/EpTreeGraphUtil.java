package com.steccn.common.util;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.steccn.common.dto.ParentTupleDTO;
import com.steccn.common.vo.EpTreeGraphDataVO;
import com.steccn.common.vo.EpTreeGraphLinkVO;
import com.steccn.common.vo.EpTreeGraphVO;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 能源流向等树图
 * @desc yequan
 */
@Slf4j
public class EpTreeGraphUtil {

    /**
     * 转换对象
     *
     * @param root 根节点
     * @return 树图
     */
    public static EpTreeGraphVO convert(ParentTupleDTO<String, BigDecimal> root) {
        EpTreeGraphVO vo = new EpTreeGraphVO();
        List<EpTreeGraphDataVO> data = new ArrayList<>();
        data.add(new EpTreeGraphDataVO(root.getField1()));
        List<EpTreeGraphLinkVO> links = Lists.newArrayList();
        cycle(root, data, links);
        vo.setData(data);
        vo.setLinks(links);
        return vo;
    }

    /**
     * 递归解析name和links
     *
     * @param parent 父节点
     * @param data   节点数据
     * @param links  关系数据
     */
    private static void cycle(ParentTupleDTO<String, BigDecimal> parent, List<EpTreeGraphDataVO> data, List<EpTreeGraphLinkVO> links) {
        List<ParentTupleDTO<String, BigDecimal>> children = parent.getChildren();
        if (CollUtil.isNotEmpty(children)) {
            for (ParentTupleDTO<String, BigDecimal> child : children) {
                //收集name和流向关系
                data.add(new EpTreeGraphDataVO(child.getField1()));
                links.add(new EpTreeGraphLinkVO(parent.getField1(), child.getField1(), child.getField2()));
                //递归遍历
                cycle(child, data, links);
            }
        }
    }

//    /**
//     * 生成数据
//     *
//     * @return
//     */
//    public static ParentTupleDTO<String, BigDecimal> generateData() {
//        ParentTupleDTO<String, BigDecimal> root = new ParentTupleDTO<>("桐城新能源", BigDecimal.valueOf(100));
//        ParentTupleDTO<String, BigDecimal> one1 = new ParentTupleDTO<>("深加工制造部", BigDecimal.valueOf(70));
//        ParentTupleDTO<String, BigDecimal> one2 = new ParentTupleDTO<>("基片制造部", BigDecimal.valueOf(30));
//
//        ParentTupleDTO<String, BigDecimal> two1 = new ParentTupleDTO<>("深加工一车间", BigDecimal.valueOf(30));
//        ParentTupleDTO<String, BigDecimal> two2 = new ParentTupleDTO<>("深加工二车间", BigDecimal.valueOf(15));
//        ParentTupleDTO<String, BigDecimal> two3 = new ParentTupleDTO<>("深加工三车间", BigDecimal.valueOf(25));
//
//        ParentTupleDTO<String, BigDecimal> two4 = new ParentTupleDTO<>("原料车间", BigDecimal.valueOf(10));
//        ParentTupleDTO<String, BigDecimal> two5 = new ParentTupleDTO<>("基片一车间", BigDecimal.valueOf(5));
//        ParentTupleDTO<String, BigDecimal> two6 = new ParentTupleDTO<>("基片二车间", BigDecimal.valueOf(15));
//
//        ParentTupleDTO<String, BigDecimal> three1 = new ParentTupleDTO<>("深加工一车间一线", BigDecimal.valueOf(15));
//        ParentTupleDTO<String, BigDecimal> three2 = new ParentTupleDTO<>("深加工一车间二线", BigDecimal.valueOf(15));
//
//        root.setChildren(Lists.newArrayList(one1, one2));
//        one1.setChildren(Lists.newArrayList(two1, two2, two3));
//        one2.setChildren(Lists.newArrayList(two4, two5, two6));
//        two1.setChildren(Lists.newArrayList(three1, three2));
//        return root;
//    }
//
//
//    public static void main(String[] args) {
//        EpTreeGraphVO convert = convert(new ParentTupleDTO<>("桐城新能源", BigDecimal.valueOf(100)));
//        String s = JSONUtil.toJsonStr(convert);
//        log.info("{}",  s);
//    }
}
