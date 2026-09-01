# Lógica de Envío Multidireccional por Tipo de Informe

Este plan describe los cambios necesarios para implementar el envío de correos a múltiples destinatarios (backup general + destinatario específico por tipo de informe) mediante el uso de checkboxes en los formularios.

## Proposed Changes

### [Networking]

#### [MODIFY] [MailSender.java](file:///C:/Users/sanph/Downloads/ComismarApp_CORREGIDO/app/src/main/java/com/comismar/informes/view/adapter/MailSender.java)
- Actualizar el método `enviarCorreo` para aceptar una lista de destinatarios (`List<String>`) en lugar de uno solo.
- Iterar sobre la lista para construir el array "to" en la petición JSON de Brevo.

### [Resources]

#### [MODIFY] [strings.xml](file:///C:/Users/sanph/Downloads/ComismarApp_CORREGIDO/app/src/main/res/values/strings.xml)
- Añadir `checkbox_send_naval_copy`: "Enviar copia de informe a cascos@comismar.es".
- Añadir `checkbox_send_cargo_copy`: "Enviar copia de informe a nacional@comismar.es".

### [UI / Layouts]

#### [MODIFY] [activity_generar_informe.xml](file:///C:/Users/sanph/Downloads/ComismarApp_CORREGIDO/app/src/main/res/layout/activity_generar_informe.xml)
- Añadir un `CheckBox` encima del botón "Generar Informe" para la copia a Naval.

#### [MODIFY] [activity_generar_mercancia.xml](file:///C:/Users/sanph/Downloads/ComismarApp_CORREGIDO/app/src/main/res/layout/activity_generar_mercancia.xml)
- Añadir un `CheckBox` encima del botón "Generar Informe" para la copia a Mercancía.

### [Activities]

#### [MODIFY] [GenerarInformeActivity.java](file:///C:/Users/sanph/Downloads/ComismarApp_CORREGIDO/app/src/main/java/com/comismar/informes/view/activity/GenerarInformeActivity.java)
- Leer el estado del CheckBox de Naval.
- Preparar la lista de correos: Backup (desde Settings) + `cascos@comismar.es` (si está marcado).

#### [MODIFY] [EditarInformeActivity.java](file:///C:/Users/sanph/Downloads/ComismarApp_CORREGIDO/app/src/main/java/com/comismar/informes/view/activity/EditarInformeActivity.java)
- Misma lógica que en generación para mantener la coherencia.

#### [MODIFY] [GenerarInformeMercanciaActivity.java](file:///C:/Users/sanph/Downloads/ComismarApp_CORREGIDO/app/src/main/java/com/comismar/informes/view/activity/GenerarInformeMercanciaActivity.java)
- Leer el estado del CheckBox de Mercancía.
- Preparar la lista de correos: Backup (desde Settings) + `nacional@comismar.es` (si está marcado).

#### [MODIFY] [EditarInformeMercanciaActivity.java](file:///C:/Users/sanph/Downloads/ComismarApp_CORREGIDO/app/src/main/java/com/comismar/informes/view/activity/EditarInformeMercanciaActivity.java)
- Misma lógica que en generación para mercancía.

## Verification Plan

### Manual Verification
1. Generar un informe Naval con el checkbox de copia activo -> Verificar que el log muestra envío a dos direcciones.
2. Generar un informe Naval con el checkbox de copia desactivado -> Verificar que solo se envía a la dirección de backup.
3. Repetir las pruebas para informes de Mercancía.
