"""校验EAM设计目录和独立页面的结构一致性，不证明业务验收。"""
from pathlib import Path
import re
import json

root = Path(__file__).resolve().parents[2] / "docs/eam"
errors = []
for p in root.rglob('*.md'):
    if not re.search(r'[\u4e00-\u9fff]', p.stem):
        errors.append(f'文档名称非中文: {p.name}')
for p in root.rglob('*.py'):
    errors.append(f'工具混入文档目录: {p.relative_to(root)}')
menu = (root / '2026-10-10-功能菜单设计.md').read_text(encoding='utf-8-sig')
catalog = (root / '页面设计/页面设计总表.md').read_text(encoding='utf-8-sig')
pattern = r'^\| ([WEIMCSOARTDX]\d{2}) \|'
menu_ids = re.findall(pattern, menu, re.M)
catalog_ids = re.findall(pattern, catalog, re.M)
if len(menu_ids) != 60 or len(set(menu_ids)) != 60 or menu_ids != catalog_ids:
    errors.append('菜单与总表编号/数量不一致')
pages = list((root / 'prototype/pages').glob('*.html'))
registry = json.loads((root / '页面设计/页面记录/页面登记.json').read_text(encoding='utf-8'))
registered = registry['pages']
if {p.stem for p in pages} != {p['id'] for p in registered} or len(registered) != len({p['id'] for p in registered}):
    errors.append('独立HTML与逐页数据/接口登记不一致')
for entry in registered:
    record = root / '页面设计/页面记录' / (entry['title'] + '-页面数据与接口记录.md')
    if not record.exists():
        errors.append('缺逐页表字段与接口记录: ' + entry['id'])
    else:
        body = record.read_text(encoding='utf-8')
        if entry['id'] not in body or '候选表与字段' not in body or '候选接口列表' not in body:
            errors.append('逐页登记内容不完整: ' + entry['id'])
for p in pages:
    html = p.read_text(encoding='utf-8-sig')
    ids = re.findall(r'\bid="([^"]+)"', html)
    if len(ids) != len(set(ids)):
        errors.append(f'{p.name}: HTML ID重复')
    if html.find('../shared/maintenance.js') > html.find('../shared/domain.js'):
        errors.append(f'{p.name}: 维护规则依赖顺序错误')
    for target in re.findall(r'data-page="([^"]+)"', html):
        if not (p.parent / (target + '.html')).exists():
            errors.append(f'{p.name}: 无跳转页面 {target}')
    for target in re.findall(r'(?:src|href)="([^"]+)"', html):
        target = target.split('?')[0].split('#')[0]
        if not target.startswith(('http:', 'https:', '#')) and not (p.parent / target).exists():
            errors.append(f'{p.name}: 无资源 {target}')
links = 0
for p in root.rglob('*.md'):
    txt = p.read_text(encoding='utf-8-sig')
    for target in re.findall(r'\]\(([^)]+)\)', txt):
        target = target.split('#')[0].split('?')[0]
        if not target or '://' in target:
            continue
        links += 1
        if not (p.parent / target).exists():
            errors.append(f'{p.relative_to(root)}: 无文档目标 {target}')
if '已建页面模型 / 后续高保真' in catalog or '首批 / #' in catalog:
    errors.append('总表仍有旧状态或锚点')
if (root / 'prototype/shared/app.js').exists():
    errors.append('存在旧整页拼接app.js')
for required in ['设计与原型进度.md', '页面设计/2026-10-10-首批字段与文档审查.md', '数据设计/首批数据结构字典.md']:
    if not (root / required).exists():
        errors.append('缺存档 ' + required)
print(f'菜单={len(menu_ids)}；目录={len(catalog_ids)}；独立HTML={len(pages)}；本地文档链接={links}；错误={len(errors)}')
for error in errors:
    print(error)
raise SystemExit(bool(errors))
