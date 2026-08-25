/**
 * ==========================================================================
 * SISTEMA DE CONTROL DE ASISTENCIA - LÓGICA DE APLICACIÓN WEB (SPA)
 * Con soporte para Base de Datos en la Nube (Firebase) y Sincronización en Tiempo Real
 * ==========================================================================
 */

// --- Configuración de Base de Datos ---
// Si deseas dejar configurada tu base de datos fija de Firebase, coloca aquí la URL:
// Ejemplo: const FIREBASE_DB_URL_POR_DEFECTO = "https://tu-proyecto-rtdb.firebaseio.com";
const FIREBASE_DB_URL_POR_DEFECTO = "";

const DB_KEYS = {
  FIREBASE_URL: 'asistencia_firebase_url',
  PROFESORES: 'asistencia_profesores',
  CURSOS: 'asistencia_cursos',
  ESTUDIANTES: 'asistencia_estudiantes',
  ASISTENCIAS: 'asistencia_registros',
  SESSION: 'asistencia_sesion_activa'
};

// Estado global en memoria
let dbFirebase = null;
let modoNubeActivo = false;

let estadoLocal = {
  profesores: [],
  cursos: [],
  estudiantes: [],
  asistencias: []
};

let usuarioActual = null;

// ==========================================================================
// INICIALIZACIÓN
// ==========================================================================
document.addEventListener('DOMContentLoaded', async () => {
  inicializarDatosPorDefecto();
  await inicializarConexionNube();
  verificarSesion();
  configurarEventos();
});

function inicializarDatosPorDefecto() {
  const profesoresIniciales = [
    {
      id_profesor: 1,
      nombre_completo: "Carlos Andrés Pérez",
      usuario: "profesor1",
      contrasena: "123456",
      correo: "carlos.perez@institucion.edu.co"
    }
  ];

  const cursosIniciales = [
    { id_curso: 1, nombre_curso: "10-A", jornada: "Mañana", id_profesor: 1 },
    { id_curso: 2, nombre_curso: "11-B", jornada: "Tarde", id_profesor: 1 }
  ];

  const estudiantesIniciales = [
    { id_estudiante: 1, nombre_completo: "Ana María Gómez", documento: "1001234567", id_curso: 1, activo: 1 },
    { id_estudiante: 2, nombre_completo: "Juan Esteban Rojas", documento: "1001234568", id_curso: 1, activo: 1 },
    { id_estudiante: 3, nombre_completo: "Laura Sofía Díaz", documento: "1001234569", id_curso: 1, activo: 1 },
    { id_estudiante: 4, nombre_completo: "Miguel Ángel Torres", documento: "1001234570", id_curso: 2, activo: 1 },
    { id_estudiante: 5, nombre_completo: "Valentina Castro", documento: "1001234571", id_curso: 2, activo: 1 }
  ];

  const hoy = new Date().toISOString().split('T')[0];
  const asistenciasIniciales = [
    { id_asistencia: 1, id_estudiante: 1, id_curso: 1, id_profesor: 1, fecha: hoy, estado: "PRESENTE", observacion: "Llegó puntual" },
    { id_asistencia: 2, id_estudiante: 2, id_curso: 1, id_profesor: 1, fecha: hoy, estado: "TARDE", observacion: "Ingreso 10 min tarde" },
    { id_asistencia: 3, id_estudiante: 3, id_curso: 1, id_profesor: 1, fecha: hoy, estado: "PRESENTE", observacion: "" }
  ];

  if (!localStorage.getItem(DB_KEYS.PROFESORES)) {
    localStorage.setItem(DB_KEYS.PROFESORES, JSON.stringify(profesoresIniciales));
  }
  if (!localStorage.getItem(DB_KEYS.CURSOS)) {
    localStorage.setItem(DB_KEYS.CURSOS, JSON.stringify(cursosIniciales));
  }
  if (!localStorage.getItem(DB_KEYS.ESTUDIANTES)) {
    localStorage.setItem(DB_KEYS.ESTUDIANTES, JSON.stringify(estudiantesIniciales));
  }
  if (!localStorage.getItem(DB_KEYS.ASISTENCIAS)) {
    localStorage.setItem(DB_KEYS.ASISTENCIAS, JSON.stringify(asistenciasIniciales));
  }

  // Cargar estado local
  estadoLocal.profesores = JSON.parse(localStorage.getItem(DB_KEYS.PROFESORES));
  estadoLocal.cursos = JSON.parse(localStorage.getItem(DB_KEYS.CURSOS));
  estadoLocal.estudiantes = JSON.parse(localStorage.getItem(DB_KEYS.ESTUDIANTES));
  estadoLocal.asistencias = JSON.parse(localStorage.getItem(DB_KEYS.ASISTENCIAS));
}

