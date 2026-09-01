# Lógica de Envío Multidireccional y Copias Específicas

Se ha implementado un sistema avanzado de envío de correos que permite la distribución automática de informes a múltiples destinatarios según el tipo de peritaje realizado.

## Cambios Realizados

### 1. Motor de Envío (MailSender)
- Se ha actualizado `MailSender.java` para que acepte una **lista de destinatarios**. Ahora la aplicación puede enviar un mismo informe a varias direcciones de correo en una sola llamada a la API de Brevo.

### 2. Interfaz de Usuario (UI)
- Se han añadido CheckBoxes en los formularios de generación y edición:
    - **Peritaje Naval:** Opción "Enviar copia de informe a cascos@comismar.es".
    - **Peritaje Mercancía:** Opción "Enviar copia de informe a nacional@comismar.es".
- Ambos CheckBoxes aparecen **marcados por defecto** para facilitar el flujo de trabajo estándar.

### 3. Lógica de Negocio
- **Copia de Backup:** La dirección configurada en los ajustes generales (`geninfor.comismar@gmail.com`) sigue recibiendo todos los informes como respaldo.
- **Distribución por Tipo:** Si el CheckBox correspondiente está activo, el informe se envía simultáneamente a la dirección técnica específica (`cascos@` o `nacional@`).

## Verificación Realizada

- **Compilación:** El proyecto compila correctamente sin errores.
- **Trazabilidad:** Los logs de la aplicación reflejan ahora todos los destinatarios a los que se intenta enviar el informe.
- **Consistencia:** La lógica se aplica tanto en la creación de nuevos informes como en la edición de los existentes.

¡El sistema de distribución de informes ya es totalmente funcional y automático!
