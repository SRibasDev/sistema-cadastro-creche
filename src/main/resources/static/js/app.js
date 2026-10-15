const API = '';
let token = localStorage.getItem('jwt') || '';
let userRole = localStorage.getItem('userRole') || '';
let currentTab = 'visao-geral';

let tutPage = 0, petPage = 0, hospPage = 0, cobPage = 0, userPage = 0;

function g(id) { return document.getElementById(id); }

function toast(msg, type = 'info') {
  const d = document.createElement('div');
  d.className = 'toast toast-' + type;
  d.textContent = msg;
  document.body.appendChild(d);
  setTimeout(() => d.remove(), 4000);
}

function authHeaders() {
  return {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  };
}

async function api(method, url, body) {
  const options = { method, headers: authHeaders() };
  if (body) options.body = JSON.stringify(body);
  const response = await fetch(API + url, options);
  if (response.status === 401) {
    logout();
    throw new Error('Sessão expirada. Faça login novamente.');
  }
  if (response.status === 204) return null;
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const errorMsg = data && data.mensagem ? data.mensagem : 'Erro na requisição (' + response.status + ')';
    throw new Error(errorMsg);
  }
  return data;
}

function formatMoney(val) {
  if (val === null || val === undefined) return 'R$ 0,00';
  return 'R$ ' + Number(val).toFixed(2).replace('.', ',');
}

function formatDateTime(dtStr) {
  if (!dtStr) return '-';
  const d = new Date(dtStr);
  if (isNaN(d)) return dtStr;
  return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
}

function formatDate(dStr) {
  if (!dStr) return '-';
  const parts = dStr.split('-');
  if (parts.length === 3) return `${parts[2]}/${parts[1]}/${parts[0]}`;
  return dStr;
}

function openModal(contentHtml) {
  g('modalContent').innerHTML = contentHtml;
  g('modalOverlay').style.display = 'flex';
}

function closeModal() {
  g('modalOverlay').style.display = 'none';
  g('modalContent').innerHTML = '';
}

// ==== AUTH ====
async function login() {
  const email = g('loginEmail').value.trim();
  const senha = g('loginSenha').value;
  if (!email || !senha) {
    toast('Preencha e-mail e senha.', 'error');
    return;
  }
  try {
    const r = await fetch(API + '/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, senha })
    });
    const d = await r.json();
    if (!r.ok) throw new Error(d.mensagem || 'Credenciais inválidas');
    token = d.token;
    userRole = d.role;
    localStorage.setItem('jwt', token);
    localStorage.setItem('userRole', userRole);
    setupSession(d.nome, d.role);
    toast('Bem-vindo, ' + d.nome + '!', 'success');
  } catch (err) {
    toast(err.message || 'Falha no login', 'error');
  }
}

function logout() {
  token = '';
  userRole = '';
  localStorage.removeItem('jwt');
  localStorage.removeItem('userRole');
  g('loginPanel').style.display = '';
  g('dashboard').style.display = 'none';
  g('userInfo').style.display = 'none';
}

function setupSession(nome, role) {
  g('userName').textContent = nome;
  g('userRoleBadge').textContent = role;
  if (role === 'ADMIN') g('userRoleBadge').classList.add('admin');
  else g('userRoleBadge').classList.remove('admin');

  g('loginPanel').style.display = 'none';
  g('dashboard').style.display = '';
  g('userInfo').style.display = 'flex';

  document.querySelectorAll('.admin-only').forEach(el => {
    el.style.display = (role === 'ADMIN') ? '' : 'none';
  });

  loadDashboard();
}

// ==== 1. DASHBOARD ====
async function loadDashboard() {
  try {
    const data = await api('GET', '/api/dashboard');
    g('dashPetsPresentes').textContent = data.petsPresentesAgora;
    g('dashEntradasHoje').textContent = data.entradasPrevistasHoje;
    g('dashSaidasHoje').textContent = data.saidasPrevistasHoje;

    g('dashOcupacaoCreche').textContent = data.ocupacaoCrecheHoje;
    g('dashCapacidadeCreche').textContent = data.capacidadeCreche;
    const pctCreche = Math.min(100, Math.round((data.ocupacaoCrecheHoje / data.capacidadeCreche) * 100));
    g('barCreche').style.width = pctCreche + '%';
    g('barCreche').className = 'progress-bar-fill' + (pctCreche >= 100 ? ' danger' : pctCreche > 80 ? ' warning' : '');

    g('dashOcupacaoHotel').textContent = data.ocupacaoHotelHoje;
    g('dashCapacidadeHotel').textContent = data.capacidadeHotel;
    const pctHotel = Math.min(100, Math.round((data.ocupacaoHotelHoje / data.capacidadeHotel) * 100));
    g('barHotel').style.width = pctHotel + '%';
    g('barHotel').className = 'progress-bar-fill' + (pctHotel >= 100 ? ' danger' : pctHotel > 80 ? ' warning' : '');

    g('dashPendenciasValor').textContent = formatMoney(data.totalCobrancasPendentesValor);
    g('dashPendenciasQtd').textContent = data.totalCobrancasPendentesQtd;

    // Tabela Presentes
    let htmlPres = '';
    data.presentesNoMomento.forEach(h => {
      htmlPres += `<tr>
        <td><strong>${h.petNome}</strong></td>
        <td><span class="badge badge-${h.modalidade.toLowerCase()}">${h.modalidade}</span></td>
        <td>${formatDateTime(h.checkInEm || h.dataEntrada)}</td>
        <td><button class="btn btn-success btn-xs" onclick="fazerCheckOut(${h.id})">Check-out</button></td>
      </tr>`;
    });
    g('dashPresentesTable').innerHTML = htmlPres || '<tr><td colspan="4" class="empty">Nenhum animal presente no momento.</td></tr>';

    // Tabela Chegadas
    let htmlCheg = '';
    data.proximasChegadas.forEach(h => {
      htmlCheg += `<tr>
        <td><strong>${h.petNome}</strong></td>
        <td>${h.tutorNome || '-'}</td>
        <td>${formatDateTime(h.dataEntrada)}</td>
        <td><button class="btn btn-primary btn-xs" onclick="fazerCheckIn(${h.id})">Check-in</button></td>
      </tr>`;
    });
    g('dashChegadasTable').innerHTML = htmlCheg || '<tr><td colspan="4" class="empty">Nenhuma chegada agendada pendente.</td></tr>';
  } catch (err) {
    toast('Erro ao carregar dashboard: ' + err.message, 'error');
  }
}

