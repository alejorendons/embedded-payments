# Casos de uso — HUs implementadas (1.1 a 1.6)

Este documento describe los casos de uso **según el comportamiento actual del código** del backend `embedded-payments`.  
**No incluye** HU 1.7 ni HU 1.8 (no están implementadas en la aplicación).

Convención de IDs: `CU-XX` = caso de uso; referencia a la historia de usuario `HU 1.X`.

---

## CU-01 — Registrar comercio

| Campo | Descripción |
|--------|-------------|
| **HU** | 1.1 |
| **Actores** | Visitante (no autenticado), Administrador autenticado (`ROLE_ADMIN`) |
| **Objetivo** | Dar de alta un nuevo comercio en la plataforma con API key para integración posterior. |

**Precondiciones**

- El cliente puede invocar el endpoint sin token (auto-registro) o con sesión de administrador (registro asistido).

**Disparador**

- Solicitud HTTP `POST /api/v1/merchants` con cuerpo JSON válido (`name`, `email`).

**Postcondiciones éxito**

- Existe un registro `Merchant` en estado **INACTIVE**.
- Se genera y persiste una API key asociada al comercio.
- Se registra evento de auditoría `merchant_registered` (origen `SELF_REGISTRATION` o `ADMIN_REGISTRATION` según contexto).

**Flujo principal**

1. El sistema valida el cuerpo (`400` si falla validación Bean Validation).
2. El dominio comprueba unicidad del email del comercio; si existe otro comercio con el mismo email → conflicto.
3. Se persiste el comercio en estado `INACTIVE`.
4. Se genera API key y se registra auditoría.
5. El sistema responde `201 Created` con datos del comercio y mensaje de éxito.

**Flujos de excepción**

| Situación | Respuesta HTTP |
|-----------|----------------|
| Datos inválidos o incompletos | `400 Bad Request` |
| Email ya registrado para otro comercio | `409 Conflict` |

**Componentes principales**

- API: `MerchantController.register`
- Aplicación: `RegisterMerchantUseCase`
- Dominio: `MerchantDomainService.buildNewMerchant`

---

## CU-02 — Actualizar información de contacto del comercio

| Campo | Descripción |
|--------|-------------|
| **HU** | 1.2 |
| **Actores** | Usuario autenticado con rol `ROLE_MERCHANT` o `ROLE_ADMIN` |
| **Objetivo** | Actualizar nombre y correo de contacto operativo del comercio. |

**Precondiciones**

- Petición autenticada con JWT Bearer.

**Disparador**

- `PUT /api/v1/merchants/{id}/contact` con `contact_name` y `contact_email`.

**Postcondiciones éxito**

- Los campos de contacto del comercio quedan actualizados.
- Se registran filas en auditoría de detalle (`merchant_audit_detail`) cuando cambian nombre o email de contacto.
- Se registra evento de auditoría `merchant_contact_updated`.

**Flujo principal**

1. Spring Security comprueba rol `ADMIN` o `MERCHANT` para la ruta.
2. Se valida el DTO (`400` si formato inválido según anotaciones).
3. Se carga el comercio por `{id}`; si no existe → `404`.
4. Se aplican cambios y se persiste.

**Flujos de excepción**

| Situación | Respuesta HTTP |
|-----------|----------------|
| Validación de entrada | `400 Bad Request` |
| `{id}` inexistente | `404 Not Found` |
| Sin autenticación o rol no permitido | `401` / `403` (según configuración de seguridad) |

**Componentes principales**

- API: `MerchantController.updateContact`
- Aplicación: `UpdateMerchantContactUseCase`

---

## CU-03 — Registrar o actualizar información financiera (cuenta bancaria)

| Campo | Descripción |
|--------|-------------|
| **HU** | 1.3 |
| **Actores** | Usuario autenticado `ROLE_MERCHANT` o `ROLE_ADMIN` |
| **Objetivo** | Asociar datos bancarios cifrados al comercio para liquidaciones. |

**Precondiciones**

- Comercio existente.
- El comercio debe estar en estado **ACTIVE** (regla de negocio en caso de uso).

**Disparador**

- `PUT /api/v1/merchants/{id}/bank-account` con `iban`, `routing_number`, `account_holder_name`.

**Postcondiciones éxito**

- Datos sensibles almacenados cifrados (AES-256-GCM) y hash del IBAN para búsquedas.
- Auditoría de detalle y evento `merchant_bank_account_registered` / actualización según corresponda.

**Flujo principal**

