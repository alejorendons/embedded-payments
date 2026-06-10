@HU16
Feature: HU 1.6 – Consulta de Comercio por ID
  Como administrador de la plataforma
  Quiero consultar la información detallada de un comercio
  Para verificar sus datos sin exponer información sensible

  Background:
    Given que el actor está listo para las pruebas de consulta de comercio
    And se ha registrado un administrador con su comercio para consulta

  @CA01 @Exitoso
  Scenario: CA-01 Administrador consulta comercio válido con HTTP 200 en JSON
    When el administrador consulta el comercio por su ID
    Then el código de respuesta HTTP es 200
    And el campo "id" de la respuesta no está vacío
    And el campo "status" de la respuesta no está vacío

  @CA02 @Exitoso
  Scenario: CA-02 Datos sensibles del comercio quedan enmascarados en la respuesta
    Given el comercio tiene una cuenta bancaria registrada
    When el administrador consulta el comercio por su ID
    Then el código de respuesta HTTP es 200
    And el campo "bank_account_data" de la respuesta contiene "ENCRYPTED"

  @CA03 @Excepcional
  Scenario: CA-03 Consultar un comercio con ID inexistente retorna HTTP 404
    When el administrador consulta un comercio con ID inexistente
    Then el código de respuesta HTTP es 404
    And el cuerpo de error contiene los campos obligatorios message y traceId

  @CA04 @Excepcional
  Scenario: CA-04 Consultar comercio sin permisos retorna HTTP 403
    When un usuario sin autenticación consulta el comercio por su ID
    Then el código de respuesta HTTP es 403
