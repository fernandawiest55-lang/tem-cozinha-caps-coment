# 🍳 Tem na Cozinha

**Equipe:** The Furious Five | Jala University  
**Stack:** HTML/CSS/JS · Java  · PostgreSQL · Gemini AI

> Aplicativo que sugere receitas personalizadas com base nos ingredientes que o usuario ja tem em casa.

---

## 📁 Estrutura do Projeto

```
tem-na-cozinha/
├── frontend/
│   ├── pages/
│   │   ├── index.html           ← Landing page
│   │   ├── login.html
│   │   ├── cadastro.html
│   │   ├── recuperar-senha.html
│   │   ├── dashboard.html
│   │   ├── receitas.html
│   │   ├── despensa.html
│   │   ├── lista-compras.html
│   │   ├── perfil.html
│   │   ├── favoritos.html
│   │   └── historico.html
│   ├── css/
│   │   ├── global.css           ← Design system e componentes
│   │   ├── landing.css          ← Estilos da landing page
│   │   ├── auth.css             ← Login e cadastro
│   │   ├── app.css              ← Dashboard e paginas autenticadas
│   │   └── receitas.css         ← Pagina de receitas e modais
│   └── js/
│       ├── auth.js              ← JWT, sessao, apiFetch, helpers
│       ├── landing.js           ← Animacoes da landing
│       ├── login.js
│       ├── cadastro.js
│       ├── dashboard.js
│       ├── receitas.js
│       ├── despensa.js
│       ├── lista-compras.js
│       └── perfil.js
│
├── backend/src/
│   ├── config/
│   │   └── ConnectionFactory.java   ← Conexao JDBC com PostgreSQL
│   ├── model/                       ← Classes de dados (POJO)
│   │   ├── Usuario.java
│   │   ├── Receita.java
│   │   ├── Ingrediente.java
│   │   ├── ListaCompras.java
│   │   ├── Avaliacao.java
│   │   └── Historico.java
│   ├── dao/                         ← Acesso ao banco via JDBC
│   │   ├── UsuarioDAO.java
│   │   ├── IngredienteDAO.java
│   │   ├── ReceitaDAO.java
│   │   └── AllDAOs.java
│   ├── util/
│   │   ├── JwtUtil.java             ← Token JWT manual (HS256)
│   │   ├── BCryptUtil.java          ← Hash de senha (SHA-256 + salt)
│   │   ├── JsonParser.java          ← Leitura do body JSON
│   │   └── ResponseUtil.java        ← Respostas HTTP padronizadas
│   ├── controller/                  ← Servlets JAX-RS
│   │   ├── Filters.java             ← CorsFilter + AuthFilter
│   │   ├── UsuarioController.java
│   │   ├── IngredienteController.java
│   │   ├── ReceitaController.java   ← Inclui integracao com Gemini
│   │   └── OtherControllers.java
│   └── web.xml
│
└── database/
    └── schema.sql                   ← Tabelas + dados de exemplo
```

Acesse: `http://localhost:3000/pages/index.html`

```

| Método | Rota | Descricao |
|--------|------|-----------|
| POST | `/api/usuarios/cadastro` | Criar conta |
| POST | `/api/usuarios/login` | Login → retorna JWT |
| POST | `/api/usuarios/recuperar-senha` | Recuperar senha |
| GET | `/api/usuarios/me` | Dados do usuario logado |
| PUT | `/api/usuarios/me` | Atualizar nome e email |
| PUT | `/api/usuarios/me/senha` | Alterar senha |
| PUT | `/api/usuarios/me/perfil-alimentar` | Salvar restricoes e dieta |
| GET | `/api/ingredientes` | Listar ingredientes |
| POST | `/api/ingredientes` | Adicionar ingrediente |
| PUT | `/api/ingredientes/{id}` | Editar ingrediente |
| DELETE | `/api/ingredientes/{id}` | Remover ingrediente |
| GET | `/api/receitas/sugestoes` | Sugestoes baseadas na despensa |
| GET | `/api/receitas/{id}` | Detalhes de uma receita |
| POST | `/api/receitas/gerar-ia` | Gerar receitas com Gemini |
| GET | `/api/favoritos` | Listar favoritos |
| POST | `/api/favoritos` | Favoritar receita |
| DELETE | `/api/favoritos/{id}` | Desfavoritar |
| GET | `/api/historico` | Historico de receitas vistas |
| POST | `/api/avaliacoes` | Avaliar receita (1 a 5) |
| GET | `/api/lista-compras` | Listar itens |
| POST | `/api/lista-compras` | Adicionar item |
| POST | `/api/lista-compras/receita/{id}` | Adicionar ingredientes da receita |
| PUT | `/api/lista-compras/{id}` | Marcar como comprado |
| DELETE | `/api/lista-compras/{id}` | Remover item |

---

## 🎨 Design System

| Variavel | Valor | Uso |
|----------|-------|-----|
| `--verde-escuro` | `#1a3a2a` | Sidebar, botao primario |
| `--verde-medio` | `#2d5a3d` | Hover |
| `--verde-claro` | `#4a8c5c` | Icones, links |
| `--laranja` | `#e8813a` | CTAs, botoes de acao |
| `--creme` | `#faf6f0` | Fundo principal |

**Fontes:** Playfair Display (titulos) · DM Sans (corpo)

---

## 👥 Equipe — The Furious Five

| Membro | |
|--------|-|
| Fernanda | Desenvolvimento |
| Lucas | Desenvolvimento |
| Natalia | Desenvolvimento |
| Riqui | Desenvolvimento |
| Thierry | Desenvolvimento |

---

*Projeto Capstone — Jala University 2026*