// ==========================================================================
// CONEXIÓN Y SINCRONIZACIÓN EN LA NUBE (FIREBASE)
// ==========================================================================
async function inicializarConexionNube() {
  const urlGuardada = localStorage.getItem(DB_KEYS.FIREBASE_URL) || FIREBASE_DB_URL_POR_DEFECTO;

  if (urlGuardada && window.firebase) {
    try {
      const cleanUrl = urlGuardada.trim();
      const config = { databaseURL: cleanUrl };

      if (!firebase.apps.length) {
        firebase.initializeApp(config);
      }
      dbFirebase = firebase.database();
      modoNubeActivo = true;

      actualizarIndicadorNube(true);

      // Escuchar cambios en tiempo real desde la nube
      dbFirebase.ref('asistencia_db').on('value', (snapshot) => {
        const data = snapshot.val();
        if (data) {
          if (data.profesores) estadoLocal.profesores = Object.values(data.profesores);
          if (data.cursos) estadoLocal.cursos = Object.values(data.cursos);
          if (data.estudiantes) estadoLocal.estudiantes = Object.values(data.estudiantes);
          if (data.asistencias) estadoLocal.asistencias = Object.values(data.asistencias);

          // Guardar copia de respaldo en LocalStorage
          localStorage.setItem(DB_KEYS.PROFESORES, JSON.stringify(estadoLocal.profesores));
          localStorage.setItem(DB_KEYS.CURSOS, JSON.stringify(estadoLocal.cursos));
          localStorage.setItem(DB_KEYS.ESTUDIANTES, JSON.stringify(estadoLocal.estudiantes));
          localStorage.setItem(DB_KEYS.ASISTENCIAS, JSON.stringify(estadoLocal.asistencias));

          // Actualizar vista activa en tiempo real
          actualizarModuloActivo();
        } else {
          // Si la base de datos en la nube está vacía, sincronizar los datos locales iniciales
          sincronizarTodoANube();
        }
      });

      console.log("[Nube] Sincronización en tiempo real activa con Firebase.");
    } catch (e) {
      console.warn("[Nube] No se pudo conectar a Firebase:", e);
      modoNubeActivo = false;
      actualizarIndicadorNube(false);
    }
  } else {
    modoNubeActivo = false;
    actualizarIndicadorNube(false);
  }
}

function actualizarIndicadorNube(conectado) {
  const icon = document.getElementById('cloudStatusIcon');
  const text = document.getElementById('cloudStatusText');
  if (!icon || !text) return;

  if (conectado) {
    icon.textContent = '🟢';
    text.textContent = 'Nube Sincronizada';
  } else {
    icon.textContent = '☁️';
    text.textContent = 'Configurar Nube';
  }
}

function sincronizarTodoANube() {
  if (modoNubeActivo && dbFirebase) {
    dbFirebase.ref('asistencia_db').set({
      profesores: estadoLocal.profesores,
      cursos: estadoLocal.cursos,
      estudiantes: estadoLocal.estudiantes,
      asistencias: estadoLocal.asistencias
    });
  }
}

// Helpers de guardado adaptativos (Nube o Local)
function guardarCursos(cursos) {
  estadoLocal.cursos = cursos;
  localStorage.setItem(DB_KEYS.CURSOS, JSON.stringify(cursos));
  if (modoNubeActivo && dbFirebase) {
    dbFirebase.ref('asistencia_db/cursos').set(cursos);
  }
}

