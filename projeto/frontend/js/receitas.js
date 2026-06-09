// JS da pagina de receitas

// Lista global de receitas e ids dos favoritos
var todasReceitas = [];
var favoritosIds  = [];

document.addEventListener('DOMContentLoaded', async function() {

  // Carrega receitas e favoritos ao mesmo tempo
  await carregarReceitas();
  await carregarFavoritosIds();

  // Filtros
  document.getElementById('searchInput').addEventListener('input', filtrarReceitas);
  document.getElementById('filtroCategoria').addEventListener('change', filtrarReceitas);
  document.getElementById('filtroDificuldade').addEventListener('change', filtrarReceitas);

  // Botao de buscar com IA
  document.getElementById('btnBuscarIA').addEventListener('click', buscarComIA);

  // Fechar modal
  document.getElementById('fecharModal').addEventListener('click', fecharModal);
  document.getElementById('receitaModal').addEventListener('click', function(e) {
    if (e.target === this) fecharModal();
  });
});

// Carrega as sugestoes de receitas baseadas na despensa
async function carregarReceitas() {
  try {
    todasReceitas = await apiFetch('/receitas/sugestoes');
    renderReceitas(todasReceitas);
  } catch (e) {
    document.getElementById('receitasGrid').innerHTML =
      '<div class="empty-state" style="grid-column:1/-1;">' +
      '<div class="empty-icon">🍽️</div>' +
      '<h3>Nenhuma receita ainda</h3>' +
      '<p>Adicione ingredientes na sua despensa e clique em "Buscar com IA"!</p>' +
      '<a href="despensa.html" class="btn-primary" style="display:inline-flex;width:auto;padding:12px 24px;margin-top:16px;">Ir para Despensa</a>' +
      '</div>';
  }
}

// Carrega os ids das receitas favoritas para marcar os corações
async function carregarFavoritosIds() {
  try {
    var favs = await apiFetch('/favoritos');
    favoritosIds = [];
    for (var i = 0; i < favs.length; i++) {
      favoritosIds.push(favs[i].receitaId || favs[i].id);
    }
  } catch (e) {}
}

// Chama a IA para gerar novas receitas com os ingredientes do usuario
async function buscarComIA() {
  var box = document.getElementById('iaLoadingBox');
  box.style.display = 'block';
  document.getElementById('receitasGrid').innerHTML = '';

  try {
    var dados = await apiFetch('/receitas/gerar-ia', { method: 'POST' });
    todasReceitas = dados;
    renderReceitas(todasReceitas);
  } catch (err) {
    document.getElementById('receitasGrid').innerHTML =
      '<div class="empty-state" style="grid-column:1/-1;">' +
      '<div class="empty-icon">❌</div>' +
      '<h3>Erro ao consultar IA</h3>' +
      '<p>' + (err.message || 'Tente novamente mais tarde.') + '</p>' +
      '</div>';
  } finally {
    box.style.display = 'none';
  }
}

// Filtra as receitas pelos campos de busca
function filtrarReceitas() {
  var busca = document.getElementById('searchInput').value.toLowerCase();
  var cat   = document.getElementById('filtroCategoria').value.toLowerCase();
  var dif   = document.getElementById('filtroDificuldade').value;

  var filtradas = [];
  for (var i = 0; i < todasReceitas.length; i++) {
    var r = todasReceitas[i];
    var buscaBate = !busca || r.titulo.toLowerCase().includes(busca);
    var catBate   = !cat   || (r.categoria || '').toLowerCase().includes(cat);
    var difBate   = !dif   || r.dificuldade === dif;
    if (buscaBate && catBate && difBate) filtradas.push(r);
  }

  renderReceitas(filtradas);
}

// Verifica se uma receita esta nos favoritos
function ehFavorito(id) {
  for (var i = 0; i < favoritosIds.length; i++) {
    if (favoritosIds[i] === id) return true;
  }
  return false;
}