// ==== 2. PRESENÇA DIÁRIA ====
async function loadPresenca() {
  try {
    const data = await api('GET', '/api/hospedagens?size=100');
    let htm = '';
    const relevantes = data.content.filter(h => h.status === 'AGENDADA' || h.status === 'EM_ANDAMENTO');
    relevantes.forEach(h => {
      let btns = '';
      if (h.status === 'AGENDADA') {
        btns = `<button class="btn btn-primary btn-xs" onclick="fazerCheckIn(${h.id})">Fazer Check-in</button>
                <button class="btn btn-warning btn-xs" onclick="marcarFalta(${h.id})">Registrar Falta</button>`;
      } else if (h.status === 'EM_ANDAMENTO') {
        btns = `<button class="btn btn-success btn-xs" onclick="fazerCheckOut(${h.id})">Fazer Check-out</button>`;
      }
      htm += `<tr>
        <td><strong>${h.petNome}</strong></td>
        <td>${h.tutorNome || '-'}</td>
        <td><span class="badge badge-${h.modalidade.toLowerCase()}">${h.modalidade}</span></td>
        <td>${formatDateTime(h.dataEntrada)} até ${formatDateTime(h.dataSaida)}</td>
        <td><span class="badge badge-${h.status.toLowerCase()}">${h.status}</span></td>
        <td><div style="display:flex;gap:6px">${btns}</div></td>
      </tr>`;
    });
    g('presencaTable').innerHTML = htm || '<tr><td colspan="6" class="empty">Nenhuma presença pendente para operar hoje.</td></tr>';
  } catch (err) {
    toast(err.message, 'error');
  }
}

async function fazerCheckIn(id) {
  try {
    await api('POST', `/api/hospedagens/${id}/check-in`);
    toast('Check-in realizado com sucesso!', 'success');
    if (currentTab === 'visao-geral') loadDashboard();
    else if (currentTab === 'presenca') loadPresenca();
    else loadHospedagens(-1);
  } catch (err) {
    toast('Erro no check-in: ' + err.message, 'error');
  }
}

async function fazerCheckOut(id) {
  try {
    await api('POST', `/api/hospedagens/${id}/check-out`);
    toast('Check-out concluído!', 'success');
    if (currentTab === 'visao-geral') loadDashboard();
    else if (currentTab === 'presenca') loadPresenca();
    else loadHospedagens(-1);
  } catch (err) {
    toast('Erro no check-out: ' + err.message, 'error');
  }
}

async function marcarFalta(id) {
  const motivo = prompt('Informe o motivo da falta:');
  if (motivo === null) return;
  if (motivo.trim().length < 3) {
    toast('O motivo da falta deve ter pelo menos 3 caracteres.', 'error');
    return;
  }
  try {
    await api('POST', `/api/hospedagens/${id}/falta?motivo=${encodeURIComponent(motivo.trim())}`);
    toast('Falta registrada!', 'success');
    if (currentTab === 'presenca') loadPresenca();
    else loadHospedagens(-1);
  } catch (err) {
    toast('Erro ao registrar falta: ' + err.message, 'error');
  }
}

// ==== 3. RESERVAS & AGENDA ====
async function loadHospedagens(dir) {
  if (dir === 0) hospPage = Math.max(0, hospPage - 1);
  else if (dir === 1) hospPage++;
  const status = g('filtroHospStatus').value;
  let url = `/api/hospedagens?page=${hospPage}&size=10`;
  try {
    const p = await api('GET', url);
    let h = '';
    let filtered = p.content;
    if (status) filtered = filtered.filter(x => x.status === status);

    filtered.forEach(hp => {
      let btns = '';
      if (hp.status === 'AGENDADA') {
        btns += `<button class="btn btn-primary btn-xs" onclick="fazerCheckIn(${hp.id})">Entrada</button>`;
        btns += `<button class="btn btn-warning btn-xs" onclick="marcarFalta(${hp.id})">Falta</button>`;
      }
      if (hp.status === 'EM_ANDAMENTO') {
        btns += `<button class="btn btn-success btn-xs" onclick="fazerCheckOut(${hp.id})">Saída</button>`;
      }
      if (userRole === 'ADMIN' && hp.status !== 'CANCELADA' && hp.status !== 'CONCLUIDA' && hp.status !== 'FALTA') {
        btns += `<button class="btn btn-danger btn-xs" onclick="cancelarHosp(${hp.id})">Cancelar</button>`;
      }
      h += `<tr>
        <td>${hp.id}</td>
        <td><strong>${hp.petNome}</strong></td>
        <td>${hp.tutorNome || '-'}</td>
        <td><span class="badge badge-${hp.modalidade.toLowerCase()}">${hp.modalidade}</span></td>
        <td>${formatDateTime(hp.dataEntrada)}</td>
        <td>${formatDateTime(hp.dataSaida)}</td>
        <td>${hp.precoAcordado ? formatMoney(hp.precoAcordado) : '-'}</td>
        <td><span class="badge badge-${hp.status.toLowerCase()}">${hp.status}</span></td>
        <td><div style="display:flex;gap:4px">${btns}</div></td>
      </tr>`;
    });
    g('hospTable').innerHTML = h || '<tr><td colspan="9" class="empty">Nenhuma hospedagem cadastrada.</td></tr>';
    g('hospPage').textContent = `Página ${p.number + 1} de ${p.totalPages || 1}`;
    g('hospPrev').disabled = p.first;
    g('hospNext').disabled = p.last;
  } catch (err) {
    toast(err.message, 'error');
  }
}

