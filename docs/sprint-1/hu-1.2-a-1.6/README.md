# Historias de Usuario 1.2 - 1.6: Gestión Avanzada de Comercios

## 📚 Índice de Documentación

Este directorio contiene toda la documentación y especificación de las historias de usuario 1.2 a 1.6 (HU 1.2 - HU 1.6) para la plataforma de Embedded Payments.

### 📖 Documentos Principales

1. **[PLAN_EJECUCION.md](./PLAN_EJECUCION.md)**
   - Plan detallado con fases de implementación
   - Arquitectura de solución
   - Orden de implementación recomendado
   - Métricas de éxito
   
2. **[ESPECIFICACION_ENDPOINTS.md](./ESPECIFICACION_ENDPOINTS.md)**
   - Especificación completa de REST API
   - Ejemplos de request/response
   - Validaciones
   - Códigos de error
   - Ejemplos cURL
   
3. **[DECISIONES_DISEÑO.md](./DECISIONES_DISEÑO.md)**
   - Justificación técnica de todas las decisiones
   - Alternativas evaluadas
   - Trade-offs explicados
   - Mitigación de riesgos
   
4. **[RESUMEN_IMPLEMENTACION.md](./RESUMEN_IMPLEMENTACION.md)**
   - Estado actual de la implementación
   - Ficheros modificados y creados
   - Checklist de validación
   - Próximos pasos

---

## 🎯 Historias de Usuario

### HU 1.2: Actualización de Información de Contacto
**Como** Comercio autenticado
**Quiero** actualizar mi información básica de contacto (nombre y email)
**Para** mantener mis datos operativos actualizados

✅ **Estado**: Implementado
- Endpoint: `PUT /api/v1/merchants/{id}/contact`
- Validación de email
- Auditoría con cambios delta
- HTTP 200 OK

### HU 1.3: Actualización de Información Financiera
**Como** Comercio autenticado y activo
**Quiero** registrar o actualizar mis datos bancarios
**Para** asegurar la correcta liquidación de pagos

✅ **Estado**: Implementado
- Endpoint: `PUT /api/v1/merchants/{id}/bank-account`
- Validación IBAN + Routing Number
- **Encriptación AES-256-GCM**
- Evento crítico en auditoría
- HTTP 200 OK

### HU 1.4: Activación de Comercio
**Como** Administrador
**Quiero** activar un comercio
**Para** habilitar su operación en la plataforma

✅ **Estado**: Implementado
- Endpoint: `PATCH /api/v1/merchants/{id}/activate`
- **Solo administrador (ROLE_ADMIN)**
- Validación de transición de estado
- Historial de cambios
- HTTP 200 OK

### HU 1.5: Desactivación de Comercio
**Como** Administrador
**Quiero** desactivar un comercio
**Para** suspender su operación por seguridad o decisión comercial

✅ **Estado**: Implementado
- Endpoint: `PATCH /api/v1/merchants/{id}/deactivate`
- **Solo administrador (ROLE_ADMIN)**
- Bloquea nuevas transacciones
- Historial de cambios
- HTTP 200 OK

### HU 1.6: Consulta Individual de Comercio
**Como** Administrador
**Quiero** consultar un comercio por su ID
**Para** auditar su estado e información

✅ **Estado**: Implementado
- Endpoint: `GET /api/v1/merchants/{id}`
- **Enmascaramiento de datos sensibles (OWASP)**
- Email, IBAN, routing: ocultos para otros
- Admin ve todo desencriptado
- HTTP 200 OK

---

## 🏗️ Arquitectura Implementada

