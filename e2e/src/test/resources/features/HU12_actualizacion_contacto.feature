@HU12
Feature: HU 1.2 – Actualización de Información de Contacto del Comercio
  Como comercio autenticado en la plataforma
  Quiero actualizar mi información de contacto (nombre y email)
  Para mantener mis datos operativos siempre actualizados

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
    And el cuerpo de error contiene los campos obligatorios message, details y traceId

  @CA04 @Excepcional
  Scenario: CA-04 Comercio inexistente retorna 404 con cuerpo de error estándar
    Given el actor está autenticado como administrador
    When intenta actualizar el contacto de un comercio que no existe en el sistema
    Then el código de respuesta HTTP es 404
    And el cuerpo de error contiene el errorCode "MERCHANT_NOT_FOUND"
    And el cuerpo de error contiene los campos obligatorios message y traceId

  @CA05 @Excepcional
  Scenario: CA-05 Sin autenticación retorna 403 Forbidden
    When actualiza el contacto sin enviar token con nombre "Test" y email "test@test.com"
    Then el código de respuesta HTTP es 403
