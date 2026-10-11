"""检查维护模型登记、菜单关联与规则覆盖；不证明生产流程已实现。"""
from pathlib import Path
import json, re
root = Path(__file__).resolve().parents[2] / 'docs/eam'
folder = root / '业务设计'
data = json.loads((folder / '维护业务设计登记.json').read_text(encoding='utf-8'))
assert data['stage'] == 'planned_models' and data['implemented'] is False
assert [r['id'] for r in data['rules']] == [f'BR{i:02}' for i in range(1,13)]
assert [r['sourceRule'] for r in data['rules']] == [f'R{i:02}' for i in range(1,13)]
menu = (root / '2026-10-10-功能菜单设计.md').read_text(encoding='utf-8')
menu_ids = set(re.findall(r'^\| ([WEIMCSOARTDX]\d{2}) \|', menu, re.M))
assert set(data['menuIds']) <= menu_ids
assert len(data['objects']) == 8
for name, states in data['objects'].items():
    assert states and len(states) == len(set(states)), name
assert 'risk_accepted' in data['objects']['defect']
assert set(data['invariants']) == {'work_close_does_not_close_problem','risk_accepted_is_not_terminal','per_problem_verification','professional_release_is_independent','published_snapshot_is_immutable'}
texts = [(folder / name).read_text(encoding='utf-8') for name in data['documents']]
for rule in data['rules']:
    assert rule['id'] in texts[1], rule['id']
for states in data['objects'].values():
    for state in states:
        assert state in texts[0], state
assert '验收者不得为该次执行主责人' in texts[0]
assert 'risk_accepted仍是未解决状态' in texts[0]
assert 'closed不改变问题状态' in texts[0]
print('Maintenance design: 8 object state sets, 12 rules, 8 menu references; PASS (design only)')
