// JS da pagina de despensa

// Lista global de ingredientes (usada no filtro)
var ingredientes = [];

document.addEventListener('DOMContentLoaded', async function() {

  // Carrega os ingredientes ao abrir a pagina
  await carregarIngredientes();

  // Busca ao digitar no campo de pesquisa
  document.getElementById('searchInput').addEventListener('input', filtrar);

  // Filtro por categoria
  document.getElementById('filtroCategoria').addEventListener('change', filtrar);

  // Botao de adicionar ingrediente
  document.getElementById('btnAddIngrediente').addEventListener('click', function() {
    abrirModal(null);
  });

  // Fechar modal pelos botoes
  document.getElementById('fecharModal').addEventListener('click', fecharModal);
  document.getElementById('cancelarModal').addEventListener('click', fecharModal);

  // Fechar modal ao clicar fora dele
  document.getElementById('ingredienteModal').addEventListener('click', function(e) {
    if (e.target === this) fecharModal();
  });

  // Botao salvar dentro do modal
  document.getElementById('salvarIngrediente').addEventListener('click', salvar);
});

// Busca os ingredientes do usuario na API e renderiza na tabela
async function carregarIngredientes() {
  try {
    ingredientes = await apiFetch('/ingredientes');
    renderTabela(ingredientes);
  } catch (e) {
    document.getElementById('despensaBody').innerHTML =
      '<tr><td colspan="5" style="text-align:center;padding:40px;color:var(--texto-claro);">Nenhum ingrediente encontrado.</td></tr>';
  }
}

// Monta o HTML da tabela com a lista de ingredientes
function renderTabela(lista) {
  var tbody = document.getElementById('despensaBody');

  if (!lista || lista.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5"><div class="empty-state">' +
      '<div class="empty-icon">📦</div>' +
      '<h3>Despensa vazia</h3>' +
      '<p>Adicione seus ingredientes para receber sugestoes de receitas!</p>' +
      '</div></td></tr>';
    return;
  }

  var html = '';
  for (var i = 0; i < lista.length; i++) {
    var item = lista[i];
    html += '<tr>' +
      '<td><strong>' + emojiIngrediente(item.nome) + '</strong> ' + item.nome + '</td>' +
      '<td>' + (item.quantidade || '—') + '</td>' +
      '<td>' + (item.unidade || '—') + '</td>' +
      '<td><span class="badge badge-verde">' + (item.local || '—') + '</span></td>' +
      '<td>' +
        '<div style="display:flex;gap:8px;">' +
          '<button class="btn-icon" onclick="abrirModal(' + item.id + ')" title="Editar" style="color:var(--verde-claro);">' +
            '<svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>' +
          '</button>' +
          '<button class="btn-icon" onclick="excluir(' + item.id + ')" title="Excluir" style="color:#e11d48;">' +
            '<svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>' +
          '</button>' +
        '</div>' +
      '</td>' +
    '</tr>';
  }
  tbody.innerHTML = html;
}

// Filtra a tabela pelo texto digitado e pela categoria selecionada
function filtrar() {
  var busca = document.getElementById('searchInput').value.toLowerCase();
  var cat   = document.getElementById('filtroCategoria').value;

  var filtrados = [];
  for (var i = 0; i < ingredientes.length; i++) {
    var item = ingredientes[i];
    var nomeBate     = !busca || item.nome.toLowerCase().includes(busca);
    var categoriaBate = !cat  || item.local === cat;
    if (nomeBate && categoriaBate) filtrados.push(item);
  }

  renderTabela(filtrados);
}

// Abre o modal de adicionar ou editar ingrediente
// id = null -> adicionar novo; id = numero -> editar existente
function abrirModal(id) {
  var modal = document.getElementById('ingredienteModal');

  // Limpa o modal
  document.getElementById('alertModal').style.display = 'none';
  document.getElementById('editId').value             = '';
  document.getElementById('inputNome').value          = '';
  document.getElementById('inputQtd').value           = '';
  document.getElementById('inputUnidade').value       = 'unidades';
  document.getElementById('inputLocal').value         = 'Geladeira';
  document.getElementById('modalTitulo').textContent  = 'Adicionar Ingrediente';

  // Se passou um id, preenche com os dados do ingrediente para editar
  if (id) {
    var item = null;
    for (var i = 0; i < ingredientes.length; i++) {
      if (ingredientes[i].id === id) { item = ingredientes[i]; break; }
    }

    if (item) {
      document.getElementById('editId').value            = item.id;
      document.getElementById('inputNome').value         = item.nome;
      document.getElementById('inputQtd').value          = item.quantidade;
      document.getElementById('inputUnidade').value      = item.unidade  || 'unidades';
      document.getElementById('inputLocal').value        = item.local    || 'Geladeira';
      document.getElementById('modalTitulo').textContent = 'Editar Ingrediente';
    }
  }

  modal.classList.add('open');
}

// Fecha o modal
function fecharModal() {
  document.getElementById('ingredienteModal').classList.remove('open');
}

// Salva o ingrediente (cria ou atualiza)
async function salvar() {
  var id        = document.getElementById('editId').value;
  var nome      = document.getElementById('inputNome').value.trim();
  var quantidade = parseFloat(document.getElementById('inputQtd').value);
  var unidade   = document.getElementById('inputUnidade').value;
  var local     = document.getElementById('inputLocal').value;

  var alertModal = document.getElementById('alertModal');
  alertModal.style.display = 'none';

  // Valida o nome
  if (!nome) {
    alertModal.className = 'alert alert-error';
    alertModal.innerHTML = '⚠️ Informe o nome do ingrediente.';
    alertModal.style.display = 'flex';
    return;
  }

  setLoading('salvarIngrediente', 'salvarSpinner', 'salvarText', true);

  try {
    if (id) {
      // Atualiza ingrediente existente
      await apiFetch('/ingredientes/' + id, {
        method: 'PUT',
        body: JSON.stringify({ nome: nome, quantidade: quantidade, unidade: unidade, local: local })
      });
    } else {
      // Cria novo ingrediente
      await apiFetch('/ingredientes', {
        method: 'POST',
        body: JSON.stringify({ nome: nome, quantidade: quantidade, unidade: unidade, local: local })
      });
    }

    fecharModal();
    await carregarIngredientes(); // recarrega a tabela

  } catch (err) {
    alertModal.className = 'alert alert-error';
    alertModal.innerHTML = '⚠️ ' + err.message;
    alertModal.style.display = 'flex';
  } finally {
    setLoading('salvarIngrediente', 'salvarSpinner', 'salvarText', false);
  }
}

// Exclui um ingrediente
async function excluir(id) {
  if (!confirm('Tem certeza que deseja remover este ingrediente?')) return;

  try {
    await apiFetch('/ingredientes/' + id, { method: 'DELETE' });
    await carregarIngredientes();
  } catch (err) {
    alert('Erro ao excluir: ' + err.message);
  }
}
