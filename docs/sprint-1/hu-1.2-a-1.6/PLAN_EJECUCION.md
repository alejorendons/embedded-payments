# Plan de Ejecución: HU 1.2 - HU 1.6 (Gestión de Comercios)

## Resumen Ejecutivo
Implementación de 5 historias de usuario para gestión avanzada de comercios:
- **HU 1.2**: Actualización de información de contacto (nombre, email)
- **HU 1.3**: Registro/actualización de datos bancarios cifrados
- **HU 1.4**: Activación de comercio (admin only)
- **HU 1.5**: Desactivación de comercio con bloqueo de transacciones
- **HU 1.6**: Consulta individual con enmascaramiento de datos sensibles

## Arquitectura de Solución

### 1. Capa de Base de Datos
**Cambios:**
- Extender tabla `merchants` con campos de contacto y datos bancarios
- Crear tabla `merchant_status_history` para auditar transiciones de estado
- Crear tabla `merchant_sensitive_data_mask` para reglas de enmascaramiento OWASP

### 2. Capa de Dominio
**Entidades:**
- `Merchant`: Extender con métodos de validación y transición de estado
- `MerchantStatus` enum: INACTIVE, ACTIVE, SUSPENDED, DISABLED
- Value Objects: `ContactInfo`, `BankAccount`

### 3. Capa de Seguridad
**Cambios:**
- Implementar autorización basada en roles: ROLE_MERCHANT, ROLE_ADMIN
- Control de acceso: propietario del comercio o administrador
- Validación de tokens JWT con extracción de claims

### 4. Capa de Aplicación (Use Cases)
- `UpdateMerchantContactUseCase` (HU 1.2)
- `RegisterBankAccountUseCase` (HU 1.3)
- `ActivateMerchantUseCase` (HU 1.4)
- `DeactivateMerchantUseCase` (HU 1.5)
- `GetMerchantDetailUseCase` (HU 1.6)

### 5. Capa de API (Controladores)
**Endpoints REST:**
```
PUT    /api/v1/merchants/{id}/contact          (HU 1.2)
PUT    /api/v1/merchants/{id}/bank-account     (HU 1.3)
PATCH  /api/v1/merchants/{id}/activate         (HU 1.4)
PATCH  /api/v1/merchants/{id}/deactivate       (HU 1.5)
GET    /api/v1/merchants/{id}                  (HU 1.6)
```

### 6. Auditoría y Logging
**Eventos:**
- MERCHANT_CONTACT_UPDATED
- MERCHANT_BANK_ACCOUNT_REGISTERED
- MERCHANT_ACTIVATED
- MERCHANT_DEACTIVATED
- MERCHANT_DETAILS_QUERIED

## Orden de Implementación

### Fase 1: Infraestructura Base (Día 1)
1. ✅ Actualizar esquema de base de datos (migrations)
2. ✅ Extender entidad `Merchant` con nuevos campos
3. ✅ Crear Value Objects (`ContactInfo`, `BankAccount`)
4. ✅ Implementar `MerchantStatus` enum con reglas de transición

### Fase 2: Seguridad y Servicios Transversales (Día 2)
5. ✅ Crear `EncryptionService` para datos bancarios
6. ✅ Crear `DataMaskingService` para enmascaramiento OWASP
7. ✅ Actualizar `SecurityConfig` con autorización por roles
8. ✅ Implementar extrator de claims JWT

### Fase 3: Use Cases (Día 3)
9. ✅ Implementar `UpdateMerchantContactUseCase` (HU 1.2)
10. ✅ Implementar `RegisterBankAccountUseCase` (HU 1.3)
11. ✅ Implementar `ActivateMerchantUseCase` (HU 1.4)
12. ✅ Implementar `DeactivateMerchantUseCase` (HU 1.5)
13. ✅ Implementar `GetMerchantDetailUseCase` (HU 1.6)

### Fase 4: API y Controladores (Día 4)
14. ✅ Crear/extender `MerchantController` con nuevos endpoints
15. ✅ Implementar DTOs de request/response con validaciones
16. ✅ Implementar manejo de errores y excepciones

### Fase 5: Auditoría (Día 5)
17. ✅ Extender `AuditEventService` con nuevos eventos
18. ✅ Crear tabla de historial de cambios
19. ✅ Implementar logging de cambios por campo

### Fase 6: Pruebas (Día 6)
20. ✅ Pruebas unitarias de use cases
21. ✅ Pruebas de integración de endpoints
22. ✅ Pruebas de seguridad (autorización, roles)
23. ✅ Pruebas de enmascaramiento de datos

## Decisiones de Diseño

### Encriptación
- **Algoritmo**: AES-256-GCM en aplicación
- **Almacenamiento**: Datos bancarios cifrados en tabla
- **Gestión de claves**: Variables de entorno (AWS Secrets Manager en producción)

### Enmascaramiento (OWASP)
- **Email**: `***@example.com` (solo para otros comercios)
- **Nombre cuenta**: Primeros 2 dígitos + `****`
- **IBAN**: Últimos 4 dígitos + `****...`
- **Routing**: No visible (excepto owner/admin)

### Validaciones
- Email: RFC 5322 + validación duplicado
- IBAN: Formato + checksum ISO 7064
- Routing Number: Formato numérico 9 dígitos

### Transiciones de Estado
```
INACTIVE -----> ACTIVE ----> SUSPENDED ----v
  ^              |                       |
  |              v                       |
  +------------- DISABLED <--------------+
```
- Solo admin puede cambiar estado
- Transiciones inválidas lanzan `InvalidStateTransitionException`

## Consideraciones Técnicas

1. **Thread Safety**: Sincronización en transiciones de estado
2. **Idempotencia**: Operaciones seguras si se repiten
3. **Performance**: Índices en merchant_id, status, email
4. **Escalabilidad**: Logs de auditoría en tabla separada

## Métricas de Éxito

- [ ] Todos los endpoints implementados
- [ ] Cobertura de tests > 80%
- [ ] Autorización verificada en todos los endpoints
- [ ] Datos sensibles enmascarados según OWASP
- [ ] Auditoría registra todos los cambios
- [ ] Validaciones funcionan correctamente
- [ ] Documentación API actualizada (Swagger)

## Estado Actual
**Última actualización**: [Timestamp]
**Desarrollador**: [Asignado]
**Estado**: INICIADO


