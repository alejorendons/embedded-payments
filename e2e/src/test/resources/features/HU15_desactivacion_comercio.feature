@HU15
Feature: HU 1.5 – Desactivación de Comercio
  Como administrador de la plataforma
  Quiero desactivar un comercio activo
  Para impedir que continúe operando en la plataforma

  Background:
    Given que el actor está listo para las pruebas de desactivación de comercio
    And se ha registrado un administrador con un comercio activo

  @CA01 @Exitoso
  Scenario: CA-01 Administrador desactiva comercio activo con HTTP 200
    When el administrador desactiva el comercio
    Then el código de respuesta HTTP es 200
    And el campo "status" de la respuesta es "INACTIVE"

  @CA02 @Excepcional
  Scenario: CA-02 Comercio desactivado bloquea nuevas transacciones con HTTP 403
    Given el administrador ha desactivado el comercio
    When el comercio desactivado intenta crear una intención de pago
    Then el código de respuesta HTTP es 403
    And el cuerpo de error contiene los campos obligatorios message y traceId