async function openHospModal() {
  const petsPage = await api('GET', '/api/pets?size=100');
  const servicos = await api('GET', '/api/servicos/lista').catch(() => []);

  let petOptions = petsPage.content.map(p => `<option value="${p.id}">${p.nome} (Tutor: ${p.tutorId})</option>`).join('');
  let servOptions = '<option value="">Sem serviço pré-selecionado</option>' +
    servicos.map(s => `<option value="${s.id}" data-preco="${s.preco}">${s.nome} - R$ ${s.preco}</option>`).join('');

  openModal(`
    <div class="modal-header">
      <h3>📅 Nova Reserva / Hospedagem</h3>
      <button class="modal-close" onclick="closeModal()">×</button>
    </div>
    <div class="form-group"><label>Pet</label><select id="fPetId">${petOptions}</select></div>
    <div class="form-group"><label>Modalidade</label>
      <select id="fModalidade">
        <option value="CRECHE">CRECHE</option>
        <option value="HOTEL">HOTEL</option>
      </select>
    </div>
    <div class="form-row">
      <div class="form-group"><label>Data/Hora Entrada</label><input type="datetime-local" id="fEnt"></div>
      <div class="form-group"><label>Data/Hora Saída</label><input type="datetime-local" id="fSai"></div>
    </div>
    <div class="form-group"><label>Serviço Vinculado</label><select id="fServId">${servOptions}</select></div>
    <div class="form-group"><label>Preço Acordado (R$)</label><input type="number" step="0.01" id="fPreco" placeholder="Ex: 75.00"></div>
    <div class="form-group"><label>Observações</label><input id="fObs" placeholder="Recomendações especiais"></div>
    <button class="btn btn-primary" id="saveHospBtn" style="width:100%;margin-top:10px">Confirmar Agendamento</button>
  `);

  g('fServId').onchange = (e) => {
    const opt = e.target.selectedOptions[0];
    if (opt && opt.dataset.preco) g('fPreco').value = opt.dataset.preco;
  };

  g('saveHospBtn').onclick = async () => {
    const petId = +g('fPetId').value;
    const modalidade = g('fModalidade').value;
    const dataEntrada = g('fEnt').value;
    const dataSaida = g('fSai').value;
    const servicoId = g('fServId').value ? +g('fServId').value : null;
    const precoAcordado = g('fPreco').value ? parseFloat(g('fPreco').value) : null;
    const observacoes = g('fObs').value || null;

    if (!dataEntrada || !dataSaida) {
      toast('Informe entrada e saída com data e horário.', 'error');
      return;
    }

    try {
      await api('POST', '/api/hospedagens', {
        petId, modalidade, dataEntrada, dataSaida, servicoId, precoAcordado, observacoes
      });
      closeModal();
      loadHospedagens(-1);
      toast('Reserva cadastrada com sucesso!', 'success');
    } catch (err) {
      toast(err.message, 'error');
    }
  };
}

async function cancelarHosp(id) {
  const motivo = prompt('Informe o motivo do cancelamento da reserva:');
  if (motivo === null) return;
  if (motivo.trim().length < 3) {
    toast('O motivo deve ter no mínimo 3 caracteres.', 'error');
    return;
  }
  try {
    await api('DELETE', `/api/hospedagens/${id}?motivo=${encodeURIComponent(motivo.trim())}`);
    toast('Reserva cancelada!', 'success');
    loadHospedagens(-1);
  } catch (err) {
    toast(err.message, 'error');
  }
}

// ==== 4. PETS ====
async function loadPets(dir) {
  if (dir === 0) petPage = Math.max(0, petPage - 1);
  else if (dir === 1) petPage++;
  try {
    const p = await api('GET', `/api/pets?page=${petPage}&size=10`);
    let h = '';
    p.content.forEach(x => {
      const saude = [x.porte, x.alergiasRestricoes ? 'Alergias: ' + x.alergiasRestricoes : '', x.vacinas ? 'Vacinas: ' + x.vacinas : '']
        .filter(Boolean).join(' · ') || 'Sem restrições';

      h += `<tr>
        <td>${x.id}</td>
        <td><strong>${x.nome}</strong></td>
        <td>${x.raca}</td>
        <td>${x.porte || '-'}</td>
        <td>${formatDate(x.dataNascimento)}</td>
        <td>ID ${x.tutorId}</td>
        <td><small style="color:var(--muted)">${saude}</small></td>
        <td>
          <button class="btn btn-primary btn-xs" onclick="openPetModal(${x.id})">Editar</button>
          ${userRole === 'ADMIN' ? `<button class="btn btn-danger btn-xs" onclick="delPet(${x.id})">Excluir</button>` : ''}
        </td>
      </tr>`;
    });
    g('petsTable').innerHTML = h || '<tr><td colspan="8" class="empty">Nenhum pet cadastrado.</td></tr>';
    g('petPage').textContent = `Página ${p.number + 1} de ${p.totalPages || 1}`;
    g('petPrev').disabled = p.first;
    g('petNext').disabled = p.last;
  } catch (err) {
    toast(err.message, 'error');
  }
}

