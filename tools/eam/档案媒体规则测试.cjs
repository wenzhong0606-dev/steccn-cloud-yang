const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs');
const source='../../docs/eam/prototype/shared/archive-media-rules.js';
const rules=fs.existsSync(require('node:path').resolve(__dirname,source))?require(source):{};
test('相册保留示意资源但与设备图片分组，跨设备与已移除资源不进入目录',()=>{
 assert.equal(typeof rules.catalog,'function','缺少设备媒体分类规则');
 const result=rules.catalog([{id:'PHOTO',equipmentId:'EQ-001',category:'image',status:'active',isPrimary:true},{id:'SAMPLE',equipmentId:'EQ-001',category:'image',status:'active',isSample:true},{id:'OTHER',equipmentId:'EQ-002',category:'model',status:'active'},{id:'REMOVED',equipmentId:'EQ-001',category:'model',status:'removed'},{id:'MODEL',equipmentId:'EQ-001',category:'model',status:'active'}],'EQ-001');
 assert.deepEqual(result.images.map(r=>r.id),['PHOTO']);assert.deepEqual(result.samples.map(r=>r.id),['SAMPLE']);assert.deepEqual(result.models.map(r=>r.id),['MODEL']);assert.equal(result.primary.id,'PHOTO');
});
test('没有明确主图不得用相册第一张冒充摘要主图',()=>{assert.equal(typeof rules.catalog,'function');assert.equal(rules.catalog([{equipmentId:'EQ-001',category:'image',status:'active'}],'EQ-001').primary,null);});
test('可预览自包含glTF，拒绝缺失外部资源与依赖外部解码器的模型',async()=>{
 assert.equal(typeof rules.inspect,'function');const json={asset:{version:'2.0'},scenes:[{nodes:[0]}],nodes:[{mesh:0}],meshes:[{primitives:[]}],buffers:[{uri:'data:application/octet-stream;base64,AAAA',byteLength:3}]};
 assert.equal((await rules.inspect(new Blob([JSON.stringify(json)]),'self.gltf')).format,'glTF');
 json.buffers[0].uri='https://outside.example/mesh.bin';await assert.rejects(()=>rules.inspect(new Blob([JSON.stringify(json)]),'external.gltf'),/外部资源/);
 delete json.buffers;json.extensionsUsed=['KHR_draco_mesh_compression'];await assert.rejects(()=>rules.inspect(new Blob([JSON.stringify(json)]),'compressed.gltf'),/压缩/);
});
test('扩展名伪装及损坏GLB拒绝进入查看器',async()=>{assert.equal(typeof rules.inspect,'function');await assert.rejects(()=>rules.inspect(new Blob(['not a model']),'bad.glb'),/GLB/);await assert.rejects(()=>rules.inspect(new Blob(['{}']),'bad.gltf'),/glTF/);});
test('独立演示资产是可解析的GLB，包含三个场景节点',async()=>{const bytes=fs.readFileSync(require('node:path').resolve(__dirname,'../../docs/eam/prototype/assets/media-demo.glb'));const result=await rules.inspect(new Blob([bytes]),'demo.glb');assert.equal(result.format,'GLB');assert.equal(result.nodes,3);});
