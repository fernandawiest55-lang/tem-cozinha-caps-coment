document.addEventListener('DOMContentLoaded', async () => {
  try {
    const data = await apiFetch('/favoritos');
    const grid = document.getElementById('favoritosGrid');
    if (!data || data.length === 0) {
      grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1;"><div class="empty-icon">❤️</div><h3>Nenhum favorito ainda</h3><p>Explore as receitas e salve suas favoritas!</p><a href="receitas.html" class="btn-primary" style="display:inline-flex;width:auto;padding:12px 24px;margin-top:16px;">Ver Receitas</a></div>`;
    <!-- estrutura principal da tela -->
      return;
    }
    grid.innerHTML = data.map(r => `
      <div class="receita-card fade-in">
    <!-- estrutura principal da tela -->
        <div class="receita-card-img">${emojiReceita(r.titulo || '')}</div>
    <!-- estrutura principal da tela -->
        <div class="receita-card-body">
    <!-- estrutura principal da tela -->
          <div class="receita-card-title">${r.titulo}</div>
    <!-- estrutura principal da tela -->
          <div class="receita-card-tags">
    <!-- estrutura principal da tela -->
            ${r.dificuldade ? `<span class="receita-card-tag">${r.dificuldade}</span>` : ''}
          </div>
          <div class="receita-card-meta"><span>⏱ ${formatTempo(r.tempoPreparo) || '—'}</span><span>${renderStars(r.nota || 0)}</span></div>
    <!-- estrutura principal da tela -->
          <div class="receita-card-actions">
    <!-- estrutura principal da tela -->
            <button class="btn-favoritar ativo" onclick="removerFavorito(${r.id || r.receitaId}, this)">❤️</button>
            <a href="receitas.html" class="btn-ver-receita" style="text-align:center;display:flex;align-items:center;justify-content:center;">Ver Receita</a>
          </div>
        </div>
      </div>`).join('');
  } catch { document.getElementById('favoritosGrid').innerHTML = '<div class="loading-placeholder">Erro ao carregar.</div>'; }
    <!-- estrutura principal da tela -->
});
async function removerFavorito(id, btn) {
  try {
    await apiFetch(`/favoritos/${id}`, { method: 'DELETE' });
    btn.closest('.receita-card').remove();
  } catch (e) { alert('Erro: ' + e.message); }
}