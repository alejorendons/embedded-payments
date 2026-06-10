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
