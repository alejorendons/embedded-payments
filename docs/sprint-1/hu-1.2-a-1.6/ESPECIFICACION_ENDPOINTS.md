# Especificación Técnica de Endpoints - HU 1.2 a 1.6

## Resumen
Implementación de 5 endpoints REST para gestión avanzada de comercios con seguridad basada en roles, auditoría y enmascaramiento de datos sensibles.

---

## Endpoints Implementados

### 1. HU 1.2: Actualizar Información de Contacto

**Endpoint:** `PUT /api/v1/merchants/{id}/contact`

**Autenticación:** Requerida (JWT)
**Autorización:** El comercio autenticado o Administrador

**Request Body:**
```json
{
  "contact_name": "Juan Mosquera",
  "contact_email": "juan@example.com"
}
```

**Validaciones:**
- `contact_name`: No puede estar vacío
- `contact_email`: Debe ser un email válido (RFC 5322)

**Response (200 OK):**
```json
{
  "id": "uuid",
  "name": "Nombre Comercio",
  "email": "merchant@example.com",
  "status": "ACTIVE",
  "updated_at": "2024-04-30T10:30:00Z",
  "message": "Contact information updated successfully"
}
```

**Errores:**
- `404 Not Found`: Comercio no existe
- `400 Bad Request`: Email inválido o campos vacíos
- `403 Forbidden`: No autorizado (no es el propietario ni admin)

**Auditoría:**
- Registra cambios en tabla `merchant_audit_detail` (campo anterior y nuevo)
- Evento: `MERCHANT_CONTACT_UPDATED`
- Incluye timestamp y usuario que realizó el cambio

---

### 2. HU 1.3: Registrar Datos Bancarios

**Endpoint:** `PUT /api/v1/merchants/{id}/bank-account`

**Autenticación:** Requerida (JWT)
**Autorización:** Comercio autenticado y activo o Administrador

**Request Body:**
```json
{
  "iban": "ES9121000418450200051332",
  "routing_number": "021000021",
  "account_holder_name": "Empresa S.L."
}
```

**Validaciones:**
- `iban`: Formato IBAN válido con checksum ISO 7064
- `routing_number`: Exactamente 9 dígitos, checksum válido (USA)
- `account_holder_name`: No puede estar vacío
- **Comercio debe estar ACTIVO**

**Encriptación:**
- Datos cifrados con AES-256-GCM
- Almacenados en columna `encrypted_bank_data` (BYTEA)
- Se genera hash SHA-256 del IBAN para búsquedas sin desencriptar

**Response (200 OK):**
```json
{
  "id": "uuid",
  "name": "Nombre Comercio",
  "email": "merchant@example.com",
  "status": "ACTIVE",
  "updated_at": "2024-04-30T10:30:00Z",
  "message": "Bank account registered successfully"
}
```

**Errores:**
- `404 Not Found`: Comercio no existe
- `400 Bad Request`: IBAN/Routing inválidos
- `403 Forbidden`: Comercio inactivo o no autorizado

**Auditoría (EVENTO CRÍTICO):**
- Registra en `merchant_audit_detail` sin mostrar datos desencriptados
- Evento: `MERCHANT_BANK_ACCOUNT_REGISTERED` o `MERCHANT_BANK_ACCOUNT_UPDATED`
- Incluye indicación `[ENCRYPTED_BANK_DATA]` en logs

---

### 3. HU 1.4: Activar Comercio

**Endpoint:** `PATCH /api/v1/merchants/{id}/activate`

**Autenticación:** Requerida (JWT)
**Autorización:** Solo Administrador (ROLE_ADMIN)

**Request Body:**
```json
{
  "reason": "Documentos verificados y aprobados"
}
```

**Validaciones:**
- Comercio debe existir
- Transición de estado: Solo INACTIVE → ACTIVE (válida)

**Response (200 OK):**
```json
{
  "id": "uuid",
  "name": "Nombre Comercio",
  "email": "merchant@example.com",
  "status": "ACTIVE",
  "updated_at": "2024-04-30T10:30:00Z",
  "message": "Merchant activated successfully"
}
```

**Errores:**
- `404 Not Found`: Comercio no existe
- `403 Forbidden`: No es administrador
- `422 Unprocessable Entity`: Transición de estado inválida

**Auditoría:**
- Registra en tabla `merchant_status_history`
- Campos: previous_status, new_status, changed_by, reason, timestamp
- Evento: `MERCHANT_ACTIVATED`

---

### 4. HU 1.5: Desactivar Comercio

**Endpoint:** `PATCH /api/v1/merchants/{id}/deactivate`

**Autenticación:** Requerida (JWT)
**Autorización:** Solo Administrador (ROLE_ADMIN)

**Request Body:**
```json
{
  "reason": "Incumplimiento de términos de servicio"
}
```

**Validaciones:**
- Comercio debe existir
- Transición de estado: ACTIVE/SUSPENDED → INACTIVE (válida)

**Response (200 OK):**
```json
{
  "id": "uuid",
  "name": "Nombre Comercio",
  "email": "merchant@example.com",
  "status": "INACTIVE",
  "updated_at": "2024-04-30T10:30:00Z",
  "message": "Merchant deactivated successfully"
}
```

**Errores:**
- `404 Not Found`: Comercio no existe
- `403 Forbidden`: No es administrador
- `422 Unprocessable Entity`: Transición de estado inválida

**Auditoría:**
- Registra en tabla `merchant_status_history`
- Evento: `MERCHANT_DEACTIVATED`

**Impacto:**
- Bloquea nuevas transacciones de pagos
- Las transacciones en progreso pueden completarse

