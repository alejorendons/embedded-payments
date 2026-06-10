# Informe de Pruebas Automatizadas — Embedded Payments

## 1. Resumen ejecutivo

| Categoría | Herramienta | Pruebas | Estado |
|---|---|---|---|
| Pruebas de aceptación (backend) | JUnit 5 + MockMvc | 11 | Pasan |
| Pruebas E2E de API | Serenity BDD + Cucumber | 11 | Pasan |
| Pruebas E2E de UI | Serenity BDD + Selenium | 3 | Requieren frontend |

---

## 2. Estrategia de pruebas

El proyecto cubre dos historias de usuario con **dos capas** de pruebas automatizadas:

```
┌─────────────────────────────────────────────────────┐
│  CAPA 1 — Pruebas de aceptación (sin servidor)      │
│  JUnit 5 + MockMvc + Spring Boot Test               │
│  Base de datos real (H2 transaccional por test)     │
└─────────────────────────────────────────────────────┘
           ↓ valida la misma lógica de negocio ↓
┌─────────────────────────────────────────────────────┐
│  CAPA 2 — Pruebas E2E (con servidor levantado)      │
│  Serenity BDD + Cucumber/Gherkin + Screenplay       │
│  API REST real → http://localhost:8085              │
│  UI Chrome  →  http://localhost:5173  (opcional)    │
└─────────────────────────────────────────────────────┘
```

---

## 3. Stack tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Lenguaje | Java | 17 / 21 |
| Framework de pruebas E2E | Serenity BDD | 4.1.0 |
| DSL de escenarios | Cucumber / Gherkin | (vía serenity-cucumber) |
| Patrón de diseño E2E | Screenplay (Actor → Task → Question) | — |
| Cliente HTTP E2E | REST Assured | (vía serenity-rest-assured) |
| Automatización UI | Selenium ChromeDriver | Auto (WebDriverManager 5.9.2) |
| Pruebas de aceptación | JUnit 5 + MockMvc | Spring Boot 4.0.5 |
| Base de datos en pruebas | H2 en memoria | Perfil `e2e` |
| Reporte | Serenity HTML Report | — |

---

## 4. HU 1.1 — Registro de Comercio

### 4.1 Criterios de aceptación

| ID | Descripción | Tipo |
|---|---|---|
| CA-01 | Registro exitoso por comercio potencial → 201, estado INACTIVE, api\_key con prefijo `epk_` | Happy path |
| CA-02 | Registro exitoso por administrador → 201, estado INACTIVE | Happy path |
| CA-03 | Payload inválido (nombre vacío o email malformado) → 400 VALIDATION\_ERROR | Excepcional |
| CA-04 | Registro duplicado (mismo email) → 409 MERCHANT\_ALREADY\_EXISTS | Excepcional |
| CA-05 | Registro exitoso genera evento de auditoría con tipo, origen y timestamp | Happy path |

---

### 4.2 Prueba de aceptación — Capa 1 (JUnit 5 + MockMvc)

**Archivo:** `src/test/java/.../merchant/api/HU11_RegisterMerchantAcceptanceTest.java`

#### CA-01 — Happy path: comercio potencial

```java
@Test
@DisplayName("CA-01: Registro exitoso por comercio potencial → 201, estado INACTIVE, api_key con prefijo epk_")
void CA01_registroPorComercioPotencial_retorna201_estadoINACTIVE_conApiKey() throws Exception {
    String body = """
            {
              "name": "Comercio Potencial",
              "email": "comercio+ca01@hu11.test"
            }
            """;

    mockMvc.perform(post("/api/v1/merchants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.status").value("INACTIVE"))
            .andExpect(jsonPath("$.api_key").value(startsWith("epk_")));
}
```

#### CA-02 — Happy path: administrador

```java
@Test
@WithMockUser(username = "admin@hu11.test", roles = "ADMIN")
@DisplayName("CA-02: Registro exitoso por administrador → 201, estado INACTIVE")
void CA02_registroPorAdministrador_retorna201_estadoINACTIVE() throws Exception {
    mockMvc.perform(post("/api/v1/merchants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"Comercio Admin\",\"email\":\"comercio+ca02@hu11.test\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("INACTIVE"));
}
```

#### CA-03 — Excepcional: payload inválido

