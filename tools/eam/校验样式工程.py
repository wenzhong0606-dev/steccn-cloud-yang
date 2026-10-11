"""检查本地样式依赖、媒体查询与关键帧迁移完整性（非视觉验收替代）。"""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[2]/'docs/eam/prototype'
files=[root/'shared/style.css',root/'shared/app.css',*sorted((root/'shared/styles').glob('*.css'))]
errors=[]
for p in files:
    source=p.read_text(encoding='utf-8-sig')
    for url in re.findall(r'@import\s+url\("([^"?]+)',source):
        if '://' in url or not (p.parent/url).is_file():errors.append(f'{p.name}: 非本地或缺失依赖 {url}')
    stripped=re.sub(r'/\*.*?\*/','',source,flags=re.S)
    stripped=re.sub(r'"(?:[^"\\]|\\.)*"|\'(?:[^\'\\]|\\.)*\'','""',stripped)
    depth=0
    for c in stripped:
        depth+=1 if c=='{' else -1 if c=='}' else 0
        if depth<0:errors.append(f'{p.name}: 多余闭括号');break
    if depth:errors.append(f'{p.name}: 括号不平衡')
    if re.search(r'^\s*(?:and\s*\(|print\s*\{|ui-spin\s*\{|\{)',stripped,re.M):errors.append(f'{p.name}: 丢失at-rule')
    if re.search(r'^\s*button\s*\{',stripped,re.M):errors.append(f'{p.name}: 全局按钮覆盖风险')
if not (root/'vendor/bootstrap/许可证.txt').is_file():errors.append('缺少第三方许可证')
print('样式工程检查:',len(files),'文件;',len(errors),'错误')
for e in errors:print(e)
raise SystemExit(bool(errors))
