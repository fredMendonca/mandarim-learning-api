-- ============================================
-- V3: Reestrutura tabela conteudos_ia para integração com LLM
-- Adiciona: status, parametros_json, conteudos_gerados_json, tempo_processamento_ms
-- Remove obrigatoriedade de: prompt (agora gerado pelo service)
-- Migra coluna aprovado → status
-- ============================================

SET search_path TO mandarim;

-- 1. Adiciona novas colunas
ALTER TABLE conteudos_ia
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDENTE',
    ADD COLUMN IF NOT EXISTS parametros_json TEXT,
    ADD COLUMN IF NOT EXISTS conteudos_gerados_json TEXT,
    ADD COLUMN IF NOT EXISTS tempo_processamento_ms BIGINT;

-- 2. Migra dados existentes: aprovado=true → APROVADO, false → PENDENTE
UPDATE conteudos_ia SET status = 'APROVADO' WHERE aprovado = TRUE;
UPDATE conteudos_ia SET status = 'PENDENTE' WHERE aprovado = FALSE OR aprovado IS NULL;

-- 3. Torna status NOT NULL após migração
ALTER TABLE conteudos_ia ALTER COLUMN status SET NOT NULL;

-- 4. Remove obrigatoriedade do prompt (agora gerado automaticamente)
ALTER TABLE conteudos_ia ALTER COLUMN prompt DROP NOT NULL;

-- 5. Remove coluna aprovado (substituída por status)
ALTER TABLE conteudos_ia DROP COLUMN IF EXISTS aprovado;

-- 6. Remove coluna resposta_json legada (substituída por conteudos_gerados_json)
ALTER TABLE conteudos_ia DROP COLUMN IF EXISTS resposta_json;