```
┌─────────────────────────────────────────┐
│         API Layer (Controllers)         │  ← MerchantController
├─────────────────────────────────────────┤
│    Application Layer (Use Cases)        │  ← 5 Use Cases
├─────────────────────────────────────────┤
│      Domain Layer (Business Logic)      │  ← Merchant + Exceptions
├─────────────────────────────────────────┤
│   Infrastructure (Persistence)          │  ← JPA Repositories
└─────────────────────────────────────────┘

Security Layer (CrossCutting)
  ├─ JWT Authentication Filter
  ├─ Role-Based Authorization (@PreAuthorize)
  └─ Ownership Validation

Audit Layer (CrossCutting)
  ├─ merchant_audit_detail (cambios por campo)
  ├─ merchant_status_history (transiciones)
  └─ audit_event (eventos generales)

Encryption Layer (Shared Service)
  ├─ EncryptionService (AES-256-GCM)
  ├─ DataMaskingService (OWASP)
  └─ ValidationService (IBAN, Routing, Email)
```

---

## 🔐 Seguridad

### Autenticación
- **Mecanismo**: JWT (JSON Web Tokens)
- **Header**: `Authorization: Bearer <token>`
- **Claims**: sub (usuario), roles (array)

### Autorización
| Endpoint | Roles Permitidos |
|----------|-----------------|
| POST /merchants | Público |
| PUT /merchants/{id}/contact | Autenticado |
| PUT /merchants/{id}/bank-account | Autenticado + ACTIVE |
| PATCH /merchants/{id}/activate | ROLE_ADMIN |
| PATCH /merchants/{id}/deactivate | ROLE_ADMIN |
| GET /merchants/{id} | Autenticado |

### Encriptación
- **Datos Bancarios**: AES-256-GCM (IV aleatorio 12 bytes)
- **Almacenamiento**: bank_account_encrypted (String) + encrypted_bank_data (BYTEA)
- **Hash**: SHA-256 para búsquedas sin desencriptar

### Enmascaramiento (OWASP)
- Email contacto: `***@domain.com` (otros comercios)
- Datos bancarios: [RESTRICTED] (no-propietarios)
- Admin: Acceso total

---

## 🗄️ Base de Datos

### Tabla: merchants (extendida)
```sql
- contact_name          VARCHAR(255)
- contact_email         VARCHAR(255)
- bank_account_encrypted TEXT
- bank_account_hash     VARCHAR(255)
- encrypted_bank_data   BYTEA
- previous_status       merchant_status
- updated_at            TIMESTAMP
```

### Tabla: merchant_status_history (NUEVA)
```sql
- merchant_id       UUID (FK)
- previous_status   merchant_status
- new_status        merchant_status
- changed_by        VARCHAR(255)
- reason            TEXT
- created_at        TIMESTAMP
```

### Tabla: merchant_audit_detail (NUEVA)
```sql
- merchant_id       UUID (FK)
- event_type        VARCHAR(100)
- field_name        VARCHAR(100)
- old_value         TEXT
- new_value         TEXT
- changed_by        VARCHAR(255)
- created_at        TIMESTAMP
```

### Índices Agregados
```sql
INDEX idx_merchants_status
INDEX idx_merchants_email
INDEX idx_merchants_contact_email
INDEX idx_merchant_status_history_merchant
INDEX idx_merchant_audit_detail_merchant
```

---

## 📦 Nuevos Componentes

### Servicios
- ✅ `EncryptionService` - Cifrado AES-256-GCM
- ✅ `DataMaskingService` - Enmascaramiento OWASP
- ✅ `AuthenticationService` - Extracción de claims JWT

### Validadores
- ✅ `@ValidIBAN` - IBAN con checksum ISO 7064
- ✅ `@ValidRoutingNumber` - Routing Number 9 dígitos

### Use Cases (5)
1. ✅ `UpdateMerchantContactUseCase` (HU 1.2)
2. ✅ `RegisterBankAccountUseCase` (HU 1.3)
3. ✅ `ActivateMerchantUseCase` (HU 1.4)
4. ✅ `DeactivateMerchantUseCase` (HU 1.5)
5. ✅ `GetMerchantDetailUseCase` (HU 1.6)

### DTOs
- ✅ `UpdateMerchantContactRequest`
- ✅ `RegisterBankAccountRequest`
- ✅ `ChangeMerchantStatusRequest`
- ✅ `MerchantDetailResponse`
- ✅ `MerchantUpdateResponse`

