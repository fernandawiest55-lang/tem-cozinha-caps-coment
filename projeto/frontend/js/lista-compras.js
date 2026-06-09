// JS da pagina de lista de compras

// Lista global de itens
var itens = [];

document.addEventListener('DOMContentLoaded', async function() {

  await carregarLista();

  // Botao de adicionar item rapido
  document.getElementById('btnAdicionarRapido').addEventListener('click', adicionarRapido);

  // Enter no campo de texto tambem adiciona
  document.getElementById('novoItemInput').addEventListener('keydown', function(e) {
    if (e.key === 'Enter') adicionarRapido();
  });

  // Botao de compartilhar lista
  document.getElementById('btnCompartilhar').addEventListener('click', compartilharLista);
});

// Busca os itens da lista na API
async function carregarLista() {
  try {
    itens = await apiFetch('/lista-compras');
    renderLista();
  } catch (e) {
    document.getElementById('listaBody').innerHTML = '<div class="loading-placeholder">Erro ao carregar lista.</div>';
  }
}

// Monta o HTML da lista agrupada por categoria
function renderLista() {
  var container = document.getElementById('listaBody');

  if (!itens || itens.length === 0) {
    container.innerHTML =
      '<div class="empty-state">' +
      '<div class="empty-icon">🛒</div>' +
      '<h3>Lista vazia</h3>' +
      '<p>Adicione itens manualmente ou gere a lista a partir de uma receita.</p>' +
      '<a href="receitas.html" class="btn-primary" style="display:inline-flex;width:auto;padding:12px 24px;margin-top:16px;">Ver Receitas</a>' +
      '</div>';
    atualizarProgresso();
    return;
  }

  // Agrupa os itens por categoria
  var grupos = {};
  for (var i = 0; i < itens.length; i++) {
    var cat = itens[i].categoria || 'Geral';
    if (!grupos[cat]) grupos[cat] = [];
    grupos[cat].push(itens[i]);
  }

  var html = '';

  // Para cada categoria, monta o bloco de itens
  for (var cat in grupos) {
    html += '<div style="padding:0 16px;">';
    html += '<div style="font-size:11px;text-transform:uppercase;letter-spacing:1px;color:var(--texto-claro);font-weight:600;padding:16px 0 8px;">' + cat + '</div>';

    var lista = grupos[cat];
    for (var j = 0; j < lista.length; j++) {
      var item = lista[j];
      var riscado = item.comprado ? 'text-decoration:line-through;color:var(--texto-claro);' : 'color:var(--texto-escuro);font-weight:500;';

      html += '<div class="lista-item-row ' + (item.comprado ? 'comprado' : '') + '" id="row-' + item.id + '">' +
        '<label style="display:flex;align-items:center;gap:12px;flex:1;cursor:pointer;padding:12px 0;border-bottom:1px solid var(--creme-escuro);">' +
          '<input type="checkbox" ' + (item.comprado ? 'checked' : '') + ' onchange="marcarItem(' + item.id + ', this)" style="width:18px;height:18px;accent-color:var(--verde-claro);cursor:pointer;flex-shrink:0;">' +
          '<span style="' + riscado + '">' + item.nome + '</span>' +
          (item.quantidade ? '<span style="font-size:13px;color:var(--texto-claro);margin-left:auto;">' + item.quantidade + '</span>' : '') +
        '</label>' +
        '<button onclick="excluirItem(' + item.id + ')" style="background:none;border:none;cursor:pointer;color:var(--texto-claro);padding:12px 8px;">' +
          '<svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>' +
        '</button>' +
      '</div>';
    }

    html += '</div>';
  }

  container.innerHTML = html;
  atualizarProgresso();
}

// Atualiza a barra de progresso de compras
function atualizarProgresso() {
  var total     = itens.length;
  var comprados = 0;

  for (var i = 0; i < itens.length; i++) {
    if (itens[i].comprado) comprados++;
  }

  var porcentagem = total > 0 ? Math.round((comprados / total) * 100) : 0;

  document.getElementById('progressoText').textContent = comprados + ' de ' + total;
  document.getElementById('progressoBar').style.width  = porcentagem + '%';
}

// Marca ou desmarca um item como comprado
async function marcarItem(id, checkbox) {
  var comprado = checkbox.checked;

  try {
    await apiFetch('/lista-compras/' + id, {
      method: 'PUT',
      body: JSON.stringify({ comprado: comprado })
    });

    // Atualiza o objeto local
    for (var i = 0; i < itens.length; i++) {
      if (itens[i].id === id) { itens[i].comprado = comprado; break; }
    }

    atualizarProgresso();

    // Atualiza o estilo do texto sem recarregar tudo
    var span = checkbox.parentElement.querySelector('span');
    if (span) {
      span.style.textDecoration = comprado ? 'line-through' : 'none';
      span.style.color          = comprado ? 'var(--texto-claro)' : 'var(--texto-escuro)';
    }

  } catch (e) {
    checkbox.checked = !comprado; // reverte se der erro
  }
}

// Remove um item da lista
async function excluirItem(id) {
  try {
    await apiFetch('/lista-compras/' + id, { method: 'DELETE' });

    // Remove do array local e re-renderiza
    var novos = [];
    for (var i = 0; i < itens.length; i++) {
      if (itens[i].id !== id) novos.push(itens[i]);
    }
    itens = novos;
    renderLista();

  } catch (err) {
    alert('Erro ao remover: ' + err.message);
  }
}

// Adiciona um item novo pelo campo de texto rapido
async function adicionarRapido() {
  var nome      = document.getElementById('novoItemInput').value.trim();
  var quantidade = document.getElementById('novoItemQtd').value.trim();

  if (!nome) return;

  try {
    var novo = await apiFetch('/lista-compras', {
      method: 'POST',
      body: JSON.stringify({ nome: nome, quantidade: quantidade })
    });

    itens.push(novo);

    // Limpa os campos
    document.getElementById('novoItemInput').value = '';
    document.getElementById('novoItemQtd').value   = '';

    renderLista();

  } catch (err) {
    alert('Erro: ' + err.message);
  }
}

// Compartilha a lista via Web Share API ou copia para a area de transferencia
function compartilharLista() {
  var pendentes = [];
  for (var i = 0; i < itens.length; i++) {
    if (!itens[i].comprado) pendentes.push(itens[i]);
  }

  if (pendentes.length === 0) {
    alert('Lista vazia!');
    return;
  }

  var texto = '';
  for (var j = 0; j < pendentes.length; j++) {
    var item = pendentes[j];
    texto += '• ' + item.nome + (item.quantidade ? ' - ' + item.quantidade : '') + '\n';
  }

  if (navigator.share) {
    navigator.share({ title: 'Minha Lista de Compras', text: texto });
  } else {
    navigator.clipboard.writeText(texto);
    alert('Lista copiada para a area de transferencia!');
  }
}
