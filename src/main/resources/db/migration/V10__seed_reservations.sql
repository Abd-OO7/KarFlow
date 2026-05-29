-- =====================================================================
-- V10 : Seed data — Réservations complètes pour tests
-- =====================================================================
--
-- Couvre tous les scénarios du workflow réservation :
--
--   PENDING   → en attente de confirmation agence          (3 réservations)
--   CONFIRMED → confirmée, pas encore convertie            (2 réservations)
--   CONVERTED → convertie en location (liée à un rental)  (2 réservations)
--   CANCELLED → annulée (client ou agence)                 (2 réservations)
--
-- Comptes clients avec mot de passe activé :
--   email    : cf. V4 (cl01–cl08)  /  nouveaux : cl09, cl10
--   password : "password123"  (BCrypt $2a$12$ — même hash que les agents)
--
-- Connexion portail client (POST /api/v1/auth/client-login) :
--   { "email": "youssef.benali@gmail.com", "password": "password123" }
-- =====================================================================

-- ── Référence au tenant principal (V4) ──────────────────────────────
SET @tenantA = UNHEX(REPLACE('a0000000-0000-0000-0000-000000000001', '-', ''));

-- ── Référence aux véhicules V4 ──────────────────────────────────────
SET @v01 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000001', '-', '')); -- Logan    AVAILABLE
SET @v02 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000002', '-', '')); -- Logan    AVAILABLE
SET @v04 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000004', '-', '')); -- Clio     AVAILABLE
SET @v06 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000006', '-', '')); -- Megane   AVAILABLE
SET @v08 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000008', '-', '')); -- Golf 8   AVAILABLE
SET @v09 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000009', '-', '')); -- Duster   AVAILABLE
SET @v10 = UNHEX(REPLACE('a9000000-0000-0000-0000-00000000000a', '-', '')); -- Tiguan   RENTED  (rental r03)
SET @v11 = UNHEX(REPLACE('a9000000-0000-0000-0000-00000000000b', '-', '')); -- Duster   AVAILABLE
SET @v13 = UNHEX(REPLACE('a9000000-0000-0000-0000-00000000000d', '-', '')); -- BMW S3   AVAILABLE
SET @v14 = UNHEX(REPLACE('a9000000-0000-0000-0000-00000000000e', '-', '')); -- Classe C RENTED  (rental r04)
SET @v15 = UNHEX(REPLACE('a9000000-0000-0000-0000-00000000000f', '-', '')); -- BMW S3   AVAILABLE

-- ── Référence aux clients V4 ─────────────────────────────────────────
SET @cl01 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000001', '-', '')); -- Youssef Benali
SET @cl02 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000002', '-', '')); -- Fatima El Amrani
SET @cl03 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000003', '-', '')); -- Omar Tazi
SET @cl04 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000004', '-', '')); -- Nadia Berrada
SET @cl05 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000005', '-', '')); -- Amine Chraibi
SET @cl06 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000006', '-', '')); -- Khadija Mansouri
SET @cl07 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000007', '-', '')); -- Rachid Alaoui
SET @cl08 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000008', '-', '')); -- Samira Ouazzani

-- ── Référence aux assurances V4 ─────────────────────────────────────
SET @ins01 = UNHEX(REPLACE('ac000000-0000-0000-0000-000000000001', '-', '')); -- Basique       30/j
SET @ins02 = UNHEX(REPLACE('ac000000-0000-0000-0000-000000000002', '-', '')); -- Tous Risques  75/j
SET @ins03 = UNHEX(REPLACE('ac000000-0000-0000-0000-000000000003', '-', '')); -- Premium+     120/j

-- ── Référence aux rentals V4 (pour CONVERTED) ───────────────────────
SET @r03 = UNHEX(REPLACE('ad000000-0000-0000-0000-000000000003', '-', '')); -- Tiguan   Tazi    ACTIVE
SET @r04 = UNHEX(REPLACE('ad000000-0000-0000-0000-000000000004', '-', '')); -- ClasseC  Berrada ACTIVE (overdue)

-- ═══════════════════════════════════════════════════════════════════
-- 1. ACTIVER les mots de passe clients (portal login)
--    Tous les clients V4 reçoivent : password = "password123"
--    Hash BCrypt 12 rounds (même que les agents)
-- ═══════════════════════════════════════════════════════════════════

