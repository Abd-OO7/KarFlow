-- =====================================================
-- V4 : Seed test data — KarFlow SaaS
-- Design Patterns used:
--   • Builder Pattern : UUIDs pre-generated, inserted in dependency order
--   • Object Mother   : realistic, reusable fixture data
--   • Fixture Factory : consistent tenant isolation across all entities
--
-- Diagramme relationnel (19 tables, 6 join tables) :
--
--   organisation ──M:N── city (via organisation_city)
--        │
--        ├──1:M── app_user
--        │           └── M:M role (via user_role)
--        │                  └── M:M permission (via role_permission)
--        ├──1:M── brand
--        │           └──1:M── vehicle_model
--        ├──1:M── category
--        ├──1:M── vehicle
--        │           ├──M:1── vehicle_model
--        │           ├──M:1── category
--        │           └──1:M── vehicle_status_history
--        ├──1:M── client
--        │           └──1:M── claim
--        ├──1:M── insurance
--        ├──1:M── rental
--        │           ├──M:1── vehicle
--        │           ├──M:1── client
--        │           ├──M:1── insurance (optional)
--        │           └──1:M── inspection_report
--        │                      └──M:1── app_user (agent)
--        └──1:1── invoice (via rental)
--                    ├──1:M── invoice_line
--                    └──1:M── payment
--
-- Two tenants created:
--   Tenant A = "KarFlow Casablanca" (full data, active operations)
--   Tenant B = "AutoLoc Marrakech"  (minimal data, separate isolation)
-- =====================================================

-- ═══════════════════════════════════════════════════
-- HELPER: UUID conversion function
-- MySQL stores BINARY(16), we use UNHEX(REPLACE(uuid,'-',''))
-- ═══════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════
-- TENANT A : KarFlow Casablanca (main test tenant)
-- ═══════════════════════════════════════════════════

SET @tenantA = UNHEX(REPLACE('a0000000-0000-0000-0000-000000000001', '-', ''));

-- ── Organisation A ──
SET @orgA = UNHEX(REPLACE('a1000000-0000-0000-0000-000000000001', '-', ''));
INSERT INTO organisation (id, tenant_id, name, siret, phone, email, logo_url, address, subscription_plan, created_at, updated_at, deleted)
VALUES (@orgA, @tenantA, 'KarFlow Casablanca', '12345678900012', '+212522334455', 'contact@karflow-casa.ma', NULL, '123 Boulevard Mohammed V, Casablanca', 'PRO', NOW(), NOW(), FALSE);

-- ── Permissions (8) ──
SET @permVR = UNHEX(REPLACE('a2000000-0000-0000-0000-000000000001', '-', ''));
SET @permVW = UNHEX(REPLACE('a2000000-0000-0000-0000-000000000002', '-', ''));
SET @permCR = UNHEX(REPLACE('a2000000-0000-0000-0000-000000000003', '-', ''));
SET @permCW = UNHEX(REPLACE('a2000000-0000-0000-0000-000000000004', '-', ''));
SET @permRM = UNHEX(REPLACE('a2000000-0000-0000-0000-000000000005', '-', ''));
SET @permIM = UNHEX(REPLACE('a2000000-0000-0000-0000-000000000006', '-', ''));
SET @permDV = UNHEX(REPLACE('a2000000-0000-0000-0000-000000000007', '-', ''));
SET @permSM = UNHEX(REPLACE('a2000000-0000-0000-0000-000000000008', '-', ''));

INSERT INTO permission (id, tenant_id, name, created_at, updated_at, deleted) VALUES
(@permVR, @tenantA, 'VEHICLE_READ',    NOW(), NOW(), FALSE),
(@permVW, @tenantA, 'VEHICLE_WRITE',   NOW(), NOW(), FALSE),
(@permCR, @tenantA, 'CLIENT_READ',     NOW(), NOW(), FALSE),
(@permCW, @tenantA, 'CLIENT_WRITE',    NOW(), NOW(), FALSE),
(@permRM, @tenantA, 'RENTAL_MANAGE',   NOW(), NOW(), FALSE),
(@permIM, @tenantA, 'INVOICE_MANAGE',  NOW(), NOW(), FALSE),
(@permDV, @tenantA, 'DASHBOARD_VIEW',  NOW(), NOW(), FALSE),
(@permSM, @tenantA, 'SETTINGS_MANAGE', NOW(), NOW(), FALSE);

-- ── Roles (3) ──
SET @roleOwner = UNHEX(REPLACE('a3000000-0000-0000-0000-000000000001', '-', ''));
SET @roleAdmin = UNHEX(REPLACE('a3000000-0000-0000-0000-000000000002', '-', ''));
SET @roleAgent = UNHEX(REPLACE('a3000000-0000-0000-0000-000000000003', '-', ''));

INSERT INTO role (id, tenant_id, name, created_at, updated_at, deleted) VALUES
(@roleOwner, @tenantA, 'OWNER', NOW(), NOW(), FALSE),
(@roleAdmin, @tenantA, 'ADMIN', NOW(), NOW(), FALSE),
(@roleAgent, @tenantA, 'AGENT', NOW(), NOW(), FALSE);

-- OWNER gets ALL permissions
INSERT INTO role_permission (role_id, permission_id) VALUES
(@roleOwner, @permVR), (@roleOwner, @permVW), (@roleOwner, @permCR), (@roleOwner, @permCW),
(@roleOwner, @permRM), (@roleOwner, @permIM), (@roleOwner, @permDV), (@roleOwner, @permSM);

-- ADMIN gets all except SETTINGS_MANAGE
INSERT INTO role_permission (role_id, permission_id) VALUES
(@roleAdmin, @permVR), (@roleAdmin, @permVW), (@roleAdmin, @permCR), (@roleAdmin, @permCW),
(@roleAdmin, @permRM), (@roleAdmin, @permIM), (@roleAdmin, @permDV);

