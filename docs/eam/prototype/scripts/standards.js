(()=>{
 if(!document.getElementById('standard-upper'))return;const s=Eam.s;
 Eam.text('standard-info',s.standard.revision+' / '+(s.standard.status==='published'?'已发布':'已停用')+' / 上限 '+s.standard.upper+'°C / 方法 '+s.standard.method+' / 审核人 '+EamDomain.personName(s.standard.approvedBy));
 document.getElementById('standard-upper').value=s.standardDraft?.upper??s.standard.upper;document.getElementById('standard-method').value=s.standardDraft?.method||s.standard.method;
 Eam.text('standard-draft',s.standardDraft?'待审核草稿 '+s.standardDraft.revision+' / 起草人 '+EamDomain.personName(s.standardDraft.author):'暂无待发布草稿');
 document.getElementById('publish-standard').disabled=!s.standardDraft;
 document.getElementById('save-standard').onclick=()=>{if(Eam.run('saveStandard',{upper:Eam.value('standard-upper'),method:Eam.value('standard-method')}))location.reload();};
 document.getElementById('publish-standard').onclick=()=>{if(Eam.run('publishStandard',{}))location.reload();};

 const author=s.standardDraft?.author, isDraftOwner=author===s.person;
 document.getElementById('copy-standard').disabled=!!s.standardDraft||!Eam.can(['engineer']);
 document.getElementById('discard-standard').disabled=!isDraftOwner;
 document.getElementById('disable-standard').disabled=s.standard.status!=='published'||!Eam.can(['engineer','supervisor','quality']);
 document.getElementById('copy-standard').onclick=()=>{if(Eam.run('copyStandard'))location.reload();};
 for(const action of ['discard','disable'])document.getElementById(action+'-standard').onclick=()=>Eam.confirm(action==='discard'?'删除本人草稿':'停用当前发布版本',reason=>{if(!Eam.run(action+'Standard',{reason}))return false;location.reload();return true;});
 for(const version of [...s.standardHistory,s.standard])Eam.row(document.getElementById('standard-history'),'版本 '+version.revision,(version.status==='published'?'已发布':version.status==='disabled'?'已停用':'历史发布')+' / 起草 '+EamDomain.personName(version.author)+' / 审核 '+EamDomain.personName(version.approvedBy)+(version.disableReason?' / 停用原因 '+version.disableReason:'')+' / '+('上限 '+version.upper+'°C / '+version.method));
 document.getElementById('save-standard').disabled=!Eam.can(['engineer'])||!!(author&&!isDraftOwner);
 document.getElementById('publish-standard').disabled=!s.standardDraft||isDraftOwner||!Eam.can(['supervisor','quality']);

 const params=Object.fromEntries(new URLSearchParams(location.search));const selected=EamProduct.selectVersion(s.standard,s.standardDraft,s.standardHistory,params);
 if(selected.readonly){Eam.text('standard-info',selected.record?'版本 '+selected.record.revision+' / '+EamProduct.label(selected.record.status)+' / 只读':'未找到此版本，请返回列表');Eam.text('standard-draft','当前查看发布记录；草稿编辑请从列表草稿入口进入');
 if(document.getElementById('standard-upper'))document.getElementById('standard-upper').value=selected.record?.upper??'';
 if(document.getElementById('standard-method'))document.getElementById('standard-method').value=selected.record?.method??'';
 document.querySelectorAll('main input,main select,main textarea,main button:not([data-page])').forEach(el=>el.disabled=true);}
})();
