# Referencia Rápida - HU 1.2 a 1.6

## 📱 Endpoints Quick Reference

```
┌─ HU 1.2: Contacto
│  PUT /api/v1/merchants/{id}/contact
│  Body: { contact_name, contact_email }
│  Auth: JWT | Roles: Autenticado
│
├─ HU 1.3: Datos Bancarios  
│  PUT /api/v1/merchants/{id}/bank-account
│  Body: { iban, routing_number, account_holder_name }
│  Auth: JWT | Roles: Autenticado + ACTIVE
│
├─ HU 1.4: Activar
│  PATCH /api/v1/merchants/{id}/activate
│  Body: { reason }
│  Auth: JWT | Roles: ROLE_ADMIN
│
├─ HU 1.5: Desactivar
│  PATCH /api/v1/merchants/{id}/deactivate
│  Body: { reason }
│  Auth: JWT | Roles: ROLE_ADMIN
│
└─ HU 1.6: Consultar
   GET /api/v1/merchants/{id}
   Auth: JWT | Roles: Autenticado
   (Email y Banco enmascarados para otros)
```

---

## 🔑 Clases Principales

### Entidad
```java
merchant/domain/entity/Merchant
  ├─ contactName, contactEmail
  ├─ bankAccountEncrypted, bankAccountHash, encryptedBankData
  ├─ status (INACTIVE, ACTIVE, SUSPENDED, DISABLED)
  └─ Métodos: updateContact(), registerBankAccount(), changeStatus()
```

### Use Cases
```java
UpdateMerchantContactUseCase(merchantId, contactName, contactEmail, actor)
RegisterBankAccountUseCase(merchantId, iban, routingNumber, accountHolder, actor)
ActivateMerchantUseCase(merchantId, reason, actor)
DeactivateMerchantUseCase(merchantId, reason, actor)
GetMerchantDetailUseCase(merchantId, requesterRole, requesterId)
```

### Servicios
```java
EncryptionService.encrypt(plaintext) → String (Base64)
EncryptionService.decrypt(encrypted) → String
DataMaskingService.maskEmail(email) → ***@domain.com
DataMaskingService.maskIBAN(iban) → ****...1332
AuthenticationService.getCurrentUser() → String
AuthenticationService.isAdmin() → boolean
```

### Validadores
```java
@ValidIBAN → IBANValidator (checksum ISO 7064)
@ValidRoutingNumber → RoutingNumberValidator (9 dígitos)
@Email → RFC 5322
@NotBlank → No vacío
```

---

## 🗄️ Tablas de Base de Datos

### merchants (extendida)
```sql
contact_name VARCHAR(255)
contact_email VARCHAR(255)
bank_account_encrypted TEXT
bank_account_hash VARCHAR(255)
encrypted_bank_data BYTEA
previous_status merchant_status
updated_at TIMESTAMP
```

### merchant_status_history (nueva)
```sql
merchant_id UUID
previous_status merchant_status
new_status merchant_status
changed_by VARCHAR(255)
reason TEXT
created_at TIMESTAMP
```

### merchant_audit_detail (nueva)
```sql
merchant_id UUID
event_type VARCHAR(100)
field_name VARCHAR(100)
old_value TEXT
new_value TEXT
changed_by VARCHAR(255)
created_at TIMESTAMP
```

---

## 🔐 Seguridad - Matriz de Acceso

| Endpoint | Público | Auth | Admin | Owner |
|----------|---------|------|-------|-------|
| POST /merchants | ✅ | - | ✅ | - |
| PUT contact | ❌ | ✅ | ✅ | ✅ |
| PUT bank | ❌ | ✅ | ✅ | ✅ |
| PATCH activate | ❌ | ❌ | ✅ | ❌ |
| PATCH deactivate | ❌ | ❌ | ✅ | ❌ |
| GET detail | ❌ | ✅ | ✅ (todo) | ✅ (enmascarado) |

---

## 📤 Enmascaramiento OWASP

| Campo | Admin | Owner | Otros |
|-------|-------|-------|-------|
| contactEmail | ✓ | ✓ | ***@domain.com |
| bankData | ✓ | ✓ | [RESTRICTED] |
| status | ✓ | ✓ | ✓ |

---

## 📊 HTTP Status Codes

| Código | Significado | Ejemplos |
|--------|------------|----------|
| 200 | OK | Actualización exitosa |
| 201 | Created | Comercio registrado |
| 400 | Bad Request | Email inválido, IBAN inválido |
| 403 | Forbidden | No autorizado, comercio inactivo |
| 404 | Not Found | Comercio inexistente |
| 422 | Unprocessable | Transición de estado inválida |