-- AGENT gets limited
INSERT INTO role_permission (role_id, permission_id) VALUES
(@roleAgent, @permVR), (@roleAgent, @permCR), (@roleAgent, @permCW), (@roleAgent, @permRM);

-- ── Users (3) — password = "password123" hashed with BCrypt 12 ──
SET @userOwner = UNHEX(REPLACE('a4000000-0000-0000-0000-000000000001', '-', ''));
SET @userAdmin = UNHEX(REPLACE('a4000000-0000-0000-0000-000000000002', '-', ''));
SET @userAgent = UNHEX(REPLACE('a4000000-0000-0000-0000-000000000003', '-', ''));

INSERT INTO app_user (id, tenant_id, username, email, password, phone, enabled, organisation_id, created_at, updated_at, deleted) VALUES
(@userOwner, @tenantA, 'salah_owner',  'owner@karflow.ma',  '$2a$12$RMUJqcSYhitsirD6aGBidOBXQvygGhrc9NHLKp5gFuiBaUWFNBW1m', '+212661000001', TRUE, @orgA, NOW(), NOW(), FALSE),
(@userAdmin, @tenantA, 'ahmed_admin',  'admin@karflow.ma',  '$2a$12$RMUJqcSYhitsirD6aGBidOBXQvygGhrc9NHLKp5gFuiBaUWFNBW1m', '+212661000002', TRUE, @orgA, NOW(), NOW(), FALSE),
(@userAgent, @tenantA, 'karim_agent',  'agent@karflow.ma',  '$2a$12$RMUJqcSYhitsirD6aGBidOBXQvygGhrc9NHLKp5gFuiBaUWFNBW1m', '+212661000003', TRUE, @orgA, NOW(), NOW(), FALSE);

INSERT INTO user_role (user_id, role_id) VALUES
(@userOwner, @roleOwner),
(@userAdmin, @roleAdmin),
(@userAgent, @roleAgent);

-- ── Cities (5) ──
SET @cityCasa    = UNHEX(REPLACE('a5000000-0000-0000-0000-000000000001', '-', ''));
SET @cityRabat   = UNHEX(REPLACE('a5000000-0000-0000-0000-000000000002', '-', ''));
SET @cityMarr    = UNHEX(REPLACE('a5000000-0000-0000-0000-000000000003', '-', ''));
SET @cityTanger  = UNHEX(REPLACE('a5000000-0000-0000-0000-000000000004', '-', ''));
SET @cityAgadir  = UNHEX(REPLACE('a5000000-0000-0000-0000-000000000005', '-', ''));

INSERT INTO city (id, tenant_id, name, region, country, latitude, longitude, created_at, updated_at, deleted) VALUES
(@cityCasa,   @tenantA, 'Casablanca',  'Casablanca-Settat',    'Maroc', 33.5731, -7.5898,  NOW(), NOW(), FALSE),
(@cityRabat,  @tenantA, 'Rabat',       'Rabat-Salé-Kénitra',   'Maroc', 34.0209, -6.8416,  NOW(), NOW(), FALSE),
(@cityMarr,   @tenantA, 'Marrakech',   'Marrakech-Safi',       'Maroc', 31.6295, -7.9811,  NOW(), NOW(), FALSE),
(@cityTanger, @tenantA, 'Tanger',      'Tanger-Tétouan-Al Hoceïma', 'Maroc', 35.7595, -5.8340, NOW(), NOW(), FALSE),
(@cityAgadir, @tenantA, 'Agadir',      'Souss-Massa',          'Maroc', 30.4278, -9.5981,  NOW(), NOW(), FALSE);

-- Organisation ↔ City
INSERT INTO organisation_city (organisation_id, city_id) VALUES
(@orgA, @cityCasa), (@orgA, @cityRabat), (@orgA, @cityMarr);

-- ── Brands (6) ──
SET @brandDacia    = UNHEX(REPLACE('a6000000-0000-0000-0000-000000000001', '-', ''));
SET @brandRenault  = UNHEX(REPLACE('a6000000-0000-0000-0000-000000000002', '-', ''));
SET @brandPeugeot  = UNHEX(REPLACE('a6000000-0000-0000-0000-000000000003', '-', ''));
SET @brandVW       = UNHEX(REPLACE('a6000000-0000-0000-0000-000000000004', '-', ''));
SET @brandBMW      = UNHEX(REPLACE('a6000000-0000-0000-0000-000000000005', '-', ''));
SET @brandMercedes = UNHEX(REPLACE('a6000000-0000-0000-0000-000000000006', '-', ''));

INSERT INTO brand (id, tenant_id, name, slug, logo_url, created_at, updated_at, deleted) VALUES
(@brandDacia,    @tenantA, 'Dacia',         'dacia',         NULL, NOW(), NOW(), FALSE),
(@brandRenault,  @tenantA, 'Renault',       'renault',       NULL, NOW(), NOW(), FALSE),
(@brandPeugeot,  @tenantA, 'Peugeot',       'peugeot',       NULL, NOW(), NOW(), FALSE),
(@brandVW,       @tenantA, 'Volkswagen',    'volkswagen',    NULL, NOW(), NOW(), FALSE),
(@brandBMW,      @tenantA, 'BMW',           'bmw',           NULL, NOW(), NOW(), FALSE),
(@brandMercedes, @tenantA, 'Mercedes-Benz', 'mercedes-benz', NULL, NOW(), NOW(), FALSE);

-- ── Categories (4) ──
SET @catEco     = UNHEX(REPLACE('a7000000-0000-0000-0000-000000000001', '-', ''));
SET @catComfort = UNHEX(REPLACE('a7000000-0000-0000-0000-000000000002', '-', ''));
SET @catSUV     = UNHEX(REPLACE('a7000000-0000-0000-0000-000000000003', '-', ''));
SET @catPremium = UNHEX(REPLACE('a7000000-0000-0000-0000-000000000004', '-', ''));

