@HU19
Feature: HU 1.9 – Creación de Pagos
  Como comercio activo en la plataforma
  Quiero generar intenciones de pago
  Para iniciar el cobro a mis clientes

  Background:
    Given que el actor está listo para las pruebas de creación de pagos

  @CA01 @Exitoso
  Scenario: CA-01 Comercio activo genera intención de pago con referencia única
    Given que el comercio está activo para pagos
    When el comercio crea una intención de pago con monto válido
    Then el código de respuesta HTTP es 201
    And el campo "id" de la respuesta no está vacío
    And el campo "status" de la respuesta no está vacío

  @CA02 @Excepcional
  Scenario: CA-02 Comercio inactivo no puede iniciar un cobro
    Given que el comercio está inactivo para pagos
    When el comercio intenta crear una intención de pago con monto válido
    Then el código de respuesta HTTP es 403
    And el cuerpo de error contiene los campos obligatorios message y traceId

  @CA03 @Excepcional
  Scenario Outline: CA-03 Monto en cero o negativo es rechazado por la plataforma
    Given que el comercio está activo para pagos
    When el comercio intenta crear una intención de pago con monto "<monto>"
    Then el código de respuesta HTTP es 400
    And el cuerpo de error contiene el errorCode "VALIDATION_ERROR"

    Examples:
      | monto  |
      | cero   |
      | negativo |
