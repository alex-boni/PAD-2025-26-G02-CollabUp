# 📱 CollabUp

**CollabUp** es una aplicación móvil desarrollada en **Java** para Android, diseñada para facilitar la organización de actividades y la colaboración en grupo. Con CollabUp, encontrar el momento ideal para reunirse, organizar tareas compartidas o coordinar compras en grupo es más fácil que nunca.

---

## 🚀 Características principales

- 👥 **Gestión de grupos**: crea o únete a grupos con tus amigos, familia o compañeros.
- 🗓️ **Planificación colaborativa**: organiza actividades y encuentra el mejor momento para todos.
- ✅ **Tareas compartidas**: asigna y marca tareas dentro de cada grupo.
- 🔔 **Notificaciones en tiempo real** gracias a la integración con Firebase.
- ☁️ **Sincronización en la nube**: todos los datos se almacenan de forma segura en **Firebase Firestore**.

---

## 🧱 Tecnologías utilizadas

- **Lenguaje:** Java ☕  
- **Entorno de desarrollo:** Android Studio  
- **Base de datos:** Firebase Firestore  
- **Autenticación:** Firebase Authentication  
- **Notificaciones:** Firebase Cloud Messaging (FCM)  
- **Diseño UI:** XML + Material Design  

---

## 📂 Estructura del proyecto

```
CollabUp/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/collabup/      # Código fuente principal (Java)
│   │   │   │             ├── view      <-- V (Vistas/UI)
│   │   │   │             ├── viewmodel <-- VM (Lógica de la Presentación)
│   │   │   │             ├── model     M (Datos/Lógica de Negocio)
│   │   │   │                   ├── data
│   │   │   │                   ├── repository
│   │   │   │                   ├── source 
│   │   │   │             
│   │   │   ├── res/                     # Recursos (layouts, drawables, strings)
│   │   │   └── AndroidManifest.xml
│   └── build.gradle
│
├── gradle/
│
└── README.md
```

---

## ⚙️ Instalación y ejecución

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/CollabUp.git
   ```
2. **Abrir en Android Studio**
   - Selecciona `File > Open...` y elige la carpeta del proyecto.
3. **Configurar Firebase**
   - Crea un proyecto en [Firebase Console](https://console.firebase.google.com/)
   - Descarga el archivo `google-services.json` y colócalo en la carpeta:
     ```
     app/
     ```
4. **Ejecutar la app**
   - Conecta un dispositivo o usa un emulador.
   - Presiona ▶️ **Run** en Android Studio.

---

## 📸 Capturas de pantalla

*(Agrega aquí imágenes del funcionamiento de la app)*  
> 📷 Ejemplo: pantalla de inicio, gestión de grupos, creación de actividades, etc.

---

## 👨‍💻 Equipo de desarrollo

Proyecto desarrollado por el **Grupo 2**:

- Alex Bonilla  
- Óscar Marín  
- Rocío Uñón  
- Rubén Hidalgo  
- Airam Martín  
- Bryan Quilumba  

---

## 🧠 Inspiración

> “¿Cuánto se tarda en encontrar un hueco libre para hacer un plan con todo el grupo de amigos?  
> ¿Y si una app pudiera ayudarte a coordinarlo todo, sin estrés?”

CollabUp surge de la necesidad de **mejorar la comunicación y la colaboración en grupos** pequeños, facilitando la organización de tareas y actividades diarias.

---

## 🏗️ Futuras mejoras

- Integración con calendarios externos (Google Calendar).  
- Soporte para chat dentro de cada grupo.  
- Integración con almacenamiento multimedia (fotos y documentos).  
- Modo oscuro y personalización de interfaz.  

---

## 📜 Licencia

Este proyecto está bajo la licencia [MIT](LICENSE).

---

## 💬 Contacto

Si deseas colaborar o tienes sugerencias, no dudes en abrir un *issue* o enviar un *pull request*.
