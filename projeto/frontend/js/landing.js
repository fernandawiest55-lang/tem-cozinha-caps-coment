// JS da pagina inicial (landing page)

document.addEventListener('DOMContentLoaded', function() {

  // Botao de menu mobile: abre e fecha o menu de navegacao
  var btnMenu  = document.getElementById('navMobileBtn');
  var navLinks = document.querySelector('.nav-links');

  if (btnMenu && navLinks) {
    btnMenu.addEventListener('click', function() {
      var aberto = navLinks.style.display === 'flex';
      navLinks.style.display = aberto ? 'none' : 'flex';
    });
  }

  // Scroll suave ao clicar em links de ancora (#como-funciona, etc)
  var anchors = document.querySelectorAll('a[href^="#"]');
  for (var i = 0; i < anchors.length; i++) {
    anchors[i].addEventListener('click', function(e) {
      e.preventDefault();
      var alvo = document.querySelector(this.getAttribute('href'));
      if (alvo) alvo.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
  }

  // Animacao de entrada nos cards: aparecem quando ficam visiveis na tela
  // IntersectionObserver fica monitorando os elementos e adiciona a classe fade-in
  var observer = new IntersectionObserver(function(entries) {
    for (var j = 0; j < entries.length; j++) {
      if (entries[j].isIntersecting) {
        entries[j].target.classList.add('fade-in');
        observer.unobserve(entries[j].target); // para de observar depois que apareceu
      }
    }
  }, { threshold: 0.1 });

  // Observa todos os cards de passos e funcionalidades
  var cards = document.querySelectorAll('.step-card, .feature-card');
  for (var k = 0; k < cards.length; k++) {
    cards[k].style.opacity = '0';
    observer.observe(cards[k]);
  }
});
