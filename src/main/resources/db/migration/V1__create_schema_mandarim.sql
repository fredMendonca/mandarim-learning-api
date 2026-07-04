-- ============================================
-- BANCO: Sistema de Aprendizado de Mandarim
-- PostgreSQL
-- ============================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE SCHEMA IF NOT EXISTS mandarim;
SET search_path TO mandarim;

-- ============================================
-- USUÁRIOS
-- ============================================

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(200) UNIQUE NOT NULL,
    idioma_nativo VARCHAR(50) DEFAULT 'Português',
    nivel_hsk_atual INTEGER DEFAULT 1,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ativo BOOLEAN DEFAULT TRUE
);

-- ============================================
-- NÍVEIS HSK
-- ============================================

CREATE TABLE niveis_hsk (
    id SERIAL PRIMARY KEY,
    nivel INTEGER NOT NULL UNIQUE,
    descricao TEXT
);

-- ============================================
-- CATEGORIAS / TEMAS
-- ============================================

CREATE TABLE temas (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    descricao TEXT
);

-- ============================================
-- CONTEÚDOS
-- ============================================

CREATE TABLE conteudos (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(30) NOT NULL,
    hanzi TEXT NOT NULL,
    pinyin TEXT,
    traducao TEXT NOT NULL,
    explicacao TEXT,
    nivel_hsk INTEGER REFERENCES niveis_hsk(nivel),
    dificuldade SMALLINT DEFAULT 1,
    origem VARCHAR(30) DEFAULT 'MANUAL',
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_tipo_conteudo
    CHECK (tipo IN ('PALAVRA', 'FRASE', 'DIALOGO'))
);

-- ============================================
-- RELAÇÃO CONTEÚDO x TEMA
-- ============================================

CREATE TABLE conteudo_temas (
    conteudo_id BIGINT REFERENCES conteudos(id) ON DELETE CASCADE,
    tema_id INTEGER REFERENCES temas(id) ON DELETE CASCADE,
    PRIMARY KEY (conteudo_id, tema_id)
);

-- ============================================
-- TAGS
-- ============================================