// Monta o HTML dos cards de receita
function renderReceitas(lista) {
  var grid = document.getElementById('receitasGrid');

  if (!lista || lista.length === 0) {
    grid.innerHTML =
      '<div class="empty-state" style="grid-column:1/-1;">' +
      '<div class="empty-icon">🔍</div>' +
      '<h3>Nenhuma receita encontrada</h3>' +
      '<p>Tente outros filtros ou clique em "Buscar com IA".</p>' +
      '</div>';
    return;
  }

  var html = '';
  for (var i = 0; i < lista.length; i++) {
    var r   = lista[i];
    var fav = ehFavorito(r.id);

    // Monta as tags da receita
    var tagsHtml = '';
    if (r.dificuldade) tagsHtml += '<span class="receita-card-tag">' + r.dificuldade + '</span>';
    if (r.categoria)   tagsHtml += '<span class="receita-card-tag">' + r.categoria + '</span>';
    if (r.tags && r.tags.length > 0) {
      var limite = Math.min(r.tags.length, 2);
      for (var t = 0; t < limite; t++) {
        tagsHtml += '<span class="receita-card-tag">' + r.tags[t] + '</span>';
      }
    }

    html +=
      '<div class="receita-card fade-in">' +
        '<div class="receita-card-img">' + emojiReceita(r.titulo) + '</div>' +
        '<div class="receita-card-body">' +
          '<div class="receita-card-title">' + r.titulo + '</div>' +
          '<div class="receita-card-tags">' + tagsHtml + '</div>' +
          '<div class="receita-card-meta">' +
            '<span>⏱ ' + (formatTempo(r.tempoPreparo) || '—') + '</span>' +
            '<span>' + renderStars(r.nota || 0) + '</span>' +
          '</div>' +
          '<div class="receita-card-actions">' +
            '<button class="btn-favoritar ' + (fav ? 'ativo' : '') + '" onclick="alternarFavorito(event, ' + r.id + ')" title="Favoritar">' +
              (fav ? '❤️' : '🤍') +
            '</button>' +
            '<button class="btn-ver-receita" onclick="abrirModal(' + r.id + ')">Ver Receita</button>' +
          '</div>' +
        '</div>' +
      '</div>';
  }

  grid.innerHTML = html;
}

// Adiciona ou remove uma receita dos favoritos
async function alternarFavorito(e, receitaId) {
  e.stopPropagation();
  var btn  = e.currentTarget;
  var fav  = ehFavorito(receitaId);

  try {
    if (fav) {
      await apiFetch('/favoritos/' + receitaId, { method: 'DELETE' });
      // Remove do array local
      var novos = [];
      for (var i = 0; i < favoritosIds.length; i++) {
        if (favoritosIds[i] !== receitaId) novos.push(favoritosIds[i]);
      }
      favoritosIds = novos;
      btn.innerHTML = '🤍';
      btn.classList.remove('ativo');
    } else {
      await apiFetch('/favoritos', { method: 'POST', body: JSON.stringify({ receitaId: receitaId }) });
      favoritosIds.push(receitaId);
      btn.innerHTML = '❤️';
      btn.classList.add('ativo');
    }
  } catch (err) {
    alert('Erro ao favoritar: ' + err.message);
  }
}

