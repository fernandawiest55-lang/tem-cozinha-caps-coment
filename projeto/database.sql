-- ===================================================
-- TEM NA COZINHA — SCHEMA DO BANCO DE DADOS
-- PostgreSQL
-- ===================================================

-- Extensão para UUID (opcional)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- USUÁRIO
CREATE TABLE IF NOT EXISTS usuario (
    id          SERIAL PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    senha       VARCHAR(255) NOT NULL,
    restricoes  TEXT DEFAULT '',
    dieta       TEXT DEFAULT '',
    ativo       BOOLEAN DEFAULT TRUE,
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- INGREDIENTE DA DESPENSA
CREATE TABLE IF NOT EXISTS ingrediente (
    id          SERIAL PRIMARY KEY,
    usuario_id  INT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    nome        VARCHAR(150) NOT NULL,
    quantidade  NUMERIC(10,2),
    unidade     VARCHAR(50) DEFAULT 'unidades',
    local       VARCHAR(100) DEFAULT 'Geladeira',
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- RECEITA
CREATE TABLE IF NOT EXISTS receita (
    id                    SERIAL PRIMARY KEY,
    titulo                VARCHAR(200) NOT NULL,
    descricao             TEXT,
    ingredientes_necessarios TEXT,  -- JSON array como string
    modo_preparo          TEXT,
    tempo_preparo         INT,      -- em minutos
    dificuldade           VARCHAR(50) DEFAULT 'Fácil',
    categoria             VARCHAR(100),
    tags                  TEXT,     -- CSV
    nota_media            NUMERIC(3,2) DEFAULT 0,
    total_avaliacoes      INT DEFAULT 0,
    criado_em             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- FAVORITO
CREATE TABLE IF NOT EXISTS favorito (
    id          SERIAL PRIMARY KEY,
    usuario_id  INT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    receita_id  INT NOT NULL REFERENCES receita(id) ON DELETE CASCADE,
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(usuario_id, receita_id)
);

-- HISTÓRICO
CREATE TABLE IF NOT EXISTS historico (
    id          SERIAL PRIMARY KEY,
    usuario_id  INT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    receita_id  INT NOT NULL REFERENCES receita(id) ON DELETE CASCADE,
    data_acesso TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AVALIAÇÃO
CREATE TABLE IF NOT EXISTS avaliacao (
    id          SERIAL PRIMARY KEY,
    usuario_id  INT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    receita_id  INT NOT NULL REFERENCES receita(id) ON DELETE CASCADE,
    nota        INT NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario  TEXT,
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(usuario_id, receita_id)
);

-- LISTA DE COMPRAS
CREATE TABLE IF NOT EXISTS lista_compras (
    id          SERIAL PRIMARY KEY,
    usuario_id  INT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    nome        VARCHAR(200) NOT NULL,
    quantidade  VARCHAR(100),
    categoria   VARCHAR(100) DEFAULT 'Geral',
    comprado    BOOLEAN DEFAULT FALSE,
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ÍNDICES
CREATE INDEX IF NOT EXISTS idx_ingrediente_usuario ON ingrediente(usuario_id);
CREATE INDEX IF NOT EXISTS idx_favorito_usuario ON favorito(usuario_id);
CREATE INDEX IF NOT EXISTS idx_historico_usuario ON historico(usuario_id);
CREATE INDEX IF NOT EXISTS idx_avaliacao_receita ON avaliacao(receita_id);
CREATE INDEX IF NOT EXISTS idx_lista_usuario ON lista_compras(usuario_id);

-- ===================================================
-- DADOS DE EXEMPLO (RECEITAS)
-- ===================================================
INSERT INTO receita (titulo, descricao, ingredientes_necessarios, modo_preparo, tempo_preparo, dificuldade, categoria, tags)
VALUES
(
  'Arroz com Feijão Tradicional',
  'O clássico prato brasileiro, nutritivo e delicioso.',
  '["arroz", "feijão", "alho", "cebola", "sal", "óleo"]',
  'Cozinhe o feijão na panela de pressão por 20 minutos.
Refogue alho e cebola em óleo quente.
Acrescente o arroz lavado e refogue por 2 minutos.
Adicione água (dobro do volume do arroz) e sal.
Cozinhe em fogo baixo até secar.
Misture o feijão cozido ao refogado de alho e cebola.',
  45, 'Fácil', 'Almoço', 'brasileiro,tradicional,proteína'
),
(
  'Omelete de Queijo e Presunto',
  'Café da manhã rápido e proteico.',
  '["ovos", "queijo", "presunto", "sal", "manteiga", "pimenta"]',
  'Bata os ovos com sal e pimenta.
Aqueça a manteiga na frigideira.
Despeje os ovos batidos.
Adicione queijo e presunto na metade.
Dobre o omelete ao meio e sirva.',
  10, 'Fácil', 'Café da manhã', 'rápido,proteico,ovos'
),
(
  'Macarrão ao Molho de Tomate',
  'Clássico italiano simples e saboroso.',
  '["macarrão", "tomate", "alho", "azeite", "manjericão", "sal"]',
  'Cozinhe o macarrão al dente.
Refogue alho no azeite.
Adicione tomates picados e cozinhe 15 minutos.
Tempere com sal e manjericão.
Misture ao macarrão e sirva.',
  30, 'Fácil', 'Almoço', 'italiano,massa,tomate'
),
(
  'Salada de Frango Grelhado',
  'Leve e nutritiva, perfeita para o almoço.',
  '["frango", "alface", "tomate", "pepino", "limão", "azeite", "sal"]',
  'Tempere o frango com sal, limão e azeite.
Grelhe por 6 minutos de cada lado.
Fatie o frango e misture aos legumes.
Tempere com azeite, limão e sal.',
  25, 'Fácil', 'Almoço', 'saudável,proteico,salada'
),
(
  'Sopa de Legumes',
  'Reconfortante e cheia de nutrientes.',
  '["batata", "cenoura", "chuchu", "abobrinha", "cebola", "alho", "sal"]',
  'Refogue cebola e alho em azeite.
Adicione todos os legumes picados.
Cubra com água e cozinhe por 20 minutos.
Tempere com sal e cheiro-verde.',
  35, 'Fácil', 'Jantar', 'saudável,vegano,legumes'
),
(
  'Frango Assado com Batatas',
  'Prato completo e suculento para o almoço.',
  '["frango", "batata", "alho", "limão", "azeite", "alecrim", "sal", "pimenta"]',
  'Marine o frango com alho, limão, azeite, sal e pimenta por 30 minutos.
Corte as batatas em cubos e tempere.
Asse em forno a 200°C por 50-60 minutos.
Vire o frango na metade do tempo.',
  90, 'Médio', 'Almoço', 'frango,assado,tradicional'
);