async function openPetModal(id) {
  const pet = id ? await api('GET', '/api/pets/' + id) : null;
  const tutores = await api('GET', '/api/tutores?size=100');
  const tutorOpts = tutores.content.map(t =>
    `<option value="${t.id}" ${pet && pet.tutorId === t.id ? 'selected' : ''}>${t.nome} (${t.telefone})</option>`
  ).join('');

  openModal(`
    <div class="modal-header">
      <h3>${id ? 'Editar' : 'Novo'} Pet</h3>
      <button class="modal-close" onclick="closeModal()">×</button>
    </div>
    <div class="form-row">
      <div class="form-group"><label>Nome</label><input id="pNome" value="${pet ? pet.nome : ''}"></div>
      <div class="form-group"><label>Raça</label><input id="pRaca" value="${pet ? pet.raca : ''}"></div>
    </div>
    <div class="form-row">
      <div class="form-group"><label>Porte</label>
        <select id="pPorte">
          <option value="PEQUENO" ${pet && pet.porte === 'PEQUENO' ? 'selected' : ''}>Pequeno</option>
          <option value="MEDIO" ${pet && pet.porte === 'MEDIO' ? 'selected' : ''}>Médio</option>
          <option value="GRANDE" ${pet && pet.porte === 'GRANDE' ? 'selected' : ''}>Grande</option>
        </select>
      </div>
      <div class="form-group"><label>Sexo</label>
        <select id="pSexo">
          <option value="MACHO" ${pet && pet.sexo === 'MACHO' ? 'selected' : ''}>Macho</option>
          <option value="FEMEA" ${pet && pet.sexo === 'FEMEA' ? 'selected' : ''}>Fêmea</option>
        </select>
      </div>
    </div>
    <div class="form-row">
      <div class="form-group"><label>Nascimento</label><input type="date" id="pNasc" value="${pet ? pet.dataNascimento : ''}"></div>
      <div class="form-group"><label>Tutor</label><select id="pTutor">${tutorOpts}</select></div>
    </div>
    <div class="form-group"><label>Alergias / Restrições Médicas</label><input id="pAlergias" value="${pet && pet.alergiasRestricoes ? pet.alergiasRestricoes : ''}" placeholder="Ex: Alergia a frango, cardíaco..."></div>
    <div class="form-group"><label>Informações de Vacinas</label><input id="pVacinas" value="${pet && pet.vacinas ? pet.vacinas : ''}" placeholder="Ex: V10 válida até 12/2026, Raiva ok"></div>
    <div class="form-group"><label>Observações Gerais</label><textarea id="pObs" placeholder="Comportamento com outros cães, medos...">${pet && pet.observacoes ? pet.observacoes : ''}</textarea></div>
    <button class="btn btn-primary" id="savePetBtn" style="width:100%;margin-top:8px">Salvar Dados do Pet</button>
  `);

  g('savePetBtn').onclick = async () => {
    const body = {
      nome: g('pNome').value.trim(),
      raca: g('pRaca').value.trim(),
      porte: g('pPorte').value,
      sexo: g('pSexo').value,
      especie: 'CANINO',
      dataNascimento: g('pNasc').value || null,
      alergiasRestricoes: g('pAlergias').value.trim() || null,
      vacinas: g('pVacinas').value.trim() || null,
      observacoes: g('pObs').value.trim() || null
    };
    const tutorId = g('pTutor').value;
    try {
      if (id) await api('PUT', '/api/pets/' + id, body);
      else await api('POST', '/api/pets/' + tutorId, body);
      closeModal();
      loadPets(-1);
      toast(id ? 'Pet atualizado!' : 'Pet cadastrado com sucesso!', 'success');
    } catch (err) {
      toast(err.message, 'error');
    }
  };
}

async function delPet(id) {
  if (!confirm(`Confirma a exclusão lógica do pet ${id}?`)) return;
  try {
    await api('DELETE', '/api/pets/' + id);
    loadPets(-1);
    toast('Pet desativado com sucesso!', 'success');
  } catch (err) {
    toast(err.message, 'error');
  }
}

// ==== 5. TUTORES ====
async function loadTutores(dir) {
  if (dir === 0) tutPage = Math.max(0, tutPage - 1);
  else if (dir === 1) tutPage++;
  try {
    const p = await api('GET', `/api/tutores?page=${tutPage}&size=10`);
    let h = '';
    p.content.forEach(t => {
      h += `<tr>
        <td>${t.id}</td>
        <td><strong>${t.nome}</strong></td>
        <td>${t.telefone}</td>
        <td>${t.cpf || '-'}</td>
        <td>${t.email || '-'}</td>
        <td>${t.observacoes || '-'}</td>
        <td>
          <button class="btn btn-primary btn-xs" onclick="openTutorModal(${t.id})">Editar</button>
          ${userRole === 'ADMIN' ? `<button class="btn btn-danger btn-xs" onclick="delTutor(${t.id})">Excluir</button>` : ''}
        </td>
      </tr>`;
    });
    g('tutoresTable').innerHTML = h || '<tr><td colspan="7" class="empty">Nenhum tutor cadastrado.</td></tr>';
    g('tutPage').textContent = `Página ${p.number + 1} de ${p.totalPages || 1}`;
    g('tutPrev').disabled = p.first;
    g('tutNext').disabled = p.last;
  } catch (err) {
    toast(err.message, 'error');
  }
}

