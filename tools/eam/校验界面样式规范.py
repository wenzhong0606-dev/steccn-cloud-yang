"""UI工程静态门禁；布局、可访问性和业务交互另作浏览器验收。"""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[2]/'docs/eam/prototype'
errors=[]
styles=root/'shared/styles'
for path in styles.glob('*.css'):
    text=path.read_text(encoding='utf-8')
    if path.name!='令牌.css' and re.search(r'#[0-9a-fA-F]{3,8}\b|rgba?\(',text):errors.append(f'{path.name}: 颜色未引用Token')
    if '!important' in text and not (path.name=='基础.css' and text.count('!important')==1 and re.search(r'\[hidden\]\s*\{\s*display\s*:\s*none\s*!important\s*;?\s*\}',text)):errors.append(f'{path.name}: 未批准important')
    if text.count('{')!=text.count('}'):errors.append(f'{path.name}: 大括号不配对')
    if '@layer ' not in text:errors.append(f'{path.name}: 未声明样式层')
for page in (root/'pages').glob('*.html'):
    html=page.read_text(encoding='utf-8')
    if len(re.findall(r'<link[^>]+rel="stylesheet"',html))!=1:errors.append(f'{page.name}: 非单一CSS入口')
    if '<style' in html or ' style=' in html:errors.append(f'{page.name}: 内联样式')
    if '../shared/ui.js' not in html:errors.append(f'{page.name}: 无公共交互层')
    if '../shared/style.css' not in html:errors.append(f'{page.name}: 无统一CSS入口')
entry=(root/'shared/style.css').read_text(encoding='utf-8')
for target in re.findall(r'url\("([^"?]+)',entry):
    if not ((root/'shared')/target).exists():errors.append(f'缺样式文件 {target}')
print(f'UI样式门禁: {len(list(styles.glob("*.css")))}分层CSS / {len(list((root/"pages").glob("*.html")))}HTML / 错误{len(errors)}')
for error in errors:print(error)
raise SystemExit(bool(errors))