1. Autenticación y autorización de rol.
2. Validación del request (IBAN, routing number, titular).
3. Carga del comercio; si no existe → `404`.
4. Si el comercio no está activo → error de dominio `MERCHANT_INACTIVE`.
5. Cifrado, persistencia y auditoría.
6. Respuesta `200 OK` con datos no sensibles del comercio.

**Flujos de excepción**

| Situación | Respuesta HTTP |
|-----------|----------------|
| Validación de IBAN/routing/titular | `400 Bad Request` |
| Comercio inactivo | `403 Forbidden` |
| Comercio no encontrado | `404 Not Found` |

**Componentes principales**

- API: `MerchantController.registerBankAccount`
- Aplicación: `RegisterBankAccountUseCase`
- Infraestructura: `EncryptionService`

---

## CU-04 — Activar comercio

| Campo | Descripción |
|--------|-------------|
| **HU** | 1.4 |
| **Actores** | Administrador (`ROLE_ADMIN`) |
| **Objetivo** | Pasar un comercio de un estado previo válido a **ACTIVE** conforme a las transiciones del dominio. |

**Precondiciones**

- Administrador autenticado.

**Disparador**

- `PATCH /api/v1/merchants/{id}/activate` con cuerpo que incluye `reason`.

**Postcondiciones éxito**

- Estado del comercio actualizado a `ACTIVE` si la transición es válida.
- Historial de estado persistido.
- Auditoría `merchant_activated`.

**Flujo principal**

1. Verificación de rol administrador.
2. Búsqueda del comercio; si no existe → `404`.
3. Transición de estado; si es inválida → excepción de dominio (`400` según manejador global).
4. Respuesta `200 OK`.

**Componentes principales**

- API: `MerchantController.activateMerchant`
- Aplicación: `ActivateMerchantUseCase`

---

## CU-05 — Desactivar comercio

| Campo | Descripción |
|--------|-------------|
| **HU** | 1.5 |
| **Actores** | Administrador (`ROLE_ADMIN`) |
| **Objetivo** | Pasar el comercio a estado **INACTIVE** y dejar traza administrativa. |

**Precondiciones**

- Administrador autenticado.

**Disparador**

- `PATCH /api/v1/merchants/{id}/deactivate` con `reason`.

**Postcondiciones éxito**

- Estado **INACTIVE** si la transición es válida.
- Historial de cambio de estado y auditoría `merchant_deactivated`.

**Flujo principal**

1. Verificación de rol administrador.
2. Carga por id → `404` si no existe.
3. Cambio de estado y persistencia.
4. `200 OK`.

**Componentes principales**

- API: `MerchantController.deactivateMerchant`
- Aplicación: `DeactivateMerchantUseCase`

---

## CU-06 — Consultar detalle de comercio por identificador

| Campo | Descripción |
|--------|-------------|
| **HU** | 1.6 |
| **Actores** | Usuario autenticado `ROLE_ADMIN` o `ROLE_MERCHANT` |
| **Objetivo** | Obtener ficha del comercio con políticas de enmascaramiento según rol y si el solicitante es el “dueño” (mismo id de comercio en el token) o administrador. |

**Precondiciones**

- JWT válido con uno de los roles permitidos para `GET /api/v1/merchants/{id}`.

**Disparador**

- `GET /api/v1/merchants/{id}`.

**Postcondiciones éxito**

- Respuesta JSON con datos públicos y campos sensibles mostrados como texto enmascarado o placeholder según reglas (`GetMerchantDetailUseCase` + `DataMaskingService`).

**Flujo principal**

1. Autenticación.
2. Resolución de rol y identidad del solicitante desde el contexto de seguridad.
3. Carga del comercio → `404` si no existe.
4. Construcción del DTO con enmascaramiento.
5. `200 OK`.

**Flujos de excepción**

| Situación | Respuesta HTTP |
|-----------|----------------|
| Comercio no existe | `404 Not Found` |
| Sin permisos para la ruta | `403 Forbidden` |

**Componentes principales**

- API: `MerchantController.getMerchantDetail`
- Aplicación: `GetMerchantDetailUseCase`
- Soporte: `DataMaskingService`

---

## Alcance explícito

- Esta especificación **no cubre** creación de cuentas de administrador por Super Admin (HU 1.7) ni gestión de perfil/cambio de contraseña de administradores (HU 1.8).
- Los flujos de **pagos/transacciones** con API key (`ROLE_API_CLIENT`) son bounded contexts distintos y no forman parte de esta lista de casos de uso de gestión de comercio.