```java
@Test
@DisplayName("CA-03: Payload inválido → 400 con cuerpo uniforme (errorCode, message, details, traceId)")
void CA03_payloadInvalido_retorna400_conCuerpoDeErrorEstandar() throws Exception {
    mockMvc.perform(post("/api/v1/merchants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"name\":\"\",\"email\":\"correo-invalido\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").isNotEmpty())
            .andExpect(jsonPath("$.details").isArray())
            .andExpect(jsonPath("$.traceId").isNotEmpty());
}
```

#### CA-04 — Excepcional: registro duplicado

```java
@Test
@DisplayName("CA-04: Registro duplicado → 409 con cuerpo uniforme")
void CA04_registroDuplicado_retorna409_conCuerpoDeErrorEstandar() throws Exception {
    String body = "{\"name\":\"Duplicado\",\"email\":\"duplicado+ca04@hu11.test\"}";

    mockMvc.perform(post("/api/v1/merchants").contentType(APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());  // primer registro OK

    mockMvc.perform(post("/api/v1/merchants").contentType(APPLICATION_JSON).content(body))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.errorCode").value("MERCHANT_ALREADY_EXISTS"))
            .andExpect(jsonPath("$.traceId").isNotEmpty());
}
```

#### CA-05 — Auditoría

```java
@Test
@DisplayName("CA-05: Registro exitoso genera evento de auditoría con origen y timestamp")
void CA05_registroExitoso_generaEventoDeAuditoria_conOrigenYTimestamp() throws Exception {
    MvcResult result = mockMvc.perform(post("/api/v1/merchants")
                    .contentType(APPLICATION_JSON)
                    .content("{\"name\":\"Auditoría\",\"email\":\"auditoria+ca05@hu11.test\"}"))
            .andExpect(status().isCreated())
            .andReturn();

    UUID merchantId = extractIdFromResponse(result.getResponse().getContentAsString());

    AuditEvent event = auditEventRepository.findAll().stream()
            .filter(e -> "merchant_registered".equals(e.getEventType())
                    && merchantId.equals(e.getEntityId()))
            .findFirst()
            .orElseThrow();

    assertThat(event.getOrigin()).isIn("SELF_REGISTRATION", "ADMIN_REGISTRATION");
    assertThat(event.getHappenedAt()).isNotNull();
}
```

---

### 4.3 Prueba E2E — Capa 2 (Serenity BDD + Cucumber)

**Archivo:** `e2e/src/test/resources/features/HU11_registro_comercio.feature`

```gherkin
@HU11
Feature: HU 1.1 – Registro de Comercio en la Plataforma de Pagos Embebidos
  Como comercio potencial o administrador del sistema
  Quiero registrarme en la plataforma de pagos
  Para poder procesar transacciones y acceder al dashboard merchant

  Background:
    Given que el actor está listo para las pruebas de registro de comercio

  @CA01 @Exitoso
  Scenario: CA-01 Registro exitoso por comercio potencial retorna 201 con estado INACTIVE y api_key
    When registra un comercio con nombre "Tienda Ejemplo" y email "tienda.ca01@e2etest.com"
    Then el código de respuesta HTTP es 201
    And el campo "status" de la respuesta es "INACTIVE"
    And el campo "api_key" de la respuesta comienza con "epk_"

  @CA02 @Exitoso
  Scenario: CA-02 Registro exitoso por administrador retorna 201 con estado INACTIVE
    When el administrador registra un comercio con nombre "Comercio Admin" y email "admin.ca02@e2etest.com"
    Then el código de respuesta HTTP es 201
    And el campo "status" de la respuesta es "INACTIVE"
    And el campo "id" de la respuesta no está vacío

  @CA03 @Excepcional
  Scenario Outline: CA-03 Registro con payload inválido retorna 400 con cuerpo de error estándar
    When intenta registrar un comercio con nombre "<nombre>" y email "<email>"
    Then el código de respuesta HTTP es 400
    And el cuerpo de error contiene el errorCode "VALIDATION_ERROR"
    And el cuerpo de error contiene los campos obligatorios message, details y traceId

    Examples:
      | nombre | email                |
      |        | valido.a@e2etest.com |
      | Tienda | no-es-email          |
      |        | tampoco-un-email     |

  @CA04 @Excepcional
  Scenario: CA-04 Registro duplicado retorna 409 con cuerpo de error estándar
    Given que ya existe un comercio con email "duplicado.ca04@e2etest.com"
    When registra un comercio con nombre "Otro Nombre" y email "duplicado.ca04@e2etest.com"
    Then el código de respuesta HTTP es 409
    And el cuerpo de error contiene el errorCode "MERCHANT_ALREADY_EXISTS"
    And el cuerpo de error contiene los campos obligatorios message y traceId
```