UPDATE client
SET password = '$2a$12$RMUJqcSYhitsirD6aGBidOBXQvygGhrc9NHLKp5gFuiBaUWFNBW1m'
WHERE tenant_id = @tenantA
  AND id IN (@cl01, @cl02, @cl03, @cl04, @cl05, @cl06, @cl07, @cl08);

-- ═══════════════════════════════════════════════════════════════════
-- 2. NOUVEAUX CLIENTS (pour diversifier les scénarios)
--    password = "password123"
-- ═══════════════════════════════════════════════════════════════════

SET @cl09 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000009', '-', ''));
SET @cl10 = UNHEX(REPLACE('ab000000-0000-0000-0000-00000000000a', '-', ''));

INSERT INTO client (id, tenant_id, first_name, last_name, email, phone, cin,
                    password, address, license_number, license_expiry, date_of_birth,
                    created_at, updated_at, deleted)
VALUES
(@cl09, @tenantA,
 'Hicham', 'Lahlou', 'hicham.lahlou@gmail.com',
 '+212661200009', 'BK901234',
 '$2a$12$RMUJqcSYhitsirD6aGBidOBXQvygGhrc9NHLKp5gFuiBaUWFNBW1m',
 '11 Rue Patrice Lumumba, Casablanca',
 'P-901234', '2030-02-14', '1996-08-03',
 NOW(), NOW(), FALSE),

(@cl10, @tenantA,
 'Zineb', 'Kettani', 'zineb.kettani@outlook.com',
 '+212661200010', 'BK012345',
 '$2a$12$RMUJqcSYhitsirD6aGBidOBXQvygGhrc9NHLKp5gFuiBaUWFNBW1m',
 '28 Avenue Bir Anzarane, Rabat',
 'P-012345', '2029-08-30', '1998-11-17',
 NOW(), NOW(), FALSE);

-- ═══════════════════════════════════════════════════════════════════
-- 3. RÉSERVATIONS — IDs fixes pour reproductibilité
-- ═══════════════════════════════════════════════════════════════════

-- IDs
SET @res01 = UNHEX(REPLACE('c0000000-0000-0000-0000-000000000001', '-', ''));
SET @res02 = UNHEX(REPLACE('c0000000-0000-0000-0000-000000000002', '-', ''));
SET @res03 = UNHEX(REPLACE('c0000000-0000-0000-0000-000000000003', '-', ''));
SET @res04 = UNHEX(REPLACE('c0000000-0000-0000-0000-000000000004', '-', ''));
SET @res05 = UNHEX(REPLACE('c0000000-0000-0000-0000-000000000005', '-', ''));
SET @res06 = UNHEX(REPLACE('c0000000-0000-0000-0000-000000000006', '-', ''));
SET @res07 = UNHEX(REPLACE('c0000000-0000-0000-0000-000000000007', '-', ''));
SET @res08 = UNHEX(REPLACE('c0000000-0000-0000-0000-000000000008', '-', ''));
SET @res09 = UNHEX(REPLACE('c0000000-0000-0000-0000-000000000009', '-', ''));

-- ───────────────────────────────────────────────────────────────────
-- PENDING (3) — en attente de confirmation par l'agence
-- ───────────────────────────────────────────────────────────────────

-- res01 : Hicham Lahlou / Golf 8 Confort / dans 4 jours pour 5 jours / sans assurance
INSERT INTO reservation (id, tenant_id, client_id, vehicle_id, insurance_id,
                         start_date, end_date,
                         estimated_total, deposit_required,
                         status, notes,
                         created_at, updated_at, deleted)
VALUES (
    @res01, @tenantA, @cl09, @v08, NULL,
    CURDATE() + INTERVAL 4 DAY, CURDATE() + INTERVAL 9 DAY,
    2100.00, 500.00,
    'PENDING',
    'Déplacement pro — arrivée en soirée, possible livraison à l''aéroport ?',
    NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 2 HOUR, FALSE
);

-- res02 : Zineb Kettani / Duster SUV / dans 6 jours pour 7 jours / Tous Risques
INSERT INTO reservation (id, tenant_id, client_id, vehicle_id, insurance_id,
                         start_date, end_date,
                         estimated_total, deposit_required,
                         status, notes,
                         created_at, updated_at, deleted)
VALUES (
    @res02, @tenantA, @cl10, @v09, @ins02,
    CURDATE() + INTERVAL 6 DAY, CURDATE() + INTERVAL 13 DAY,
    3675.00, 1000.00,
    'PENDING',
    'Vacances famille Agadir. Besoin siège bébé si possible.',
    NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 5 HOUR, FALSE
);

