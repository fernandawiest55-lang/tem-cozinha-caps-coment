// JS da pagina de cadastro (3 etapas)

document.addEventListener('DOMContentLoaded', function() {

  Auth.redirecionarSeLogado();

  // Etapa atual (começa na 1)
  var etapaAtual = 1;

  // Dados do formulário acumulados entre as etapas
  var dadosForm = {
    nome: '',
    email: '',
    senha: '',
    restricoes: [],
    dieta: []
  };

  // Toggle mostrar/ocultar senha
  var toggleBtn  = document.getElementById('toggleSenha1');
  var senhaInput = document.getElementById('senha');
  if (toggleBtn && senhaInput) {
    toggleBtn.addEventListener('click', function() {
      senhaInput.type = senhaInput.type === 'password' ? 'text' : 'password';
    });
  }

  // Chips de restricoes: clicar seleciona/deseleciona
  var chipsRestricoes = document.querySelectorAll('#restricoesGrid .chip');
  for (var i = 0; i < chipsRestricoes.length; i++) {
    chipsRestricoes[i].addEventListener('click', function() {
      this.classList.toggle('selected');
      var val = this.dataset.val;

      // Adiciona ou remove da lista
      var idx = dadosForm.restricoes.indexOf(val);
      if (idx > -1) {
        dadosForm.restricoes.splice(idx, 1); // remove
      } else {
        dadosForm.restricoes.push(val); // adiciona
      }
    });
  }

  // Chips de dieta: single select (so um por vez)
  var chipsDieta = document.querySelectorAll('#dietaGrid .chip');
  for (var j = 0; j < chipsDieta.length; j++) {
    chipsDieta[j].addEventListener('click', function() {
      // Remove selecao de todos
      for (var k = 0; k < chipsDieta.length; k++) {
        chipsDieta[k].classList.remove('selected');
      }
      // Seleciona so o clicado
      this.classList.add('selected');
      dadosForm.dieta = [this.dataset.val];
    });
  }

  // Função que muda a etapa exibida
  function irParaEtapa(numero) {
    // Esconde todos os paineis
    var paineis = document.querySelectorAll('.step-panel');
    for (var p = 0; p < paineis.length; p++) {
      paineis[p].classList.remove('active');
    }

    // Mostra o painel da etapa certa
    document.getElementById('panel-' + numero).classList.add('active');

    // Atualiza os indicadores de progresso
    for (var n = 1; n <= 3; n++) {
      var ind = document.getElementById('step-ind-' + n);
      ind.classList.remove('active', 'done');
      if (n < numero) ind.classList.add('done');
      else if (n === numero) ind.classList.add('active');
    }

    // Atualiza as linhas entre os indicadores
    for (var l = 1; l <= 2; l++) {
      var linha = document.getElementById('line-' + l);
      if (l < numero) linha.classList.add('done');
      else linha.classList.remove('done');
    }

    etapaAtual = numero;
  }

  // Botao "Continuar" da etapa 1: valida e avança
  document.getElementById('btnStep1').addEventListener('click', function() {
    var nome      = document.getElementById('nome').value.trim();
    var email     = document.getElementById('email').value.trim();
    var senha     = document.getElementById('senha').value;
    var confirmar = document.getElementById('confirmarSenha').value;

    // Limpa erros anteriores
    document.getElementById('nomeError').textContent     = '';
    document.getElementById('emailError').textContent    = '';
    document.getElementById('senhaError').textContent    = '';
    document.getElementById('confirmarError').textContent = '';

    var valido = true;
    if (!Validar.nome(nome))   { document.getElementById('nomeError').textContent     = 'Informe seu nome completo.';            valido = false; }
    if (!Validar.email(email)) { document.getElementById('emailError').textContent    = 'Informe um e-mail valido.';             valido = false; }
    if (!Validar.senha(senha)) { document.getElementById('senhaError').textContent    = 'A senha deve ter ao menos 6 caracteres.'; valido = false; }
    if (senha !== confirmar)   { document.getElementById('confirmarError').textContent = 'As senhas nao coincidem.';              valido = false; }
    if (!valido) return;

    dadosForm.nome  = nome;
    dadosForm.email = email;
    dadosForm.senha = senha;
    irParaEtapa(2);
  });

  // Botao "Continuar" da etapa 2: avança para o resumo
  document.getElementById('btnStep2').addEventListener('click', function() {
    var restricoesTexto = dadosForm.restricoes.length ? dadosForm.restricoes.join(', ') : 'Nenhuma selecionada';
    document.getElementById('restricoesSummary').textContent = restricoesTexto;
    irParaEtapa(3);
  });

  // Botao "Voltar" na etapa 2
  document.getElementById('btnBack1').addEventListener('click', function() {
    irParaEtapa(1);
  });

  // Botao "Entrar no app": envia o cadastro para a API
  document.getElementById('btnFinalizar').addEventListener('click', async function() {
    setLoading('btnFinalizar', 'btnFinalizarSpinner', 'btnFinalizarText', true);

    try {
      var dados = await apiFetch('/usuarios/cadastro', {
        method: 'POST',
        body: JSON.stringify({
          nome:      dadosForm.nome,
          email:     dadosForm.email,
          senha:     dadosForm.senha,
          restricoes: dadosForm.restricoes.join(','),
          dieta:     dadosForm.dieta.join(',')
        })
      });

      Auth.salvarSessao(dados.token, dados.usuario);
      window.location.href = 'dashboard.html';

    } catch (err) {
      irParaEtapa(1);
      mostrarAlerta(err.message || 'Erro ao criar conta. Tente novamente.');
    } finally {
      setLoading('btnFinalizar', 'btnFinalizarSpinner', 'btnFinalizarText', false);
    }
  });
});
