// JS da pagina de perfil

document.addEventListener('DOMContentLoaded', async function() {

  await carregarPerfil();
  await carregarStats();

  // Chips de restricoes: toggle ao clicar
  var chipsRestricoes = document.querySelectorAll('#restricoesGrid .chip');
  for (var i = 0; i < chipsRestricoes.length; i++) {
    chipsRestricoes[i].addEventListener('click', function() {
      this.classList.toggle('selected');
    });
  }

  // Chips de dieta: single select
  var chipsDieta = document.querySelectorAll('#dietaGrid .chip');
  for (var j = 0; j < chipsDieta.length; j++) {
    chipsDieta[j].addEventListener('click', function() {
      for (var k = 0; k < chipsDieta.length; k++) {
        chipsDieta[k].classList.remove('selected');
      }
      this.classList.add('selected');
    });
  }

  document.getElementById('btnSalvarPerfil').addEventListener('click', salvarPerfil);
  document.getElementById('btnSalvarRestricoes').addEventListener('click', salvarRestricoes);
  document.getElementById('btnAlterarSenha').addEventListener('click', alterarSenha);
});

// Carrega os dados do usuario e preenche os campos
async function carregarPerfil() {
  try {
    var usuario = await apiFetch('/usuarios/me');

    document.getElementById('inputNome').value      = usuario.nome  || '';
    document.getElementById('inputEmail').value     = usuario.email || '';
    document.getElementById('perfilNome').textContent  = usuario.nome  || '';
    document.getElementById('perfilEmail').textContent = usuario.email || '';
    document.getElementById('avatarGrande').textContent = (usuario.nome || 'U')[0].toUpperCase();

    // Marca os chips de restricoes ja salvos
    if (usuario.restricoes) {
      var restricoes = usuario.restricoes.split(',');
      for (var i = 0; i < restricoes.length; i++) {
        var chip = document.querySelector('#restricoesGrid [data-val="' + restricoes[i].trim() + '"]');
        if (chip) chip.classList.add('selected');
      }
    }

    // Marca os chips de dieta ja salvos
    if (usuario.dieta) {
      var dietas = usuario.dieta.split(',');
      for (var j = 0; j < dietas.length; j++) {
        var chipD = document.querySelector('#dietaGrid [data-val="' + dietas[j].trim() + '"]');
        if (chipD) chipD.classList.add('selected');
      }
    }

  } catch (e) {}
}

// Carrega os numeros de estatisticas do usuario
async function carregarStats() {
  try {
    var ingredientes = await apiFetch('/ingredientes');
    var favoritos    = await apiFetch('/favoritos');
    var historico    = await apiFetch('/historico');

    document.getElementById('statsIngr').textContent = ingredientes.length || 0;
    document.getElementById('statsFav').textContent  = favoritos.length    || 0;
    document.getElementById('statsHist').textContent = historico.length    || 0;
  } catch (e) {}
}

// Salva o nome e email do perfil
async function salvarPerfil() {
  var nome  = document.getElementById('inputNome').value.trim();
  var email = document.getElementById('inputEmail').value.trim();
  var alertEl = document.getElementById('alertPerfil');

  if (!nome || !Validar.email(email)) {
    alertEl.className = 'alert alert-error';
    alertEl.innerHTML = '⚠️ Preencha todos os campos corretamente.';
    alertEl.style.display = 'flex';
    return;
  }

  setLoading('btnSalvarPerfil', 'salvarPerfilSpinner', 'salvarPerfilText', true);

  try {
    await apiFetch('/usuarios/me', {
      method: 'PUT',
      body: JSON.stringify({ nome: nome, email: email })
    });

    // Atualiza o localStorage com os novos dados
    var usuarioAtual = Auth.pegarUsuario();
    usuarioAtual.nome  = nome;
    usuarioAtual.email = email;
    Auth.salvarSessao(Auth.pegarToken(), usuarioAtual);

    // Atualiza os elementos visiveis na pagina
    document.getElementById('perfilNome').textContent   = nome;
    document.getElementById('perfilEmail').textContent  = email;
    document.getElementById('avatarGrande').textContent = nome[0].toUpperCase();
    document.getElementById('sidebarName').textContent  = nome;
    document.getElementById('sidebarAvatar').textContent = nome[0].toUpperCase();

    alertEl.className = 'alert alert-success';
    alertEl.innerHTML = '✅ Perfil atualizado com sucesso!';
    alertEl.style.display = 'flex';
    setTimeout(function() { alertEl.style.display = 'none'; }, 3000);

  } catch (err) {
    alertEl.className = 'alert alert-error';
    alertEl.innerHTML = '⚠️ ' + err.message;
    alertEl.style.display = 'flex';
  } finally {
    setLoading('btnSalvarPerfil', 'salvarPerfilSpinner', 'salvarPerfilText', false);
  }
}

