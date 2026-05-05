-- Test data for development
-- This file inserts a test merchant account for local development

BEGIN;

-- Insert test merchant with hashed password
-- Password: password123 (bcrypt hashed)
INSERT INTO merchants (
    id,
    email,
    password,
    status,
    created_at,
    updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    'test@example.com',
    '$2a$10$r9z4bJN1w.lH2Oy9VT9cw.g2Q0K8Ql0z0qE6F8K6L2M3N4O5P6Q7R',
    'ACTIVE',
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Insert merchant contact info
INSERT INTO merchant_contacts (
    id,
    merchant_id,
    first_name,
    last_name,
    email,
    phone,
    company_name,
    created_at,
    updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440001'::uuid,
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    'Test',
    'Merchant',
    'test@example.com',
    '+1234567890',
    'Test Company',
    NOW(),
    NOW()
) ON CONFLICT (merchant_id) DO NOTHING;

-- Insert merchant bank account
INSERT INTO merchant_bank_accounts (
    id,
    merchant_id,
    account_holder_name,
    account_number,
    routing_number,
    bank_name,
    account_type,
    created_at,
    updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440002'::uuid,
    '550e8400-e29b-41d4-a716-446655440000'::uuid,
    'Test Merchant',
    '123456789',
    '021000021',
    'Test Bank',
    'CHECKING',
    NOW(),
    NOW()
) ON CONFLICT (merchant_id) DO NOTHING;

COMMIT;
