/* 专业视图数据绑定；页面骨架保留在 HTML，所有数据来自档案模拟接口。 */
window.EamArchiveBusinessView = (() => {
  const element = (tag, text, className = '') => {
    const node = document.createElement(tag);
    node.textContent = text;
    node.className = className;
    return node;
  };
  function render(domain, rows, display, openRecord) {
    const host = document.querySelector('[data-business-body="' + domain + '"]');
    if (!host) return;
    host.replaceChildren();
    if (!rows.length) {
      host.append(element('p', '当前范围没有可展示的业务数据。', 'text-secondary mb-0'));
      return;
    }
    if (domain === 'parameters') {
      const invalid = rows.filter(row => row.validationError || !row.definition);
      host.append(element('p', '当前范围 ' + rows.length + ' 项参数 · ' + invalid.length + ' 项待校验或配置', 'mb-1 fw-semibold'));
      host.append(element('p', '数值和范围按模板固定单位、精度与限值校验；修改入口位于记录详情，报废设备只读。', 'text-secondary mb-0'));
    } else if (domain === 'structure') {
      const roots = rows.filter(row => !rows.some(parent => parent.id === row.parentId));
      const visited = new Set();
      function branch(row, parent) {
        if (visited.has(row.id)) return;
        visited.add(row.id);
        const children = rows.filter(child => child.parentId === row.id);
        const item = document.createElement('li');
        item.className = 'list-unstyled my-1';
        const button = element('button', row.name + ' · ' + (row.code || row.content), 'btn btn-sm btn-outline-secondary text-start');
        button.type = 'button';
        button.onclick = () => openRecord(row, domain);
        if (children.length) {
          const details = document.createElement('details');
          details.open = true;
          const summary = element('summary', row.name + ' · ' + (row.code || row.content), 'fw-semibold');
          const list = document.createElement('ul');
          list.className = 'ps-3 mb-0 border-start';
          details.append(summary, button, list);
          children.forEach(child => branch(child, list));
          item.append(details);
        } else item.append(button);
        parent.append(item);
      }
      const tree = document.createElement('ul');
      tree.className = 'ps-0 mb-0';
      tree.setAttribute('aria-label', '当前筛选的设备组成层级');
      roots.forEach(row => branch(row, tree));
      host.append(tree);
      if (visited.size !== rows.length) host.append(element('p', '部分组成关系存在环路，需在结构维护中检查。', 'text-warning mb-0'));
    } else if (domain === 'history') {
      const list = document.createElement('ol');
      list.className = 'list-group list-group-flush';
      for (const row of rows) {
        const item = document.createElement('li');
        item.className = 'list-group-item px-2 py-2 border-start';
        const button = element('button', row.name, 'btn btn-sm btn-outline-secondary');
        button.type = 'button';
        button.onclick = () => openRecord(row, domain);
        item.append(element('div', (row.eventAt || row.updatedAt || '时间未记录') + ' · ' + row.source, 'small text-secondary'), button, element('div', row.evidence || '—', 'small mt-1'));
        list.append(item);
      }
      host.append(list);
    } else if (domain === 'trend') {
      const config = display.trend;
      if (!config) { host.append(element('p', '尚未配置测点与单位。', 'text-secondary')); return; }
      const points = rows.filter(row => row.quality === config.quality && typeof row[config.metric] === 'number' && Number.isFinite(row[config.metric]) && Number.isFinite(Date.parse(row.eventAt))).sort((a,b) => Date.parse(a.eventAt) - Date.parse(b.eventAt));
      host.append(element('p', config.name + ' · ' + config.pointId + ' · 单位 ' + config.unit + ' · 有效样本 ' + points.length + '/' + rows.length, 'small mb-1'));
      if (!points.length) { host.append(element('p', '没有质量合格的有效读数，无法绘制趋势。', 'text-secondary')); return; }
      const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
      svg.setAttribute('viewBox', '0 0 640 140');
      svg.setAttribute('role', 'img');
      svg.setAttribute('aria-label', config.name + '趋势；仅展示当前筛选范围内的有效采样');
      const values = points.map(row => row[config.metric]), low = Math.min(...values) - 1, high = Math.max(...values, config.upper ?? -Infinity) + 1;
      const start = Date.parse(points[0].eventAt), span = Date.parse(points.at(-1).eventAt) - start;
      const x = row => span ? 45 + (Date.parse(row.eventAt) - start) / span * 560 : 325;
      const y = value => 108 - (value - low) / (high - low) * 88;
      const shape = (tag, attrs) => { const node = document.createElementNS(svg.namespaceURI, tag); for (const [key,value] of Object.entries(attrs)) node.setAttribute(key,value); svg.append(node); return node; };
      shape('line', {x1:45,y1:108,x2:605,y2:108,stroke:'currentColor'});
      if (Number.isFinite(config.upper)) {
        shape('line',{x1:45,y1:y(config.upper),x2:605,y2:y(config.upper),stroke:'var(--bs-warning)', 'stroke-dasharray':'5 4'});
        shape('text',{x:45,y:y(config.upper)-4,'font-size':10,fill:'currentColor'}).textContent='上限 '+config.upper+' '+config.unit;
      }
      shape('polyline',{points:points.map(row=>x(row)+','+y(row[config.metric])).join(' '),fill:'none',stroke:'var(--bs-primary)','stroke-width':2});
      for (const row of points) {
        const dot=shape('circle',{cx:x(row),cy:y(row[config.metric]),r:3,fill:'var(--bs-primary)'});
        const title=document.createElementNS(svg.namespaceURI,'title'); title.textContent=row.eventAt+' · '+row[config.metric]+' '+config.unit;dot.append(title);
      }
      shape('text',{x:45,y:132,'font-size':10,fill:'currentColor'}).textContent=points[0].eventAt;
      if(points.length>1)shape('text',{x:605,y:132,'text-anchor':'end','font-size':10,fill:'currentColor'}).textContent=points.at(-1).eventAt;
      host.append(svg,element('p','阈值依据：'+config.thresholdSource+'；静态模拟采样，不代表实时信号。无效质量不绘入曲线，原记录保留在下表。','small text-secondary mb-0'));
    }
  }
  function state(domain, mode, message) {
    const host = document.querySelector('[data-business-body="' + domain + '"]');
    if (!host) return;
    host.replaceChildren(element('p', message || (mode === 'loading' ? '正在读取业务数据…' : '业务数据读取失败，请通过清单重试。'), 'text-secondary mb-0'));
  }
  return {render,state};
})();