async function openTutorModal(id) {
  const t = id ? await api('GET', '/api/tutores/' + id) : null;
  openModal(`
    <div class="modal-header">
      <h3>${id ? 'Editar' : 'Novo'} Tutor</h3>
      <button class="modal-close" onclick="closeModal()">×</button>
    </div>
    <div class="form-group"><label>Nome Completo</label><input id="tNome" value="${t ? t.nome : ''}"></div>
    <div class="form-row">
      <div class="form-group"><label>Telefone com DDD</label><input id="tTel" value="${t ? t.telefone : ''}" placeholder="(11) 98765-4321"></div>
      <div class="form-group"><label>CPF</label><input id="tCpf" value="${t && t.cpf ? t.cpf : ''}" placeholder="000.000.000-00"></div>
    </div>
    <div class="form-group"><label>E-mail</label><input type="email" id="tEmail" value="${t && t.email ? t.email : ''}" placeholder="cliente@email.com"></div>
    <div class="form-group"><label>Observações Operacionais</label><textarea id="tObs" placeholder="Horários de preferência, contato de emergência...">${t && t.observacoes ? t.observacoes : ''}</textarea></div>
    <button class="btn btn-primary" id="saveTutorBtn" style="width:100%;margin-top:8px">Salvar Dados do Tutor</button>
  `);

  g('saveTutorBtn').onclick = async () => {
    const body = {
      nome: g('tNome').value.trim(),
      telefone: g('tTel').value.trim(),
      cpf: g('tCpf').value.trim(),
      email: g('tEmail').value.trim() || null,
      observacoes: g('tObs').value.trim() || null
    };
    try {
      if (id) await api('PUT', '/api/tutores/' + id, body);
      else await api('POST', '/api/tutores', body);
      closeModal();
      loadTutores(-1);
      toast(id ? 'Tutor atualizado!' : 'Tutor cadastrado!', 'success');
    } catch (err) {
      toast(err.message, 'error');
    }
  };
}

async function delTutor(id) {
  if (!confirm(`Confirma a exclusão lógica do tutor ${id}?`)) return;
  try {
    await api('DELETE', '/api/tutores/' + id);
    loadTutores(-1);
    toast('Tutor desativado com sucesso!', 'success');
  } catch (err) {
    toast(err.message, 'error');
  }
}

// ==== 6. SERVIÇOS ====
async function loadServicos() {
  try {
    const servicos = await api('GET', '/api/servicos/todos');
    let h = '';
    servicos.content.forEach(s => {
      const statusBadge = s.ativo ? '<span class="badge badge-ativo">Ativo</span>' : '<span class="badge badge-inativo">Inativo</span>';
      const toggleBtn = s.ativo
        ? `<button class="btn btn-danger btn-xs" onclick="toggleServico(${s.id}, false)">Desativar</button>`
        : `<button class="btn btn-success btn-xs" onclick="toggleServico(${s.id}, true)">Ativar</button>`;

      h += `<tr>
        <td>${s.id}</td>
        <td><strong>${s.nome}</strong><br><small style="color:var(--muted)">${s.descricao || ''}</small></td>
        <td>${s.tipo}</td>
        <td>${formatMoney(s.preco)}</td>
        <td>${s.unidade}</td>
        <td>${statusBadge}</td>
        <td>
          <div style="display:flex;gap:4px">
            <button class="btn btn-primary btn-xs" onclick="openServicoModal(${s.id})">Editar</button>
            ${toggleBtn}
          </div>
        </td>
      </tr>`;
    });
    g('servicosTable').innerHTML = h || '<tr><td colspan="7" class="empty">Nenhum serviço cadastrado no catálogo.</td></tr>';
  } catch (err) {
    toast(err.message, 'error');
  }
}

