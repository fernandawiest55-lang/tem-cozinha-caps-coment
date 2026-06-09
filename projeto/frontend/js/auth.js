// Endereço base da API do backend
const API_BASE = 'http://localhost:8080/api';

// Objeto Auth: guarda as funções de sessão do usuario
const Auth = {

  // Salva o token e os dados do usuario no localStorage do navegador
  salvarSessao(token, usuario) {
    localStorage.setItem('tnc_token', token);
    localStorage.setItem('tnc_usuario', JSON.stringify(usuario));
  },

  // Retorna o token salvo
  pegarToken() {
    return localStorage.getItem('tnc_token');
  },

  // Retorna o objeto do usuario salvo
  pegarUsuario() {
    const u = localStorage.getItem('tnc_usuario');
    return u ? JSON.parse(u) : null;
  },

  // Faz logout: remove token e redireciona para o login
  sair() {
    localStorage.removeItem('tnc_token');
    localStorage.removeItem('tnc_usuario');
    window.location.href = 'login.html';
  },

  // Se o usuario ja estiver logado, manda direto pro dashboard
  redirecionarSeLogado() {
    if (this.pegarToken()) {
      window.location.href = 'dashboard.html';
    }
  },

  // Se o usuario NAO estiver logado, manda pro login
  exigirLogin() {
    if (!this.pegarToken()) {
      window.location.href = 'login.html';
    }
  }
};

// Objeto Validar: funções simples de validação de campos
const Validar = {

  // Verifica se o email tem formato valido
  email(valor) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(valor);
  },

  // Senha precisa ter pelo menos 6 caracteres
  senha(valor) {
    return valor.length >= 6;
  },

  // Nome precisa ter pelo menos 2 caracteres
  nome(valor) {
    return valor.trim().length >= 2;
  }
};

// Mostra uma caixa de alerta na tela
// tipo pode ser 'error' (vermelho) ou 'success' (verde)
function mostrarAlerta(msg, tipo = 'error') {
  const box = document.getElementById('alertBox');
  if (!box) return;

  box.className = 'alert alert-' + tipo;
  box.innerHTML = '<span>' + (tipo === 'error' ? '⚠️' : '✅') + '</span> ' + msg;
  box.style.display = 'flex';

  // Some automaticamente depois de 5 segundos
  setTimeout(function() {
    box.style.display = 'none';
  }, 5000);
}

// Liga ou desliga o estado de carregamento de um botão
function setLoading(btnId, spinnerId, textoId, carregando) {
  const btn     = document.getElementById(btnId);
  const spinner = document.getElementById(spinnerId);
  const texto   = document.getElementById(textoId);

  if (!btn) return;

  btn.disabled = carregando;
  if (spinner) spinner.style.display = carregando ? 'inline-block' : 'none';
  if (texto)   texto.style.opacity   = carregando ? '0' : '1';
}

// Faz uma chamada para a API com o token JWT automatico
async function apiFetch(caminho, opcoes) {
  if (!opcoes) opcoes = {};

  const token = Auth.pegarToken();

  // Monta os headers com o token se existir
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = 'Bearer ' + token;

  // Mistura os headers passados com os padroes
  if (opcoes.headers) {
    for (var k in opcoes.headers) headers[k] = opcoes.headers[k];
  }

  const resposta = await fetch(API_BASE + caminho, Object.assign({}, opcoes, { headers: headers }));

  // Tenta ler o JSON da resposta
  var dados = {};
  try {
    dados = await resposta.json();
  } catch (e) {}

  // Se deu erro HTTP, lanca uma excecao com a mensagem
  if (!resposta.ok) {
    throw new Error(dados.message || 'Erro ' + resposta.status);
  }

  return dados;
}

