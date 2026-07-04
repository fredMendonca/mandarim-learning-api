SET search_path TO mandarim;

-- Níveis HSK
INSERT INTO niveis_hsk (nivel, descricao) VALUES
(1, 'HSK 1 - Iniciante'),
(2, 'HSK 2 - Básico'),
(3, 'HSK 3 - Intermediário inicial'),
(4, 'HSK 4 - Intermediário'),
(5, 'HSK 5 - Avançado inicial'),
(6, 'HSK 6 - Avançado')
ON CONFLICT DO NOTHING;

-- Temas básicos
INSERT INTO temas (nome, descricao) VALUES
('Saudações', 'Cumprimentos e expressões básicas'),
('Família', 'Vocabulário sobre família'),
('Comida', 'Alimentos, bebidas e restaurante'),
('Viagem', 'Frases úteis para viagens'),
('Trabalho', 'Vocabulário profissional'),
('Números', 'Números, datas e quantidades'),
('Tempo', 'Clima, horas e dias')
ON CONFLICT DO NOTHING;

-- Tags básicas
INSERT INTO tags (nome) VALUES
('iniciante'),
('pronuncia'),
('vocabulário'),
('gramática'),
('tradução'),
('revisão')
ON CONFLICT DO NOTHING;