INSERT INTO category (id, tenant_id, name, description, daily_rate_multiplier, created_at, updated_at, deleted) VALUES
(@catEco,     @tenantA, 'Économique', 'Véhicules compacts et économiques',        1.00, NOW(), NOW(), FALSE),
(@catComfort, @tenantA, 'Confort',    'Berlines confortables pour longs trajets',  1.30, NOW(), NOW(), FALSE),
(@catSUV,     @tenantA, 'SUV',        'SUV et crossovers spacieux',               1.50, NOW(), NOW(), FALSE),
(@catPremium, @tenantA, 'Premium',    'Véhicules haut de gamme et luxe',          2.00, NOW(), NOW(), FALSE);

-- ── Vehicle Models (10) ──
SET @modelLogan     = UNHEX(REPLACE('a8000000-0000-0000-0000-000000000001', '-', ''));
SET @modelSandero   = UNHEX(REPLACE('a8000000-0000-0000-0000-000000000002', '-', ''));
SET @modelDuster    = UNHEX(REPLACE('a8000000-0000-0000-0000-000000000003', '-', ''));
SET @modelClio      = UNHEX(REPLACE('a8000000-0000-0000-0000-000000000004', '-', ''));
SET @modelMegane    = UNHEX(REPLACE('a8000000-0000-0000-0000-000000000005', '-', ''));
SET @model308       = UNHEX(REPLACE('a8000000-0000-0000-0000-000000000006', '-', ''));
SET @modelGolf      = UNHEX(REPLACE('a8000000-0000-0000-0000-000000000007', '-', ''));
SET @modelTiguan    = UNHEX(REPLACE('a8000000-0000-0000-0000-000000000008', '-', ''));
SET @modelSerie3    = UNHEX(REPLACE('a8000000-0000-0000-0000-000000000009', '-', ''));
SET @modelClasseC   = UNHEX(REPLACE('a8000000-0000-0000-0000-00000000000a', '-', ''));

INSERT INTO vehicle_model (id, tenant_id, name, horse_power, door_count, seat_count, trunk_volume, fuel_type, transmission_type, year, brand_id, created_at, updated_at, deleted) VALUES
(@modelLogan,   @tenantA, 'Logan',       90,  4, 5, 510, 'DIESEL',   'MANUAL',    2023, @brandDacia,    NOW(), NOW(), FALSE),
(@modelSandero, @tenantA, 'Sandero',     90,  4, 5, 328, 'GASOLINE', 'MANUAL',    2024, @brandDacia,    NOW(), NOW(), FALSE),
(@modelDuster,  @tenantA, 'Duster',     150,  4, 5, 445, 'DIESEL',   'MANUAL',    2024, @brandDacia,    NOW(), NOW(), FALSE),
(@modelClio,    @tenantA, 'Clio V',     100,  4, 5, 391, 'GASOLINE', 'AUTOMATIC', 2024, @brandRenault,  NOW(), NOW(), FALSE),
(@modelMegane,  @tenantA, 'Megane',     130,  4, 5, 434, 'HYBRID',   'AUTOMATIC', 2023, @brandRenault,  NOW(), NOW(), FALSE),
(@model308,     @tenantA, '308',        130,  4, 5, 412, 'DIESEL',   'AUTOMATIC', 2024, @brandPeugeot,  NOW(), NOW(), FALSE),
(@modelGolf,    @tenantA, 'Golf 8',     150,  4, 5, 381, 'GASOLINE', 'AUTOMATIC', 2024, @brandVW,       NOW(), NOW(), FALSE),
(@modelTiguan,  @tenantA, 'Tiguan',     150,  4, 5, 615, 'DIESEL',   'AUTOMATIC', 2023, @brandVW,       NOW(), NOW(), FALSE),
(@modelSerie3,  @tenantA, 'Série 3',    184,  4, 5, 480, 'DIESEL',   'AUTOMATIC', 2024, @brandBMW,      NOW(), NOW(), FALSE),
(@modelClasseC, @tenantA, 'Classe C',   200,  4, 5, 455, 'GASOLINE', 'AUTOMATIC', 2024, @brandMercedes, NOW(), NOW(), FALSE);

-- ── Vehicles (15) — mix of statuses ──
SET @v01 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000001', '-', ''));
SET @v02 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000002', '-', ''));
SET @v03 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000003', '-', ''));
SET @v04 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000004', '-', ''));
SET @v05 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000005', '-', ''));
SET @v06 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000006', '-', ''));
SET @v07 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000007', '-', ''));
SET @v08 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000008', '-', ''));
SET @v09 = UNHEX(REPLACE('a9000000-0000-0000-0000-000000000009', '-', ''));
SET @v10 = UNHEX(REPLACE('a9000000-0000-0000-0000-00000000000a', '-', ''));
SET @v11 = UNHEX(REPLACE('a9000000-0000-0000-0000-00000000000b', '-', ''));
SET @v12 = UNHEX(REPLACE('a9000000-0000-0000-0000-00000000000c', '-', ''));
SET @v13 = UNHEX(REPLACE('a9000000-0000-0000-0000-00000000000d', '-', ''));
SET @v14 = UNHEX(REPLACE('a9000000-0000-0000-0000-00000000000e', '-', ''));
SET @v15 = UNHEX(REPLACE('a9000000-0000-0000-0000-00000000000f', '-', ''));

