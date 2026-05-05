# Decisiones de Diseño - HU 1.2 a 1.6

## Arquitectura General

### Patrón Hexagonal (Puertos y Adaptadores)
Se mantiene el patrón utilizado en HU 1.1:
- **API Layer** (Puertos entrada): Controllers + DTOs + Validación
- **Application Layer**: Use Cases
- **Domain Layer**: Entities + Exceptions + Repository Interfaces
- **Infrastructure Layer** (Adaptadores): JPA Repositories + Persistence

**Ventaja:** Desacoplamiento, testabilidad, escalabilidad.

---

## 1. Modelo de Entidad - Merchant

### Decisión: Extender la entidad existente en lugar de crear tablas separadas
**Justificación:**
- Mantiene integridad referencial (FK única a merchants)
- Simplifica consultas (un JOIN vs múltiples)
- El comercio es agregado raíz (DDD)

### Campos Agregados:
```java
- contactName          // HU 1.2
- contactEmail         // HU 1.2
- bankAccountEncrypted // HU 1.3 (texto cifrado)
- bankAccountHash      // HU 1.3 (para búsquedas sin desencriptar)
- encryptedBankData    // HU 1.3 (BYTEA para serialización)
- previousStatus       // HU 1.4, 1.5 (auditoría)
- updatedAt            // Timestamp de actualización
```

### Status Enum (Máquina de Estados)
```java
INACTIVE ─→ ACTIVE ─→ SUSPENDED
  ↑                       ↓
  └────← DISABLED ←──────┘
```

**Regla de transición:** Validadas en `Merchant.changeStatus()` lanzando `InvalidStateTransitionException`.

---

## 2. Encriptación de Datos Bancarios

### Decisión: AES-256-GCM en aplicación + Hash para búsquedas

**Alternativas Evaluadas:**
| Opción | Ventajas | Desventajas |
|--------|----------|------------|
| **AES-256-GCM (elegida)** | Autenticación integrada, NIST aprobado | Requiere gestión de claves |
| Tokens (HCE) | Tercero maneja claves | Dependencia externa, latencia |
| DB native (pgcrypto) | Encriptación en reposo | No auditable a nivel app |

**Implementación:**
```java
EncryptionService.encrypt(plaintext) → Base64(IV + AES-256-GCM(plaintext))
```

- **IV (Initialization Vector)**: 12 bytes generados aleatoriamente por SecureRandom
- **Tag**: 128 bits para autenticación
- **Almacenamiento**: `bank_account_encrypted` (STRING) + `encrypted_bank_data` (BYTEA)
- **Hash**: SHA-256 para búsquedas (ej: validar duplicado IBAN sin desencriptar)

**Gestión de Claves:**
```properties
encryption.secret-key=${ENCRYPTION_KEY}  // Variable de entorno
```
Debe ser una clave AES-256 en Base64 (32 bytes = 256 bits).

---

## 3. Validación IBAN y Routing Number

### Decisión: Validación offline con checksum

**Niveles de validación:**

1. **Formato (Regex)**
   - IBAN: `^[A-Z]{2}\d{2}[A-Z0-9]{1,30}$`
   - Routing: `^\d{9}$`

2. **Checksum (Algoritmo)**
   - IBAN: ISO 7064 mod 97
   - Routing: Ponderación específica de EE.UU.

3. **Futuro: Online validation**
   - API de bancos centrales
   - BIC code verification
   - Status de cuenta activa

**Custom Validators:**
- `@ValidIBAN` con `IBANValidator`
- `@ValidRoutingNumber` con `RoutingNumberValidator`

---

## 4. Seguridad - Autorización Basada en Roles

### Decisión: Roles JWT + @PreAuthorize

```
Roles implementados:
- ROLE_ADMIN     : Gestión de estados, auditoría
- ROLE_MERCHANT  : Gestión de sus propios datos
- ROLE_USER      : Futuro (clientes finales)
```

**Flujo:**
1. JWT contiene claims: `sub` (usuario), `roles` (array)
2. `JwtAuthenticationFilter` extrae y construye `GrantedAuthority`
3. `@PreAuthorize("hasRole('ADMIN')")` valida en controller
4. Para ownership: `AuthenticationService.getMerchantId()` y comparación manual

### Decisión: Ownership check manual vs decorador
**Elegido:** Manual en controller porque:
- Flexibility: Diferentes reglas por endpoint
- Clarity: Explícito qué se valida
- Testability: Fácil mock de seguridad

---

## 5. Enmascaramiento de Datos (OWASP)

### Decisión: Enmascaramiento por rol en layer Application

**Matriz de Visibilidad:**

| Campo | Admin | Owner | Otros Merchants |
|-------|-------|-------|-----------------|
| email | ✓ | ✓ | ✗ |
| contactEmail | ✓ | ✓ | `***@domain.com` |
| bankData | ✓ Desencriptado | ✓ Desencriptado | ✗ `[RESTRICTED]` |
| status | ✓ | ✓ | ✓ |

**Implementación:**
```java
GetMerchantDetailUseCase.buildMaskedDetail()
  → Aplica reglas según requesterRole + requesterId
  → Retorna MerchantDetail (record inmutable)
  → Controller mapea a DTO response
```

**Ventajas:**
- Centralizado (una fuente de verdad)
- Testeable
- Auditables qué se enmascaró y por qué

---

## 6. Auditoría de Cambios

### Decisión: Dos niveles de auditoría

