# 🧪 Camp&Go - Rama de desarrollo (`dev`)

Este documento está destinado a desarrolladores y colaboradores que trabajan activamente en la rama `dev` del proyecto **Camp&Go**. Aquí encontrarás detalles técnicos, instrucciones de configuración, convenciones internas y dependencias necesarias para contribuir de manera efectiva al proyecto.

---

## ⚙️ Configuración del entorno

### Requisitos

- **Android Studio Giraffe o superior**  
- **JDK 17**  
- SDK mínimo: `API 24 (Android 7.0)`  
- Conexión a Internet (para dependencias de Firebase y APIs externas)

### Claves API y configuración sensible

Para proteger los datos sensibles, **no incluimos archivos como `google-services.json` ni `secrets.xml` en el repositorio**.

#### Pasos para configurar localmente:

1. Crear el archivo `app/google-services.json` desde [Firebase Console](https://console.firebase.google.com/).  
2. Crear manualmente el archivo `app/src/main/res/values/secrets.xml` con este contenido:

```xml
<resources>
    <string name="google_maps_key">TU_CLAVE_GOOGLE_MAPS</string>
    <string name="open_weather_key">TU_CLAVE_OPENWEATHER</string>
</resources>