function guardarEstudiantes(estudiantes) {
  estadoLocal.estudiantes = estudiantes;
  localStorage.setItem(DB_KEYS.ESTUDIANTES, JSON.stringify(estudiantes));
  if (modoNubeActivo && dbFirebase) {
    dbFirebase.ref('asistencia_db/estudiantes').set(estudiantes);
  }
}

function guardarAsistencias(asistencias) {
  estadoLocal.asistencias = asistencias;
  localStorage.setItem(DB_KEYS.ASISTENCIAS, JSON.stringify(asistencias));
  if (modoNubeActivo && dbFirebase) {
    dbFirebase.ref('asistencia_db/asistencias').set(asistencias);
  }
}

// ==========================================================================
// SESIÓN Y VISTAS
// ==========================================================================
function verificarSesion() {
  const sesionGuardada = localStorage.getItem(DB_KEYS.SESSION);
  if (sesionGuardada) {
    usuarioActual = JSON.parse(sesionGuardada);
    mostrarDashboard();
  } else {
    mostrarLogin();
  }
}

function mostrarLogin() {
  document.getElementById('loginSection').style.display = 'flex';
  document.getElementById('dashboardSection').style.display = 'none';
  document.getElementById('loginUsuario').value = '';
  document.getElementById('loginContrasena').value = '';
  document.getElementById('loginError').textContent = '';
}

function mostrarDashboard() {
  document.getElementById('loginSection').style.display = 'none';
  document.getElementById('dashboardSection').style.display = 'flex';

  document.getElementById('sidebarNombreProfesor').textContent = usuarioActual.nombre_completo;
  document.getElementById('sidebarAvatar').textContent = usuarioActual.nombre_completo.charAt(0).toUpperCase();

  const hoyStr = new Date().toISOString().split('T')[0];
  document.getElementById('fechaAsistencia').value = hoyStr;
  document.getElementById('reporteFechaHasta').value = hoyStr;

  const mesAtras = new Date();
  mesAtras.setMonth(mesAtras.getMonth() - 1);
  document.getElementById('reporteFechaDesde').value = mesAtras.toISOString().split('T')[0];

  cambiarModulo('asistencia');
}

let moduloActual = 'asistencia';

function cambiarModulo(modulo) {
  moduloActual = modulo;
  document.querySelectorAll('.module-panel').forEach(p => p.style.display = 'none');
  document.querySelectorAll('.nav-item').forEach(btn => btn.classList.remove('active'));

  if (modulo === 'asistencia') {
    document.getElementById('panelAsistencia').style.display = 'block';
    document.getElementById('navAsistencia').classList.add('active');
    cargarCursosAsistencia();
  } else if (modulo === 'estudiantes') {
    document.getElementById('panelEstudiantes').style.display = 'block';
    document.getElementById('navEstudiantes').classList.add('active');
    cargarCursosEstudiantes();
  } else if (modulo === 'reportes') {
    document.getElementById('panelReportes').style.display = 'block';
    document.getElementById('navReportes').classList.add('active');
    cargarCursosReportes();
    consultarReportes();
  }
}

function actualizarModuloActivo() {
  if (moduloActual === 'asistencia') {
    cargarCursosAsistencia();
  } else if (moduloActual === 'estudiantes') {
    cargarCursosEstudiantes();
  } else if (moduloActual === 'reportes') {
    cargarCursosReportes();
    consultarReportes();
  }
}