INSERT INTO vehicle (id, tenant_id, license_plate, color, mileage, daily_rate, photo_url, status, vehicle_model_id, category_id, created_at, updated_at, deleted) VALUES
-- Économiques (5)
(@v01, @tenantA, '12345-A-1',  'Blanc',  15200.00, 250.00, NULL, 'AVAILABLE',   @modelLogan,   @catEco,     NOW(), NOW(), FALSE),
(@v02, @tenantA, '12346-A-1',  'Gris',   22400.00, 250.00, NULL, 'AVAILABLE',   @modelLogan,   @catEco,     NOW(), NOW(), FALSE),
(@v03, @tenantA, '12347-A-1',  'Bleu',    8500.00, 280.00, NULL, 'RENTED',      @modelSandero, @catEco,     NOW(), NOW(), FALSE),
(@v04, @tenantA, '12348-A-1',  'Rouge',  12000.00, 300.00, NULL, 'AVAILABLE',   @modelClio,    @catEco,     NOW(), NOW(), FALSE),
(@v05, @tenantA, '12349-A-1',  'Noir',   31000.00, 250.00, NULL, 'MAINTENANCE', @modelLogan,   @catEco,     NOW(), NOW(), FALSE),
-- Confort (3)
(@v06, @tenantA, '12350-A-1',  'Gris',   18000.00, 400.00, NULL, 'AVAILABLE',   @modelMegane,  @catComfort, NOW(), NOW(), FALSE),
(@v07, @tenantA, '12351-A-1',  'Blanc',  14500.00, 380.00, NULL, 'RENTED',      @model308,     @catComfort, NOW(), NOW(), FALSE),
(@v08, @tenantA, '12352-A-1',  'Bleu',   21000.00, 420.00, NULL, 'AVAILABLE',   @modelGolf,    @catComfort, NOW(), NOW(), FALSE),
-- SUV (4)
(@v09, @tenantA, '12353-A-1',  'Noir',   25000.00, 450.00, NULL, 'AVAILABLE',   @modelDuster,  @catSUV,     NOW(), NOW(), FALSE),
(@v10, @tenantA, '12354-A-1',  'Blanc',  16000.00, 500.00, NULL, 'RENTED',      @modelTiguan,  @catSUV,     NOW(), NOW(), FALSE),
(@v11, @tenantA, '12355-A-1',  'Gris',   11000.00, 480.00, NULL, 'AVAILABLE',   @modelDuster,  @catSUV,     NOW(), NOW(), FALSE),
(@v12, @tenantA, '12356-A-1',  'Vert',   28000.00, 450.00, NULL, 'GARAGE',      @modelDuster,  @catSUV,     NOW(), NOW(), FALSE),
-- Premium (3)
(@v13, @tenantA, '12357-A-1',  'Noir',    9000.00, 700.00, NULL, 'AVAILABLE',   @modelSerie3,  @catPremium, NOW(), NOW(), FALSE),
(@v14, @tenantA, '12358-A-1',  'Blanc',   5000.00, 800.00, NULL, 'RENTED',      @modelClasseC, @catPremium, NOW(), NOW(), FALSE),
(@v15, @tenantA, '12359-A-1',  'Gris',   12000.00, 750.00, NULL, 'AVAILABLE',   @modelSerie3,  @catPremium, NOW(), NOW(), FALSE);

-- ── Vehicle Status History (sample entries) ──
SET @vsh01 = UNHEX(REPLACE('aa000000-0000-0000-0000-000000000001', '-', ''));
SET @vsh02 = UNHEX(REPLACE('aa000000-0000-0000-0000-000000000002', '-', ''));
SET @vsh03 = UNHEX(REPLACE('aa000000-0000-0000-0000-000000000003', '-', ''));
SET @vsh04 = UNHEX(REPLACE('aa000000-0000-0000-0000-000000000004', '-', ''));

INSERT INTO vehicle_status_history (id, tenant_id, vehicle_id, previous_status, new_status, change_date, comment, created_at, updated_at, deleted) VALUES
(@vsh01, @tenantA, @v03, 'AVAILABLE', 'RENTED',      NOW() - INTERVAL 5 DAY,  'Location client Benali',      NOW(), NOW(), FALSE),
(@vsh02, @tenantA, @v07, 'AVAILABLE', 'RENTED',      NOW() - INTERVAL 3 DAY,  'Location client El Amrani',   NOW(), NOW(), FALSE),
(@vsh03, @tenantA, @v05, 'AVAILABLE', 'MAINTENANCE', NOW() - INTERVAL 10 DAY, 'Problème freins — révision',  NOW(), NOW(), FALSE),
(@vsh04, @tenantA, @v10, 'AVAILABLE', 'RENTED',      NOW() - INTERVAL 7 DAY,  'Location client Tazi',        NOW(), NOW(), FALSE);

-- ── Clients (8) ──
SET @cl01 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000001', '-', ''));
SET @cl02 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000002', '-', ''));
SET @cl03 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000003', '-', ''));
SET @cl04 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000004', '-', ''));
SET @cl05 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000005', '-', ''));
SET @cl06 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000006', '-', ''));
SET @cl07 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000007', '-', ''));
SET @cl08 = UNHEX(REPLACE('ab000000-0000-0000-0000-000000000008', '-', ''));

