# 设备相关业务梳理与 EAM 重构边界

日期：2026-10-09。依据：当前仓库源码静态分析，并补充 EAM 启动日志与 Nacos 实例接口的运行状态核验。业务功能和代码问题仍属于静态分析结论，尚未通过运行测试验证。

运行环境核验记录（2026-10-09，本次会话查询时）：

- 已直接查询 Nacos `192.168.16.194:8848`：命名空间 `yang`、分组 `DEFAULT_GROUP` 下的 `steccn-eam` 注册实例为 `1.0.0.3:19705`，返回 `healthy=true`、`enabled=true`。
- EAM 日志记录服务启动及 Nacos 注册成功，实际启动端口为 `19705`；本地 `bootstrap.yaml` 中的端口为 `9705`，两者不同。
- 日志确认已加载 `steccn-eam.yaml` 与共享配置 `application.yaml`，并于当日 16:57 成功接收配置变更和完成刷新。
- 数据库连接、实际表结构及查询读写尚未核验。获取或刷新数据库配置不等于数据库连接成功。
- Kafka 连接和消息消费链路尚未核验；未通过 EAM 业务 HTTP 接口验证服务功能。

以上状态为本次核验时的结果，不代表持续监控结果。

## 1. 已明确的目标和约束

- 梳理现有设备相关业务，在 steccn-eam 中重构。
- EAM 允许重新设计数据库表和接口。
- 调用方暂时维持现状，不要求本轮同步修改消费接口的其他服务。
- 本文是现状清单和重构建议，不代表已完成迁移，也不是已批准的实现规格。

## 2. 当前业务分布

| 位置 | 当前职责 | 重构处理建议 |
| --- | --- | --- |
| steccn-mes | 设备模型、设备树状态、点检统计、温度报表、缺陷统计、检修计划模型、运行报表、备件 ERP 同步 | 设备核心业务逐步迁入 EAM |
| steccn-data-platform-come | 接收设备启停信号、保存 mes_eqp_signal、调用 MES 累加运行统计、部分通知 | 原始采集入口保留；设备业务通过兼容入口接入 EAM |
| steccn-data-platform | 启停记录查询、停机次数和时长计算、查询 MES 设备名称 | 当前接口保留；业务统计最终由 EAM 统一提供 |
| steccn-platform | 备件查询和维护接口、备件单位换算、信号映射配置、组织信息和消息服务；另有工位设备与 IoT 信息接口 | 共享能力保留，备件主数据归属单独确认；工位配置不直接等同设备资产台账 |
| steccn-canal | 监听 mes_eqp_spare、mes_eqp_spare_unit_conversion，向 platform 等同步 | 新表不会自动触发现有监听，需要明确兼容同步方案 |
| steccn-pms | 通过 MES 获取泡界线点检数据；另有人员岗位等调用 | 设备数据调用保留旧契约；人员类调用不随设备业务整体迁移 |
| steccn-eam | 当前可见业务入口是 HelloController；已有 Spring Boot 和 api/biz 模块 | 承载重构后的设备领域 |

重要：同名 RemoteMesFeignApi 分别存在于 mes-api 和 platform-api，后者包含其他集成业务，不能只按类名批量替换。

## 3. 设备功能清单

| 功能 | 当前主要代码 | 当前可见实现 |
| --- | --- | --- |
| 设备台账与设备树 | MesEquipment、MesEquipmentServiceImpl | 编码、名称、型号、厂家、购置日期、资产编码、分类、部门、父节点和树路径；查询和状态修改 |
| 分类 | MesEqpClassification | 组织、物料、点检、设备、点检方法、维保分类混用一个模型 |
| 信号映射与状态联动 | KafkaConsumer、平台 SysConfiguration | 消费 eqp_signal_data_order；按 KAFKA_EQP_SIGNAL 映射标签与设备；Redis 缓存映射与脉冲状态；存在车间和设备专用规则 |
| 点检计划 | MesEqpInsPlanSchedule | 计划周期和执行时间模型；本模块未见完整的任务生成业务 |
| 点检任务统计 | MesEqpInsTaskServiceImpl | 任务完成数、完成率、点检项合格率、组织维度聚合 |
| 点检监控 | SpotCheckTask、taskCompleteMonitor | XXL-JOB 定时统计；从 DP 获取阈值和通知范围，通过 platform 发送消息 |
| 点检测量数据 | MesEqpInsTaskEqpItemServiceImpl、温度报表 | 温度项目、按产线和班次展示、趋势图、泡界线位置查询 |
| 缺陷 | MesDefect、MesDefectServiceImpl | 缺陷模型与统计；关联设备、检修、工单等标识，未见完整缺陷处理闭环接口 |
| 检修/维保 | MesEqpServicePlan | 模型和基础 Service；查询设备时输出部位和维护方法，未见完整维修工单流程 |
| 运行统计 | MesEqpReportServiceImpl | 启动时间、累计运行/停止时长、最长/最短运行时长、分页报表 |
| 备件 | MesEqpSpareServiceImpl | 导出、ERP 同步、长度与单位处理、同步到 QMS、供应商查询 |

