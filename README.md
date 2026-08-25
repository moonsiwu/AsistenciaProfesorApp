# 🎓 Sistema de Control de Asistencia - Portal Docente

Aplicación web moderna, responsiva y corporativa para la gestión diaria de asistencia de estudiantes por curso, control de matrícula y generación de reportes con exportación a Excel/CSV.

---

## 🌟 Características Principales

1. **🔐 Acceso Institucional y Control de Sesión:**
   - Inicio de sesión con autenticación para docentes.
   - Datos de prueba listos para evaluación rápida (`profesor1` / `123456`) con botón de autocompletar en 1 clic.

2. **📅 Registro de Asistencia Diario:**
   - Selección dinámica de cursos y fecha.
   - **Métricas en tiempo real:** Contadores de *Total Estudiantes, Presentes, Ausentes y Tardes/Excusas* que se actualizan al instante al interactuar con la tabla.
   - Botón de acción rápida: *"Marcar Todos Presentes"*.
   - Registro de observaciones individuales.

3. **👥 Gestión de Estudiantes y Cursos:**
   - Creación de nuevos cursos con jornada (Mañana, Tarde, Noche, Única).
   - Matrícula de estudiantes con validación de documento único.
   - Opción para dar de baja conservando el historial de asistencia.

4. **📊 Reportes y Consultas con Exportación:**
   - Filtro por curso y rango de fechas (*Desde / Hasta*).
   - Cálculo automático del porcentaje de asistencia del grupo.
   - **Exportación a CSV / Excel:** Descarga inmediata del reporte con codificación UTF-8 compatible con Microsoft Excel.

5. **💾 Persistencia de Datos:**
   - Utiliza `LocalStorage` en el navegador para guardar y recordar todos los cambios, estudiantes y asistencias sin necesidad de configurar un servidor backend.

---

## 🚀 Cómo Publicar la Página en GitHub Pages (en 3 pasos)

Para que cualquier persona pueda abrir tu página directamente desde internet a través de un enlace de GitHub:

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