INSERT INTO client (id, tenant_id, first_name, last_name, email, phone, cin, address, license_number, license_expiry, date_of_birth, created_at, updated_at, deleted) VALUES
(@cl01, @tenantA, 'Youssef',  'Benali',    'youssef.benali@gmail.com',   '+212661100001', 'BK123456', '45 Rue Ibn Batouta, Casablanca',  'P-123456', '2028-06-15', '1990-03-12', NOW(), NOW(), FALSE),
(@cl02, @tenantA, 'Fatima',   'El Amrani', 'fatima.elamrani@gmail.com',  '+212661100002', 'BK234567', '12 Avenue Hassan II, Rabat',       'P-234567', '2027-09-20', '1985-07-22', NOW(), NOW(), FALSE),
(@cl03, @tenantA, 'Omar',     'Tazi',      'omar.tazi@outlook.com',      '+212661100003', 'BK345678', '78 Boulevard Zerktouni, Casablanca', 'P-345678', '2029-01-10', '1992-11-05', NOW(), NOW(), FALSE),
(@cl04, @tenantA, 'Nadia',    'Berrada',   'nadia.berrada@yahoo.fr',     '+212661100004', 'BK456789', '23 Rue Allal Ben Abdellah, Fès',  'P-456789', '2026-12-30', '1988-04-18', NOW(), NOW(), FALSE),
(@cl05, @tenantA, 'Amine',    'Chraibi',   'amine.chraibi@gmail.com',    '+212661100005', 'BK567890', '56 Avenue FAR, Marrakech',         'P-567890', '2028-03-25', '1995-09-30', NOW(), NOW(), FALSE),
(@cl06, @tenantA, 'Khadija',  'Mansouri',  'khadija.mansouri@gmail.com', '+212661100006', 'BK678901', '89 Rue de la Liberté, Tanger',     'P-678901', '2027-07-15', '1991-01-14', NOW(), NOW(), FALSE),
(@cl07, @tenantA, 'Rachid',   'Alaoui',    'rachid.alaoui@hotmail.com',  '+212661100007', 'BK789012', '34 Boulevard Anfa, Casablanca',    'P-789012', '2029-05-01', '1987-06-08', NOW(), NOW(), FALSE),
(@cl08, @tenantA, 'Samira',   'Ouazzani',  'samira.ouazzani@gmail.com',  '+212661100008', 'BK890123', '67 Avenue Mohammed VI, Agadir',    'P-890123', '2028-11-20', '1993-12-25', NOW(), NOW(), FALSE);

-- ── Insurance (3) ──
SET @ins01 = UNHEX(REPLACE('ac000000-0000-0000-0000-000000000001', '-', ''));
SET @ins02 = UNHEX(REPLACE('ac000000-0000-0000-0000-000000000002', '-', ''));
SET @ins03 = UNHEX(REPLACE('ac000000-0000-0000-0000-000000000003', '-', ''));

INSERT INTO insurance (id, tenant_id, name, description, coverage_type, daily_rate, provider, created_at, updated_at, deleted) VALUES
(@ins01, @tenantA, 'Basique',       'Couverture responsabilité civile',        'TIERS',        30.00,  'Wafa Assurance',   NOW(), NOW(), FALSE),
(@ins02, @tenantA, 'Tous Risques',  'Couverture complète tous dommages',       'TOUS_RISQUES', 75.00,  'AXA Assurance',   NOW(), NOW(), FALSE),
(@ins03, @tenantA, 'Premium+',      'Tous risques + assistance 0km + véhicule de remplacement', 'PREMIUM', 120.00, 'Saham Assurance', NOW(), NOW(), FALSE);

-- ── Rentals (6) — various statuses ──
SET @r01 = UNHEX(REPLACE('ad000000-0000-0000-0000-000000000001', '-', ''));
SET @r02 = UNHEX(REPLACE('ad000000-0000-0000-0000-000000000002', '-', ''));
SET @r03 = UNHEX(REPLACE('ad000000-0000-0000-0000-000000000003', '-', ''));
SET @r04 = UNHEX(REPLACE('ad000000-0000-0000-0000-000000000004', '-', ''));
SET @r05 = UNHEX(REPLACE('ad000000-0000-0000-0000-000000000005', '-', ''));
SET @r06 = UNHEX(REPLACE('ad000000-0000-0000-0000-000000000006', '-', ''));

INSERT INTO rental (id, tenant_id, start_date, end_date, actual_return_date, mileage_before, mileage_after, deposit, total_amount, status, notes, vehicle_id, client_id, insurance_id, created_at, updated_at, deleted) VALUES
-- Rental 1: ACTIVE (vehicle v03, client Benali) — en cours
(@r01, @tenantA, CURDATE() - INTERVAL 5 DAY, CURDATE() + INTERVAL 2 DAY, NULL, 8500.00, NULL, 500.00, 1960.00, 'ACTIVE', 'Location semaine', @v03, @cl01, @ins01, NOW() - INTERVAL 5 DAY, NOW(), FALSE),
-- Rental 2: ACTIVE (vehicle v07, client El Amrani) — en cours
(@r02, @tenantA, CURDATE() - INTERVAL 3 DAY, CURDATE() + INTERVAL 4 DAY, NULL, 14500.00, NULL, 1000.00, 3822.00, 'ACTIVE', 'Déplacement professionnel', @v07, @cl02, @ins02, NOW() - INTERVAL 3 DAY, NOW(), FALSE),
-- Rental 3: ACTIVE (vehicle v10, client Tazi) — retour prévu demain (upcoming)
(@r03, @tenantA, CURDATE() - INTERVAL 7 DAY, CURDATE() + INTERVAL 1 DAY, NULL, 16000.00, NULL, 1500.00, 5250.00, 'ACTIVE', 'Vacances famille', @v10, @cl03, @ins02, NOW() - INTERVAL 7 DAY, NOW(), FALSE),
-- Rental 4: ACTIVE (vehicle v14, client Berrada) — OVERDUE (ended 2 days ago)
(@r04, @tenantA, CURDATE() - INTERVAL 10 DAY, CURDATE() - INTERVAL 2 DAY, NULL, 5000.00, NULL, 2000.00, 12800.00, 'ACTIVE', 'Location premium', @v14, @cl04, @ins03, NOW() - INTERVAL 10 DAY, NOW(), FALSE),
-- Rental 5: RETURNED (completed last week)
(@r05, @tenantA, CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL 15 DAY, CURDATE() - INTERVAL 15 DAY, 20000.00, 20850.00, 500.00, 1625.00, 'RETURNED', 'Court séjour', @v01, @cl05, @ins01, NOW() - INTERVAL 20 DAY, NOW(), FALSE),
-- Rental 6: RETURNED (completed 2 weeks ago, with late return)
(@r06, @tenantA, CURDATE() - INTERVAL 30 DAY, CURDATE() - INTERVAL 25 DAY, CURDATE() - INTERVAL 23 DAY, 22000.00, 23200.00, 500.00, 2375.00, 'RETURNED', 'Retour tardif +2 jours', @v02, @cl06, @ins01, NOW() - INTERVAL 30 DAY, NOW(), FALSE);

