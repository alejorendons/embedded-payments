# Guía de Ejecución y Testing - HU 1.2 a 1.6

## 📋 Prerrequisitos

### Software Requerido
- Java 17+ (compatible con Spring Boot 3.x)
- Maven 3.8+
- PostgreSQL 12+
- Git

### Dependencias del Proyecto
```xml
<!-- Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Jakarta Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 🔧 Configuración Inicial

### 1. Generar Encryption Key

```bash
# Ejecutar desde Java o Spring Boot console
java -c "
import com.paymentplatform.embeddedpayments.shared.security.EncryptionService;
System.out.println(EncryptionService.generateKey());
"
```

O desde Spring Boot:
```bash
# Agregar a una clase @Component o @Service y ejecutar
@PostConstruct
public void printEncryptionKey() {
    String key = EncryptionService.generateKey();
    System.out.println("Generated Key: " + key);
}
```

### 2. Configurar application.properties

```properties
# Base de Datos
spring.datasource.url=jdbc:postgresql://localhost:5432/embedded_payments
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=validate

# Encriptación
encryption.secret-key=${ENCRYPTION_KEY}

# JWT
spring.security.oauth2.jwt.issuer=http://localhost:8080
spring.security.oauth2.jwt.audience=embedded-payments-api

# Logging
logging.level.com.paymentplatform=DEBUG
logging.level.org.springframework.security=DEBUG
```

### 3. Ejecutar Migraciones de Base de Datos

```bash
# Conectar a PostgreSQL
psql -U postgres -d embedded_payments

# Ejecutar scripts
\i db/001_create_schema.sql
\i db/002_add_contact_and_bank_data.sql

# Verificar tablas
\dt merchant*
```

### 4. Compilar el Proyecto

```bash
mvn clean install
```

---

## 🚀 Ejecutar la Aplicación

### Desarrollo Local

```bash
# Opción 1: Maven
mvn spring-boot:run

# Opción 2: IDE (ejecutar EmbeddedPaymentsApplication.java)
# Opción 3: Java
java -jar target/embedded-payments-*.jar
```

### Verificar que la aplicación está corriendo

```bash
curl http://localhost:8080/v3/api-docs

# Debería retornar OpenAPI JSON
```

---

## 🧪 Testing Manual

### Herramientas Recomendadas
- **Postman** (colecciones incluidas en `postman/`)
- **cURL** (línea de comandos)
- **REST Client extension en VS Code**

### 1. Registrar un Comercio (HU 1.1 - Prerrequisito)

```bash
curl -X POST http://localhost:8080/api/v1/merchants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tienda Tech S.L.",
    "email": "info@tiendatech.es"
  }'

# Response (esperado):
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tienda Tech S.L.",
  "email": "info@tiendatech.es",
  "status": "INACTIVE",
  "updated_at": "2024-04-30T10:30:00Z",
  "message": "Merchant registered successfully"
}

# Guardar el ID para pruebas posteriores
MERCHANT_ID="550e8400-e29b-41d4-a716-446655440000"
```

### 2. Obtener JWT Token (Prerrequisito)

```bash
# Obtener token de admin (simulado, ajustar según implementación de auth)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "password"
  }'

# Guardar token
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3. HU 1.2 - Actualizar Información de Contacto

```bash
curl -X PUT http://localhost:8080/api/v1/merchants/$MERCHANT_ID/contact \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "contact_name": "Juan Mosquera",
    "contact_email": "juan@tiendatech.es"
  }'

# Response (esperado 200 OK):
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tienda Tech S.L.",
  "email": "info@tiendatech.es",
  "status": "INACTIVE",
  "updated_at": "2024-04-30T10:30:01Z",
  "message": "Contact information updated successfully"
}

# Verificar auditoría
SELECT * FROM merchant_audit_detail WHERE merchant_id = '$MERCHANT_ID';
```

### 4. HU 1.4 - Activar Comercio (Admin)

```bash
curl -X PATCH http://localhost:8080/api/v1/merchants/$MERCHANT_ID/activate \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Documentos verificados"
  }'

# Response (esperado 200 OK):
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tienda Tech S.L.",
  "email": "info@tiendatech.es",
  "status": "ACTIVE",
  "updated_at": "2024-04-30T10:30:02Z",
  "message": "Merchant activated successfully"
}

# Verificar historial
SELECT * FROM merchant_status_history WHERE merchant_id = '$MERCHANT_ID';
```

### 5. HU 1.3 - Registrar Datos Bancarios

