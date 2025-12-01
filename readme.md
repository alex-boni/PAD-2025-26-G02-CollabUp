# 📱 CollabUp

Es una aplicación móvil desarrollada en **Java** para Android, que nace de una necesidad cotidiana: la complejidad de coordinar agendas y tareas dentro de grupos sociales, ya sean amigos, familiares o compañeros de piso.
La aplicación es una herramienta de gestión social que permite la organización simultánea y colaborativa. A diferencia de las herramientas de gestión de proyectos profesionales, CollabUp se centra en **Collabs**: espacios compartidos que pueden representar desde una lista de la compra común hasta la planificación de un viaje o una quedada de fin de semana. 
Además, en cada Collab se pueden añadir distintos **Collab Views**, un apartado específico que potencia la organización. Cada Collab View puede ser un calendario o una lista, y cada uno a su vez contiene una colección de **Collab Items**. Un Collab Item se asemeja a una tarea, se le puede asignar una fecha, miembros e incluso las Collab Views en las que aparecerá el Item ya que son globales en un Collab, y se pueden asignar a los Collab Views que quieras.
El objetivo principal es eliminar la fricción en la comunicación, permitiendo a los usuarios visualizar días libres comunes y gestionar tareas compartidas sin necesidad de mensajería constante, centralizando la información en un entorno intuitivo y accesible.


---

## 🚀 Características principales

- 👥 **Nuestros Collabs como una gestión de grupos**: Crea o únete a grupos (collabs) con tus amigos, familia o compañeros.
- ✅ **Gestión de Tareas Inteligente (Collab Items)**: Crea, asigna responsables y visualiza todas las tareas compartidas.
- 🗓️ **Vistas Adaptables (Collab Views)**: Organiza la información de tu Collab en Listas o Calendarios que contengan tus collab items.
- ☁️ **Sincronización en la nube**: Todos los datos e imagenes se almacenan de forma segura en **Firebase Firestore**.

---

## 🧱 Tecnologías utilizadas

- **Lenguaje:** Java ☕
- **SDK Mínimo:** Android API 24 (Nougat)
- **Entorno de desarrollo:** Android Studio  
- **Base de datos:** Firebase Firestore
- **Autenticación:** Facebook SDK & Google Play Services Auth
- **Diseño UI:** XML + Material Design  

---

## 📂 Estructura del proyecto

El proyecto sigue un patrón **MVC (Modelo-Vista-Controlador)** adaptada al ecosistema Android, para separar las preocupaciones de la interfaz de usuario, la lógica de negocio y la gestión de datos, facilitando el mantenimiento y la escalabilidad.
```

CollabUp/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/collabup/           # Código fuente principal (Java)
│   │       │             ├── controlador                <-- Logica de UI, negocio y navegación entre vistas
│   │       │             │     ├── fragmento            <-- fragments(Lógica de la Presentación)
│   │       │             │     ├── *.java               <-- activities
│   │       │             │     └── MainActivity.java    <-- Launcher  
│   │       │             ├
│   │       │             └── modelo     M (Datos/Lógica de Negocio)
│   │       │                   ├── adapters             <-- adaptadores para mostrar conjuntos de datos en las vistas
│   │       │                   ├── collabView           <-- recursos para visualizacion de los distintos collab views
│   │       │                   ├── interfaz             <-- interfaces DAO, Callback compartidos
│   │       │                   └── *.java               <-- Clases (usuario, collab ..)
│   │       │             
│   │       ├── res/      # Recursos (layouts, layout-lands, menus, drawables, strings, values)
│   │       └── AndroidManifest.xml
│   │       
│   ├── build.gradle
│   └── google-services.json
│
├── gradle/
│
└── README.md
```

---

## ⚙️ Instalación y ejecución

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/alex-boni/PAD-2025-26-G02-CollabUp.git
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

### Collab: Crea, edita e invita a tus amigos. 
<img src="https://github.com/alex-boni/PAD-2025-26-G02-CollabUp/blob/module/collab/ScreenShot/collab%20details%20pixel%209.png?raw=true" alt="Texto alternativo" height="400" />` `
<img src="https://github.com/alex-boni/PAD-2025-26-G02-CollabUp/blob/module/collab/ScreenShot/list%20collabs%20pixel%209.png?raw=true" alt="Texto alternativo" height="400"/>` `
<img src="https://github.com/alex-boni/PAD-2025-26-G02-CollabUp/blob/module/collab/ScreenShot/collab%20edit%20pixel%209.png?raw=true" alt="Texto alternativo" height="400"/>

---

## 👨‍💻 Equipo de desarrollo

Proyecto desarrollado por el **Grupo 02 de la Asignatura de PAD UCM 2025-2026**:

- [Alex Guillermo Bonilla Taco](https://github.com/alex-boni)
- [Óscar Marín](https://github.com/Oscmarin715)
- [Rocío Uñón](https://github.com/rouu04)
- [Rubén Hidalgo](https://github.com/RubizZ)
- [Airam Martín Soto](https://github.com/airamsoto)
- [Bryan Quilumba](https://github.com/bryanX02)

---

## 🏗️ Futuras mejoras

- Integración con calendarios externos (Google Calendar).  
- Soporte para chat dentro de cada grupo.   
- Modo oscuro y personalización de interfaz.  

---

## 📜 Licencia

Este proyecto está bajo la licencia [MIT](LICENSE).

---

## 💬 Contacto

Si deseas colaborar o tienes sugerencias, no dudes en abrir un *issue* o enviar un *pull request*.
