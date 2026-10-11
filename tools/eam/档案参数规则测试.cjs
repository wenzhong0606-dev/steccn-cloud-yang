const test=require('node:test'),assert=require('node:assert/strict'),{json,validate}=require('../../docs/eam/prototype/mock/archive-rules.js');
test('参数类型、单位、精度与上下限校验',()=>{const r=json.parameters['PAR-MX-01'];assert.equal(validate(r,'76 kW').value,76);for(const value of ['76 KW','abc kW','501 kW','0 kW','76.123 kW','Infinity kW'])assert.throws(()=>validate(r,value));});
test('范围不可倒置，不接受不完整或模糊值',()=>{const r=json.parameters['PAR-MX-04'];assert.equal(validate(r,'0～75 °C').upper,75);for(const value of ['75～0 °C','75 °C','-51～75 °C','0～151 °C','0~75 °C'])assert.throws(()=>validate(r,value));});