-- ── Inspection Reports (4) — departure + return for completed rentals ──
SET @ir01 = UNHEX(REPLACE('ae000000-0000-0000-0000-000000000001', '-', ''));
SET @ir02 = UNHEX(REPLACE('ae000000-0000-0000-0000-000000000002', '-', ''));
SET @ir03 = UNHEX(REPLACE('ae000000-0000-0000-0000-000000000003', '-', ''));
SET @ir04 = UNHEX(REPLACE('ae000000-0000-0000-0000-000000000004', '-', ''));

INSERT INTO inspection_report (id, tenant_id, type, fuel_level, mileage, exterior_front_scratches, exterior_front_dents, exterior_rear_scratches, exterior_rear_dents, exterior_left_scratches, exterior_left_dents, exterior_right_scratches, exterior_right_dents, interior_condition, tire_condition, comments, rental_id, agent_id, created_at, updated_at, deleted) VALUES
-- Rental 5: departure + return
(@ir01, @tenantA, 'DEPARTURE', 0.90, 20000.00, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'Bon état',     'Bon état', 'RAS au départ',     @r05, @userAgent, NOW() - INTERVAL 20 DAY, NOW(), FALSE),
(@ir02, @tenantA, 'RETURN',    0.70, 20850.00, FALSE, FALSE, FALSE, FALSE, TRUE,  FALSE, FALSE, FALSE, 'Bon état',     'Bon état', 'Légère rayure côté gauche', @r05, @userAgent, NOW() - INTERVAL 15 DAY, NOW(), FALSE),
-- Rental 6: departure + return
(@ir03, @tenantA, 'DEPARTURE', 0.85, 22000.00, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'Bon état',     'Bon état', 'RAS au départ',     @r06, @userAgent, NOW() - INTERVAL 30 DAY, NOW(), FALSE),
(@ir04, @tenantA, 'RETURN',    0.50, 23200.00, FALSE, FALSE, TRUE,  FALSE, FALSE, FALSE, FALSE, FALSE, 'Acceptable',   'Usure normale', 'Rayure arrière constatée', @r06, @userAdmin, NOW() - INTERVAL 23 DAY, NOW(), FALSE);

-- ── Invoices (4) — for completed + active rentals ──
SET @inv01 = UNHEX(REPLACE('af000000-0000-0000-0000-000000000001', '-', ''));
SET @inv02 = UNHEX(REPLACE('af000000-0000-0000-0000-000000000002', '-', ''));
SET @inv03 = UNHEX(REPLACE('af000000-0000-0000-0000-000000000003', '-', ''));
SET @inv04 = UNHEX(REPLACE('af000000-0000-0000-0000-000000000004', '-', ''));

INSERT INTO invoice (id, tenant_id, invoice_number, subtotal, tax_rate, tax_amount, discount, total_amount, status, due_date, paid_date, rental_id, created_at, updated_at, deleted) VALUES
-- Invoice 1: PAID (rental 5 — returned successfully)
(@inv01, @tenantA, 'INV-2026-00001', 1354.17, 20.00, 270.83, 0.00, 1625.00, 'PAID',      CURDATE() - INTERVAL 10 DAY, CURDATE() - INTERVAL 12 DAY, @r05, NOW() - INTERVAL 15 DAY, NOW(), FALSE),
-- Invoice 2: PAID (rental 6 — returned with late fee)
(@inv02, @tenantA, 'INV-2026-00002', 1979.17, 20.00, 395.83, 0.00, 2375.00, 'PAID',      CURDATE() - INTERVAL 5 DAY,  CURDATE() - INTERVAL 8 DAY,  @r06, NOW() - INTERVAL 23 DAY, NOW(), FALSE),
-- Invoice 3: SENT (rental 1 — active, invoice generated)
(@inv03, @tenantA, 'INV-2026-00003', 1633.33, 20.00, 326.67, 0.00, 1960.00, 'SENT',      CURDATE() + INTERVAL 25 DAY, NULL, @r01, NOW() - INTERVAL 5 DAY, NOW(), FALSE),
-- Invoice 4: DRAFT (rental 2 — active, just generated)
(@inv04, @tenantA, 'INV-2026-00004', 3185.00, 20.00, 637.00, 0.00, 3822.00, 'DRAFT',     CURDATE() + INTERVAL 27 DAY, NULL, @r02, NOW() - INTERVAL 3 DAY, NOW(), FALSE);

-- ── Invoice Lines (detailed breakdown) ──
SET @il01 = UNHEX(REPLACE('b0000000-0000-0000-0000-000000000001', '-', ''));
SET @il02 = UNHEX(REPLACE('b0000000-0000-0000-0000-000000000002', '-', ''));
SET @il03 = UNHEX(REPLACE('b0000000-0000-0000-0000-000000000003', '-', ''));
SET @il04 = UNHEX(REPLACE('b0000000-0000-0000-0000-000000000004', '-', ''));
SET @il05 = UNHEX(REPLACE('b0000000-0000-0000-0000-000000000005', '-', ''));
SET @il06 = UNHEX(REPLACE('b0000000-0000-0000-0000-000000000006', '-', ''));
SET @il07 = UNHEX(REPLACE('b0000000-0000-0000-0000-000000000007', '-', ''));
SET @il08 = UNHEX(REPLACE('b0000000-0000-0000-0000-000000000008', '-', ''));
SET @il09 = UNHEX(REPLACE('b0000000-0000-0000-0000-000000000009', '-', ''));