---

### 5. HU 1.6: Consultar Detalles de Comercio

**Endpoint:** `GET /api/v1/merchants/{id}`

**Autenticación:** Requerida (JWT)
**Autorización:** Administrador o propietario del comercio

**Response (200 OK) - Admin:**
```json
{
  "id": "uuid",
  "name": "Nombre Comercio",
  "email": "merchant@example.com",
  "contact_name": "Juan Mosquera",
  "contact_email": "juan@example.com",
  "status": "ACTIVE",
  "bank_account_data": "[ENCRYPTED - Access granted]",
  "updated_at": "2024-04-30T10:30:00Z"
}
```

**Response (200 OK) - Merchant (no propietario):**
```json
{
  "id": "uuid",
  "name": "Nombre Comercio",
  "email": "merchant@example.com",
  "contact_name": "Juan Mosquera",
  "contact_email": "***@example.com",  // ENMASCARADO
  "status": "ACTIVE",
  "bank_account_data": "[RESTRICTED]",  // NO VISIBLE
  "updated_at": "2024-04-30T10:30:00Z"
}
```

**Errores:**
- `404 Not Found`: Comercio no existe
- `403 Forbidden`: No autorizado

**Enmascaramiento (OWASP):**
- **Email de contacto**: Mostrar solo dominio (`***@example.com`) - solo para otros comercios
- **Datos bancarios**: No visible excepto para propietario y admin
- **Admin**: Siempre ve datos completos

**Auditoría:**
- Registra en `audit_event`
- Evento: `MERCHANT_DETAILS_QUERIED`

---

## Máquina de Estados (Transiciones Válidas)

```
┌─────────┐     ┌────────┐     ┌──────────┐
│INACTIVE │────▶│ ACTIVE │────▶│ SUSPENDED│
└─────────┘     └────────┘     └──────────┘
    ▲              ▼                 ▼
    │          ┌────────┐        ┌────────┐
    └──────────│DISABLED│◀───────│        │
               └────────┘        └────────┘

- Solo admin puede cambiar estado
- Transiciones inválidas lanzan InvalidStateTransitionException
```

---

## Seguridad

### Autenticación
- JWT en header: `Authorization: Bearer <token>`
- Validación de firma y expiración

### Autorización
```
POST   /api/v1/merchants              : Público
PUT    /api/v1/merchants/{id}/contact : Autenticado
PUT    /api/v1/merchants/{id}/bank-account : Autenticado + ACTIVE
PATCH  /api/v1/merchants/{id}/activate   : ROLE_ADMIN
PATCH  /api/v1/merchants/{id}/deactivate : ROLE_ADMIN
GET    /api/v1/merchants/{id}         : Autenticado
```

### Encriptación
- **Datos Bancarios**: AES-256-GCM (generador de IV seguro)
- **Hash**: SHA-256 para búsquedas sin desencriptar

### Enmascaramiento (OWASP)
- Email: Mostrar solo dominio
- IBAN: Últimos 4 dígitos
- Routing: No mostrar
- Nombre de cuenta: Primeros 2 caracteres + ****

---

## Auditoría y Logging

### Eventos Registrados
```
MERCHANT_CONTACT_UPDATED        -> merchant_audit_detail + audit_event
MERCHANT_BANK_ACCOUNT_REGISTERED -> merchant_audit_detail + audit_event (CRÍTICO)
MERCHANT_ACTIVATED              -> merchant_status_history + audit_event
MERCHANT_DEACTIVATED            -> merchant_status_history + audit_event
MERCHANT_DETAILS_QUERIED        -> audit_event (READ_ACCESS)
```

### Campos Auditados
- Usuario que realizó el cambio
- Timestamp exacto
- Valor anterior y nuevo (solo no-sensibles)
- Razón del cambio (status transitions)

---

## Validaciones Customizadas

### @ValidIBAN
```java
- Formato: XX99 (2 letras + 2 dígitos + 1-30 alfanuméricos)
- Checksum: ISO 7064 mod 97
- Ejemplo válido: ES9121000418450200051332
```

### @ValidRoutingNumber
```java
- Formato: Exactamente 9 dígitos
- Checksum: Algoritmo de validación de routing (USA)
- Ejemplo válido: 021000021
```

---

## Configuración Requerida

```properties
# Encryption key (AES-256 en Base64)
encryption.secret-key=<generado con EncryptionService.generateKey()>

# Spring Security
spring.security.oauth2.jwt.issuer=http://localhost:8080
spring.security.oauth2.jwt.audience=embedded-payments-api
```

---

## Ejemplos de Uso (cURL)

### Activar Comercio (Admin)
```bash
curl -X PATCH http://localhost:8080/api/v1/merchants/{merchantId}/activate \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Verificación completada"}'
```

### Actualizar Información de Contacto
```bash
curl -X PUT http://localhost:8080/api/v1/merchants/{merchantId}/contact \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"contact_name": "Juan Mosquera", "contact_email": "juan@example.com"}'
```

### Registrar Cuenta Bancaria
```bash
curl -X PUT http://localhost:8080/api/v1/merchants/{merchantId}/bank-account \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "iban": "ES9121000418450200051332",
    "routing_number": "021000021",
    "account_holder_name": "Empresa S.L."
  }'
```

---

## Estado de Implementación
- ✅ Entidades y Value Objects
- ✅ Validadores personalizados
- ✅ Servicios de encriptación y enmascaramiento
- ✅ Use Cases
- ✅ Controladores y DTOs
- ✅ Auditoría extendida
- ⏳ Pruebas unitarias e integración (siguiente sprint)