---

## 🧪 Pruebas (Próximo Sprint)

### Pendiente
- [ ] Unit tests de validadores
- [ ] Unit tests de use cases
- [ ] Integration tests de endpoints
- [ ] Tests de autorización
- [ ] Tests de enmascaramiento
- [ ] Tests de encriptación
- [ ] Tests de máquina de estados

**Target Coverage**: >80%

---

## 🚀 Deployment

### Requisitos
1. Configurar `encryption.secret-key` en variables de entorno
2. Ejecutar migrations de base de datos
3. Configurar JWT issuer/audience en Spring Security
4. Configurar CORS si es necesario

### Checklist
- [ ] Encryption key configurada
- [ ] Migrations ejecutadas
- [ ] JWT configurado
- [ ] Smoke tests pasando
- [ ] Logs en producción
- [ ] Monitoring activo

---

## 📝 Validaciones Implementadas

### Email
- Formato RFC 5322
- Validación de duplicado (en contexto)
- @Email annotation

### IBAN
- Formato: XX99 + alfanuméricos (máx 34 caracteres)
- Checksum: ISO 7064 mod 97
- Países soportados: Todos (configuración por país futura)

### Routing Number
- Formato: Exactamente 9 dígitos
- Checksum: Validación de EE.UU.
- Solo para mercados USA

### Estados Válidos
```
INACTIVE ──→ ACTIVE ──→ SUSPENDED
   ↑                        ↓
   └←─────── DISABLED ←─────┘
```

---

## 🎓 Patrones Utilizados

1. **Hexagonal Architecture**: API → Application → Domain → Infrastructure
2. **Repository Pattern**: Abstracción de persistencia
3. **Use Case Pattern**: Lógica aislada y testeable
4. **Value Object Pattern**: ContactInfo, BankAccount (futuros)
5. **State Machine Pattern**: MerchantStatus con validaciones
6. **DTO Pattern**: Request/Response con validación
7. **AOP**: Transacciones, logging (futuro)

---

## 📊 Métricas

### Código
- **Clases**: 27 nuevas
- **LOC**: ~3,500 (código) + ~2,000 (documentación)
- **Métodos**: ~120
- **Ciclomática máxima**: 6

### Cobertura Esperada
- **Unit tests**: 80%+
- **Integration**: 85%+
- **Overall**: 82%+

---

## 🔗 Referencias

- [Especificación IBAN - ISO 13616](https://www.iban.com/structure)
- [Routing Numbers - American Bankers Association](https://www.aba.com/)
- [OWASP Data Masking](https://owasp.org/www-community/attacks/Information_Disclosure)
- [RFC 5322 - Email Format](https://tools.ietf.org/html/rfc5322)
- [Spring Security Documentation](https://spring.io/projects/spring-security)

---

## ❓ FAQ

**P: ¿Por qué AES-256-GCM y no tokens?**
R: Control total sobre las claves, mejor auditoría, cumplimiento PCI-DSS.

**P: ¿Qué pasa si cambio el encryption.secret-key?**
R: Los datos antiguos no se desencriptarán. Implementar key rotation en Sprint 2.

**P: ¿Puedo consultar datos bancarios de otro comercio?**
R: No, HTTPStatus 403 Forbidden si no eres admin o propietario.

**P: ¿Se pierden datos al desactivar?**
R: No, solo se marca como INACTIVE. Recuperable por admin.

**P: ¿Hay un endpoint para ver historial de cambios?**
R: No en esta HU, implementable en Sprint 2 como `/merchants/{id}/history`.

---

## 📞 Soporte

Para preguntas o problemas:
1. Revisar `DECISIONES_DISEÑO.md` para justificaciones
2. Revisar `ESPECIFICACION_ENDPOINTS.md` para detalles API
3. Ejecutar tests para validar funcionalidad
4. Revisar logs de auditoría en base de datos

---

**Versión**: 1.0
**Última actualización**: 30 de Abril de 2026
**Estado**: ✅ Implementación Completada