边界说明：有实体或继承 MyBatis-Plus Service 并不等于已有可用的完整管理流程。新增台账 CRUD、点检执行、工单闭环等，应列为补齐功能，不能称为纯迁移。

## 4. 现有数据模型

- mes_equipment：设备主数据和当前运行状态混在同一表；parentCode/parentCodes 维护层级。
- mes_eqp_classification：分类树和多种分类用途。
- mes_eqp_service_plan：检修作业计划，含设备关联、部位和维护方法。
- mes_eqp_ins_plan_schedule：点检计划绑定和执行周期。
- mes_eqp_ins_task：点检任务、计划时间、完成时间、部门和计划关联。
- mes_eqp_ins_task_eqp_item：任务、设备、点检项及检测结果关联。
- mes_defect：缺陷及设备、检修、工单关联。
- mes_eqp_report：设备运行累计数据。
- mes_eqp_spare：备件信息；相关单位换算表还参与 Canal 同步。
- mes_eqp_signal：采集服务与查询服务中的原始启停信号。

设备 state 的实体注释定义为 0运行、1告警、2待机、3关机。原始启停信号 0/1 是另一套语义，不能共用一套状态解释。

以上是源码映射信息，真实表结构、约束、数据量、索引和外部写入方仍需数据库核验。

## 5. 现有契约和调用关系

MES HTTP 主入口：/mes/eqp，提供设备信息、点检统计、缺陷图表、温度报表、原料看板、运行报表和设备数量。

MES 设备 Feign 契约：

- GET /feign/mes/eqp/getByCode：data-platform 查询启停记录时获取设备名称。
- POST /feign/mes/eqp/state：设备状态修改契约；采集代码中的一处调用已注释，不能认定为当前活跃链路。
- POST /feign/mes/eqp/changeReportData：data-platform-come 调用运行统计更新，包括历史重算入口。
- POST /feign/mes/eqp/ins/pao：PMS 报表查询泡界线数据。

RemoteMesFeignApi 还提供人员、岗位、部门、菜单权限接口；PMS 与 platform 中可见多个相关调用。这些不是设备核心领域，不整体迁入 EAM。

两条设备数据链路：

1. Kafka 标签消息 → MES KafkaConsumer → 映射配置/Redis → 设备树状态和告警。
2. HTTP /data/platform/mes/signal → 保存原始信号，并异步调用 MES changeReportData → 更新累计运行报表。

它们并非一个天然统一的数据流程，重构时要明确两者事件来源、重复关系、时间顺序和状态优先级。

## 6. 建议的 EAM 结构（待讨论）

保留 steccn-eam-api / steccn-eam-biz 的现有模块结构，在 biz 中按业务拆包，而非新增多个微服务：

- equipment：设备主数据、层级、分类及设备部位。
- telemetry：标签映射、信号归一化、设备当前状态和状态历史。
- inspection：点检计划、任务、结果；区分当前迁移的统计功能与后续补齐的执行功能。
- maintenance：缺陷和检修计划；维修工单闭环后续单独设计。
- spare：设备备件关联与 ERP 对接；与平台现有备件主数据保持明确边界。
- reporting：运行时长、点检指标、温度报表和业务看板。
- integration：platform/DP/OA/ERP 调用适配，避免领域逻辑直接依赖各系统 DTO。

建议候选新表：eam_equipment、eam_equipment_category、eam_equipment_part、eam_signal_mapping、eam_equipment_state、eam_equipment_state_event、eam_operation_interval、eam_inspection_plan、eam_inspection_task、eam_inspection_result、eam_defect、eam_maintenance_plan。备件表归属确认后再定。

新表设计原则：