CREATE TABLE tags (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE conteudo_tags (
    conteudo_id BIGINT REFERENCES conteudos(id) ON DELETE CASCADE,
    tag_id INTEGER REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (conteudo_id, tag_id)
);

-- ============================================
-- GRAMÁTICA
-- ============================================

CREATE TABLE estruturas_gramaticais (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL UNIQUE,
    descricao TEXT,
    exemplo_hanzi TEXT,
    exemplo_pinyin TEXT,
    exemplo_traducao TEXT
);

CREATE TABLE conteudo_gramatica (
    conteudo_id BIGINT REFERENCES conteudos(id) ON DELETE CASCADE,
    gramatica_id INTEGER REFERENCES estruturas_gramaticais(id) ON DELETE CASCADE,
    PRIMARY KEY (conteudo_id, gramatica_id)
);

-- ============================================
-- VOCABULÁRIO DENTRO DAS FRASES
-- ============================================

CREATE TABLE conteudo_vocabulario (
    frase_id BIGINT REFERENCES conteudos(id) ON DELETE CASCADE,
    palavra_id BIGINT REFERENCES conteudos(id) ON DELETE CASCADE,
    PRIMARY KEY (frase_id, palavra_id)
);

-- ============================================
-- SESSÕES DE ESTUDO
-- ============================================

CREATE TABLE sessoes_estudo (
    id BIGSERIAL PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fim TIMESTAMP,
    duracao_segundos INTEGER
);

-- ============================================
-- EXERCÍCIOS
-- ============================================

CREATE TABLE exercicios (
    id BIGSERIAL PRIMARY KEY,
    conteudo_id BIGINT NOT NULL REFERENCES conteudos(id),
    tipo VARCHAR(50) NOT NULL,
    enunciado TEXT NOT NULL,
    resposta_esperada TEXT,
    dificuldade SMALLINT DEFAULT 1,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_tipo_exercicio
    CHECK (tipo IN (
        'MULTIPLA_ESCOLHA',
        'TRADUCAO_PT_MANDARIM',
        'TRADUCAO_MANDARIM_PT',
        'PINYIN',
        'ESCRITA_LIVRE'
    ))
);

-- ============================================
-- ALTERNATIVAS DE QUESTÕES
-- ============================================

CREATE TABLE alternativas (
    id BIGSERIAL PRIMARY KEY,
    exercicio_id BIGINT NOT NULL REFERENCES exercicios(id) ON DELETE CASCADE,
    texto TEXT NOT NULL,
    correta BOOLEAN DEFAULT FALSE
);

-- ============================================
-- RESPOSTAS DOS USUÁRIOS
-- ============================================

CREATE TABLE respostas (
    id BIGSERIAL PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    sessao_id BIGINT REFERENCES sessoes_estudo(id),
    exercicio_id BIGINT NOT NULL REFERENCES exercicios(id),

    resposta_usuario TEXT NOT NULL,
    correta BOOLEAN,
    nota NUMERIC(5,2),
    tempo_resposta_segundos INTEGER,

    feedback TEXT,
    feedback_ia TEXT,

    respondido_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- REVISÃO ESPAÇADA
-- ============================================

CREATE TABLE revisoes (
    id BIGSERIAL PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    conteudo_id BIGINT NOT NULL REFERENCES conteudos(id),

    ultima_revisao TIMESTAMP,
    proxima_revisao DATE,
    intervalo_dias INTEGER DEFAULT 1,
    facilidade NUMERIC(4,2) DEFAULT 2.50,
    repeticoes INTEGER DEFAULT 0,
    lapsos INTEGER DEFAULT 0,

    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(usuario_id, conteudo_id)
);

-- ============================================
-- CONTEÚDO GERADO POR IA
-- ============================================

CREATE TABLE conteudos_ia (
    id BIGSERIAL PRIMARY KEY,
    usuario_id UUID REFERENCES usuarios(id),

    prompt TEXT NOT NULL,
    resposta_json JSONB,
    aprovado BOOLEAN DEFAULT FALSE,
    observacao TEXT,

    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- RECOMENDAÇÕES PERSONALIZADAS
-- ============================================

CREATE TABLE recomendacoes (
    id BIGSERIAL PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    conteudo_id BIGINT REFERENCES conteudos(id),

    motivo TEXT,
    score_prioridade NUMERIC(6,3),
    origem VARCHAR(50) DEFAULT 'REGRA',

    criada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    visualizada BOOLEAN DEFAULT FALSE
);

-- ============================================
-- MÉTRICAS DIÁRIAS PARA BI
-- ============================================

CREATE TABLE estatisticas_diarias (
    id BIGSERIAL PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    data_referencia DATE NOT NULL,

    exercicios_realizados INTEGER DEFAULT 0,
    acertos INTEGER DEFAULT 0,
    erros INTEGER DEFAULT 0,
    taxa_acerto NUMERIC(5,2),

    tempo_estudo_segundos INTEGER DEFAULT 0,
    palavras_estudadas INTEGER DEFAULT 0,
    frases_estudadas INTEGER DEFAULT 0,

    UNIQUE(usuario_id, data_referencia)
);

-- ============================================
-- LOG DE EVENTOS DO SISTEMA
-- ============================================

CREATE TABLE eventos_usuario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id UUID REFERENCES usuarios(id),
    tipo_evento VARCHAR(100) NOT NULL,
    descricao TEXT,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- ÍNDICES
-- ============================================

CREATE INDEX idx_conteudos_tipo ON conteudos(tipo);
CREATE INDEX idx_conteudos_hsk ON conteudos(nivel_hsk);
CREATE INDEX idx_conteudos_origem ON conteudos(origem);

CREATE INDEX idx_respostas_usuario ON respostas(usuario_id);
CREATE INDEX idx_respostas_exercicio ON respostas(exercicio_id);
CREATE INDEX idx_respostas_data ON respostas(respondido_em);

CREATE INDEX idx_revisoes_usuario ON revisoes(usuario_id);
CREATE INDEX idx_revisoes_proxima ON revisoes(proxima_revisao);

CREATE INDEX idx_estatisticas_usuario ON estatisticas_diarias(usuario_id);
CREATE INDEX idx_estatisticas_data ON estatisticas_diarias(data_referencia);

CREATE INDEX idx_recomendacoes_usuario ON recomendacoes(usuario_id);
