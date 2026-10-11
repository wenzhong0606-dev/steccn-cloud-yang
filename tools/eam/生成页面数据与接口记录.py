"""从页面登记源生成逐页候选数据/接口记录及索引；不是正式数据库/API设计。"""
from pathlib import Path
from html.parser import HTMLParser
import json
root=Path(__file__).resolve().parents[2]/'docs/eam'
folder=root/'页面设计/页面记录'
registry=json.loads((folder/'页面登记.json').read_text(encoding='utf-8'))
class Controls(HTMLParser):
    def __init__(self):
        super().__init__();self.rows=[]
    def handle_starttag(self,tag,attrs):
        a=dict(attrs)
        if tag in ('input','select','textarea') and a.get('id') and not a['id'].startswith('confirm-'):
            self.rows.append((a['id'],a.get('type',tag),'必填' if 'required' in a else '按场景校验',a.get('maxlength','按候选字段规则')))
index=['# 首批页面候选数据与接口索引','',f'日期：{registry["date"]}。{len(registry["pages"])}个独立HTML已逐页登记。新建或修改页面必须同步更新页面登记源与本页记录；一致性工具检查覆盖。','', '正式数据库、DDL、OpenAPI及接口兼容方案应在全部页面模型完成后统一设计。此处接口路径、字段名和表名均为候选，不表示已实现或已经批准正式契约。','','| 页面ID | 页面 | 候选对象 | 接口记录数 |','| --- | --- | --- | --- |']
unique={}
for page in registry['pages']:
    html=root/'prototype/pages'/f"{page['id']}.html"
    parser=Controls();parser.feed(html.read_text(encoding='utf-8'))
    apis=[]
    for group in page['apiGroups']:
        apis.extend(registry['apiGroups'][group])
    for a in apis:unique[(a['method'],a['path'])]=a
    name=page['title']+'-页面数据与接口记录.md'
    lines=['# '+page['title']+'：页面数据与接口记录','','日期：2026-10-10。页面ID：`'+page['id']+'`；[独立HTML](../../prototype/pages/'+page['id']+'.html)。','','## 页面范围、角色与操作','','入口：系统平台 → EAM模块 → 角色工作台/模块列表 → 本页。PC与移动端共享业务规则，顶部切换仅改变评审布局，不改变角色或单据状态。', '',page['scope'],'','角色与操作授权见下方候选接口表；当前演示身份由评审控件切换，真实授权必须由正式平台身份与后端实施。删除/撤回/停用和历史规则参见[首批操作矩阵](../2026-10-10-首批页面操作矩阵与交互验收.md)。','','## 本页界面治理记录','','本页使用唯一公共样式入口、本地Bootstrap 5.3.3与五层CSS；主题、模块导航及键盘语义由公共UI层提供。业务范围以本页登记说明为准；模拟接口与正式候选接口应分别记录，主题偏好属于本机会话偏好。布局与状态验收须按[UI/UX规范](../HTML高保真页面UIUX审计与优化规范.md)及[本轮审计](../2026-10-10-首批UIUX审计与优化记录.md)执行。','','## 候选表与字段（待统一设计）','','不按页面建表；工作台、列表、全景和履历是共享对象的聚合投影。候选字段用逻辑名称记录，类型/长度/外键/索引/租户约束待模型全部完成后确定。','', '| 业务对象 | 可能的表及字段 |','| --- | --- |']
    for obj in page['objects']:lines.append('| '+obj+' | '+registry['objects'][obj]+' |')
    lines+=['','## 本页表单与查询控件','','| 控件ID | 类型 | 校验 | 长度记录 |','| --- | --- | --- | --- |']
    lines += ['| '+' | '.join(row)+' |' for row in parser.rows] or ['| 无独立输入 | 只读/导航投影 | 身份与范围校验 | 不适用 |']
    lines+=['','查询展示字段以独立HTML表头及候选对象字段为准。prototype中的personId/ownerId等对应人员外键；state数组只用于模拟，不是物理表设计。表单限制、必填和业务守卫需在正式服务重复校验。','','## 关系与状态约束','','设备为多数业务主关联；部位通过固定ID引用，名称为发现时快照；文件对象和对象关联分离。任务冻结标准/计划版本，结果引用任务；报告分流专项检查/工单，工单包含多个执行版本，验收指向实际执行版本。已发布/已执行对象不可覆盖或直接删除。', '', '正式服务按租户、组织、设备与实际指派过滤。修改带revision，跨状态/重复请求按业务语义拒绝或幂等，审计记录人员、时间、原因和前后快照；不能仅用前端按钮授权。','','## 候选接口列表（未接入）','','路径是待合并的候选命名；当前HTML不发这些业务HTTP请求。文件上传会话和预览地址必须由正式服务分配；不得把本地拟议目标当成真实上传地址。','','| 方法 | 候选路径 | 请求字段/查询 | 返回对象 | 权限/前置条件 |','| --- | --- | --- | --- | --- |']
    lines += ['| '+a['method']+' | `'+a['path']+'` | '+a['request']+' | '+a['response']+' | '+a['role']+' |' for a in apis] or ['| 无 | 评审目录无独立业务接口 | 共享导航与页面登记 | 不适用 | 沿用平台身份 |']
    lines+=['','通用异常候选：400字段校验、401未认证、403越权、404对象不存在、409版本/状态冲突、413大小超限、415文件类型不匹配、503暂时不可用。分页请求建议包含page/size和稳定排序；正式错误格式和幂等契约后续统一。','','## 当前实现、缺口与验收证据','','当前：独立HTML、会话模拟、共用导航与双端切换；本页范围见首段。缺口：正式服务、真实鉴权/上传/事务/并发/离线同步未实施；多设备完整作业链与多标准路线库还需后续模型。', '', '验收：逐页检查列表→详情→返回、筛选分页/空态、角色与状态按钮、草稿与历史只读、失败/冲突保持数据、PC/移动布局及键盘可达。规则与浏览器证据分开记录，不把候选接口清单称作API测试通过。', '', '[商用审计与本轮证据](../2026-10-10-首批商用审计与优化记录.md) · [进度入口](../../设计与原型进度.md) · [登记索引](首批页面候选数据与接口索引.md)','']
    (folder/name).write_text('\n'.join(lines),encoding='utf-8')
    index.append(f"| {page['id']} | [{page['title']}]({name}) | {'、'.join(page['objects'])} | {len(apis)} |")
index+=['','## 后续统一设计入口','','模型完成后再根据这些记录合并同义表/字段、统一主数据和状态机、确定外键/唯一键/索引/事务、收敛接口与DTO、制定旧调用方兼容及迁移方案。当前去重索引仅用于发现重复，不是正式接口发布。','',f'本批候选方法/路径去重共{len(unique)}项。','']
(folder/'首批页面候选数据与接口索引.md').write_text('\n'.join(index),encoding='utf-8')
summary=['# 首批候选接口汇总（待统一设计）','','日期：2026-10-10。此清单来自各页登记，仅为模型期候选项；未实施、未发布正式API。','','| 方法 | 候选路径 | 请求 | 返回 | 权限 |','| --- | --- | --- | --- | --- |']
for _,a in sorted(unique.items()):summary.append('| '+' | '.join(a[k] for k in ['method','path','request','response','role'])+' |')
summary+=['','来源：[逐页记录索引](页面设计/页面记录/首批页面候选数据与接口索引.md)。正式契约设计在所有页面模型完成后进行。']
(root/'首批候选接口汇总.md').write_text('\n'.join(summary)+'\n',encoding='utf-8')
print(f'页面记录={len(registry["pages"])}；候选接口去重={len(unique)}')
