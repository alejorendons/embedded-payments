@HU13
Feature: HU 1.3 – Registro de Información Financiera del Comercio
  Como comercio registrado en la plataforma
  Quiero registrar mi cuenta bancaria
  Para poder recibir los pagos liquidados por la plataforma

  Background:
    Given que el actor está listo para las pruebas de información financiera

  @CA01 @Exitoso
  Scenario: CA-01 Comercio activo registra cuenta bancaria con HTTP 200
    Given que el comercio está activo
    When registra una cuenta bancaria con datos válidos
    Then el código de respuesta HTTP es 200
    And el campo "status" de la respuesta es "ACTIVE"

  @CA02 @Excepcional
  Scenario: CA-02 Comercio inactivo retorna HTTP 403 al registrar cuenta bancaria
    Given que el comercio está inactivo
    When intenta registrar una cuenta bancaria con datos válidos
    Then el código de respuesta HTTP es 403
    And el cuerpo de error contiene los campos obligatorios message y traceId

  @CA03 @Excepcional
  Scenario: CA-03 Datos bancarios inválidos retornan HTTP 400
    Given que el comercio está activo
    When intenta registrar una cuenta bancaria con datos inválidos
    Then el código de respuesta HTTP es 400
    And el cuerpo de error contiene el errorCode "VALIDATION_ERROR"
    And el cuerpo de error contiene los campos obligatorios message, details y traceId
