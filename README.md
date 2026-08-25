# 🎓 Sistema de Control de Asistencia - Portal Docente

Aplicación web moderna, responsiva y corporativa para la gestión diaria de asistencia de estudiantes por curso, control de matrícula y generación de reportes con exportación a Excel/CSV.

Incluye **sincronización en tiempo real en la nube** para que múltiples computadoras o celulares vean las actualizaciones al instante.

---

## 🌟 Características Principales

1. **🔐 Acceso Institucional y Control de Sesión:**
   - Inicio de sesión con autenticación para docentes.
   - Datos de prueba listos para evaluación rápida (`profesor1` / `123456`) con botón de autocompletar en 1 clic.

2. **☁️ Sincronización en la Nube en Tiempo Real:**
   - Compatible con **Firebase Realtime Database** gratuita de Google.
   - Cuando un docente crea un curso, registra un estudiante o toma asistencia en su computadora, **se actualiza automáticamente en las demás pantallas abiertas sin recargar la página**.

3. **📅 Registro de Asistencia Diario:**
   - Selección dinámica de cursos y fecha.
   - **Métricas en tiempo real:** Contadores de *Total Estudiantes, Presentes, Ausentes y Tardes/Excusas* que se actualizan al instante al interactuar con la tabla.
   - Botón de acción rápida: *"Marcar Todos Presentes"*.
   - Registro de observaciones individuales.

4. **👥 Gestión de Estudiantes y Cursos:**
   - Creación de nuevos cursos con jornada (Mañana, Tarde, Noche, Única).
   - Matrícula de estudiantes con validación de documento único.
   - Opción para dar de baja conservando el historial de asistencia.

5. **📊 Reportes y Consultas con Exportación:**
   - Filtro por curso y rango de fechas (*Desde / Hasta*).
   - Cálculo automático del porcentaje de asistencia del grupo.
   - **Exportación a CSV / Excel:** Descarga inmediata del reporte con codificación UTF-8 compatible con Microsoft Excel.

---

## ☁️ Cómo Sincronizar Varias Computadoras (Firebase Gratis)

Por defecto, la página guarda los datos en el navegador (`LocalStorage`). Para que todas las computadoras compartan la misma información por internet:

1. Entra a [console.firebase.google.com](https://console.firebase.google.com) con tu cuenta de Google.
2. Crea un proyecto gratuito y en el menú ve a **Realtime Database** > **Crear base de datos**.
3. Elige la ubicación por defecto y selecciona **Modo de prueba (Test mode)**.
4. Copia la URL de tu base de datos (ejemplo: `https://tu-proyecto-rtdb.firebaseio.com/`).
5. En la aplicación web, haz clic en el botón **☁️ Configurar Nube** (en la barra lateral), pega tu URL y haz clic en **Guardar y Conectar**.

*¡A partir de ese momento, cualquier cambio que hagas se reflejará al instante en todas las computadoras!*

---

## 🚀 Cómo Publicar la Página en GitHub Pages (en 3 pasos)

1. **Sube el proyecto a tu repositorio de GitHub:**
   - Sube todos los archivos (`index.html`, carpeta `css`, carpeta `js`, etc.) a la rama principal (`main`).

2. **Activa GitHub Pages:**
   - En tu repositorio de GitHub, ve a **Settings** (Configuración) ⚙️.
   - En el menú lateral izquierdo, haz clic en **Pages**.
   - En la sección **Build and deployment** > **Branch**:
     - Selecciona la rama `main`.
     - Carpeta: `/ (root)`.
     - Haz clic en **Save** (Guardar).

3. **¡Listo!**
   - En 1 minuto, GitHub te dará un enlace público como:
     `https://tu-usuario.github.io/tu-repositorio/`

---

## 💻 Ejecución Local

### Opción Web (Navegador):
Solo haz doble clic en el archivo [`index.html`](index.html) para abrir la aplicación web en Chrome, Edge, Firefox o cualquier navegador.

### Opción de Escritorio (Java Swing):
Si deseas ejecutar la versión nativa de escritorio en Java, haz doble clic en [`ejecutar.bat`](ejecutar.bat).

---

## 🔑 Credenciales de Acceso de Prueba

- **Usuario:** `profesor1`
- **Contraseña:** `123456`
