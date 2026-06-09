# 🍳 Tem na Cozinha

**Equipe:** The Furious Five | Jala University  
**Stack:** HTML/CSS/JS · Java puro · PostgreSQL · Apache Tomcat · Gemini AI

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

---

## ⚙️ Como Configurar

### 1. Banco de Dados

Crie o banco no PostgreSQL:

```bash
createdb tem_na_cozinha
psql -d tem_na_cozinha -f database/schema.sql
```

Se preferir pelo pgAdmin: botao direito em **Databases** → **Create** → **Database** → nome `tem_na_cozinha` → depois abra o Query Tool e cole o conteudo do `schema.sql`.

Edite as credenciais em `backend/src/config/ConnectionFactory.java`:

```java
private static final String URL     = "jdbc:postgresql://localhost:5432/tem_na_cozinha";
private static final String USUARIO = "postgres";
private static final String SENHA   = "123456";
```

### 2. Backend no IntelliJ + Tomcat

**Dependencias necessarias:**

Adicione em `File → Project Structure → Libraries`:
- `postgresql-42.x.x.jar` — driver JDBC do PostgreSQL
- `javax.servlet-api-4.0.1` — busque no Maven: `javax.servlet:javax.servlet-api:4.0.1`

**Chave da API Gemini:**

A chave fica no topo do `ReceitaController.java`:

```java
private static final String CHAVE_GEMINI = "sua-chave-aqui";
```

Gere sua chave em: https://aistudio.google.com/app/apikey

**Rodar:**

Configure o Tomcat no IntelliJ (`Run → Edit Configurations → Tomcat`) e clique em Run. A API sobe em `http://localhost:8080/api`.

### 3. Frontend

Abra qualquer pagina diretamente no navegador:

```
frontend/pages/index.html
```

Ou rode um servidor local:

```bash
cd frontend && python3 -m http.server 3000
```

Acesse: `http://localhost:3000/pages/index.html`

---

## 🔌 Endpoints da API

Todas as rotas exceto login, cadastro e recuperar-senha exigem o header:
```
Authorization: Bearer <token>
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
