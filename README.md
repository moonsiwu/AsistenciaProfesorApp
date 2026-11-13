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