async function openServicoModal(id) {
  const s = id ? await api('GET', '/api/servicos/' + id) : null;
  openModal(`
    <div class="modal-header">
      <h3>${id ? 'Editar' : 'Novo'} Serviço</h3>
      <button class="modal-close" onclick="closeModal()">×</button>
    </div>
    <div class="form-group"><label>Nome do Serviço</label><input id="sNome" value="${s ? s.nome : ''}" placeholder="Ex: Diária Creche Meio Período"></div>
    <div class="form-row">
      <div class="form-group"><label>Tipo</label>
        <select id="sTipo">
          <option value="CRECHE" ${s && s.tipo === 'CRECHE' ? 'selected' : ''}>CRECHE</option>
          <option value="HOTEL" ${s && s.tipo === 'HOTEL' ? 'selected' : ''}>HOTEL</option>
          <option value="BANHO_TOSA" ${s && s.tipo === 'BANHO_TOSA' ? 'selected' : ''}>BANHO E TOSA</option>
          <option value="OUTRO" ${s && s.tipo === 'OUTRO' ? 'selected' : ''}>OUTRO</option>
        </select>
      </div>
      <div class="form-group"><label>Cobrança</label>
        <select id="sUnidade">
          <option value="DIARIA" ${s && s.unidade === 'DIARIA' ? 'selected' : ''}>Por Diária</option>
          <option value="HORA" ${s && s.unidade === 'HORA' ? 'selected' : ''}>Por Hora</option>
          <option value="UNIDADE" ${s && s.unidade === 'UNIDADE' ? 'selected' : ''}>Por Unidade / Sessão</option>
        </select>
      </div>
    </div>
    <div class="form-group"><label>Preço Atual (R$)</label><input type="number" step="0.01" id="sPreco" value="${s ? s.preco : ''}" placeholder="Ex: 80.00"></div>
    <div class="form-group"><label>Descrição Detalhada</label><textarea id="sDesc">${s && s.descricao ? s.descricao : ''}</textarea></div>
    <button class="btn btn-primary" id="saveServBtn" style="width:100%;margin-top:8px">Salvar Serviço</button>
  `);

  g('saveServBtn').onclick = async () => {
    const body = {
      nome: g('sNome').value.trim(),
      tipo: g('sTipo').value,
      unidade: g('sUnidade').value,
      preco: parseFloat(g('sPreco').value),
      descricao: g('sDesc').value.trim() || null
    };
    try {
      if (id) await api('PUT', '/api/servicos/' + id, body);
      else await api('POST', '/api/servicos', body);
      closeModal();
      loadServicos();
      toast(id ? 'Serviço atualizado!' : 'Serviço cadastrado com sucesso!', 'success');
    } catch (err) {
      toast(err.message, 'error');
    }
  };
}

async function toggleServico(id, ativo) {
  try {
    await api('PATCH', `/api/servicos/${id}/status?ativo=${ativo}`);
    loadServicos();
    toast(`Serviço ${ativo ? 'ativado' : 'desativado'} com sucesso!`, 'success');
  } catch (err) {
    toast(err.message, 'error');
  }
}

// ==== 7. FINANCEIRO ====
async function loadCobrancas(dir) {
  if (dir === 0) cobPage = Math.max(0, cobPage - 1);
  else if (dir === 1) cobPage++;
  const status = g('filtroCobrancaStatus').value;
  let url = `/api/cobrancas?page=${cobPage}&size=10`;
  if (status) url += `&status=${status}`;
  try {
    const p = await api('GET', url);
    let h = '';
    p.content.forEach(c => {
      let btns = '';
      if (c.status !== 'PAGA' && c.status !== 'CANCELADA') {
        btns += `<button class="btn btn-success btn-xs" onclick="openPagamentoModal(${c.id}, ${c.saldoDevedor})">Pagar</button>`;
      }
      if (userRole === 'ADMIN' && c.status !== 'CANCELADA' && Number(c.valorPago) === 0) {
        btns += `<button class="btn btn-danger btn-xs" onclick="cancelarCobranca(${c.id})">Cancelar</button>`;
      }
      h += `<tr>
        <td>${c.id}</td>
        <td><strong>${c.tutorNome || 'Tutor #' + c.tutorId}</strong></td>
        <td>${c.descricao}</td>
        <td>${formatMoney(c.valorCobrado)}</td>
        <td>${formatMoney(c.valorPago)}</td>
        <td style="color:${c.saldoDevedor > 0 ? '#f59e0b' : '#10b981'};font-weight:700">${formatMoney(c.saldoDevedor)}</td>
        <td>${formatDate(c.dataVencimento)}</td>
        <td><span class="badge badge-${c.status.toLowerCase()}">${c.status}</span></td>
        <td><div style="display:flex;gap:4px">${btns}</div></td>
      </tr>`;
    });
    g('cobrancasTable').innerHTML = h || '<tr><td colspan="9" class="empty">Nenhuma fatura encontrada.</td></tr>';
    g('cobPage').textContent = `Página ${p.number + 1} de ${p.totalPages || 1}`;
    g('cobPrev').disabled = p.first;
    g('cobNext').disabled = p.last;
  } catch (err) {
    toast(err.message, 'error');
  }
}

async function openCobrancaModal() {
  const tutores = await api('GET', '/api/tutores?size=100');
  const tutorOpts = tutores.content.map(t => `<option value="${t.id}">${t.nome}</option>`).join('');

  openModal(`
    <div class="modal-header">
      <h3>💰 Emitir Nova Cobrança</h3>
      <button class="modal-close" onclick="closeModal()">×</button>
    </div>
    <div class="form-group"><label>Tutor Responsável</label><select id="cTutorId">${tutorOpts}</select></div>
    <div class="form-group"><label>Descrição da Fatura</label><input id="cDesc" placeholder="Ex: Diárias Creche Novembro"></div>
    <div class="form-row">
      <div class="form-group"><label>Valor Total (R$)</label><input type="number" step="0.01" id="cValor" placeholder="Ex: 150.00"></div>
      <div class="form-group"><label>Desconto Autorizado (R$)</label><input type="number" step="0.01" id="cDescVal" value="0.00"></div>
    </div>
    <div class="form-group"><label>Vencimento</label><input type="date" id="cVenc"></div>
    <div class="form-group"><label>Observações</label><input id="cObs" placeholder="Detalhes opcionais"></div>
    <button class="btn btn-primary" id="saveCobBtn" style="width:100%;margin-top:8px">Gerar Cobrança</button>
  `);

  g('saveCobBtn').onclick = async () => {
    const body = {
      tutorId: +g('cTutorId').value,
      descricao: g('cDesc').value.trim(),
      valorTotal: parseFloat(g('cValor').value),
      desconto: parseFloat(g('cDescVal').value || 0),
      dataVencimento: g('cVenc').value,
      observacoes: g('cObs').value.trim() || null
    };
    try {
      await api('POST', '/api/cobrancas', body);
      closeModal();
      loadCobrancas(-1);
      toast('Cobrança gerada com sucesso!', 'success');
    } catch (err) {
      toast(err.message, 'error');
    }
  };
}

