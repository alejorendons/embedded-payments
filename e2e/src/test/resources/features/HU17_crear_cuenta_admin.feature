@HU17
Feature: HU 1.7 – Creación de Cuentas de Administrador
  Como Super Administrador de la plataforma
  Quiero crear cuentas de administrador con un rol asignado
  Para delegar la gestión de comercios a otros administradores

  Background:
    Given que el actor está listo para las pruebas de creación de cuentas de administrador
    And se ha autenticado un Super Administrador para las pruebas

  @CA01 @Exitoso
  Scenario: CA-01 Super Admin crea cuenta de administrador con rol asignado y HTTP 201
    When el Super Admin crea una cuenta de administrador
    Then el código de respuesta HTTP es 201
    And el campo "role" de la respuesta es "ADMIN"
    And el campo "id" de la respuesta no está vacío

  @CA02 @Excepcional
  Scenario: CA-02 Email duplicado retorna HTTP 409 al crear cuenta de administrador
    Given ya existe una cuenta de administrador creada por el Super Admin
    When el Super Admin intenta crear otra cuenta de administrador con el mismo email
    Then el código de respuesta HTTP es 409
    And el cuerpo de error contiene los campos obligatorios message y traceId

  @CA03 @Excepcional
  Scenario: CA-03 Datos inválidos retornan HTTP 400 al crear cuenta de administrador
    When el Super Admin intenta crear una cuenta de administrador con datos inválidos
    Then el código de respuesta HTTP es 400
    And el cuerpo de error contiene el errorCode "VALIDATION_ERROR"
    And el cuerpo de error contiene los campos obligatorios message, details y traceId
