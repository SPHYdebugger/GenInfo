# GenINFOR App - Sistema de Gestión de Informes Periciales Navales

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android)
![Java](https://img.shields.io/badge/Language-Java-ED8B00?logo=java)
![License](https://img.shields.io/badge/License-MIT-blue)

**GenINFOR App** es una solución móvil profesional diseñada específicamente para peritos e inspectores marítimos. La aplicación permite la digitalización completa del proceso de inspección, desde la captura de datos y fotografías en el lugar del siniestro hasta la generación y envío automático de informes técnicos en formato PDF.

## 🚀 Funcionalidades Clave

- **Gestión Integral de Informes:** Creación, edición y almacenamiento local de informes técnicos detallados.
- **Captura Multimedia:** Integración con la cámara del dispositivo y galería, con guardado automático de copias de seguridad en una carpeta dedicada (`GenInfor`) en la galería pública.
- **Motor de PDF Personalizado:** Generación de documentos PDF dinámicos que incluyen tablas de datos, logotipos corporativos, firmas digitales y anexos fotográficos optimizados.
- **Envío Automatizado:** Integración con protocolos SMTP para el envío silencioso de informes a la central una vez finalizados.
- **Seguridad y Privacidad:** Sistema de autenticación de usuarios y gestión de persistencia de datos local (offline-first).
- **Menú de Configuración Avanzado:** Acceso protegido a ajustes críticos como la dirección de correo de destino y preferencias de automatización.

## 🛠️ Stack Tecnológico y Arquitectura

Este proyecto demuestra capacidades sólidas en el desarrollo nativo de Android y el uso de librerías industriales estándar:

- **Lenguaje:** Java (Android SDK).
- **Persistencia de Datos:** **Room Persistence Library** (sobre SQLite) para una gestión de datos robusta, escalable y con tipado seguro.
- **Generación de Documentos:** **iTextG** para la construcción programática de archivos PDF complejos, manejando eventos de página para encabezados y pies de página dinámicos.
- **Comunicación y Networking:** **JavaMail API** para la gestión de envíos de correo electrónico directamente desde el cliente.
- **Diseño de UI:** Material Design Components, XML Layouts personalizados, y gestión de recursos multi-idioma (Español/Inglés).
- **Hardware:** Integración avanzada con la **Camera API** y **MediaStore API**, incluyendo la gestión de permisos en tiempo de ejecución y visibilidad de paquetes para Android 11+.

## ⚙️ Desarrollo y Mejores Prácticas

- **Arquitectura:** Estructura modular dividida en capas de Modelo, Vista y Utilidades (MV), facilitando el mantenimiento y la escalabilidad.
- **Optimización de Recursos:** Algoritmos de compresión de imágenes integrados para garantizar que los PDFs generados sean ligeros para el envío por email sin perder calidad visual.
- **Compatibilidad:** Implementación de `FileProvider` y declaraciones de `<queries>` para asegurar un funcionamiento fluido desde Android 5.0 hasta Android 14.
- **UX Detallada:** Flujos de usuario optimizados con diálogos de confirmación, estados de carga (overlays) y accesos directos ocultos para administración.

## 📦 Instalación y Configuración

1. Clonar el repositorio.
2. Abrir el proyecto con **Android Studio (versión Jellyfish o superior)**.
3. Configurar las credenciales SMTP en la clase `MailSender` (o a través del menú de configuración en la app).
4. Compilar y desplegar en un dispositivo físico o emulador (API 21+).

---
*Este proyecto es una muestra de ingeniería de software aplicada a soluciones de movilidad para sectores técnicos especializados.*