**Step definitions clave — `HU11_RegistroComercioSteps.java`:**

```java
// Genera sufijo único por escenario → evita colisiones en ejecuciones consecutivas
@Before
public void configurarEntorno() {
    scenarioSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    RestAssured.baseURI = "http://localhost:8085";
    actor.can(CallAnApi.at("http://localhost:8085"));
}

@Cuando("registra un comercio con nombre {string} y email {string}")
public void registraUnComercio(String nombre, String email) {
    actor.attemptsTo(RegistrarComercio.con(nombre, emailUnico(email)));
}

@Cuando("el administrador registra un comercio con nombre {string} y email {string}")
public void administradorRegistraUnComercio(String nombre, String email) {
    actor.attemptsTo(RegistrarComercio.comoAdministrador(nombre, emailUnico(email)));
}
```

---

## 5. HU 1.2 — Actualización de Información de Contacto

### 5.1 Criterios de aceptación

| ID | Descripción | Tipo |
|---|---|---|
| CA-01 | Datos válidos → 200, campo `message` = "Contact information updated successfully" | Happy path |
| CA-02 | `contact_name` vacío → 400 VALIDATION\_ERROR con `details` | Excepcional |
| CA-03 | `contact_email` con formato inválido → 400 VALIDATION\_ERROR con `details` | Excepcional |
| CA-04 | Comercio no existe → 404 MERCHANT\_NOT\_FOUND | Excepcional |
| CA-05 | Sin token de autenticación → 403 Forbidden | Excepcional |
| CA-06 | Actualización exitosa genera evento de auditoría `merchant_contact_updated` | Happy path |

---

### 5.2 Prueba de aceptación — Capa 1 (JUnit 5 + MockMvc)

**Archivo:** `src/test/java/.../merchant/api/HU12_UpdateContactAcceptanceTest.java`

#### CA-01 — Happy path

```java
@Test
@WithMockUser(roles = "MERCHANT")
@DisplayName("CA-01: Datos válidos → 200 con id del comercio y mensaje de éxito")
void CA01_datosValidos_retorna200_conMensajeExito() throws Exception {
    Merchant m = crearComercio("ACTIVE");

    mockMvc.perform(put("/api/v1/merchants/{id}/contact", m.getId())
                    .contentType(APPLICATION_JSON)
                    .content("{\"contact_name\":\"Ana García\",\"contact_email\":\"ana.garcia@example.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(m.getId().toString()))
            .andExpect(jsonPath("$.message").value("Contact information updated successfully"));
}
```

#### CA-02 — Excepcional: nombre vacío

```java
@Test
@WithMockUser(roles = "MERCHANT")
@DisplayName("CA-02: contact_name vacío → 400 con cuerpo uniforme")
void CA02_contactNameVacio_retorna400_conCuerpoDeErrorEstandar() throws Exception {
    Merchant m = crearComercio("ACTIVE");

    mockMvc.perform(put("/api/v1/merchants/{id}/contact", m.getId())
                    .contentType(APPLICATION_JSON)
                    .content("{\"contact_name\":\"\",\"contact_email\":\"valido@example.com\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.details").isArray())
            .andExpect(jsonPath("$.traceId").isNotEmpty());
}
```

#### CA-05 — Excepcional: sin autenticación

```java
@Test
@DisplayName("CA-05: Sin autenticación → 403 Forbidden")
void CA05_sinAutenticacion_retornaForbidden() throws Exception {
    Merchant m = crearComercio("ACTIVE");

    mockMvc.perform(put("/api/v1/merchants/{id}/contact", m.getId())
                    .contentType(APPLICATION_JSON)
                    .content(VALID_CONTACT_BODY))
            .andExpect(status().isForbidden());
}
```

#### CA-06 — Auditoría

```java
@Test
@WithMockUser(roles = "MERCHANT")
@DisplayName("CA-06: Actualización exitosa genera evento 'merchant_contact_updated' con timestamp")
void CA06_actualizacionExitosa_generaEventoDeAuditoria_conTimestamp() throws Exception {
    Merchant m = crearComercio("ACTIVE");

    mockMvc.perform(put("/api/v1/merchants/{id}/contact", m.getId())
                    .contentType(APPLICATION_JSON).content(VALID_CONTACT_BODY))
            .andExpect(status().isOk());

    AuditEvent event = auditEventRepository.findAll().stream()
            .filter(e -> "merchant_contact_updated".equals(e.getEventType())
                    && m.getId().equals(e.getEntityId()))
            .findFirst().orElseThrow();

    assertThat(event.getHappenedAt()).isNotNull();
}
```