// ==========================================================================
// EVENTOS Y CONFIGURACIÓN
// ==========================================================================
function configurarEventos() {
  document.getElementById('loginForm').addEventListener('submit', (e) => {
    e.preventDefault();
    ejecutarLogin();
  });

  document.getElementById('btnDemoFill').addEventListener('click', () => {
    document.getElementById('loginUsuario').value = 'profesor1';
    document.getElementById('loginContrasena').value = '123456';
    document.getElementById('loginError').textContent = '';
  });

  document.getElementById('btnLogout').addEventListener('click', () => {
    if (confirm('¿Desea cerrar la sesión actual?')) {
      localStorage.removeItem(DB_KEYS.SESSION);
      usuarioActual = null;
      mostrarLogin();
      showToast('Sesión cerrada correctamente');
    }
  });

  document.getElementById('navAsistencia').addEventListener('click', () => cambiarModulo('asistencia'));
  document.getElementById('navEstudiantes').addEventListener('click', () => cambiarModulo('estudiantes'));
  document.getElementById('navReportes').addEventListener('click', () => cambiarModulo('reportes'));

  document.getElementById('comboCursoAsistencia').addEventListener('change', cargarEstudiantesAsistencia);
  document.getElementById('fechaAsistencia').addEventListener('change', cargarEstudiantesAsistencia);
  document.getElementById('btnMarcarTodosPresentes').addEventListener('click', marcarTodosPresentes);
  document.getElementById('btnGuardarAsistencia').addEventListener('click', guardarAsistencia);

  document.getElementById('comboCursoEstudiantes').addEventListener('change', cargarTablaEstudiantes);
  document.getElementById('btnModalNuevoCurso').addEventListener('click', () => abrirModal('modalNuevoCurso'));
  document.getElementById('btnModalNuevoEstudiante').addEventListener('click', () => abrirModal('modalNuevoEstudiante'));
  document.getElementById('formNuevoCurso').addEventListener('submit', guardarNuevoCurso);
  document.getElementById('formNuevoEstudiante').addEventListener('submit', guardarNuevoEstudiante);

  document.getElementById('btnConsultarReporte').addEventListener('click', consultarReportes);
  document.getElementById('btnExportarCSV').addEventListener('click', exportarReporteCSV);

  // Modal Nube
  document.getElementById('btnAbrirConfigNube').addEventListener('click', () => {
    const url = localStorage.getItem(DB_KEYS.FIREBASE_URL) || '';
    document.getElementById('inputDatabaseURL').value = url;
    abrirModal('modalConfigNube');
  });

  document.getElementById('formConfigNube').addEventListener('submit', async (e) => {
    e.preventDefault();
    const url = document.getElementById('inputDatabaseURL').value.trim();
    if (url) {
      localStorage.setItem(DB_KEYS.FIREBASE_URL, url);
      cerrarModales();
      showToast('Conectando a la base de datos en la nube...');
      location.reload();
    }
  });

  document.getElementById('btnDesconectarNube').addEventListener('click', () => {
    localStorage.removeItem(DB_KEYS.FIREBASE_URL);
    cerrarModales();
    showToast('Usando almacenamiento local (LocalStorage)');
    setTimeout(() => location.reload(), 800);
  });

  document.querySelectorAll('.btn-close-modal').forEach(btn => {
    btn.addEventListener('click', () => cerrarModales());
  });
}

// ==========================================================================
// AUTENTICACIÓN
// ==========================================================================
function ejecutarLogin() {
  const usuario = document.getElementById('loginUsuario').value.trim();
  const pass = document.getElementById('loginContrasena').value;
  const errorLbl = document.getElementById('loginError');

  if (!usuario || !pass) {
    errorLbl.textContent = 'Por favor, ingresa tu usuario y contraseña.';
    return;
  }

  const profesor = estadoLocal.profesores.find(p => p.usuario === usuario && p.contrasena === pass);

  if (profesor) {
    usuarioActual = profesor;
    localStorage.setItem(DB_KEYS.SESSION, JSON.stringify(profesor));
    mostrarDashboard();
    showToast('Bienvenido, ' + profesor.nombre_completo);
  } else {
    errorLbl.textContent = 'Usuario o contraseña incorrectos.';
  }
}

// ==========================================================================
// MÓDULO 1: TOMAR ASISTENCIA
// ==========================================================================
function cargarCursosAsistencia() {
  const combo = document.getElementById('comboCursoAsistencia');
  const valorActual = combo.value;
  combo.innerHTML = '';

  const cursos = estadoLocal.cursos.filter(c => c.id_profesor === usuarioActual.id_profesor);
  if (cursos.length === 0) {
    combo.innerHTML = '<option value="">Sin cursos asignados</option>';
    document.getElementById('tbodyAsistencia').innerHTML = '<tr><td colspan="4">No tienes cursos creados. Crea uno en Gestión de Estudiantes.</td></tr>';
    actualizarMetricasAsistencia(0, 0, 0, 0);
    return;
  }

  cursos.forEach(c => {
    const opt = document.createElement('option');
    opt.value = c.id_curso;
    opt.textContent = `${c.nombre_curso} (${c.jornada})`;
    combo.appendChild(opt);
  });

  if (valorActual && cursos.some(c => c.id_curso == valorActual)) {
    combo.value = valorActual;
  }

  cargarEstudiantesAsistencia();
}

