// JS do dashboard: carrega os dados do usuario ao abrir a pagina

document.addEventListener('DOMContentLoaded', async function() {

  // Carrega tudo em paralelo pra ser mais rapido
  carregarStats();
  carregarReceitas();
  carregarDespensa();
  carregarLista();
});

// Carrega os numeros dos cards de estatistica
async function carregarStats() {
  try {
    var despensa   = await apiFetch('/ingredientes');
    var favoritos  = await apiFetch('/favoritos');
    var lista      = await apiFetch('/lista-compras');

    document.getElementById('statIngredientes').textContent = despensa.length  || 0;
    document.getElementById('statFavoritos').textContent    = favoritos.length || 0;
    document.getElementById('statLista').textContent        = lista.length     || 0;
    document.getElementById('statReceitas').textContent     = '∞';
  } catch (e) {
    // Se der erro, deixa os zeros mesmo
  }
}

// Carrega as receitas sugeridas pela IA no dashboard
async function carregarReceitas() {
  var container = document.getElementById('receitasList');

  try {
    var dados = await apiFetch('/receitas/sugestoes');

    if (!dados || dados.length === 0) {
      container.innerHTML = '<div class="empty-state" style="padding:32px;grid-column:span 3;">' +
        '<div class="empty-icon">🤖</div>' +
        '<h3>Adicione ingredientes</h3>' +
        '<p>Va para a Despensa e adicione ingredientes para receber sugestoes!</p>' +
        '</div>';
      return;
    }

    // Monta os mini cards de receita
    var html = '';
    for (var i = 0; i < dados.length; i++) {
      var r = dados[i];
      html += '<div class="receita-mini-card" onclick="window.location.href=\'receitas.html\'">' +
        '<div class="receita-mini-emoji">' + emojiReceita(r.titulo) + '</div>' +
        '<div class="receita-mini-titulo">' + r.titulo + '</div>' +
        '<div class="receita-mini-meta">' +
        '<span class="receita-mini-tag">' + (r.dificuldade || 'Facil') + '</span>' +
        '<span>⏱ ' + formatTempo(r.tempoPreparo) + '</span>' +
        '</div>' +
        '</div>';
    }
    container.innerHTML = html;

  } catch (e) {
    container.innerHTML = '<div class="loading-placeholder">❌ Nao foi possivel carregar receitas.</div>';
  }
}

// Carrega os primeiros 5 ingredientes da despensa
async function carregarDespensa() {
  var container = document.getElementById('despensaList');

  try {
    var dados = await apiFetch('/ingredientes');

    if (!dados || dados.length === 0) {
      container.innerHTML = '<div class="loading-placeholder">Nenhum ingrediente cadastrado.</div>';
      return;
    }

    var html = '';
    var limite = Math.min(dados.length, 5); // mostra no maximo 5
    for (var i = 0; i < limite; i++) {
      var item = dados[i];
      html += '<div class="despensa-item-dash">' +
        '<span class="despensa-item-name">' + emojiIngrediente(item.nome) + ' ' + item.nome + '</span>' +
        '<span class="despensa-item-qty">' + item.quantidade + ' ' + (item.unidade || '') + '</span>' +
        '</div>';
    }
    container.innerHTML = html;

  } catch (e) {
    container.innerHTML = '<div class="loading-placeholder">Erro ao carregar.</div>';
  }
}

// Carrega os primeiros 5 itens da lista de compras
async function carregarLista() {
  var container = document.getElementById('listaRapida');

  try {
    var dados = await apiFetch('/lista-compras');

    if (!dados || dados.length === 0) {
      container.innerHTML = '<div class="loading-placeholder">Lista vazia.</div>';
      return;
    }

    var html = '';
    var limite = Math.min(dados.length, 5);
    for (var i = 0; i < limite; i++) {
      var item = dados[i];
      html += '<div class="lista-item-dash ' + (item.comprado ? 'checked' : '') + '" id="lista-' + item.id + '">' +
        '<input type="checkbox" ' + (item.comprado ? 'checked' : '') + ' onchange="marcarItem(' + item.id + ', this)">' +
        '<span>' + item.nome + '</span>' +
        '</div>';
    }
    container.innerHTML = html;

  } catch (e) {
    container.innerHTML = '<div class="loading-placeholder">Erro ao carregar.</div>';
  }
}

// Marca ou desmarca um item da lista diretamente do dashboard
async function marcarItem(id, checkbox) {
  try {
    await apiFetch('/lista-compras/' + id, {
      method: 'PUT',
      body: JSON.stringify({ comprado: checkbox.checked })
    });

    // Atualiza o estilo da linha
    var linha = document.getElementById('lista-' + id);
    if (linha) {
      if (checkbox.checked) linha.classList.add('checked');
      else                  linha.classList.remove('checked');
    }

  } catch (e) {
    // Reverte o checkbox se der erro
    checkbox.checked = !checkbox.checked;
  }
}