-- res03 : Samira Ouazzani / BMW Série 3 Premium / dans 10 jours pour 4 jours / Premium+
INSERT INTO reservation (id, tenant_id, client_id, vehicle_id, insurance_id,
                         start_date, end_date,
                         estimated_total, deposit_required,
                         status, notes,
                         created_at, updated_at, deleted)
VALUES (
    @res03, @tenantA, @cl08, @v13, @ins03,
    CURDATE() + INTERVAL 10 DAY, CURDATE() + INTERVAL 14 DAY,
    3320.00, 2000.00,
    'PENDING',
    'Événement VIP — véhicule impeccable exigé.',
    NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 1 HOUR, FALSE
);

-- ───────────────────────────────────────────────────────────────────
-- CONFIRMED (2) — confirmées par l'agence, pas encore commencées
-- ───────────────────────────────────────────────────────────────────

-- res04 : Amine Chraibi / BMW Série 3 / demain pour 3 jours / Tous Risques
INSERT INTO reservation (id, tenant_id, client_id, vehicle_id, insurance_id,
                         start_date, end_date,
                         estimated_total, deposit_required,
                         status, notes, confirmed_at,
                         created_at, updated_at, deleted)
VALUES (
    @res04, @tenantA, @cl05, @v15, @ins02,
    CURDATE() + INTERVAL 1 DAY, CURDATE() + INTERVAL 4 DAY,
    2325.00, 2000.00,
    'CONFIRMED',
    'Confirmation OK par téléphone. Prise en charge à 09h00.',
    NOW() - INTERVAL 30 MINUTE,
    NOW() - INTERVAL 3 HOUR, NOW() - INTERVAL 30 MINUTE, FALSE
);

-- res05 : Rachid Alaoui / Clio Éco / dans 14 jours pour 10 jours / Basique
INSERT INTO reservation (id, tenant_id, client_id, vehicle_id, insurance_id,
                         start_date, end_date,
                         estimated_total, deposit_required,
                         status, notes, confirmed_at,
                         created_at, updated_at, deleted)
VALUES (
    @res05, @tenantA, @cl07, @v04, @ins01,
    CURDATE() + INTERVAL 14 DAY, CURDATE() + INTERVAL 24 DAY,
    3300.00, 500.00,
    'CONFIRMED',
    'Client fidèle — 3e location cette année.',
    NOW() - INTERVAL 2 DAY,
    NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 2 DAY, FALSE
);

-- ───────────────────────────────────────────────────────────────────
-- CONVERTED (2) — converties en location (liées aux rentals r03, r04)
-- ───────────────────────────────────────────────────────────────────

-- res06 : Omar Tazi / Tiguan SUV → rental r03 (ACTIVE, retour demain)
INSERT INTO reservation (id, tenant_id, client_id, vehicle_id, insurance_id,
                         start_date, end_date,
                         estimated_total, deposit_required,
                         status, notes, confirmed_at, converted_at,
                         rental_id,
                         created_at, updated_at, deleted)
VALUES (
    @res06, @tenantA, @cl03, @v10, @ins02,
    CURDATE() - INTERVAL 7 DAY, CURDATE() + INTERVAL 1 DAY,
    5250.00, 1500.00,
    'CONVERTED',
    'Réservation confirmée puis convertie en location le jour J.',
    NOW() - INTERVAL 8 DAY,
    NOW() - INTERVAL 7 DAY,
    @r03,
    NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 7 DAY, FALSE
);

-- res07 : Nadia Berrada / Classe C Premium → rental r04 (ACTIVE, en retard)
INSERT INTO reservation (id, tenant_id, client_id, vehicle_id, insurance_id,
                         start_date, end_date,
                         estimated_total, deposit_required,
                         status, notes, confirmed_at, converted_at,
                         rental_id,
                         created_at, updated_at, deleted)
VALUES (
    @res07, @tenantA, @cl04, @v14, @ins03,
    CURDATE() - INTERVAL 10 DAY, CURDATE() - INTERVAL 2 DAY,
    12800.00, 2000.00,
    'CONVERTED',
    'Mariage — durée susceptible d''être prolongée.',
    NOW() - INTERVAL 11 DAY,
    NOW() - INTERVAL 10 DAY,
    @r04,
    NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 10 DAY, FALSE
);