function cargarEstudiantesAsistencia() {
  const idCurso = parseInt(document.getElementById('comboCursoAsistencia').value);
  const fecha = document.getElementById('fechaAsistencia').value;
  const tbody = document.getElementById('tbodyAsistencia');
  tbody.innerHTML = '';

  if (!idCurso || !fecha) return;

  const estudiantes = estadoLocal.estudiantes.filter(e => e.id_curso === idCurso && e.activo === 1);
  const asistenciasFecha = estadoLocal.asistencias.filter(a => a.id_curso === idCurso && a.fecha === fecha);

  if (estudiantes.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" style="padding: 24px; color: var(--text-secondary);">No hay estudiantes matriculados en este curso.</td></tr>';
    actualizarMetricasAsistencia(0, 0, 0, 0);
    document.getElementById('lblEstadoAsistencia').textContent = '0 estudiantes en este curso';
    return;
  }

  estudiantes.forEach(est => {
    const prev = asistenciasFecha.find(a => a.id_estudiante === est.id_estudiante);
    const estado = prev ? prev.estado : 'PRESENTE';
    const obs = prev ? prev.observacion : '';

    const tr = document.createElement('tr');
    tr.dataset.idEstudiante = est.id_estudiante;

    tr.innerHTML = `
      <td style="font-weight: 600;">${est.nombre_completo}</td>
      <td><code>${est.documento}</code></td>
      <td>
        <select class="form-select select-estado" style="max-width: 160px; margin: 0 auto;">
          <option value="PRESENTE" ${estado === 'PRESENTE' ? 'selected' : ''}>PRESENTE</option>
          <option value="AUSENTE" ${estado === 'AUSENTE' ? 'selected' : ''}>AUSENTE</option>
          <option value="TARDE" ${estado === 'TARDE' ? 'selected' : ''}>TARDE</option>
          <option value="EXCUSA" ${estado === 'EXCUSA' ? 'selected' : ''}>EXCUSA</option>
        </select>
      </td>
      <td>
        <input type="text" class="form-input input-obs" value="${obs}" placeholder="Observaciones..." style="max-width: 280px; margin: 0 auto;" />
      </td>
    `;

    tbody.appendChild(tr);
  });

  tbody.querySelectorAll('.select-estado').forEach(sel => {
    sel.addEventListener('change', recalcularMetricasAsistenciaEnVivo);
  });

  recalcularMetricasAsistenciaEnVivo();
  document.getElementById('lblEstadoAsistencia').textContent = `${estudiantes.length} estudiante(s) cargados para la fecha`;
}

function recalcularMetricasAsistenciaEnVivo() {
  const selects = document.querySelectorAll('#tbodyAsistencia .select-estado');
  let total = selects.length;
  let presentes = 0, ausentes = 0, tardeExcusas = 0;

  selects.forEach(sel => {
    const val = sel.value;
    if (val === 'PRESENTE') presentes++;
    else if (val === 'AUSENTE') ausentes++;
    else tardeExcusas++;
  });

  actualizarMetricasAsistencia(total, presentes, ausentes, tardeExcusas);
}

function actualizarMetricasAsistencia(total, presentes, ausentes, otros) {
  document.getElementById('metricTotalEstudiantes').textContent = total;
  document.getElementById('metricPresentes').textContent = presentes;
  document.getElementById('metricAusentes').textContent = ausentes;
  document.getElementById('metricTardes').textContent = otros;
}

function marcarTodosPresentes() {
  document.querySelectorAll('#tbodyAsistencia .select-estado').forEach(sel => {
    sel.value = 'PRESENTE';
  });
  recalcularMetricasAsistenciaEnVivo();
  showToast('Todos los estudiantes marcados como PRESENTES');
}