---

### 5.3 Prueba E2E — Capa 2 (Serenity BDD + Cucumber — API)

**Archivo:** `e2e/src/test/resources/features/HU12_actualizacion_contacto.feature`

```gherkin
@HU12
Feature: HU 1.2 – Actualización de Información de Contacto del Comercio

  Background:
    Given que el actor está listo para las pruebas de actualización de contacto
    And se ha registrado un administrador con su comercio para las pruebas

  @CA01 @Exitoso
  Scenario: CA-01 Actualización exitosa con datos válidos retorna 200 con mensaje de éxito
    Given el actor está autenticado como administrador
    When actualiza el contacto con nombre "María López" y email "maria.lopez@example.com"
    Then el código de respuesta HTTP es 200
    And el campo "message" de la respuesta es "Contact information updated successfully"

  @CA02 @Excepcional
  Scenario: CA-02 contact_name vacío retorna 400 con cuerpo de error estándar
    Given el actor está autenticado como administrador
    When actualiza el contacto con nombre "" y email "valido@example.com"
    Then el código de respuesta HTTP es 400
    And el cuerpo de error contiene el errorCode "VALIDATION_ERROR"
    And el cuerpo de error contiene los campos obligatorios message, details y traceId

  @CA03 @Excepcional
  Scenario: CA-03 contact_email inválido retorna 400 con cuerpo de error estándar
    Given el actor está autenticado como administrador
    When actualiza el contacto con nombre "Nombre Válido" y email "no-es-un-email"
    Then el código de respuesta HTTP es 400
    And el cuerpo de error contiene el errorCode "VALIDATION_ERROR"

  @CA04 @Excepcional
  Scenario: CA-04 Comercio inexistente retorna 404 con cuerpo de error estándar
    Given el actor está autenticado como administrador
    When intenta actualizar el contacto de un comercio que no existe en el sistema
    Then el código de respuesta HTTP es 404
    And el cuerpo de error contiene el errorCode "MERCHANT_NOT_FOUND"

  @CA05 @Excepcional
  Scenario: CA-05 Sin autenticación retorna 403 Forbidden
    When actualiza el contacto sin enviar token con nombre "Test" y email "test@test.com"
    Then el código de respuesta HTTP es 403
```

**Background — flujo de autenticación real:**

```java
// HU12_ActualizacionContactoSteps.java
@Dado("se ha registrado un administrador con su comercio para las pruebas")
public void seHaRegistradoUnAdministradorConSuComercio() {
    String uniqueEmail = DataFactory.uniqueEmail("admin.hu12");
    // Registra en /api/v1/auth/register → obtiene merchantId
    // Hace login en /api/v1/auth/login → obtiene JWT
    // Guarda ambos en la memoria del actor
    actor.attemptsTo(AutenticarActor.comoAdminConEmail(uniqueEmail));
}
```

---

### 5.4 Prueba E2E — Capa 2 UI (Serenity BDD + Selenium Chrome)

**Archivo:** `e2e/src/test/resources/features/HU12_actualizacion_contacto_UI.feature`

```gherkin
@HU12UI @UI
Feature: HU 1.2 UI – Actualización de Contacto mediante la interfaz web

  Background:
    Given existe una cuenta de administrador lista para las pruebas UI
    And el actor ha iniciado sesión en la plataforma web

  @CA01UI @ExitosoUI
  Scenario: CA-01 Actualización exitosa muestra notificación de éxito
    When completa el formulario de contacto con nombre "María López" y email "maria.lopez@example.com"
    Then ve la notificación de éxito "Contact information updated successfully"

  @CA02UI @ExcepcionalUI
  Scenario: CA-02 Nombre vacío muestra error de validación en el formulario
    When envía el formulario de contacto con nombre "" y email "valido@example.com"
    Then el formulario muestra el error de nombre "Contact name is required"

  @CA03UI @ExcepcionalUI
  Scenario: CA-03 Email inválido muestra error de validación en el formulario
    When envía el formulario de contacto con nombre "Nombre Válido" y email "no-es-email"
    Then el formulario muestra el error de email "Please enter a valid email"
```