function openPagamentoModal(cobrancaId, saldoDevedor) {
  openModal(`
    <div class="modal-header">
      <h3>💳 Registrar Pagamento (Fatura #${cobrancaId})</h3>
      <button class="modal-close" onclick="closeModal()">×</button>
    </div>
    <p style="font-size:13px;color:var(--muted);margin-bottom:12px">
      Saldo devedor restante: <strong style="color:#fbbf24">${formatMoney(saldoDevedor)}</strong>
    </p>
    <div class="form-group"><label>Valor Pago (R$)</label><input type="number" step="0.01" id="pValor" value="${saldoDevedor.toFixed(2)}"></div>
    <div class="form-group"><label>Método de Pagamento</label>
      <select id="pForma">
        <option value="PIX">PIX</option>
        <option value="DINHEIRO">Dinheiro</option>
        <option value="CARTAO_CREDITO">Cartão de Crédito</option>
        <option value="CARTAO_DEBITO">Cartão de Débito</option>
        <option value="TRANSFERENCIA">Transferência Bancária</option>
      </select>
    </div>
    <div class="form-group"><label>Identificador / Comprovante (opcional)</label><input id="pComp" placeholder="Ex: PIX9873645"></div>
    <div class="form-group"><label>Observações</label><input id="pObsPag" placeholder="Informações adicionais"></div>
    <button class="btn btn-success" id="confirmPagBtn" style="width:100%;margin-top:8px">Confirmar Recebimento</button>
  `);

  g('confirmPagBtn').onclick = async () => {
    const body = {
      cobrancaId,
      valor: parseFloat(g('pValor').value),
      formaPagamento: g('pForma').value,
      comprovante: g('pComp').value.trim() || null,
      observacoes: g('pObsPag').value.trim() || null
    };
    try {
      await api('POST', `/api/cobrancas/${cobrancaId}/pagamentos`, body);
      closeModal();
      loadCobrancas(-1);
      toast('Pagamento registrado com sucesso!', 'success');
    } catch (err) {
      toast(err.message, 'error');
    }
  };
}

async function cancelarCobranca(id) {
  const motivo = prompt('Informe o motivo do cancelamento da cobrança:');
  if (motivo === null) return;
  try {
    await api('DELETE', `/api/cobrancas/${id}?motivo=${encodeURIComponent(motivo.trim())}`);
    toast('Cobrança cancelada!', 'success');
    loadCobrancas(-1);
  } catch (err) {
    toast(err.message, 'error');
  }
}

// ==== 8. FUNCIONÁRIOS ====
async function loadUsuarios(dir) {
  if (dir === 0) userPage = Math.max(0, userPage - 1);
  else if (dir === 1) userPage++;
  try {
    const p = await api('GET', `/api/usuarios?page=${userPage}&size=10`);
    let h = '';
    p.content.forEach(u => {
      const statusBadge = u.ativo ? '<span class="badge badge-ativo">Ativo</span>' : '<span class="badge badge-inativo">Desativado</span>';
      const toggleBtn = u.ativo
        ? `<button class="btn btn-danger btn-xs" onclick="toggleUsuario(${u.id}, false)">Desativar</button>`
        : `<button class="btn btn-success btn-xs" onclick="toggleUsuario(${u.id}, true)">Ativar</button>`;

      h += `<tr>
        <td>${u.id}</td>
        <td><strong>${u.nome}</strong></td>
        <td>${u.email}</td>
        <td><span class="user-badge ${u.role === 'ADMIN' ? 'admin' : ''}">${u.role}</span></td>
        <td>${statusBadge}</td>
        <td>${formatDateTime(u.criadoEm)}</td>
        <td>
          <div style="display:flex;gap:4px">
            <button class="btn btn-primary btn-xs" onclick="openUsuarioModal(${u.id})">Editar</button>
            <button class="btn btn-warning btn-xs" onclick="openTrocarSenhaModal(${u.id})">Senha</button>
            ${toggleBtn}
          </div>
        </td>
      </tr>`;
    });
    g('usuariosTable').innerHTML = h || '<tr><td colspan="7" class="empty">Nenhum funcionário cadastrado.</td></tr>';
    g('userPage').textContent = `Página ${p.number + 1} de ${p.totalPages || 1}`;
    g('userPrev').disabled = p.first;
    g('userNext').disabled = p.last;
  } catch (err) {
    toast(err.message, 'error');
  }
}

