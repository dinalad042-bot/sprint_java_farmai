-- ============================================
-- FarmAI - Fix Test Data for AGRICOLE Users
-- Run this in phpMyAdmin to fix missing farm associations
-- ============================================

USE farmai;

-- ============================================
-- PROBLEM IDENTIFIED:
-- User ID 10 (fermier1@farmai.com) is AGRICOLE but has NO farm
-- ============================================

-- Step 1: Add a farm for user ID 10 (fermier1@farmai.com)
INSERT INTO ferme (nom_ferme, lieu, surface, id_fermier)
VALUES ('Ferme Fermier 1', 'Nabeul, Tunisie', 45.0, 10)
ON DUPLICATE KEY UPDATE nom_ferme = VALUES(nom_ferme);

-- Step 2: Verify the farm was created
SELECT 'Ferme for user 10:' as info;
SELECT * FROM ferme WHERE id_fermier = 10;

-- ============================================
-- Step 3: Insert sample analyses for testing
-- ============================================

-- First, let's check existing analyses
SELECT 'Existing analyses:' as info;
SELECT * FROM analyse;

-- Insert analyses for the new farm (id_ferme will be auto-generated, let's find it)
-- Assuming the new farm gets id_ferme = 5, or we use a subquery

INSERT INTO analyse (resultat_technique, id_technicien, id_ferme, image_url)
VALUES 
    ('Analyse de sol: pH 6.8, bonne teneur en matière organique. Recommandation: fertilisation azotée légère.', 2, 
     (SELECT id_ferme FROM ferme WHERE id_fermier = 10), NULL),
    ('Détection de nuisibles: Pucerons présents à 5%. Traitement biologique recommandé.', 2,
     (SELECT id_ferme FROM ferme WHERE id_fermier = 10), NULL),
    ('Analyse foliaire: État sanitaire excellent. Pas de carences détectées.', 5,
     (SELECT id_ferme FROM ferme WHERE id_fermier = 10), NULL);

-- Step 4: Insert sample conseils for the analyses
INSERT INTO conseil (description_conseil, priorite, id_analyse)
SELECT 'Appliquer un traitement biologique contre les pucerons dans les 3 jours', 'HAUTE', id_analyse
FROM analyse WHERE id_ferme = (SELECT id_ferme FROM ferme WHERE id_fermier = 10)
AND resultat_technique LIKE '%Pucerons%'
LIMIT 1;

INSERT INTO conseil (description_conseil, priorite, id_analyse)
SELECT 'Fertilisation azotée légère recommandée avant la prochaine saison', 'MOYENNE', id_analyse
FROM analyse WHERE id_ferme = (SELECT id_ferme FROM ferme WHERE id_fermier = 10)
AND resultat_technique LIKE '%pH 6.8%'
LIMIT 1;

INSERT INTO conseil (description_conseil, priorite, id_analyse)
SELECT 'Continuer les pratiques actuelles, excellent état sanitaire', 'BASSE', id_analyse
FROM analyse WHERE id_ferme = (SELECT id_ferme FROM ferme WHERE id_fermier = 10)
AND resultat_technique LIKE '%excellent%'
LIMIT 1;

-- ============================================
-- Step 5: Also add analyses for user ID 3 (Agricole Test) if none exist
-- ============================================
INSERT INTO analyse (resultat_technique, id_technicien, id_ferme, image_url)
SELECT 'Analyse complète du sol: Structure argileuse, bon drainage. Irrigation modérée recommandée.', 2, 3, NULL
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM analyse WHERE id_ferme = 3
);

INSERT INTO conseil (description_conseil, priorite, id_analyse)
SELECT 'Irrigation modérée: 2 fois par semaine maximum', 'MOYENNE', id_analyse
FROM analyse WHERE id_ferme = 3
LIMIT 1;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Show all farms with their owners
SELECT 'All farms with owners:' as info;
SELECT 
    f.id_ferme,
    f.nom_ferme,
    f.lieu,
    f.surface,
    f.id_fermier,
    CONCAT(u.prenom, ' ', u.nom) as fermier_name,
    u.email as fermier_email,
    u.role
FROM ferme f
JOIN user u ON f.id_fermier = u.id_user
ORDER BY f.id_ferme;

-- Show all analyses with farm details
SELECT 'All analyses:' as info;
SELECT 
    a.id_analyse,
    a.date_analyse,
    a.resultat_technique,
    f.nom_ferme,
    CONCAT(u.prenom, ' ', u.nom) as technicien_name
FROM analyse a
JOIN ferme f ON a.id_ferme = f.id_ferme
JOIN user u ON a.id_technicien = u.id_user
ORDER BY a.date_analyse DESC;

-- Show all conseils with analysis details
SELECT 'All conseils:' as info;
SELECT 
    c.id_conseil,
    c.description_conseil,
    c.priorite,
    a.resultat_technique as analyse_resultat,
    f.nom_ferme
FROM conseil c
JOIN analyse a ON c.id_analyse = a.id_analyse
JOIN ferme f ON a.id_ferme = f.id_ferme
ORDER BY 
    CASE c.priorite 
        WHEN 'HAUTE' THEN 1 
        WHEN 'MOYENNE' THEN 2 
        WHEN 'BASSE' THEN 3 
    END;

-- Count summary
SELECT 'Summary:' as info;
SELECT 
    (SELECT COUNT(*) FROM user WHERE role = 'AGRICOLE') as agricole_users,
    (SELECT COUNT(*) FROM ferme) as total_farms,
    (SELECT COUNT(*) FROM analyse) as total_analyses,
    (SELECT COUNT(*) FROM conseil) as total_conseils;

-- ============================================
-- INSTRUCTIONS:
-- 1. Copy and paste this entire script into phpMyAdmin SQL tab
-- 2. Click "Go" to execute
-- 3. The verification queries at the end will show the results
-- 4. Log in with fermier1@farmai.com to test the feature
-- ============================================