```bash
curl -X PUT http://localhost:8080/api/v1/merchants/$MERCHANT_ID/bank-account \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "iban": "ES9121000418450200051332",
    "routing_number": "021000021",
    "account_holder_name": "Tienda Tech S.L."
  }'

# Response (esperado 200 OK):
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tienda Tech S.L.",
  "email": "info@tiendatech.es",
  "status": "ACTIVE",
  "updated_at": "2024-04-30T10:30:03Z",
  "message": "Bank account registered successfully"
}

# Verificar que se guardó cifrado
SELECT id, bank_account_hash FROM merchants WHERE id = '$MERCHANT_ID';
# El bank_account_encrypted debe estar lleno pero ilegible
```

### 6. HU 1.6 - Consultar Detalles con Enmascaramiento

```bash
# Como Admin (ve todo)
curl -X GET http://localhost:8080/api/v1/merchants/$MERCHANT_ID \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Response (Admin):
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tienda Tech S.L.",
  "email": "info@tiendatech.es",
  "contact_name": "Juan Mosquera",
  "contact_email": "juan@tiendatech.es",
  "status": "ACTIVE",
  "bank_account_data": "[ENCRYPTED - Access granted]",
  "updated_at": "2024-04-30T10:30:03Z"
}

# Como otro comercio (email enmascarado)
curl -X GET http://localhost:8080/api/v1/merchants/$MERCHANT_ID \
  -H "Authorization: Bearer $OTHER_MERCHANT_TOKEN"

# Response (Otro comercio):
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tienda Tech S.L.",
  "email": "info@tiendatech.es",
  "contact_name": "Juan Mosquera",
  "contact_email": "***@tiendatech.es",  # ← ENMASCARADO
  "status": "ACTIVE",
  "bank_account_data": "[RESTRICTED]",   # ← NO VISIBLE
  "updated_at": "2024-04-30T10:30:03Z"
}
```

### 7. HU 1.5 - Desactivar Comercio

```bash
curl -X PATCH http://localhost:8080/api/v1/merchants/$MERCHANT_ID/deactivate \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Incumplimiento de términos"
  }'

# Response (esperado 200 OK):
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Tienda Tech S.L.",
  "email": "info@tiendatech.es",
  "status": "INACTIVE",
  "updated_at": "2024-04-30T10:30:04Z",
  "message": "Merchant deactivated successfully"
}

# Verificar estado
SELECT status FROM merchants WHERE id = '$MERCHANT_ID';
# Debería ser INACTIVE
```

---

## 🔍 Validaciones a Probar

### 1. Email Inválido (HU 1.2)

```bash
curl -X PUT http://localhost:8080/api/v1/merchants/$MERCHANT_ID/contact \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "contact_name": "Juan",
    "contact_email": "email-invalido"
  }'

# Response (esperado 400 Bad Request):
{
  "timestamp": "2024-04-30T10:30:04Z",
  "status": 400,
  "error_code": "VALIDATION_ERROR",
  "message": "Validation error",
  "details": ["contact_email: must be a valid email address"]
}
```

### 2. IBAN Inválido (HU 1.3)

```bash
curl -X PUT http://localhost:8080/api/v1/merchants/$MERCHANT_ID/bank-account \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "iban": "INVALID",
    "routing_number": "021000021",
    "account_holder_name": "Test"
  }'

# Response (esperado 400 Bad Request):
{
  "status": 400,
  "error_code": "VALIDATION_ERROR",
  "message": "Validation error",
  "details": ["iban: Invalid IBAN format or checksum"]
}
```

### 3. Routing Inválido (HU 1.3)

```bash
curl -X PUT http://localhost:8080/api/v1/merchants/$MERCHANT_ID/bank-account \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "iban": "ES9121000418450200051332",
    "routing_number": "12345",
    "account_holder_name": "Test"
  }'

# Response (esperado 400 Bad Request):
{
  "status": 400,
  "error_code": "VALIDATION_ERROR",
  "message": "Validation error",
  "details": ["routing_number: Invalid routing number format"]
}
```

### 4. No Autorizado - Cambiar Estado (HU 1.4)

```bash
# Como comercio normal (no admin)
curl -X PATCH http://localhost:8080/api/v1/merchants/$MERCHANT_ID/activate \
  -H "Authorization: Bearer $MERCHANT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "test"}'

# Response (esperado 403 Forbidden):
{
  "timestamp": "2024-04-30T10:30:04Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### 5. Transición de Estado Inválida

```bash
# Intentar INACTIVE → SUSPENDED (inválido)
# Primero desactivar
curl -X PATCH http://localhost:8080/api/v1/merchants/$MERCHANT_ID/deactivate \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "test"}'

