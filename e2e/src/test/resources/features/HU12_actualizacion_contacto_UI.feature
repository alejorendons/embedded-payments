@HU12UI @UI
Feature: HU 1.2 UI – Actualización de Contacto mediante la interfaz web
  Como comercio autenticado en la plataforma
  Quiero actualizar mi información de contacto desde la interfaz web
  Para mantener mis datos siempre actualizados sin usar la API directamente

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
