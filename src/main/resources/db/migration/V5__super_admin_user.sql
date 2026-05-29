-- =====================================================
-- V5 : Super Admin user for KarFlow platform management
-- =====================================================

SET @platformTenant = UNHEX(REPLACE('00000000-0000-0000-0000-000000000001', '-', ''));

-- Platform organisation
SET @platformOrg = UNHEX(REPLACE('00000000-0000-0000-0000-000000000002', '-', ''));
INSERT INTO organisation (id, tenant_id, name, email, subscription_plan, created_at, updated_at, deleted)
VALUES (@platformOrg, @platformTenant, 'KarFlow Platform', 'platform@karflow.ma', 'PLATFORM', NOW(), NOW(), FALSE);

-- Permission for platform tenant
SET @pPermAll = UNHEX(REPLACE('00000000-0000-0000-0000-000000000010', '-', ''));
INSERT INTO permission (id, tenant_id, name, created_at, updated_at, deleted)
VALUES (@pPermAll, @platformTenant, 'PLATFORM_MANAGE', NOW(), NOW(), FALSE);

-- SUPER_ADMIN role
SET @superAdminRole = UNHEX(REPLACE('00000000-0000-0000-0000-000000000020', '-', ''));
INSERT INTO role (id, tenant_id, name, created_at, updated_at, deleted)
VALUES (@superAdminRole, @platformTenant, 'SUPER_ADMIN', NOW(), NOW(), FALSE);

INSERT INTO role_permission (role_id, permission_id)
VALUES (@superAdminRole, @pPermAll);

-- Super admin user (password: password123)
SET @superAdminUser = UNHEX(REPLACE('00000000-0000-0000-0000-000000000030', '-', ''));
INSERT INTO app_user (id, tenant_id, username, email, password, phone, enabled, organisation_id, created_at, updated_at, deleted)
VALUES (@superAdminUser, @platformTenant, 'super_admin', 'superadmin@karflow.ma',
        '$2a$12$RMUJqcSYhitsirD6aGBidOBXQvygGhrc9NHLKp5gFuiBaUWFNBW1m',
        '+212600000000', TRUE, @platformOrg, NOW(), NOW(), FALSE);

INSERT INTO user_role (user_id, role_id)
VALUES (@superAdminUser, @superAdminRole);
