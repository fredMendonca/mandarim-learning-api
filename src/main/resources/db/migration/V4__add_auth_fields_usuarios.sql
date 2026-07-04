-- ============================================
-- V4: Adiciona campos de autenticação à tabela usuarios
-- Campos: senha, role, ultimo_login
-- Cria usuário admin inicial
-- ============================================

SET search_path TO mandarim;

-- 1. Adicionar novas colunas
ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS senha VARCHAR(255),
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'ALUNO',
    ADD COLUMN IF NOT EXISTS ultimo_login TIMESTAMP;

-- 2. Definir role ALUNO para usuários existentes
UPDATE usuarios SET role = 'ALUNO' WHERE role IS NULL;

-- 3. Tornar role NOT NULL
ALTER TABLE usuarios ALTER COLUMN role SET NOT NULL;

-- 4. Criar usuário admin inicial
-- Senha: Admin@123 (BCrypt hash)
INSERT INTO usuarios (id, nome, email, senha, role, idioma_nativo, nivel_hsk_atual, ativo, criado_em)
VALUES (
    gen_random_uuid(),
    'Administrador',
    'admin@mandarim.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    'Português',
    1,
    true,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO UPDATE SET
    senha = EXCLUDED.senha,
    role = 'ADMIN';
