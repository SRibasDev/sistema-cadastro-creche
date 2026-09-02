const API='';
let token=localStorage.getItem('jwt')||'';
let userRole='';
let tutPage=0,petPage=0,hospPage=0;

function login(){
 fetch(API+'/api/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},
  body:JSON.stringify({email:g('loginEmail').value,senha:g('loginSenha').value})})
 .then(r=>r.ok?r.json():r.json().then(e=>Promise.reject(e)))
 .then(d=>{token=d.token;userRole=d.role;localStorage.setItem('jwt',token);
  g('userName').textContent=d.nome;g('userRole').textContent=d.role;
  g('userInfo').style.display='';g('loginPanel').style.display='none';
  g('dashboard').style.display='';loadTutores(0);})
 .catch(e=>toast('Login falhou: '+(e.mensagem||'Credenciais invalidas'),'error'));}
function logout(){token='';localStorage.removeItem('jwt');
 g('loginPanel').style.display='';g('dashboard').style.display='none';g('userInfo').style.display='none';}
function auth(){return{'Authorization':'Bearer '+token,'Content-Type':'application/json'};}
function api(method,url,body){
 let o={method,headers:auth()};if(body)o.body=JSON.stringify(body);
 return fetch(API+url,o).then(r=>{if(r.status===204)return null;
  return r.ok?r.json():r.json().then(e=>Promise.reject(e));});}
