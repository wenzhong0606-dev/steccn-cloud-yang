"""核对PRD编号覆盖及目标详情子视图；不代替业务验收。"""
from pathlib import Path
import json,re
r=Path(__file__).resolve().parents[2]/'docs/eam'
m=json.loads((r/'页面设计/设备档案重设计/设备档案模型登记.json').read_text(encoding='utf-8'))
assert {x['id'] for x in m['views']}=={f'A{i:02}' for i in range(1,26)}
assert len(m['views'])==25 and len(m['regions'])==15
assert {x[0] for x in m['modals']}=={f'M{i:02}' for i in range(1,25)} and len(m['modals'])==24
assert len(m['scenarios'])==15 and {x['id'] for x in m['scenarios']}=={f'S{i:02}' for i in range(1,16)}
html=(r/'prototype/pages/device.html').read_text(encoding='utf-8')
tabs=re.findall(r'data-tab="([^"]+)"',html);panels=re.findall(r'data-panel="([^"]+)"',html)
assert len(tabs)==len(set(tabs)) and set(tabs)==set(panels)
assert all(x['target'].split('tab=')[-1] in panels for x in m['views'] if x['target'].startswith('device?tab='))
assert (r/'prototype/pages/functional-locations.html').exists()
print('PRD模型：25视图 / 15业务区域 / 24组件；21详情子视图覆盖，错误0；不是CRUD验收')