---

## 🎯 Estados Válidos (Máquina de Estados)

```
INACTIVE ◄─────────┐
   │               │
   │ (admin)       │ (admin)
   ▼               │
ACTIVE ────────► SUSPENDED
   │               │
   │ (admin)       │ (admin)
   └──────────┬────┘
              │
              ▼
         DISABLED
         (terminal)
```

---

## 🔧 Configuración Mínima

```properties
# Encryption (generar con EncryptionService.generateKey())
encryption.secret-key=xxxxxxxxxxxxx

# Database
spring.datasource.url=jdbc:postgresql://localhost/db
spring.datasource.username=user
spring.datasource.password=pass

# JWT
spring.security.oauth2.jwt.issuer=http://localhost:8080
spring.security.oauth2.jwt.audience=api

# Logging
logging.level.com.paymentplatform=DEBUG
```

---

## 🧪 Tests rápidos con cURL

### Actualizar Contacto
```bash
curl -X PUT http://localhost:8080/api/v1/merchants/ID/contact \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"contact_name":"Juan","contact_email":"juan@test.com"}'
```

### Registrar Banco
```bash
curl -X PUT http://localhost:8080/api/v1/merchants/ID/bank-account \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "iban":"ES9121000418450200051332",
    "routing_number":"021000021",
    "account_holder_name":"Test"
  }'
```

### Activar (Admin)
```bash
curl -X PATCH http://localhost:8080/api/v1/merchants/ID/activate \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Docs verified"}'
```

### Desactivar (Admin)
```bash
curl -X PATCH http://localhost:8080/api/v1/merchants/ID/deactivate \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason":"Violation"}'
```

### Consultar Detalles
```bash
curl -X GET http://localhost:8080/api/v1/merchants/ID \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔍 Verificar en Base de Datos

### Cambios de Contacto
```sql
SELECT * FROM merchant_audit_detail 
WHERE event_type = 'MERCHANT_CONTACT_UPDATED'
ORDER BY created_at DESC LIMIT 5;
```

### Cambios de Estado
```sql
SELECT * FROM merchant_status_history 
WHERE merchant_id = 'ID'
ORDER BY created_at DESC;
```

### Todos los Eventos
```sql
SELECT * FROM audit_event 
WHERE entity_id = 'ID'
ORDER BY happened_at DESC;
```

---

## ✅ Checklist de Despliegue

- [ ] Encryption key generada y configurada
- [ ] Migrations ejecutadas (001 y 002)
- [ ] Spring Boot compilado sin errores
- [ ] JWT configurado
- [ ] Roles creados en BD
- [ ] Test: POST /merchants (público)
- [ ] Test: PUT /contact (autenticado)
- [ ] Test: PUT /bank (validaciones)
- [ ] Test: PATCH /activate (admin)
- [ ] Test: GET /detail (enmascaramiento)
- [ ] Auditoría visible en BD
- [ ] Logs funcionales

---

## 🚨 Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| Cannot resolve symbol | IDE desincronizado | Invalidar caches IDE |
| Columna no existe | Migrations no ejecutadas | Ejecutar 002_add_contact_and_bank_data.sql |
| encryption.secret-key no configurada | Variable de entorno faltante | Establecer ENCRYPTION_KEY |
| 401 Unauthorized | Token inválido/expirado | Regenerar token |
| 403 Forbidden | Rol insuficiente | Verificar roles de usuario |
| 422 Invalid state transition | Transición no permitida | Consultar máquina de estados |

---

## 📚 Documentos Relacionados

1. **PLAN_EJECUCION.md** - Plan detallado
2. **ESPECIFICACION_ENDPOINTS.md** - API completa
3. **DECISIONES_DISEÑO.md** - Justificaciones
4. **RESUMEN_IMPLEMENTACION.md** - Estado actual
5. **GUIA_EJECUCION_TESTING.md** - Testing detallado

---

## 🎓 Patrones Usados

- **Hexagonal**: API → App → Domain → Infra
- **Repository**: Abstracción de persistencia
- **Use Case**: Lógica aislada
- **State Machine**: Validación de transiciones
- **DTO**: Request/Response tipados
- **Value Object**: Contacto, Banco (futuro)

---

## 🔐 Seguridad en 3 Capas

1. **Authentication**: JWT en Authorization header
2. **Authorization**: Roles y ownership checks
3. **Encryption**: AES-256-GCM para datos sensibles

---

**Versión**: 1.0  
**Estado**: ✅ Implementado  
**Próximos Pasos**: Testing y Deploy


