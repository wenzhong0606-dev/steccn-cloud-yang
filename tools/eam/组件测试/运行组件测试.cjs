const {spawnSync}=require('node:child_process'),path=require('node:path');
let failed=0;
for(const file of ['_pilot-test.mjs','_form-dialog-test.mjs','_card-test.mjs','_tabs-test.mjs','_migration-test.mjs','_batch-migration-test.mjs','../组件闭环回归测试.cjs','真实页面回归.cjs']){
 const result=spawnSync(process.execPath,[path.join(__dirname,file)],{encoding:'utf8'});
 const lines=(result.stdout||'').trim().split(/\r?\n/);console.log(file+' · '+(lines.find(l=>l.startsWith('# tests '))||lines.at(-1)));
 if(result.status!==0){failed++;console.error(result.stdout,result.stderr);}
}
process.exitCode=failed?1:0;