INSERT INTO invoice_line (id, tenant_id, label, quantity, unit_price, total_price, line_type, invoice_id, created_at, updated_at, deleted) VALUES
-- Invoice 1 lines (rental 5: 5 jours Logan Éco + assurance basique)
(@il01, @tenantA, 'Location véhicule 12345-A-1 — 5 jour(s)',     5.00, 250.00, 1250.00, 'RENTAL_DAYS', @inv01, NOW(), NOW(), FALSE),
(@il02, @tenantA, 'Assurance Basique',                             5.00,  30.00,  150.00, 'INSURANCE',   @inv01, NOW(), NOW(), FALSE),
-- Invoice 2 lines (rental 6: 5+2 jours Logan Éco + assurance + retard)
(@il03, @tenantA, 'Location véhicule 12346-A-1 — 7 jour(s)',     7.00, 250.00, 1750.00, 'RENTAL_DAYS', @inv02, NOW(), NOW(), FALSE),
(@il04, @tenantA, 'Assurance Basique',                             7.00,  30.00,  210.00, 'INSURANCE',   @inv02, NOW(), NOW(), FALSE),
(@il05, @tenantA, 'Retour tardif — 2 jour(s) supplémentaire(s)',  2.00, 375.00,  750.00, 'LATE_RETURN',  @inv02, NOW(), NOW(), FALSE),
-- Invoice 3 lines (rental 1: 7 jours Sandero Éco + assurance basique)
(@il06, @tenantA, 'Location véhicule 12347-A-1 — 7 jour(s)',     7.00, 280.00, 1960.00, 'RENTAL_DAYS', @inv03, NOW(), NOW(), FALSE),
(@il07, @tenantA, 'Assurance Basique',                             7.00,  30.00,  210.00, 'INSURANCE',   @inv03, NOW(), NOW(), FALSE),
-- Invoice 4 lines (rental 2: 7 jours 308 Confort + assurance tous risques)
(@il08, @tenantA, 'Location véhicule 12351-A-1 — 7 jour(s)',     7.00, 494.00, 3458.00, 'RENTAL_DAYS', @inv04, NOW(), NOW(), FALSE),
(@il09, @tenantA, 'Assurance Tous Risques',                        7.00,  75.00,  525.00, 'INSURANCE',   @inv04, NOW(), NOW(), FALSE);

-- ── Payments (3) — for paid invoices ──
SET @pay01 = UNHEX(REPLACE('b1000000-0000-0000-0000-000000000001', '-', ''));
SET @pay02 = UNHEX(REPLACE('b1000000-0000-0000-0000-000000000002', '-', ''));
SET @pay03 = UNHEX(REPLACE('b1000000-0000-0000-0000-000000000003', '-', ''));

INSERT INTO payment (id, tenant_id, amount, payment_date, payment_method, transaction_ref, notes, invoice_id, created_at, updated_at, deleted) VALUES
(@pay01, @tenantA, 1625.00, NOW() - INTERVAL 12 DAY, 'CREDIT_CARD',   'TXN-2026-001', 'Paiement complet par CB',        @inv01, NOW(), NOW(), FALSE),
(@pay02, @tenantA, 1500.00, NOW() - INTERVAL 9 DAY,  'CASH',          NULL,            'Acompte espèces',                @inv02, NOW(), NOW(), FALSE),
(@pay03, @tenantA,  875.00, NOW() - INTERVAL 8 DAY,  'BANK_TRANSFER', 'VIR-2026-042', 'Solde par virement',             @inv02, NOW(), NOW(), FALSE);

-- ── Claims (3) ──
SET @claim01 = UNHEX(REPLACE('b2000000-0000-0000-0000-000000000001', '-', ''));
SET @claim02 = UNHEX(REPLACE('b2000000-0000-0000-0000-000000000002', '-', ''));
SET @claim03 = UNHEX(REPLACE('b2000000-0000-0000-0000-000000000003', '-', ''));

INSERT INTO claim (id, tenant_id, subject, description, status, priority, resolution, resolved_at, client_id, rental_id, created_at, updated_at, deleted) VALUES
(@claim01, @tenantA, 'Climatisation défaillante', 'La climatisation ne fonctionne plus correctement depuis le 2ème jour de location.', 'RESOLVED', 'HIGH', 'Remise de 200 MAD accordée. Véhicule envoyé en maintenance.', NOW() - INTERVAL 10 DAY, @cl05, @r05, NOW() - INTERVAL 14 DAY, NOW(), FALSE),
(@claim02, @tenantA, 'Facturation incorrecte', 'Le montant facturé ne correspond pas au devis initial. Différence de 150 MAD.', 'IN_PROGRESS', 'MEDIUM', NULL, NULL, @cl06, @r06, NOW() - INTERVAL 5 DAY, NOW(), FALSE),
(@claim03, @tenantA, 'Propreté du véhicule', 'Le véhicule n''était pas propre au moment de la prise en charge.', 'OPEN', 'LOW', NULL, NULL, @cl01, @r01, NOW() - INTERVAL 2 DAY, NOW(), FALSE);


-- ═══════════════════════════════════════════════════
-- TENANT B : AutoLoc Marrakech (isolated second tenant)
-- ═══════════════════════════════════════════════════

SET @tenantB = UNHEX(REPLACE('b0000000-0000-0000-0000-000000000099', '-', ''));
SET @orgB    = UNHEX(REPLACE('b1000000-0000-0000-0000-000000000099', '-', ''));

INSERT INTO organisation (id, tenant_id, name, siret, phone, email, address, subscription_plan, created_at, updated_at, deleted)
VALUES (@orgB, @tenantB, 'AutoLoc Marrakech', '98765432100012', '+212524445566', 'info@autoloc-marrakech.ma', '45 Avenue Mohammed VI, Marrakech', 'FREE', NOW(), NOW(), FALSE);