function g(id){return document.getElementById(id);}
function closeModal(){g('modalOverlay').style.display='none';}
function toast(msg,type){let d=document.createElement('div');d.className='toast toast-'+type;d.textContent=msg;document.body.appendChild(d);setTimeout(()=>d.remove(),3500);}
// ==== TUTORES ====
function loadTutores(dir){
 if(dir===0)tutPage=Math.max(0,tutPage-1);else if(dir===1)tutPage++;
 api('GET','/api/tutores?page='+tutPage+'&size=10').then(p=>{
  let h='';p.content.forEach(t=>{h+=`<tr><td>${t.id}</td><td>${t.nome}</td><td>${t.telefone}</td>
   <td><button class=\"btn btn-primary btn-xs\" onclick=\"openTutorModal(${t.id})\">Editar</button>
   ${userRole==='ADMIN'?`<button class=\"btn btn-danger btn-xs\" onclick=\"delTutor(${t.id})\">Excluir</button>`:''}</td></tr>`;});
  g('tutoresTable').innerHTML=h||'<tr><td colspan=\"4\" class=\"empty\">Nenhum tutor</td></tr>';
  g('tutPage').textContent='Pagina '+(p.number+1)+' de '+p.totalPages;
  g('tutPrev').disabled=p.first;g('tutNext').disabled=p.last;})
 .catch(e=>toast(e.mensagem||'Erro','error'));}
async function openTutorModal(id){
 let t=id?await api('GET','/api/tutores/'+id):null;
 g('modalContent').innerHTML=`<h3>${id?'Editar':'Novo'} Tutor</h3>
  <div class=\"form-group\"><label>Nome</label><input id=\"fNome\" value=\"${t?t.nome:''}\"></div>
  <div class=\"form-group\"><label>Telefone (11 digitos)</label><input id=\"fTel\" value=\"${t?t.telefone:''}\"></div>
  <div class=\"form-group\"><label>CPF</label><input id=\"fCpf\" value=\"${t?t.cpf||'':''}\"></div>
  <button class=\"btn btn-primary\" id=\"saveBtn\">Salvar</button>`;
 g('modalOverlay').style.display='';
 g('saveBtn').onclick=async()=>{
  let body={nome:g('fNome').value,telefone:g('fTel').value,cpf:g('fCpf').value};
  try{if(id)await api('PUT','/api/tutores/'+id,body);else await api('POST','/api/tutores',body);
   closeModal();loadTutores(-1);toast(id?'Atualizado!':'Criado!','success');}
  catch(e){toast(e.mensagem||'Erro','error');}};}
function delTutor(id){if(confirm('Excluir tutor '+id+'?'))api('DELETE','/api/tutores/'+id).then(()=>{loadTutores(-1);toast('Excluido','success');}).catch(e=>toast(e.mensagem,'error'));}
// ==== PETS ====
function loadPets(dir){
 if(dir===0)petPage=Math.max(0,petPage-1);else if(dir===1)petPage++;
 api('GET','/api/pets?page='+petPage+'&size=10').then(p=>{
  let h='';p.content.forEach(x=>{h+=`<tr><td>${x.id}</td><td>${x.nome}</td><td>${x.raca}</td><td>${x.dataNascimento||''}</td><td>${x.tutorId}</td>
   <td><button class=\"btn btn-primary btn-xs\" onclick=\"openPetModal(${x.id})\">Editar</button>
   ${userRole==='ADMIN'?`<button class=\"btn btn-danger btn-xs\" onclick=\"delPet(${x.id})\">Excluir</button>`:''}</td></tr>`;});
  g('petsTable').innerHTML=h||'<tr><td colspan=\"6\" class=\"empty\">Nenhum pet</td></tr>';
  g('petPage').textContent='Pagina '+(p.number+1)+' de '+p.totalPages;
  g('petPrev').disabled=p.first;g('petNext').disabled=p.last;})
 .catch(e=>toast(e.mensagem||'Erro','error'));}
async function openPetModal(id){
 let p=id?await api('GET','/api/pets/'+id):null;
 g('modalContent').innerHTML=`<h3>${id?'Editar':'Novo'} Pet</h3>
  <div class=\"form-group\"><label>Nome</label><input id=\"fNome\" value=\"${p?p.nome:''}\"></div>
  <div class=\"form-group\"><label>Raca</label><input id=\"fRaca\" value=\"${p?p.raca:''}\"></div>
  <div class=\"form-group\"><label>Nascimento</label><input type=\"date\" id=\"fNasc\" value=\"${p?p.dataNascimento||'':''}\"></div>
  <div class=\"form-group\"><label>ID do Tutor</label><input type=\"number\" id=\"fTid\" value=\"${p?p.tutorId||'':''}\"></div>
  <button class=\"btn btn-primary\" id=\"saveBtn\">Salvar</button>`;
 g('modalOverlay').style.display='';
 g('saveBtn').onclick=async()=>{
  let body={nome:g('fNome').value,raca:g('fRaca').value,dataNascimento:g('fNasc').value};
  let tid=g('fTid').value;
  try{if(id)await api('PUT','/api/pets/'+id,body);else await api('POST','/api/pets/'+tid,body);
   closeModal();loadPets(-1);toast(id?'Atualizado!':'Criado!','success');}
  catch(e){toast(e.mensagem||'Erro','error');}};}
function delPet(id){if(confirm('Excluir pet '+id+'?'))api('DELETE','/api/pets/'+id).then(()=>{loadPets(-1);toast('Excluido','success');}).catch(e=>toast(e.mensagem,'error'));}
// ==== HOSPEDAGENS ====
function loadHospedagens(dir){
 if(dir===0)hospPage=Math.max(0,hospPage-1);else if(dir===1)hospPage++;
 api('GET','/api/hospedagens?page='+hospPage+'&size=10').then(p=>{
  let h='';p.content.forEach(hp=>{
   let cls='badge-'+hp.status.toLowerCase();
   let btns='';
   if(hp.status==='AGENDADA')btns=`<button class=\"btn btn-primary btn-xs\" onclick=\"mudarStatus(${hp.id},'EM_ANDAMENTO')\">Iniciar</button>`;
   if(hp.status==='EM_ANDAMENTO')btns=`<button class=\"btn btn-success btn-xs\" onclick=\"mudarStatus(${hp.id},'CONCLUIDA')\">Concluir</button>`;
   h+=`<tr><td>${hp.id}</td><td>${hp.petNome}</td><td>${hp.tutorNome}</td><td>${hp.dataEntrada}</td><td>${hp.dataSaida}</td>
    <td><span class=\"badge ${cls}\">${hp.status}</span></td>
    <td>${btns}<button class=\"btn btn-danger btn-xs\" onclick=\"cancelarHosp(${hp.id})\" ${hp.status==='CANCELADA'||hp.status==='CONCLUIDA'?'disabled':''}>Cancelar</button></td></tr>`;});
  g('hospTable').innerHTML=h||'<tr><td colspan=\"7\" class=\"empty\">Nenhuma hospedagem</td></tr>';
  g('hospPage').textContent='Pagina '+(p.number+1)+' de '+p.totalPages;
  g('hospPrev').disabled=p.first;g('hospNext').disabled=p.last;})
 .catch(e=>toast(e.mensagem||'Erro','error'));}
function mudarStatus(id,s){api('PATCH','/api/hospedagens/'+id+'/status?status='+s).then(()=>{loadHospedagens(-1);toast('Status: '+s,'success');}).catch(e=>toast(e.mensagem||'Transicao invalida','error'));}
function openHospModal(){
 g('modalContent').innerHTML=`<h3>Nova Hospedagem</h3>
  <div class=\"form-group\"><label>ID do Pet</label><input type=\"number\" id=\"fPetId\"></div>
  <div class=\"form-group\"><label>Data Entrada</label><input type=\"date\" id=\"fEnt\"></div>
  <div class=\"form-group\"><label>Data Saida</label><input type=\"date\" id=\"fSai\"></div>
  <div class=\"form-group\"><label>Observacoes</label><input id=\"fObs\"></div>
  <button class=\"btn btn-primary\" id=\"saveBtn\">Salvar</button>`;
 g('modalOverlay').style.display='';
 g('saveBtn').onclick=async()=>{
  let body={petId:+g('fPetId').value,dataEntrada:g('fEnt').value,dataSaida:g('fSai').value,observacoes:g('fObs').value||null};
  try{await api('POST','/api/hospedagens',body);closeModal();loadHospedagens(-1);toast('Criada!','success');}
  catch(e){toast(e.mensagem||'Erro','error');}};}
function cancelarHosp(id){if(confirm('Cancelar hospedagem '+id+'?'))api('DELETE','/api/hospedagens/'+id).then(()=>{loadHospedagens(-1);toast('Cancelada','success');}).catch(e=>toast(e.mensagem,'error'));}

// ==== TABS ====
document.querySelectorAll('.tab').forEach(t=>t.onclick=function(){
 document.querySelectorAll('.tab').forEach(x=>x.classList.remove('active'));
 document.querySelectorAll('.panel').forEach(x=>x.classList.remove('active'));
 this.classList.add('active');g('panel-'+this.dataset.tab).classList.add('active');
 if(this.dataset.tab==='tutores')loadTutores(-1);
 else if(this.dataset.tab==='pets')loadPets(-1);
 else if(this.dataset.tab==='hospedagens')loadHospedagens(-1);});

// ==== AUTO-LOGIN ====
if(token){try{let p=JSON.parse(atob(token.split('.')[1]));g('userName').textContent=p.sub;
 api('GET','/api/tutores?size=1').then(()=>{g('userInfo').style.display='';g('loginPanel').style.display='none';
  g('dashboard').style.display='';loadTutores(0);}).catch(()=>logout());}catch(e){logout();}}