// Retorna um emoji baseado no nome da receita
function emojiReceita(titulo) {
  if (!titulo) return '🍽️';
  const t = titulo.toLowerCase();
  if (t.includes('frango') || t.includes('galinha')) return '🍗';
  if (t.includes('peixe') || t.includes('atum') || t.includes('salmao')) return '🐟';
  if (t.includes('bolo') || t.includes('torta')) return '🎂';
  if (t.includes('sopa') || t.includes('caldo')) return '🍲';
  if (t.includes('salada')) return '🥗';
  if (t.includes('arroz')) return '🍚';
  if (t.includes('macarrao') || t.includes('massa')) return '🍝';
  if (t.includes('feijao')) return '🫘';
  if (t.includes('ovo')) return '🍳';
  return '🍽️';
}

// Retorna um emoji baseado no nome do ingrediente
function emojiIngrediente(nome) {
  if (!nome) return '📦';
  const n = nome.toLowerCase();
  if (n.includes('tomate'))  return '🍅';
  if (n.includes('cebola'))  return '🧅';
  if (n.includes('alho'))    return '🧄';
  if (n.includes('cenoura')) return '🥕';
  if (n.includes('batata'))  return '🥔';
  if (n.includes('frango'))  return '🍗';
  if (n.includes('carne'))   return '🥩';
  if (n.includes('ovo'))     return '🥚';
  if (n.includes('leite'))   return '🥛';
  if (n.includes('arroz'))   return '🌾';
  return '📦';
}

// Formata o tempo de preparo: 90 -> "1h 30min", 45 -> "45 min"
function formatTempo(minutos) {
  if (!minutos) return '';
  if (minutos >= 60) {
    var h   = Math.floor(minutos / 60);
    var min = minutos % 60;
    return min > 0 ? h + 'h ' + min + 'min' : h + 'h';
  }
  return minutos + ' min';
}

// Formata uma data ISO para o formato brasileiro: "09/06/2026 às 14:30"
function formatDate(dataStr) {
  if (!dataStr) return '—';
  try {
    var d = new Date(dataStr);
    return d.toLocaleDateString('pt-BR') + ' às ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  } catch (e) {
    return dataStr;
  }
}

// Renderiza estrelas de avaliacao: nota 3.5 -> "⭐⭐⭐☆☆"
function renderStars(nota) {
  var resultado = '';
  for (var i = 1; i <= 5; i++) {
    resultado += i <= Math.round(nota) ? '⭐' : '☆';
  }
  return resultado;
}

// Quando a pagina carregar, configura o sidebar com os dados do usuario
// e o botão de logout (funciona em todas as paginas do app)
document.addEventListener('DOMContentLoaded', function() {
  var usuario = Auth.pegarUsuario();

  // Preenche o nome e avatar no sidebar
  var avatarEl = document.getElementById('sidebarAvatar');
  var nomeEl   = document.getElementById('sidebarName');

  if (avatarEl && usuario) avatarEl.textContent = (usuario.nome || 'U')[0].toUpperCase();
  if (nomeEl   && usuario) nomeEl.textContent   = usuario.nome || '';

  // Configura o botao de logout
  var btnLogout = document.getElementById('btnLogout');
  if (btnLogout) {
    btnLogout.addEventListener('click', function() {
      Auth.sair();
    });
  }

  // Configura o botao de menu mobile
  var mobileBtn = document.getElementById('mobileMenuBtn');
  var sidebar   = document.getElementById('sidebar');
  if (mobileBtn && sidebar) {
    mobileBtn.addEventListener('click', function() {
      sidebar.classList.toggle('open');
    });
  }

  // Texto de saudacao no dashboard
  var greetEl = document.getElementById('greetingText');
  if (greetEl && usuario) {
    var hora = new Date().getHours();
    var saudacao = hora < 12 ? 'Bom dia' : hora < 18 ? 'Boa tarde' : 'Boa noite';
    greetEl.textContent = saudacao + ', ' + (usuario.nome || '') + '! O que vamos cozinhar hoje?';
  }
});
