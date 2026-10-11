# Bootstrap 原生设备列表调整记录

日期：2026-10-10。依据：使用Bootstrap原生效果重做台账设备列表。

设备table改为table/table-hover/align-middle/mb-0；thead使用sticky-top/table-light，表头与单元格通过px-3/py-2/py-3提供间距。生命周期与健康显示badge/text-bg-*，直接操作使用btn/btn-link/btn-sm，操作组使用d-flex/flex-nowrap/gap-1，编码通过small及text-body-secondary分层。移除ledger-table专属视觉规则，不再挂data-table，避免公共自定义表格冻结列、卡片化与密度规则影响。

外层继续保留已确认的应用容器高度分配、标题与分页固定以及内部滚动。这些是布局规则，不用于定制表格视觉。原有更多菜单和动作授权沿用，未新增Bootstrap JS依赖。移动使用原生响应式横向表格，实测表格896px、容器322px，无文档横向溢出。

82项模拟业务测试通过。无新增候选字段或接口，原数据和事件继续沿用。截图：Bootstrap原生设备列表-PC.png、Bootstrap原生设备列表-移动端.png。


补充：主副标题组合（名称/编码、位置/型号）保留分层及自然换行；其他单元格text-nowrap。制造商上限160px、责任人100px，使用Bootstrap text-truncate并通过title展示完整值。宽度作为展示配置设置于节点，不增加表格自定义CSS。浏览器计算样式确认主副标题normal，其余nowrap；脚本语法检查通过。

修正最终规则：名称/编码、位置/型号的主副标题各占一行，每一行均不换行，并分别使用Bootstrap text-truncate；每行最大宽度200px，title保留完整文本。此规则替代此前主副标题自然换行的说明。