# Luego intentar SUSPENDED (no válido desde INACTIVE)
curl -X PATCH http://localhost:8080/api/v1/merchants/$MERCHANT_ID/suspend \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "test"}'

# Response (esperado 422 Unprocessable Entity):
{
  "status": 422,
  "error_code": "INVALID_STATE_TRANSITION",
  "message": "Cannot transition from INACTIVE to SUSPENDED"
}
```

### 6. Comercio No Encontrado

```bash
curl -X GET http://localhost:8080/api/v1/merchants/00000000-0000-0000-0000-000000000000 \
  -H "Authorization: Bearer $TOKEN"

# Response (esperado 404 Not Found):
{
  "status": 404,
  "error_code": "MERCHANT_NOT_FOUND",
  "message": "Merchant not found with id: 00000000-0000-0000-0000-000000000000"
}
```

---

## 📊 Verificar Auditoría

### Consultar Cambios de Contacto

```sql
SELECT * FROM merchant_audit_detail 
WHERE merchant_id = '550e8400-e29b-41d4-a716-446655440000'
  AND event_type = 'MERCHANT_CONTACT_UPDATED'
ORDER BY created_at DESC;
```

### Consultar Historial de Estados

```sql
SELECT * FROM merchant_status_history 
WHERE merchant_id = '550e8400-e29b-41d4-a716-446655440000'
ORDER BY created_at DESC;
```

### Consultar Todos los Eventos

```sql
SELECT * FROM audit_event 
WHERE entity_id = '550e8400-e29b-41d4-a716-446655440000'
ORDER BY happened_at DESC;
```

---

## 🐛 Debugging

### Habilitar Logs de Spring Security

```properties
logging.level.org.springframework.security.web=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Ver SQL Ejecutado

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
```

### Validar Token JWT

```bash
# Decodificar token en http://jwt.io
# Verificar claims:
# {
#   "sub": "usuario@example.com",
#   "roles": ["ROLE_ADMIN"],
#   "exp": 1234567890,
#   ...
# }
```

---

## 📝 Colecciones Postman

Las colecciones están disponibles en:
- `postman/embedded-payments-sprint1.postman_collection.json`

### Importar a Postman
1. Abrir Postman
2. Click en "Import"
3. Seleccionar archivo JSON
4. Configurar variables de entorno:
   - `{{base_url}}`: http://localhost:8080
   - `{{merchant_id}}`: ID del comercio
   - `{{token}}`: JWT token
   - `{{admin_token}}`: JWT token de admin

---

## ✅ Checklist de Validación Manual

- [ ] HU 1.2: Contacto actualizado y auditado
- [ ] HU 1.3: Datos bancarios cifrados y auditados
- [ ] HU 1.3: Validación IBAN correcta
- [ ] HU 1.3: Validación Routing correcta
- [ ] HU 1.4: Admin puede activar
- [ ] HU 1.4: Comercio normal no puede activar (403)
- [ ] HU 1.5: Admin puede desactivar
- [ ] HU 1.5: Transición de estado inválida (422)
- [ ] HU 1.6: Email enmascarado para otros
- [ ] HU 1.6: Datos bancarios ocultos para otros
- [ ] HU 1.6: Admin ve todo desencriptado
- [ ] Auditoría: Cambios registrados correctamente
- [ ] Auditoría: Timestamps precisos
- [ ] Errores: HTTP status codes correctos

---

## 🚨 Troubleshooting

### Error: Cannot resolve symbol 'InvalidStateTransitionException'
**Solución**: Reindexar proyecto (Intellij: File → Invalidate Caches)

### Error: Cannot resolve column 'contact_name'
**Solución**: Ejecutar migrations de base de datos (002_add_contact_and_bank_data.sql)

### Error: encryption.secret-key not configured
**Solución**: Generar clave y establecer variable de entorno ENCRYPTION_KEY

### Error: 401 Unauthorized
**Solución**: Verificar token JWT válido y no expirado

### Error: 403 Forbidden
**Solución**: Verificar que usuario tiene el rol requerido

---

## 📚 Recursos Adicionales

- [Spring Security Docs](https://spring.io/projects/spring-security)
- [Spring Data JPA Docs](https://spring.io/projects/spring-data-jpa)
- [Validations with @Valid](https://www.baeldung.com/spring-boot-bean-validation)
- [JWT Token Authentication](https://www.baeldung.com/spring-security-oauth-jwt)

---

**Versión**: 1.0
**Última actualización**: 30 de Abril de 2026


