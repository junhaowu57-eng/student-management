const fs = require('node:fs');
const vm = require('node:vm');
const crypto = require('node:crypto');
const assert = require('node:assert/strict');
const path = require('node:path');
const root = path.resolve(__dirname, '..');
const marker = 'codexedit' + Date.now();
const encode = v => Buffer.from(JSON.stringify(v)).toString('base64url');
const unsigned = encode({alg:'HS256'}) + '.' + encode({sub:marker,role:'ADMIN',exp:Math.floor(Date.now()/1000)+600});
const token = unsigned + '.' + crypto.createHmac('sha256','student_management_secret_key_student_management_secret_key').update(unsigned).digest('base64url');
const elements = {};
for (const match of fs.readFileSync(path.join(root,'frontend/index.html'),'utf8').matchAll(/\bid="([^"]+)"/g)) elements[match[1]] = {value:'',style:{display:'none'},innerHTML:''};
const requests = [], alerts = [], pending = [];
const sandbox = {
  document:{getElementById:id=>elements[id] || null},
  localStorage:{getItem:()=>token},
  alert:msg=>alerts.push(msg),console:{log:()=>{}},
  fetch:async(url,options)=>{requests.push({url,method:options.method,body:options.body});return fetch(url,options);}
};
vm.createContext(sandbox);
vm.runInContext(fs.readFileSync(path.join(root,'frontend/script.js'),'utf8'),sandbox);
const originalLoad = sandbox.loadStudents;
sandbox.loadStudents = () => {const p=originalLoad();pending.push(p);return p;};
async function api(method,route,body){
  const r=await fetch('http://localhost:8080'+route,{method,headers:{Authorization:'Bearer '+token,'Content-Type':'application/json'},body:body===undefined?undefined:JSON.stringify(body)});
  const result=await r.json();assert.equal(result.code,200,JSON.stringify(result));return result.data;
}
let id;
(async()=>{
 try {
  await api('POST','/students',{name:marker,age:18,gender:'男',email:'before@example.com'});
  const page=await api('GET','/students?name='+marker);id=page.records[0].id;
  await sandbox.editStudent(id);
  assert.equal(elements.editId.value,id);
  assert.equal(elements.editName.value,marker);
  assert.equal(elements.editAge.value,18);
  assert.equal(elements.editGender.value,'男');
  assert.equal(elements.editEmail.value,'before@example.com');
  assert.equal(elements.editBox.style.display,'block');
  console.log('PASS: editStudent fills matching HTML fields and opens edit form');
  Object.assign(elements.editName,{value:marker+'updated'});
  elements.editAge.value='24';elements.editGender.value='女';elements.editEmail.value='after@example.com';
  await sandbox.saveStudent();await Promise.all(pending);
  const row=await api('GET','/students/'+id);
  assert.equal(row.name,marker+'updated');assert.equal(row.age,24);assert.equal(row.gender,'女');assert.equal(row.email,'after@example.com');
  assert.equal(elements.editBox.style.display,'none');
  assert.ok(alerts.includes('修改成功'));
  assert.ok(requests.some(r=>r.method==='PUT'&&JSON.parse(r.body).id===id));
  assert.ok(elements.studentTableBody.innerHTML.includes(marker+'updated'));
  console.log('PASS: saveStudent PUT persists all fields, shows success, closes form and refreshes list');
 } finally {
  if(id){await api('DELETE','/students/'+id);console.log('Temporary student deleted');}
 }
})().catch(e=>{console.error(e);process.exitCode=1;});
