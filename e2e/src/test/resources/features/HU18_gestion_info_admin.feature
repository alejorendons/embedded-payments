@HU18
Feature: HU 1.8 – Gestión de Información del Administrador
  Como administrador de la plataforma
  Quiero gestionar mis datos personales y mi contraseña
  Para mantener actualizada y segura mi cuenta

  Background:
    Given que el actor está listo para las pruebas de gestión de información del administrador
    And se ha registrado y autenticado un administrador para gestionar su perfil

  @CA01 @Exitoso
  Scenario: CA-01 Actualización de datos personales válidos con HTTP 200
    When el administrador actualiza sus datos personales con información válida
    Then el código de respuesta HTTP es 200

  @CA02 @Exitoso
  Scenario: CA-02 Cambio de contraseña exitoso invalida sesiones con HTTP 200
    When el administrador cambia su contraseña exitosamente
    Then el código de respuesta HTTP es 200

  @CA03 @Excepcional
  Scenario: CA-03 Contraseña actual incorrecta retorna HTTP 401
    When el administrador intenta cambiar su contraseña con la contraseña actual incorrecta
    Then el código de respuesta HTTP es 401
    And el cuerpo de error contiene los campos obligatorios message y traceId

  @CA04 @Excepcional
  Scenario: CA-04 Email ya registrado retorna HTTP 409 al actualizar perfil
    Given existe otro administrador registrado con un email distinto
    When el administrador intenta actualizar su perfil con el email de otro administrador
    Then el código de respuesta HTTP es 409
    And el cuerpo de error contiene los campos obligatorios message y traceId

  @CA05 @Excepcional
  Scenario: CA-05 Datos inválidos retornan HTTP 400 al actualizar perfil
    When el administrador intenta actualizar su perfil con datos inválidos
    Then el código de respuesta HTTP es 400
    And el cuerpo de error contiene el errorCode "VALIDATION_ERROR"
    And el cuerpo de error contiene los campos obligatorios message, details y traceId
