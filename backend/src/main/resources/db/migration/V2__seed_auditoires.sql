-- Migration V2: Initial Auditoires for UCC Campus

INSERT INTO auditoires (code, name, promotion, level, position_order, side) VALUES
('N1-A01', 'Auditoire Jean-Paul II', 'L1 Sciences Informatiques', 1, 1, 'gauche'),
('N1-A02', 'Auditoire Cardinal Malula', 'L1 Droit', 1, 2, 'gauche'),
('N1-A03', 'Auditoire Mgr Tshibangu', 'L1 Sciences Économiques', 1, 3, 'droite'),
('N2-A01', 'Auditoire Thomas d''Aquin', 'L2 Génie Logiciel', 2, 1, 'gauche'),
('N2-A02', 'Auditoire Saint Augustin', 'L2 Réseaux & Télécoms', 2, 2, 'gauche'),
('N2-A03', 'Auditoire Mère Teresa', 'L2 Droit Privé', 2, 3, 'droite'),
('N3-A01', 'Auditoire Benoît XVI', 'L3 Génie Logiciel', 3, 1, 'gauche'),
('N3-A02', 'Auditoire François', 'Master 1 IA & Big Data', 3, 2, 'droite')
ON CONFLICT (code) DO NOTHING;
