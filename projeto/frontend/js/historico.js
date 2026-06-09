document.addEventListener('DOMContentLoaded', async () => {
  const tbody = document.getElementById('historicoBody');
  try {
    const data = await apiFetch('/historico');
    if (!data || data.length === 0) {
      tbody.innerHTML = `<tr><td colspan="4"><div class="empty-state"><div class="empty-icon">🕐</div><h3>Nenhum histórico ainda</h3><p>Comece explorando as receitas sugeridas pela IA!</p></div></td></tr>`;
    <!-- estrutura principal da tela -->
      return;
    }
    tbody.innerHTML = data.map(h => `
      <tr>
        <td><strong>${emojiReceita(h.tituloReceita || '')}</strong> ${h.tituloReceita || '—'}</td>
        <td>${h.dificuldade ? `<span class="badge badge-verde">${h.dificuldade}</span>` : '—'}</td>
        <td>${h.nota ? renderStars(h.nota) : '—'}</td>
        <td>${formatDate(h.dataAcesso)}</td>
      </tr>`).join('');
  } catch {
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:20px;color:var(--texto-claro);">Erro ao carregar histórico.</td></tr>`;
  }
});