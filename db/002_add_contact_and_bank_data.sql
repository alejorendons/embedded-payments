-- Migration: Agregar campos de contacto y datos bancarios a merchants
-- Historia de Usuario: HU 1.2, 1.3, 1.4, 1.5, 1.6
-- Descripción: Extiende la tabla merchants con información de contacto, datos bancarios cifrados,
--              y soporte para auditoría de cambios de estado

BEGIN;

-- ================================
-- ALTER merchants TABLE
-- ================================

-- Agregar columnas de contacto (HU 1.2)
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS contact_name VARCHAR(255);
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS contact_email VARCHAR(255);

-- Agregar columnas de datos bancarios cifrados (HU 1.3)
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS bank_account_encrypted TEXT;
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS bank_account_hash VARCHAR(255);
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS encrypted_bank_data BYTEA;

-- Agregar soporte para historial de estado (HU 1.4, 1.5)
ALTER TABLE merchants ADD COLUMN IF NOT EXISTS previous_status merchant_status;

-- ================================
-- CREATE merchant_status_history TABLE
-- ================================
-- Registra todas las transiciones de estado de comercios para auditoría

CREATE TABLE IF NOT EXISTS merchant_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    previous_status merchant_status NOT NULL,
    new_status merchant_status NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_merchant_status_history_merchant ON merchant_status_history(merchant_id);
CREATE INDEX IF NOT EXISTS idx_merchant_status_history_timestamp ON merchant_status_history(created_at);

-- ================================
-- CREATE merchant_audit_detail TABLE
-- ================================
-- Registra cambios detallados por campo (HU 1.2, 1.3)
-- Para seguimiento granular de auditoría

CREATE TABLE IF NOT EXISTS merchant_audit_detail (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    merchant_id UUID NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    event_type VARCHAR(100) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    changed_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_merchant_audit_detail_merchant ON merchant_audit_detail(merchant_id);
CREATE INDEX IF NOT EXISTS idx_merchant_audit_detail_event ON merchant_audit_detail(event_type);

-- ================================
-- CREATE INDICES
-- ================================

-- Índices de búsqueda (HU 1.6)
CREATE INDEX IF NOT EXISTS idx_merchants_status ON merchants(status);
CREATE INDEX IF NOT EXISTS idx_merchants_email ON merchants(email);
CREATE INDEX IF NOT EXISTS idx_merchants_contact_email ON merchants(contact_email);

COMMIT;