**Nivel 1: Cambios de Estado** (`merchant_status_history`)
```
merchantId | previousStatus | newStatus | changedBy | reason | createdAt
```
- Propósito: Historial de transiciones
- Cuando: ActivateMerchant, DeactivateMerchant
- Ventaja: Búsquedas rápidas por estado

**Nivel 2: Cambios de Datos** (`merchant_audit_detail`)
```
merchantId | eventType | fieldName | oldValue | newValue | changedBy | createdAt
```
- Propósito: Compliance (PCI-DSS, GDPR)
- Cuando: Todos los cambios
- Nota: Datos sensibles como `[ENCRYPTED_BANK_DATA]` (nunca valores)

**Nivel 3: Eventos Generales** (`audit_event`)
```
eventType | entityType | entityId | origin | actor | happendAt
```
- Propósito: Auditoría general del sistema
- Cuando: Todas las operaciones relevantes

---

## 7. Transacciones y Consistencia

### Decisión: @Transactional en Use Cases

```java
@Transactional
public Merchant execute(...) {
    merchant.changeStatus("ACTIVE");
    merchantRepository.save(merchant);
    statusHistoryRepository.save(history);
    auditEventService.register(...);
}
```

**Propiedades:**
- **Propagation**: REQUIRED (default)
- **Isolation**: READ_COMMITTED (PostgreSQL default)
- **Rollback**: RuntimeException (no checked)

**Garantías ACID:**
- **Atomicidad**: Transacción única
- **Consistencia**: Validaciones en dominio
- **Aislamiento**: Level COMMITTED previene dirty reads
- **Durabilidad**: PostgreSQL WAL

---

## 8. DTOs: Records vs Clases

### Decisión: Records Java 16+

```java
public record UpdateMerchantContactRequest(
    String contactName,
    String contactEmail
) {}
```

**Ventajas:**
- Inmutabilidad garantizada
- Menos boilerplate (sin getters/setters)
- Serialización JSON automática
- Mejor performance (final class)

**Desventajas:**
- No extensibles
- Sin builder pattern

---

## 9. Validación: Bean Validation vs Custom Logic

### Decisión: Híbrida

**Bean Validation (@Valid):**
- Formato básico: `@Email`, `@NotBlank`
- Rápido, declarativo

**Custom Validators:**
- Lógica compleja: `@ValidIBAN` con checksum
- Reusable, testeable

**Use Case Validation:**
- Reglas de negocio: "Comercio debe estar ACTIVO"
- Lanzar `MerchantInactiveException`

---

## 10. Manejo de Errores

### Decisión: Excepciones específicas con códigos HTTP

```java
public class DomainException extends RuntimeException {
    HttpStatus status;
    String errorCode;
    List<String> details;
}

public class MerchantNotFoundException extends DomainException {
    super(HttpStatus.NOT_FOUND, "MERCHANT_NOT_FOUND", message, details);
}
```

**Ventajas:**
- HTTP status y código de error acoplados
- Detalles para debugging
- `GlobalExceptionHandler` centralizado

---

## 11. Índices de Base de Datos

### Decisión: Índices estratégicos

```sql
CREATE INDEX idx_merchants_status         ON merchants(status);
CREATE INDEX idx_merchants_email          ON merchants(email);
CREATE INDEX idx_merchants_contact_email  ON merchants(contact_email);
CREATE INDEX idx_merchant_status_history_merchant   ON merchant_status_history(merchant_id);
CREATE INDEX idx_merchant_audit_detail_merchant     ON merchant_audit_detail(merchant_id);
CREATE INDEX idx_merchant_audit_detail_event       ON merchant_audit_detail(event_type);
```

**Justificación:**
- `status`: Queries por "obtener todos ACTIVOS"
- `contact_email`: Validar duplicados
- `merchant_id`: Historial y auditoría por comercio

---

## 12. Configuración Spring Security

### Decisión: @EnableMethodSecurity + @PreAuthorize

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // ...
}
```

**Alternativas:**
- HttpSecurity (declarativa en config): Rígida para ownership
- Method Security (anotaciones): Flexible para lógica dinámica

**Elegida:** Method Security por flexibilidad en ownership checks.

---

## 13. Logging y Observabilidad

### Decidido (Futuros)
- [ ] Structured Logging (JSON con contexto de transacción)
- [ ] Distributed Tracing (Spring Cloud Sleuth)
- [ ] Métricas (Micrometer)
- [ ] Health checks

**Hito:** Sprint 2 o 3

---

## Riesgos y Mitigaciones

| Riesgo | Severidad | Mitigación |
|--------|-----------|-----------|
| Fuga de datos bancarios | CRÍTICO | Encriptación AES-256-GCM + IV aleatorio |
| Transición estado inválida | ALTA | Máquina de estados en entidad |
| Escalabilidad de auditoría | MEDIA | Índices + particionamiento futuro |
| Rate limiting | MEDIA | Implementar en gateway (Sprint 2) |
| JWT expiration | ALTA | Claims con TTL corto (15 min) + refresh |

---

## Performance Esperado

| Operación | Latencia | Bottleneck |
|-----------|----------|-----------|
| Crear comercio | ~50ms | Hashing password |
| Actualizar contacto | ~20ms | DB write |
| Registrar banco | ~80ms | Encriptación AES-256 |
| Cambiar estado | ~30ms | DB + audit |
| Consultar detalles | ~15ms | DB read + enmascaramiento |

---

## Estado del Diseño
- ✅ Aprobado
- ✅ Implementado
- ⏳ Pendiente: Pruebas de carga (Sprint 2)