// Abre o modal com os detalhes de uma receita
async function abrirModal(receitaId) {
  var overlay = document.getElementById('receitaModal');
  var body    = document.getElementById('modalBody');

  overlay.classList.add('open');
  body.innerHTML = '<div class="loading-placeholder" style="padding:40px;">Carregando receita...</div>';

  try {
    var r = await apiFetch('/receitas/' + receitaId);
    document.getElementById('modalTitulo').textContent = r.titulo;

    // Monta os ingredientes como tags
    var ingredHtml = '';
    var ingreds = r.ingredientesNecessarios || [];
    for (var i = 0; i < ingreds.length; i++) {
      ingredHtml += '<span class="modal-ingrediente-tag">' + ingreds[i] + '</span>';
    }

    // Monta os passos do modo de preparo
    var passosHtml = '';
    var passos = (r.modoPreparo || '').split('\n');
    for (var p = 0; p < passos.length; p++) {
      if (passos[p].trim()) passosHtml += '<li>' + passos[p] + '</li>';
    }

    // Monta o HTML completo do modal
    body.innerHTML =
      '<div style="font-size:56px;text-align:center;margin-bottom:8px;">' + emojiReceita(r.titulo) + '</div>' +
      '<div class="modal-meta-grid">' +
        '<div class="modal-meta-item"><div class="modal-meta-label">Tempo</div><div class="modal-meta-value">' + (formatTempo(r.tempoPreparo) || '—') + '</div></div>' +
        '<div class="modal-meta-item"><div class="modal-meta-label">Dificuldade</div><div class="modal-meta-value">' + (r.dificuldade || '—') + '</div></div>' +
        '<div class="modal-meta-item"><div class="modal-meta-label">Avaliacao</div><div class="modal-meta-value">' + (r.nota ? r.nota.toFixed(1) + ' ⭐' : '—') + '</div></div>' +
      '</div>' +
      (r.descricao ? '<p style="font-size:14px;color:var(--texto-claro);margin-bottom:16px;">' + r.descricao + '</p>' : '') +
      '<h4 style="font-size:14px;font-weight:700;margin-bottom:8px;text-transform:uppercase;">Ingredientes</h4>' +
      '<div class="modal-ingredientes">' + ingredHtml + '</div>' +
      '<h4 style="font-size:14px;font-weight:700;margin-bottom:12px;text-transform:uppercase;margin-top:16px;">Modo de Preparo</h4>' +
      '<ol class="modal-passos">' + passosHtml + '</ol>' +
      '<div class="avaliacao-form">' +
        '<h4>Avaliar esta receita</h4>' +
        '<div class="avaliacao-stars" id="starsBox">' +
          '<button onclick="selecionarEstrela(' + receitaId + ', 1)">☆</button>' +
          '<button onclick="selecionarEstrela(' + receitaId + ', 2)">☆</button>' +
          '<button onclick="selecionarEstrela(' + receitaId + ', 3)">☆</button>' +
          '<button onclick="selecionarEstrela(' + receitaId + ', 4)">☆</button>' +
          '<button onclick="selecionarEstrela(' + receitaId + ', 5)">☆</button>' +
        '</div>' +
        '<textarea id="comentarioInput" placeholder="Comentario opcional..." style="width:100%;padding:10px;border:2px solid var(--creme-escuro);border-radius:8px;font-size:13px;resize:vertical;min-height:60px;"></textarea>' +
        '<button class="btn-primary" style="margin-top:10px;" onclick="enviarAvaliacao(' + receitaId + ')">Enviar Avaliacao</button>' +
      '</div>' +
      '<div class="modal-actions">' +
        '<button class="btn-secondary" onclick="compartilharReceita(' + receitaId + ')" style="flex:1;padding:12px;">📤 Compartilhar</button>' +
        '<button class="btn-laranja" onclick="adicionarNaLista(' + receitaId + ')" style="flex:1;padding:12px;">🛒 Lista de Compras</button>' +
      '</div>';

  } catch (e) {
    body.innerHTML = '<div class="loading-placeholder">Erro ao carregar receita.</div>';
  }
}

// Fecha o modal de receita
function fecharModal() {
  document.getElementById('receitaModal').classList.remove('open');
}

// Seleciona a nota de avaliacao (preenche as estrelas)
function selecionarEstrela(receitaId, nota) {
  var stars = document.querySelectorAll('#starsBox button');
  for (var i = 0; i < stars.length; i++) {
    stars[i].textContent = i < nota ? '⭐' : '☆';
  }
  document.getElementById('starsBox').dataset.nota = nota;
}

// Envia a avaliacao para a API
async function enviarAvaliacao(receitaId) {
  var nota      = parseInt(document.getElementById('starsBox').dataset.nota || 0);
  var comentario = document.getElementById('comentarioInput').value;

  if (!nota) { alert('Selecione uma avaliacao.'); return; }

  try {
    await apiFetch('/avaliacoes', {
      method: 'POST',
      body: JSON.stringify({ receitaId: receitaId, nota: nota, comentario: comentario })
    });
    alert('Avaliacao enviada! Obrigado 😊');
  } catch (err) {
    alert('Erro: ' + err.message);
  }
}

// Compartilha a receita via Web Share API
async function compartilharReceita(receitaId) {
  var receita = null;
  for (var i = 0; i < todasReceitas.length; i++) {
    if (todasReceitas[i].id === receitaId) { receita = todasReceitas[i]; break; }
  }

  if (navigator.share && receita) {
    navigator.share({ title: receita.titulo, text: 'Confira essa receita: ' + receita.titulo, url: window.location.href });
  } else {
    navigator.clipboard.writeText(window.location.href);
    alert('Link copiado!');
  }
}

// Adiciona os ingredientes da receita na lista de compras
async function adicionarNaLista(receitaId) {
  try {
    await apiFetch('/lista-compras/receita/' + receitaId, { method: 'POST' });
    alert('Ingredientes adicionados a lista de compras! 🛒');
  } catch (err) {
    alert('Erro: ' + err.message);
  }
}