async function openUsuarioModal(id) {
  const u = id ? await api('GET', '/api/usuarios/' + id) : null;
  openModal(`
    <div class="modal-header">
      <h3>${id ? 'Editar Usuário' : 'Novo Funcionário / Usuário'}</h3>
      <button class="modal-close" onclick="closeModal()">×</button>
    </div>
    <div class="form-group"><label>Nome</label><input id="uNome" value="${u ? u.nome : ''}"></div>
    <div class="form-group"><label>E-mail</label><input type="email" id="uEmail" value="${u ? u.email : ''}"></div>
    ${!id ? `<div class="form-group"><label>Senha Inicial (mínimo 8 caracteres)</label><input type="password" id="uSenha"></div>` : ''}
    <div class="form-group"><label>Perfil de Acesso</label>
      <select id="uRole">
        <option value="FUNCIONARIO" ${u && u.role === 'FUNCIONARIO' ? 'selected' : ''}>FUNCIONARIO (Operacional)</option>
        <option value="ADMIN" ${u && u.role === 'ADMIN' ? 'selected' : ''}>ADMIN (Dono / Administrador)</option>
      </select>
    </div>
    <button class="btn btn-primary" id="saveUserBtn" style="width:100%;margin-top:8px">${id ? 'Salvar Alterações' : 'Criar Conta'}</button>
  `);

  g('saveUserBtn').onclick = async () => {
    try {
      if (id) {
        await api('PUT', '/api/usuarios/' + id, {
          nome: g('uNome').value.trim(),
          email: g('uEmail').value.trim(),
          role: g('uRole').value
        });
        toast('Usuário atualizado!', 'success');
      } else {
        await api('POST', '/api/auth/register', {
          nome: g('uNome').value.trim(),
          email: g('uEmail').value.trim(),
          senha: g('uSenha').value,
          role: g('uRole').value
        });
        toast('Usuário cadastrado com sucesso!', 'success');
      }
      closeModal();
      loadUsuarios(-1);
    } catch (err) {
      toast(err.message, 'error');
    }
  };
}

function openTrocarSenhaModal(id) {
  openModal(`
    <div class="modal-header">
      <h3>🔑 Redefinir Senha do Usuário #${id}</h3>
      <button class="modal-close" onclick="closeModal()">×</button>
    </div>
    <div class="form-group"><label>Nova Senha (mínimo 8 caracteres)</label><input type="password" id="novaSenhaInput"></div>
    <button class="btn btn-primary" id="saveSenhaBtn" style="width:100%;margin-top:8px">Salvar Nova Senha</button>
  `);

  g('saveSenhaBtn').onclick = async () => {
    const novaSenha = g('novaSenhaInput').value;
    if (!novaSenha || novaSenha.length < 8) {
      toast('A senha deve ter no mínimo 8 caracteres.', 'error');
      return;
    }
    try {
      await api('PATCH', `/api/usuarios/${id}/senha`, { novaSenha });
      closeModal();
      toast('Senha redefinida com sucesso!', 'success');
    } catch (err) {
      toast(err.message, 'error');
    }
  };
}

async function toggleUsuario(id, ativo) {
  try {
    await api('PATCH', `/api/usuarios/${id}/status?ativo=${ativo}`);
    loadUsuarios(-1);
    toast(`Usuário ${ativo ? 'ativado' : 'desativado'} com sucesso!`, 'success');
  } catch (err) {
    toast(err.message, 'error');
  }
}

// ==== 9. RELATÓRIOS ====
async function gerarRelatorios() {
  const inicio = g('relDataInicio').value;
  const fim = g('relDataFim').value;
  let q = '';
  if (inicio && fim) q = `?inicio=${inicio}&fim=${fim}`;

  try {
    const fin = await api('GET', '/api/relatorios/financeiro' + q);
    g('relTotalEmitido').textContent = formatMoney(fin.totalEmitido);
    g('relQtdCobrancas').textContent = `${fin.quantidadeCobrancas} fatura(s) gerada(s)`;
    g('relTotalRecebido').textContent = formatMoney(fin.totalRecebido);
    g('relQtdPagamentos').textContent = `${fin.quantidadePagamentos} pagamento(s) recebido(s)`;
    g('relTotalPendente').textContent = formatMoney(fin.totalPendente);

    const reservas = await api('GET', '/api/relatorios/reservas' + q);
    let h = '';
    reservas.forEach(r => {
      h += `<tr>
        <td>${r.id}</td>
        <td><strong>${r.petNome}</strong></td>
        <td>${r.tutorNome || '-'}</td>
        <td>${r.modalidade}</td>
        <td>${formatDateTime(r.dataEntrada)}</td>
        <td>${formatDateTime(r.dataSaida)}</td>
        <td><span class="badge badge-${r.status.toLowerCase()}">${r.status}</span></td>
      </tr>`;
    });
    g('relReservasTable').innerHTML = h || '<tr><td colspan="7" class="empty">Nenhuma reserva encontrada no período.</td></tr>';
  } catch (err) {
    toast(err.message, 'error');
  }
}

// ==== NAVEGAÇÃO DE ABAS ====
document.querySelectorAll('.tab').forEach(btn => {
  btn.onclick = function() {
    document.querySelectorAll('.tab').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
    this.classList.add('active');
    const tabName = this.dataset.tab;
    currentTab = tabName;
    g('panel-' + tabName).classList.add('active');

    if (tabName === 'visao-geral') loadDashboard();
    else if (tabName === 'presenca') loadPresenca();
    else if (tabName === 'agenda') loadHospedagens(0);
    else if (tabName === 'pets') loadPets(0);
    else if (tabName === 'tutores') loadTutores(0);
    else if (tabName === 'servicos') loadServicos();
    else if (tabName === 'financeiro') loadCobrancas(0);
    else if (tabName === 'funcionarios') loadUsuarios(0);
    else if (tabName === 'relatorios') {
      const hoje = new Date().toISOString().split('T')[0];
      const inicioMes = new Date(new Date().setDate(1)).toISOString().split('T')[0];
      g('relDataInicio').value = inicioMes;
      g('relDataFim').value = hoje;
      gerarRelatorios();
    }
  };
});

// ==== INICIALIZAÇÃO AUTOMÁTICA ====
if (token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    setupSession(payload.sub, userRole || 'FUNCIONARIO');
  } catch (e) {
    logout();
  }
}