// Salva as restricoes e dieta alimentar
async function salvarRestricoes() {
  // Pega os chips selecionados
  var chipsRestSel = document.querySelectorAll('#restricoesGrid .chip.selected');
  var chipsDistSel = document.querySelectorAll('#dietaGrid .chip.selected');

  var restricoes = [];
  for (var i = 0; i < chipsRestSel.length; i++) restricoes.push(chipsRestSel[i].dataset.val);

  var dieta = [];
  for (var j = 0; j < chipsDistSel.length; j++) dieta.push(chipsDistSel[j].dataset.val);

  try {
    await apiFetch('/usuarios/me/perfil-alimentar', {
      method: 'PUT',
      body: JSON.stringify({ restricoes: restricoes.join(','), dieta: dieta.join(',') })
    });

    var alertEl = document.getElementById('alertPerfil');
    alertEl.className = 'alert alert-success';
    alertEl.innerHTML = '✅ Preferencias salvas!';
    alertEl.style.display = 'flex';
    setTimeout(function() { alertEl.style.display = 'none'; }, 3000);

  } catch (err) {
    alert('Erro: ' + err.message);
  }
}

// Altera a senha do usuario
async function alterarSenha() {
  var senhaAtual  = document.getElementById('senhaAtual').value;
  var novaSenha   = document.getElementById('novaSenha').value;
  var confirmar   = document.getElementById('confirmarSenha').value;
  var alertEl     = document.getElementById('alertSenha');

  alertEl.style.display = 'none';

  if (!senhaAtual || !novaSenha) {
    alertEl.className = 'alert alert-error';
    alertEl.innerHTML = '⚠️ Preencha a senha atual e a nova senha.';
    alertEl.style.display = 'flex';
    return;
  }
  if (novaSenha !== confirmar) {
    alertEl.className = 'alert alert-error';
    alertEl.innerHTML = '⚠️ As senhas nao coincidem.';
    alertEl.style.display = 'flex';
    return;
  }
  if (!Validar.senha(novaSenha)) {
    alertEl.className = 'alert alert-error';
    alertEl.innerHTML = '⚠️ A nova senha deve ter ao menos 6 caracteres.';
    alertEl.style.display = 'flex';
    return;
  }

  try {
    await apiFetch('/usuarios/me/senha', {
      method: 'PUT',
      body: JSON.stringify({ senhaAtual: senhaAtual, novaSenha: novaSenha })
    });

    // Limpa os campos de senha
    document.getElementById('senhaAtual').value  = '';
    document.getElementById('novaSenha').value   = '';
    document.getElementById('confirmarSenha').value = '';

    alertEl.className = 'alert alert-success';
    alertEl.innerHTML = '✅ Senha alterada com sucesso!';
    alertEl.style.display = 'flex';
    setTimeout(function() { alertEl.style.display = 'none'; }, 3000);

  } catch (err) {
    alertEl.className = 'alert alert-error';
    alertEl.innerHTML = '⚠️ ' + err.message;
    alertEl.style.display = 'flex';
  }
}