-- Mettre à jour les rentals r03 et r04 pour pointer vers leur réservation d'origine
UPDATE rental SET reservation_id = @res06 WHERE id = @r03 AND tenant_id = @tenantA;
UPDATE rental SET reservation_id = @res07 WHERE id = @r04 AND tenant_id = @tenantA;

-- ───────────────────────────────────────────────────────────────────
-- CANCELLED (2) — annulées
-- ───────────────────────────────────────────────────────────────────

-- res08 : Youssef Benali / Megane Confort — annulée par le client
INSERT INTO reservation (id, tenant_id, client_id, vehicle_id, insurance_id,
                         start_date, end_date,
                         estimated_total, deposit_required,
                         status, notes, cancellation_reason, cancelled_at,
                         created_at, updated_at, deleted)
VALUES (
    @res08, @tenantA, @cl01, @v06, @ins01,
    CURDATE() + INTERVAL 3 DAY, CURDATE() + INTERVAL 8 DAY,
    2150.00, 500.00,
    'CANCELLED',
    NULL,
    'Changement de programme — voyage annulé.',
    NOW() - INTERVAL 1 DAY,
    NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 1 DAY, FALSE
);

-- res09 : Fatima El Amrani / Duster SUV — refusée par l'agence (chevauchement)
INSERT INTO reservation (id, tenant_id, client_id, vehicle_id, insurance_id,
                         start_date, end_date,
                         estimated_total, deposit_required,
                         status, notes, cancellation_reason, cancelled_at,
                         created_at, updated_at, deleted)
VALUES (
    @res09, @tenantA, @cl02, @v09, @ins02,
    CURDATE() + INTERVAL 6 DAY, CURDATE() + INTERVAL 10 DAY,
    2260.00, 1000.00,
    'CANCELLED',
    NULL,
    'Véhicule déjà réservé sur cette période. Nous vous proposons le Duster v11.',
    NOW() - INTERVAL 4 HOUR,
    NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 4 HOUR, FALSE
);

-- ═══════════════════════════════════════════════════════════════════
-- 4. RÉSUMÉ FINAL — comptes de test disponibles
-- ═══════════════════════════════════════════════════════════════════
--
-- BACKOFFICE (POST /api/v1/auth/login) :
--   owner@karflow.ma   / password123  → OWNER  (accès complet)
--   admin@karflow.ma   / password123  → ADMIN
--   agent@karflow.ma   / password123  → AGENT
--
-- PORTAIL CLIENT (POST /api/v1/auth/client-login) :
--   youssef.benali@gmail.com    / password123  → res08 CANCELLED, rental r01 ACTIVE
--   fatima.elamrani@gmail.com   / password123  → res09 CANCELLED, rental r02 ACTIVE
--   omar.tazi@outlook.com       / password123  → res06 CONVERTED → rental r03 ACTIVE
--   nadia.berrada@yahoo.fr      / password123  → res07 CONVERTED → rental r04 OVERDUE
--   amine.chraibi@gmail.com     / password123  → res04 CONFIRMED, rental r05 RETURNED
--   khadija.mansouri@gmail.com  / password123  → rental r06 RETURNED
--   rachid.alaoui@hotmail.com   / password123  → res05 CONFIRMED
--   samira.ouazzani@gmail.com   / password123  → res03 PENDING
--   hicham.lahlou@gmail.com     / password123  → res01 PENDING  (nouveau)
--   zineb.kettani@outlook.com   / password123  → res02 PENDING  (nouveau)
--
-- STATUTS RÉSERVATIONS :
--   res01 PENDING   — Golf 8,     Hicham Lahlou,    dans 4 jours (5 nuits)
--   res02 PENDING   — Duster,     Zineb Kettani,    dans 6 jours (7 nuits)
--   res03 PENDING   — BMW S3,     Samira Ouazzani,  dans 10 jours (4 nuits)
--   res04 CONFIRMED — BMW S3 #2,  Amine Chraibi,    demain (3 nuits)
--   res05 CONFIRMED — Clio,       Rachid Alaoui,    dans 14 jours (10 nuits)
--   res06 CONVERTED — Tiguan → r03, Omar Tazi,      en cours (retour demain)
--   res07 CONVERTED — ClasseC → r04, Nadia Berrada, en cours (EN RETARD)
--   res08 CANCELLED — Megane,     Youssef Benali,   annulée par client
--   res09 CANCELLED — Duster,     Fatima El Amrani, refusée par agence
-- ═══════════════════════════════════════════════════════════════════