function guardarAsistencia() {
  const idCurso = parseInt(document.getElementById('comboCursoAsistencia').value);
  const fecha = document.getElementById('fechaAsistencia').value;
  if (!idCurso || !fecha) return;

  const rows = document.querySelectorAll('#tbodyAsistencia tr');
  if (rows.length === 0) {
    showToast('No hay estudiantes para registrar', 'error');
    return;
  }

  let asistencias = [...estadoLocal.asistencias];
  asistencias = asistencias.filter(a => !(a.id_curso === idCurso && a.fecha === fecha));

  let nextId = asistencias.length > 0 ? Math.max(...asistencias.map(a => a.id_asistencia || 0)) + 1 : 1;

  rows.forEach(tr => {
    const idEstudiante = parseInt(tr.dataset.idEstudiante);
    if (!idEstudiante) return;

    const estado = tr.querySelector('.select-estado').value;
    const observacion = tr.querySelector('.input-obs').value.trim();

    asistencias.push({
      id_asistencia: nextId++,
      id_estudiante: idEstudiante,
      id_curso: idCurso,
      id_profesor: usuarioActual.id_profesor,
      fecha: fecha,
      estado: estado,
      observacion: observacion
    });
  });

  guardarAsistencias(asistencias);
  document.getElementById('lblEstadoAsistencia').textContent = `Asistencia guardada correctamente (${formatearFecha(fecha)})`;
  showToast('¡Asistencia sincronizada y guardada con éxito!');
}

// ==========================================================================
// MÓDULO 2: GESTIÓN DE ESTUDIANTES
// ==========================================================================
function cargarCursosEstudiantes() {
  const combo = document.getElementById('comboCursoEstudiantes');
  const valorActual = combo.value;
  combo.innerHTML = '';

  const cursos = estadoLocal.cursos.filter(c => c.id_profesor === usuarioActual.id_profesor);
  if (cursos.length === 0) {
    combo.innerHTML = '<option value="">Sin cursos</option>';
    document.getElementById('tbodyEstudiantes').innerHTML = '<tr><td colspan="4">Crea un nuevo curso para comenzar.</td></tr>';
    document.getElementById('lblContadorEstudiantes').textContent = '0 estudiantes';
    return;
  }

  cursos.forEach(c => {
    const opt = document.createElement('option');
    opt.value = c.id_curso;
    opt.textContent = `${c.nombre_curso} (${c.jornada})`;
    combo.appendChild(opt);
  });

  if (valorActual && cursos.some(c => c.id_curso == valorActual)) {
    combo.value = valorActual;
  }

  cargarTablaEstudiantes();
}

function cargarTablaEstudiantes() {
  const idCurso = parseInt(document.getElementById('comboCursoEstudiantes').value);
  const tbody = document.getElementById('tbodyEstudiantes');
  tbody.innerHTML = '';

  if (!idCurso) return;

  const estudiantes = estadoLocal.estudiantes.filter(e => e.id_curso === idCurso && e.activo === 1);
  const cursoActual = estadoLocal.cursos.find(c => c.id_curso === idCurso);

  if (estudiantes.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" style="padding: 24px; color: var(--text-secondary);">No hay estudiantes matriculados en este curso.</td></tr>';
    document.getElementById('lblContadorEstudiantes').textContent = '0 estudiantes matriculados';
    return;
  }

  estudiantes.forEach((est, index) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${index + 1}</td>
      <td style="font-weight: 600;">${est.nombre_completo}</td>
      <td><code>${est.documento}</code></td>
      <td>
        <button class="btn-danger" style="padding: 5px 12px; font-size: 12px;" onclick="darDeBajaEstudiante(${est.id_estudiante}, '${est.nombre_completo}')">
          Dar de Baja
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });

  document.getElementById('lblContadorEstudiantes').textContent = `${estudiantes.length} estudiante(s) activo(s) en ${cursoActual ? cursoActual.nombre_curso : ''}`;
}

