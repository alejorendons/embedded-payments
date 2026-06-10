@HU14
Feature: HU 1.4 – Activación de Comercio
  Como administrador de la plataforma
  Quiero activar un comercio registrado
  Para que pueda comenzar a operar y procesar pagos

  Background:
    Given que el actor está listo para las pruebas de activación de comercio
    And se ha registrado un administrador con un comercio inactivo

  @CA01 @Exitoso
  Scenario: CA-01 Administrador activa comercio inactivo con HTTP 200
    When el administrador activa el comercio
    Then el código de respuesta HTTP es 200
    And el campo "status" de la respuesta es "ACTIVE"

  @CA02 @Excepcional
  Scenario: CA-02 Activar un comercio con ID inexistente retorna HTTP 404
    When el administrador intenta activar un comercio con ID inexistente
    Then el código de respuesta HTTP es 404
    And el cuerpo de error contiene los campos obligatorios message y traceId