-- Permissions for tenant B
SET @bPermVR = UNHEX(REPLACE('b2000000-0000-0000-0000-000000000091', '-', ''));
SET @bPermVW = UNHEX(REPLACE('b2000000-0000-0000-0000-000000000092', '-', ''));
SET @bPermCR = UNHEX(REPLACE('b2000000-0000-0000-0000-000000000093', '-', ''));
SET @bPermCW = UNHEX(REPLACE('b2000000-0000-0000-0000-000000000094', '-', ''));
SET @bPermRM = UNHEX(REPLACE('b2000000-0000-0000-0000-000000000095', '-', ''));
SET @bPermIM = UNHEX(REPLACE('b2000000-0000-0000-0000-000000000096', '-', ''));
SET @bPermDV = UNHEX(REPLACE('b2000000-0000-0000-0000-000000000097', '-', ''));
SET @bPermSM = UNHEX(REPLACE('b2000000-0000-0000-0000-000000000098', '-', ''));

INSERT INTO permission (id, tenant_id, name, created_at, updated_at, deleted) VALUES
(@bPermVR, @tenantB, 'VEHICLE_READ',    NOW(), NOW(), FALSE),
(@bPermVW, @tenantB, 'VEHICLE_WRITE',   NOW(), NOW(), FALSE),
(@bPermCR, @tenantB, 'CLIENT_READ',     NOW(), NOW(), FALSE),
(@bPermCW, @tenantB, 'CLIENT_WRITE',    NOW(), NOW(), FALSE),
(@bPermRM, @tenantB, 'RENTAL_MANAGE',   NOW(), NOW(), FALSE),
(@bPermIM, @tenantB, 'INVOICE_MANAGE',  NOW(), NOW(), FALSE),
(@bPermDV, @tenantB, 'DASHBOARD_VIEW',  NOW(), NOW(), FALSE),
(@bPermSM, @tenantB, 'SETTINGS_MANAGE', NOW(), NOW(), FALSE);

SET @bRoleOwner = UNHEX(REPLACE('b3000000-0000-0000-0000-000000000099', '-', ''));
INSERT INTO role (id, tenant_id, name, created_at, updated_at, deleted)
VALUES (@bRoleOwner, @tenantB, 'OWNER', NOW(), NOW(), FALSE);

INSERT INTO role_permission (role_id, permission_id) VALUES
(@bRoleOwner, @bPermVR), (@bRoleOwner, @bPermVW), (@bRoleOwner, @bPermCR), (@bRoleOwner, @bPermCW),
(@bRoleOwner, @bPermRM), (@bRoleOwner, @bPermIM), (@bRoleOwner, @bPermDV), (@bRoleOwner, @bPermSM);

SET @bUser = UNHEX(REPLACE('b4000000-0000-0000-0000-000000000099', '-', ''));
INSERT INTO app_user (id, tenant_id, username, email, password, phone, enabled, organisation_id, created_at, updated_at, deleted)
VALUES (@bUser, @tenantB, 'hassan_owner', 'owner@autoloc.ma', '$2a$12$RMUJqcSYhitsirD6aGBidOBXQvygGhrc9NHLKp5gFuiBaUWFNBW1m', '+212661999001', TRUE, @orgB, NOW(), NOW(), FALSE);

INSERT INTO user_role (user_id, role_id) VALUES (@bUser, @bRoleOwner);

-- Link tenant B to Marrakech
INSERT INTO organisation_city (organisation_id, city_id) VALUES (@orgB, @cityMarr);

-- Tenant B brands + vehicles (minimal)
SET @bBrand = UNHEX(REPLACE('b6000000-0000-0000-0000-000000000099', '-', ''));
SET @bCat   = UNHEX(REPLACE('b7000000-0000-0000-0000-000000000099', '-', ''));
SET @bModel = UNHEX(REPLACE('b8000000-0000-0000-0000-000000000099', '-', ''));
SET @bVeh   = UNHEX(REPLACE('b9000000-0000-0000-0000-000000000099', '-', ''));

INSERT INTO brand (id, tenant_id, name, slug, created_at, updated_at, deleted)
VALUES (@bBrand, @tenantB, 'Hyundai', 'hyundai', NOW(), NOW(), FALSE);

INSERT INTO category (id, tenant_id, name, description, daily_rate_multiplier, created_at, updated_at, deleted)
VALUES (@bCat, @tenantB, 'Économique', 'Petites voitures', 1.00, NOW(), NOW(), FALSE);

INSERT INTO vehicle_model (id, tenant_id, name, horse_power, door_count, seat_count, trunk_volume, fuel_type, transmission_type, year, brand_id, created_at, updated_at, deleted)
VALUES (@bModel, @tenantB, 'i10', 67, 4, 5, 252, 'GASOLINE', 'MANUAL', 2024, @bBrand, NOW(), NOW(), FALSE);

INSERT INTO vehicle (id, tenant_id, license_plate, color, mileage, daily_rate, status, vehicle_model_id, category_id, created_at, updated_at, deleted)
VALUES (@bVeh, @tenantB, '99001-B-1', 'Blanc', 5000.00, 200.00, 'AVAILABLE', @bModel, @bCat, NOW(), NOW(), FALSE);

-- Tenant B client
SET @bClient = UNHEX(REPLACE('bb000000-0000-0000-0000-000000000099', '-', ''));
INSERT INTO client (id, tenant_id, first_name, last_name, email, phone, cin, license_number, license_expiry, date_of_birth, created_at, updated_at, deleted)
VALUES (@bClient, @tenantB, 'Mehdi', 'Ziani', 'mehdi.ziani@gmail.com', '+212661999002', 'MA999888', 'P-999888', '2029-01-01', '1994-05-20', NOW(), NOW(), FALSE);