function guardarNuevoCurso(e) {
  e.preventDefault();
  const nombre = document.getElementById('nuevoCursoNombre').value.trim();
  const jornada = document.getElementById('nuevoCursoJornada').value;

  if (!nombre) {
    showToast('El nombre del curso es obligatorio', 'error');
    return;
  }

  const cursos = [...estadoLocal.cursos];
  const nextId = cursos.length > 0 ? Math.max(...cursos.map(c => c.id_curso || 0)) + 1 : 1;

  cursos.push({
    id_curso: nextId,
    nombre_curso: nombre,
    jornada: jornada,
    id_profesor: usuarioActual.id_profesor
  });

  guardarCursos(cursos);
  cerrarModales();
  document.getElementById('formNuevoCurso').reset();
  cargarCursosEstudiantes();
  document.getElementById('comboCursoEstudiantes').value = nextId;
  cargarTablaEstudiantes();
  showToast(`Curso "${nombre}" creado exitosamente`);
}

function guardarNuevoEstudiante(e) {
  e.preventDefault();
  const idCurso = parseInt(document.getElementById('comboCursoEstudiantes').value);
  if (!idCurso) {
    showToast('Seleccione un curso primero', 'error');
    return;
  }

  const nombre = document.getElementById('nuevoEstudianteNombre').value.trim();
  const documento = document.getElementById('nuevoEstudianteDoc').value.trim();

  if (!nombre || !documento) {
    showToast('Nombre y documento son obligatorios', 'error');
    return;
  }

  const estudiantes = [...estadoLocal.estudiantes];
  if (estudiantes.some(est => est.documento === documento && est.activo === 1)) {
    showToast('Ya existe un estudiante con ese documento', 'error');
    return;
  }

  const nextId = estudiantes.length > 0 ? Math.max(...estudiantes.map(est => est.id_estudiante || 0)) + 1 : 1;

  estudiantes.push({
    id_estudiante: nextId,
    nombre_completo: nombre,
    documento: documento,
    id_curso: idCurso,
    activo: 1
  });

  guardarEstudiantes(estudiantes);
  cerrarModales();
  document.getElementById('formNuevoEstudiante').reset();
  cargarTablaEstudiantes();
  showToast(`Estudiante "${nombre}" matriculado correctamente`);
}

function darDeBajaEstudiante(idEstudiante, nombre) {
  if (confirm(`¿Está seguro de dar de baja a "${nombre}"?\n(Su historial de asistencia se conservará en el sistema)`)) {
    let estudiantes = estadoLocal.estudiantes.map(e => {
      if (e.id_estudiante === idEstudiante) {
        return { ...e, activo: 0 };
      }
      return e;
    });
    guardarEstudiantes(estudiantes);
    cargarTablaEstudiantes();
    showToast(`Estudiante "${nombre}" dado de baja`);
  }
}

// ==========================================================================
// MÓDULO 3: REPORTES Y CONSULTAS
// ==========================================================================
function cargarCursosReportes() {
  const combo = document.getElementById('comboCursoReportes');
  const valorActual = combo.value;
  combo.innerHTML = '';

  const cursos = estadoLocal.cursos.filter(c => c.id_profesor === usuarioActual.id_profesor);
  cursos.forEach(c => {
    const opt = document.createElement('option');
    opt.value = c.id_curso;
    opt.textContent = `${c.nombre_curso} (${c.jornada})`;
    combo.appendChild(opt);
  });

  if (valorActual && cursos.some(c => c.id_curso == valorActual)) {
    combo.value = valorActual;
  }
}