---

## 6. Arquitectura Screenplay (patrón E2E)

```
Actor ("AdminComercio")
  │
  ├── Abilities
  │     └── CallAnApi.at("http://localhost:8085")
  │
  ├── Tasks (lo que hace)
  │     ├── AutenticarActor   → POST /auth/register + POST /auth/login
  │     ├── RegistrarComercio → POST /api/v1/merchants
  │     └── ActualizarContacto→ PUT  /api/v1/merchants/{id}/contact
  │
  └── Questions (lo que verifica)
        ├── ElCodigoDeRespuestaEs → SerenityRest.lastResponse().statusCode()
        ├── ElCampoJsonEs         → lastResponse().jsonPath().get(field)
        └── ElCuerpoDeErrorEsValido → valida errorCode + message + traceId
```

---

## 7. Cómo ejecutar las pruebas

### Pruebas de aceptación (sin servidor)

```powershell
# Desde la raíz del proyecto
mvn test
```

No requiere nada levantado. Usa H2 en memoria con `@Transactional` (rollback automático tras cada test).

### Pruebas E2E de API

```powershell
# Terminal 1 — levantar backend con perfil e2e (H2, sin PostgreSQL)
mvn spring-boot:run "-Dspring-boot.run.profiles=e2e"

# Terminal 2 — correr escenarios Cucumber
cd e2e
mvn test
```

### Pruebas E2E de UI (Chrome)

```powershell
# Terminal 1 — backend
mvn spring-boot:run "-Dspring-boot.run.profiles=e2e"

# Terminal 2 — frontend Vue.js
cd frontend\payment-gateway-ui
npm run dev

# Terminal 3 — escenarios UI
cd e2e
mvn test "-Dtest=EmbeddedPaymentsUIRunner"
```

---

## 8. Reportes

### Reporte Serenity (E2E)

Después de `mvn test` en `e2e/`, el reporte HTML se genera en:

```
e2e/target/site/serenity/index.html
```

Abrir en el navegador:

```powershell
start e2e\target\site\serenity\index.html
```

El reporte incluye:
- Resultado por escenario (PASS / FAIL)
- Cada paso Gherkin con su resultado y duración
- Capturas de pantalla por acción (configurado `FOR_EACH_ACTION`)
- Trazas HTTP: URL, headers, body de request y response
- Gráfica de cobertura por historia de usuario y tag

### Reporte Surefire (aceptación + E2E)

```
e2e/target/surefire-reports/
target/surefire-reports/
```

Archivos XML y TXT por clase de test, compatibles con Jenkins / GitHub Actions.

---

## 9. Resumen de cobertura por HU y tipo

| HU | CA | Descripción | Tipo | Aceptación | E2E API | E2E UI |
|---|---|---|---|---|---|---|
| HU 1.1 | CA-01 | Registro comercio potencial → 201 + INACTIVE + api\_key | Happy path | ✅ | ✅ | — |
| HU 1.1 | CA-02 | Registro administrador → 201 + INACTIVE | Happy path | ✅ | ✅ | — |
| HU 1.1 | CA-03 | Payload inválido → 400 VALIDATION\_ERROR | Excepcional | ✅ | ✅ (3 ejemplos) | — |
| HU 1.1 | CA-04 | Email duplicado → 409 MERCHANT\_ALREADY\_EXISTS | Excepcional | ✅ | ✅ | — |
| HU 1.1 | CA-05 | Auditoría en registro | Happy path | ✅ | — | — |
| HU 1.2 | CA-01 | Actualización válida → 200 + message | Happy path | ✅ | ✅ | ✅ |
| HU 1.2 | CA-02 | Nombre vacío → 400 VALIDATION\_ERROR | Excepcional | ✅ | ✅ | ✅ |
| HU 1.2 | CA-03 | Email inválido → 400 VALIDATION\_ERROR | Excepcional | ✅ | ✅ | ✅ |
| HU 1.2 | CA-04 | Comercio inexistente → 404 MERCHANT\_NOT\_FOUND | Excepcional | ✅ | ✅ | — |
| HU 1.2 | CA-05 | Sin autenticación → 403 Forbidden | Excepcional | ✅ | ✅ | — |
| HU 1.2 | CA-06 | Auditoría en actualización | Happy path | ✅ | — | — |