- 设备 id、业务编码和父设备 id 分开，不再混用。
- 资产启用/删除状态与运行/告警状态分开。
- 运行状态变化保留事件时间、来源及去重标识，累计值可重算。
- 时长统一持久化为秒，API 明确单位，在展示层换算。
- 旧 id 与新 id 建立映射，保留设备业务编码；兼容响应不能直接泄露新 id 破坏旧调用。
- 规则可配置，消除固定车间 id、设备 id、温度点检项 id 与通知人员 id。

## 7. 调用方暂不变化时的接入方式

建议先在 EAM 完成新模型和新接口，旧服务随后充当兼容入口：

调用方 → 原服务名/原路径/原 DTO → 旧服务兼容适配 → EAM 新接口/新模型。

需要保持的是原服务发现名称、路径、请求和返回结构、编码、必要的旧 id 和错误语义。仅在 EAM 增加同路径接口无法解决 Feign 仍然调用 steccn-mes 的问题。

方案对比：

- 推荐：MES 保留设备兼容入口并转发 EAM；调用方不用改，人员等非设备功能继续留在原服务。代价是短期增加一次调用与 DTO 转换。
- 备选：EAM 独立运行，MES 暂时保持旧业务。适合先验证重构，但需明确此阶段并未迁移真实业务；双系统数据如何同步必须设计。
- 后续：调用方直接改用 EAM API，再移除兼容层。当前用户要求暂不进行此步。

Gateway 路由只能覆盖经过 Gateway 的 HTTP 请求，不能自动替换服务发现方式的 Feign 调用。

Kafka 切换要检查 group-id、topic 和消费者部署：MES 与 EAM 同时监听可能重复处理，也可能分摊消息导致各自数据不完整。不能只复制 Listener 后同时启用。

新表写入后，旧表 Canal 同步不会自动继续工作。备件若先保持旧主数据归属，可暂不改变该同步链路；若迁移主数据，必须另行设计事件同步或兼容投影。

## 8. 重构时需要处理的代码问题

以下是静态代码发现，未通过运行复现：

1. MesEquipmentServiceImpl.changeEqpState 查找 parent 时仍使用当前设备 equipmentCode，而非父关联；旧逻辑也将 1 当作停止，与实体 1告警的定义冲突。
2. KafkaConsumer 设备树构造使用 id 作为 Map key，随后用 rootEqpCode 取根；children 没有默认初始化，构造树时还使用了错误的 equipment 变量。此分支有无法处理树或空指针的风险。
3. KafkaConsumer 计算了 eqpTmp 的状态后，更新时却传入 equipment；树状态计算结果可能未被正确写入。
4. HbCollectServiceImpl 将秒除以 60 后传入累计报表，而 queryReport 的当前运行时长按小时计算，存在分钟/小时混用。
5. 采集请求先提交异步读取上一条信号/累加报表的任务，再批量保存信号；缺少明确排序、幂等和事务边界，需验证竞态、重复和乱序场景。
6. SpotCheckTask 传入昨天日期，taskCompleteMonitor 又查询该日期之前一天；定时统计区间和消息日期可能不一致。
7. SpotCheckTask 捕获异常后仅日志记录，仍输出任务完成，调度侧失败反馈需要重新设计。
8. 当前未发现 MES/EAM 专属测试；迁移前应建立状态树、事件去重与乱序、统计单位、业务日边界和旧契约兼容的验证。

## 9. 建议实施顺序

1. 确定第一期迁移范围、数据源与旧入口兼容方式。
2. 核验真实表结构、数据和所有外部写入方，设计新表与旧 id 映射。
3. 优先完成设备台账、分类、层级和查询，建立 EAM API。
4. 重构状态事件和联动规则、运行区间与统计；验证历史回放结果。
5. 迁移点检统计、温度与缺陷报表、监控任务；补齐功能单独列项。
6. 明确备件归属后迁移 ERP 集成与同步。
7. 以旧服务兼容入口接入 EAM，切换单一状态处理者和业务写入方。
8. 后续按调用方改造进度停用旧契约与旧表。

后续目标已明确为覆盖现有设备功能，并按角色、场景、双端使用方式设计设备管理及体系支撑能力；ISO 9001 是举例，相关体系和标准应由业务适用性分析确定。完整设计见 [EAM 角色、场景、流程、字段与验收设计](2026-10-09-eam-role-scenario-design.md)。其中区分现有功能迁移和新增管理闭环，分期顺序不替代全量功能覆盖。
