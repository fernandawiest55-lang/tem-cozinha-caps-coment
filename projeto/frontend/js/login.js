// JS da pagina de login

document.addEventListener('DOMContentLoaded', function() {

  // Se o usuario ja estiver logado, manda pro dashboard direto
  Auth.redirecionarSeLogado();

  // Botao de mostrar/ocultar senha
  var toggleBtn  = document.getElementById('toggleSenha');
  var senhaInput = document.getElementById('senha');

  if (toggleBtn && senhaInput) {
    toggleBtn.addEventListener('click', function() {
      // Alterna entre password e text
      var visivel = senhaInput.type === 'text';
      senhaInput.type = visivel ? 'password' : 'text';

      // Troca o icone do botao
      toggleBtn.innerHTML = visivel
        ? '<svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>'
        : '<svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></svg>';
    });
  }

  // Formulario de login
  var form = document.getElementById('loginForm');
  if (form) {
    form.addEventListener('submit', async function(e) {
      e.preventDefault(); // evita recarregar a pagina

      var email = document.getElementById('email').value.trim();
      var senha = document.getElementById('senha').value;

      // Limpa mensagens de erro anteriores
      document.getElementById('emailError').textContent = '';
      document.getElementById('senhaError').textContent = '';

      // Valida os campos
      var valido = true;
      if (!Validar.email(email)) {
        document.getElementById('emailError').textContent = 'Informe um e-mail valido.';
        valido = false;
      }
      if (!Validar.senha(senha)) {
        document.getElementById('senhaError').textContent = 'A senha deve ter ao menos 6 caracteres.';
        valido = false;
      }
      if (!valido) return;

      // Mostra spinner no botao
      setLoading('btnLogin', 'btnLoginSpinner', 'btnLoginText', true);

      try {
        // Envia o login para a API
        var dados = await apiFetch('/usuarios/login', {
          method: 'POST',
          body: JSON.stringify({ email: email, senha: senha })
        });

        // Salva o token e redireciona
        Auth.salvarSessao(dados.token, dados.usuario);
        window.location.href = 'dashboard.html';

      } catch (err) {
        mostrarAlerta(err.message || 'E-mail ou senha invalidos.');
      } finally {
        setLoading('btnLogin', 'btnLoginSpinner', 'btnLoginText', false);
      }
    });
  }
});