function consultarReportes() {
  const idCurso = parseInt(document.getElementById('comboCursoReportes').value);
  const desde = document.getElementById('reporteFechaDesde').value;
  const hasta = document.getElementById('reporteFechaHasta').value;
  const tbody = document.getElementById('tbodyReportes');
  tbody.innerHTML = '';

  if (!idCurso || !desde || !hasta) return;

  if (desde > hasta) {
    showToast('La fecha "Desde" no puede ser mayor a "Hasta"', 'error');
    return;
  }

  const asistencias = estadoLocal.asistencias.filter(a =>
    a.id_curso === idCurso && a.fecha >= desde && a.fecha <= hasta
  );

  const mapaEstudiantes = {};
  estadoLocal.estudiantes.forEach(e => mapaEstudiantes[e.id_estudiante] = e.nombre_completo);

  let conteo = { PRESENTE: 0, AUSENTE: 0, TARDE: 0, EXCUSA: 0 };

  if (asistencias.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" style="padding: 24px; color: var(--text-secondary);">No se encontraron registros de asistencia en el rango de fechas seleccionado.</td></tr>';
    actualizarMetricasReporte(0, 0, 0, 0);
    document.getElementById('lblResumenReporte').textContent = 'Total registros: 0 | Asistencia promedio: 0.0%';
    return;
  }

  asistencias.sort((a, b) => b.fecha.localeCompare(a.fecha));

  asistencias.forEach(a => {
    conteo[a.estado] = (conteo[a.estado] || 0) + 1;
    const nombreEstudiante = mapaEstudiantes[a.id_estudiante] || 'Estudiante #' + a.id_estudiante;

    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${formatearFecha(a.fecha)}</td>
      <td style="font-weight: 600;">${nombreEstudiante}</td>
      <td><span class="badge badge-${a.estado.toLowerCase()}">${a.estado}</span></td>
      <td>${a.observacion || '-'}</td>
    `;
    tbody.appendChild(tr);
  });

  const total = asistencias.length;
  const presentes = conteo.PRESENTE || 0;
  const ausentes = conteo.AUSENTE || 0;
  const otros = (conteo.TARDE || 0) + (conteo.EXCUSA || 0);
  const porcentaje = total > 0 ? ((presentes / total) * 100).toFixed(1) : 0;

  actualizarMetricasReporte(total, presentes, ausentes, otros);

  const cursoObj = estadoLocal.cursos.find(c => c.id_curso === idCurso);
  document.getElementById('lblResumenReporte').textContent =
    `Total registros: ${total} | Asistencia promedio: ${porcentaje}% | Curso: ${cursoObj ? cursoObj.nombre_curso : ''}`;
}

function actualizarMetricasReporte(total, presentes, ausentes, otros) {
  document.getElementById('reporteTotalRegistros').textContent = total;
  document.getElementById('reporteTotalPresentes').textContent = presentes;
  document.getElementById('reporteTotalAusentes').textContent = ausentes;
  document.getElementById('reporteTotalTardes').textContent = otros;
}

function exportarReporteCSV() {
  const idCurso = parseInt(document.getElementById('comboCursoReportes').value);
  const desde = document.getElementById('reporteFechaDesde').value;
  const hasta = document.getElementById('reporteFechaHasta').value;

  const asistencias = estadoLocal.asistencias.filter(a =>
    a.id_curso === idCurso && a.fecha >= desde && a.fecha <= hasta
  );

  if (asistencias.length === 0) {
    showToast('No hay datos para exportar', 'error');
    return;
  }

  const mapaEstudiantes = {};
  estadoLocal.estudiantes.forEach(e => mapaEstudiantes[e.id_estudiante] = e);

  let csvContent = '\uFEFF';
  csvContent += 'Fecha,Estudiante,Documento,Estado,Observaciones\n';

  asistencias.forEach(a => {
    const est = mapaEstudiantes[a.id_estudiante] || { nombre_completo: 'Desconocido', documento: '' };
    const fila = [
      `"${a.fecha}"`,
      `"${est.nombre_completo}"`,
      `"${est.documento}"`,
      `"${a.estado}"`,
      `"${(a.observacion || '').replace(/"/g, '""')}"`
    ];
    csvContent += fila.join(',') + '\n';
  });

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.setAttribute('href', url);
  link.setAttribute('download', `Reporte_Asistencia_${desde}_al_${hasta}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  showToast('Reporte CSV descargado con éxito');
}

// ==========================================================================
// UTILIDADES Y MODALES
// ==========================================================================
function abrirModal(idModal) {
  document.getElementById(idModal).classList.add('active');
}

function cerrarModales() {
  document.querySelectorAll('.modal-backdrop').forEach(m => m.classList.remove('active'));
}

function formatearFecha(fechaISO) {
  if (!fechaISO) return '';
  const partes = fechaISO.split('-');
  if (partes.length === 3) {
    return `${partes[2]}/${partes[1]}/${partes[0]}`;
  }
  return fechaISO;
}

function showToast(mensaje, tipo = 'success') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast ${tipo}`;
  toast.textContent = mensaje;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}
