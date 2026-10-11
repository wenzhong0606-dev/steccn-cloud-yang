# 全页面UI一致性与纵向可达性审计

日期：2026-10-10。结论：现有页面尚未达到全页面统一及纵向可达验收标准。此次记录问题，不将检查等同于修复。

## 已确认的问题

| 优先级 | 问题 | 证据与影响 | 建议 |
| --- | --- | --- | --- |
| P1 | 低高度表格塌缩 | 1024×600异常、故障、台账等表格0px，部分列表6～14px；内部滚动存在但有效视口不可用 | 列表定义最小有效高度；高度不足切单一main滚动，收起指标/辅助说明 |
| P1 | 页面类型猜测误判 | ui.js对main直下含table-wrapper的card自动加business-list-page；待验收为多卡详情，不能套单列表flex结构 | 显式声明list/detail/workbench/archive，移除启发式判断 |
| P2 | 移动筛选占位过大 | list-filters在390×844高206px；problem-toolbar88～128px；同类列表不一致 | 核心搜索同行、其他条件统一高级筛选；保留标签与读屏名称 |
| P2 | 标题字号不一致 | 浏览器计算标题22、24、26px，字重500；源码有title/work-heading/archive-pagehead和眉标混用 | 页面标题22px统一组件，设备摘要不代替页面标题 |
| P2 | 容器语义与标题不统一 | 问题列表、旧list-card通常无h2；详情card有标题；容器直接class名及首尾结构不同 | 列表容器设列表标题/结果数/工具栏，布局包装不重复标题 |
| P2 | 状态表达粗糙 | badge颜色以状态正则匹配决定，部分技术完成/复核状态落为secondary | 状态键按业务配置明确映射，不依赖中文文案 |
| P2 | CSS与DOM治理边界混杂 | ui.js扫描并改类/移动按钮/推断布局；原页面样式和全页面统一层并存 | 页面显式HTML组件槽位，公共JS仅管理局部交互 |
| P2 | 移动树挤压列表 | 390×844台账表格视口85px，树固定200px | 树采用抽屉或可调整导航模式，避免同时挤压两个固定区 |

## 逐页检查矩阵

静态审计25个源HTML；浏览器25入口×3尺寸=75次。入口defect按现有逻辑跳转异常中心，动态表格检查时可能仍为加载状态，不能据此推断业务末条已可达。

| 源页面 | 筛选结构 | 静态表格数 | 可见表格视口最小值(px) |
| --- | --- | ---: | ---: |
| abnormal-events | problem-toolbar | 1 | 0 |
| acceptance | field/form | 2 | 11 |
| asset-files | field/form | 0 | 无表格或隐藏/加载 |
| catalog | field/form | 0 | 无表格或隐藏/加载 |
| check-list | list-filters | 1 | 6 |
| defect-list | problem-toolbar | 1 | 14 |
| defect | field/form | 0 | 无表格或隐藏/加载 |
| device | field/form | 0 | 无表格或隐藏/加载 |
| equipment-templates | ledger-toolbar | 1 | 104 |
| equipment | ledger-toolbar | 1 | 0 |
| failure-events | problem-toolbar | 1 | 0 |
| functional-locations | ledger-toolbar | 1 | 104 |
| hazards | problem-toolbar | 1 | 14 |
| history | field/form | 0 | 无表格或隐藏/加载 |
| inspection-list | list-filters | 1 | 6 |
| inspection | field/form | 0 | 无表格或隐藏/加载 |
| order-list | problem-toolbar | 1 | 14 |
| order | field/form | 1 | 无表格或隐藏/加载 |
| plan-list | list-filters | 1 | 6 |
| plans | field/form | 0 | 无表格或隐藏/加载 |
| platform | field/form | 0 | 无表格或隐藏/加载 |
| route-list | list-filters | 1 | 6 |
| standard-list | list-filters | 1 | 6 |
| standards | field/form | 0 | 无表格或隐藏/加载 |
| workbench | field/form | 0 | 无表格或隐藏/加载 |

## 建议标准组件

1. PageHeader：面包屑、标题、说明、主操作；说明及眉标按需显示。
2. BusinessPanel：固定标题栏、正文插槽、可选固定页脚；明确list/form/summary模式。
3. FilterBar：统一搜索input-group、核心条件、高级筛选、清空、已选条件；明确即时筛选与查询模式。
4. DataTable：Bootstrap原生表格、列宽上限、主副标题分行截断、排序/空态/错误/加载、操作菜单。
5. StatusBadge：业务键到颜色/名称的显式配置；不能用文案正则替代业务映射。
6. Pagination：结果数、分页、页大小及加载禁用；业务列表共用。
7. ModalForm：固定首尾、正文滚动、错误与焦点、关闭滚动恢复。
8. AdaptiveWorkspace：有高度预算与退化规则；按页面模式复用，不扫描猜测。

组件样本见prototype/components/标准组件.html。样本是静态HTML结构契约，保持独立页面，通过复制标准槽位及公共Bootstrap类复用；没有虚构业务接口。现有导航/主题是已复用组件，业务容器尚需按上表逐页接入，不能宣称已完成重构。

## 证据边界与下一步

保存三份尺寸JSON及结构清单。1920/2K、125%/150%真实浏览器缩放、移动横屏、软键盘、所有角色和全部弹窗末项/提交按钮可点击尚未验收。视口等效缩小不能替代真实缩放。应先修P1，再逐页按组件接入，最后执行滚到底部及关键操作可达的验收。此次不修改业务表、字段或API，原82项业务测试结果不证明视觉可达性。