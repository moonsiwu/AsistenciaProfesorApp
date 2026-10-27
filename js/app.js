/**
 * ==========================================================================
 * SISTEMA DE CONTROL DE ASISTENCIA - LÓGICA DE APLICACIÓN WEB (SPA)
 * Con soporte para Base de Datos en la Nube (Firebase) y Sincronización en Tiempo Real
 * ==========================================================================
 */

// --- Configuración de Base de Datos ---
const FIREBASE_DB_URL_POR_DEFECTO = "";

const DB_KEYS = {
  FIREBASE_URL: 'asistencia_firebase_url',
  PROFESORES: 'asistencia_profesores',
  CURSOS: 'asistencia_cursos',
  ESTUDIANTES: 'asistencia_estudiantes',
  ASISTENCIAS: 'asistencia_registros',
  SESSION: 'asistencia_sesion_activa',
  NOTAS: 'asistencia_notas'
};

// Estado global en memoria
let dbFirebase = null;
let modoNubeActivo = false;

let estadoLocal = {
  profesores: [],
  cursos: [],
  estudiantes: [],
  asistencias: [],
  notas: []
};

let usuarioActual = null;

// ==========================================================================
// INICIALIZACIÓN
// ==========================================================================
document.addEventListener('DOMContentLoaded', async () => {
  inicializarDatosPorDefecto();
  verificarSesion();
  configurarEventos();
  setInterval(actualizarFechaHora, 1000);
  await inicializarConexionNube();
});

function inicializarDatosPorDefecto() {
  const profesoresIniciales = [
    {
      id_profesor: 1,
      nombre_completo: "Carlos Andrés Pérez",
      usuario: "profesor1",
      contrasena: "123456",
      correo: "carlos.perez@institucion.edu.co",
      telefono: "+57 300 123 4567",
      departamento: "Ciencias y Tecnología",
      especialidad: "Matemáticas y Física",
      titulo: "Licenciatura en Matemáticas",
      bio: "Docente con más de 10 años de experiencia en educación secundaria y media académica.",
      horario: "Lunes a Viernes, 7:00 AM - 3:00 PM"
    },
    {
      id_profesor: 2,
      nombre_completo: "María Fernanda Gómez",
      usuario: "profesor2",
      contrasena: "123456",
      correo: "maria.gomez@institucion.edu.co",
      telefono: "+57 310 987 6543",
      departamento: "Humanidades e Idiomas",
      especialidad: "Lengua Castellana e Inglés",
      titulo: "Magíster en Lingüística y Educación",
      bio: "Docente titular del área de Humanidades y directora de grupo de los grados 9-A y 10-B.",
      horario: "Lunes a Viernes, 7:00 AM - 3:00 PM"
    }
  ];

  const cursosIniciales = [
    { id_curso: 1, nombre_curso: "10-A", jornada: "Mañana", id_profesor: 1 },
    { id_curso: 2, nombre_curso: "11-B", jornada: "Tarde", id_profesor: 1 },
    { id_curso: 3, nombre_curso: "9-A", jornada: "Mañana", id_profesor: 2 },
    { id_curso: 4, nombre_curso: "10-B", jornada: "Tarde", id_profesor: 2 }
  ];

  const estudiantesIniciales = [
    { id_estudiante: 1, nombre_completo: "Ana María Gómez", documento: "1001234567", id_curso: 1, activo: 1 },
    { id_estudiante: 2, nombre_completo: "Juan Esteban Rojas", documento: "1001234568", id_curso: 1, activo: 1 },
    { id_estudiante: 3, nombre_completo: "Laura Sofía Díaz", documento: "1001234569", id_curso: 1, activo: 1 },
    { id_estudiante: 4, nombre_completo: "Miguel Ángel Torres", documento: "1001234570", id_curso: 2, activo: 1 },
    { id_estudiante: 5, nombre_completo: "Valentina Castro", documento: "1001234571", id_curso: 2, activo: 1 },
    { id_estudiante: 6, nombre_completo: "Santiago Ramírez", documento: "1001234572", id_curso: 3, activo: 1 },
    { id_estudiante: 7, nombre_completo: "Camila Andrea Mora", documento: "1001234573", id_curso: 3, activo: 1 },
    { id_estudiante: 8, nombre_completo: "Daniel Alejandro Ríos", documento: "1001234574", id_curso: 4, activo: 1 }
  ];

  const hoy = new Date().toISOString().split('T')[0];
  const asistenciasIniciales = [
    { id_asistencia: 1, id_estudiante: 1, id_curso: 1, id_profesor: 1, fecha: hoy, estado: "PRESENTE", observacion: "Llegó puntual" },
    { id_asistencia: 2, id_estudiante: 2, id_curso: 1, id_profesor: 1, fecha: hoy, estado: "TARDE", observacion: "Ingreso 10 min tarde" },
    { id_asistencia: 3, id_estudiante: 3, id_curso: 1, id_profesor: 1, fecha: hoy, estado: "PRESENTE", observacion: "" },
    { id_asistencia: 4, id_estudiante: 6, id_curso: 3, id_profesor: 2, fecha: hoy, estado: "PRESENTE", observacion: "Participación activa" },
    { id_asistencia: 5, id_estudiante: 7, id_curso: 3, id_profesor: 2, fecha: hoy, estado: "PRESENTE", observacion: "" }
  ];

  const notasIniciales = [
    { id_nota: 1, id_estudiante: 1, id_curso: 1, periodo: 1, nota1: 4.5, nota2: 3.8, nota3: 4.2, nota4: 4.0, promedio: 4.13, year: 2026 },
    { id_nota: 2, id_estudiante: 2, id_curso: 1, periodo: 1, nota1: 3.0, nota2: 2.8, nota3: 3.5, nota4: 3.2, promedio: 3.13, year: 2026 },
    { id_nota: 3, id_estudiante: 3, id_curso: 1, periodo: 1, nota1: 4.8, nota2: 4.9, nota3: 5.0, nota4: 4.7, promedio: 4.85, year: 2026 },
    { id_nota: 4, id_estudiante: 4, id_curso: 2, periodo: 1, nota1: 3.5, nota2: 3.0, nota3: 3.8, nota4: 3.3, promedio: 3.40, year: 2026 },
    { id_nota: 5, id_estudiante: 5, id_curso: 2, periodo: 1, nota1: 2.5, nota2: 2.0, nota3: 2.8, nota4: 2.3, promedio: 2.40, year: 2026 },
    { id_nota: 6, id_estudiante: 6, id_curso: 3, periodo: 1, nota1: 4.4, nota2: 4.6, nota3: 4.5, nota4: 4.7, promedio: 4.55, year: 2026 },
    { id_nota: 7, id_estudiante: 7, id_curso: 3, periodo: 1, nota1: 3.9, nota2: 4.1, nota3: 4.0, nota4: 4.2, promedio: 4.05, year: 2026 },
    { id_nota: 8, id_estudiante: 8, id_curso: 4, periodo: 1, nota1: 3.6, nota2: 3.8, nota3: 3.7, nota4: 3.9, promedio: 3.75, year: 2026 }
  ];

  if (!localStorage.getItem(DB_KEYS.PROFESORES)) localStorage.setItem(DB_KEYS.PROFESORES, JSON.stringify(profesoresIniciales));
  if (!localStorage.getItem(DB_KEYS.CURSOS)) localStorage.setItem(DB_KEYS.CURSOS, JSON.stringify(cursosIniciales));
  if (!localStorage.getItem(DB_KEYS.ESTUDIANTES)) localStorage.setItem(DB_KEYS.ESTUDIANTES, JSON.stringify(estudiantesIniciales));
  if (!localStorage.getItem(DB_KEYS.ASISTENCIAS)) localStorage.setItem(DB_KEYS.ASISTENCIAS, JSON.stringify(asistenciasIniciales));
  if (!localStorage.getItem(DB_KEYS.NOTAS)) localStorage.setItem(DB_KEYS.NOTAS, JSON.stringify(notasIniciales));

  // Cargar estado local
  estadoLocal.profesores = JSON.parse(localStorage.getItem(DB_KEYS.PROFESORES));
  estadoLocal.cursos = JSON.parse(localStorage.getItem(DB_KEYS.CURSOS));
  estadoLocal.estudiantes = JSON.parse(localStorage.getItem(DB_KEYS.ESTUDIANTES));
  estadoLocal.asistencias = JSON.parse(localStorage.getItem(DB_KEYS.ASISTENCIAS));
  estadoLocal.notas = JSON.parse(localStorage.getItem(DB_KEYS.NOTAS));

  asegurarCuentasDemostracion();
}

function asegurarCuentasDemostracion() {
  if (!Array.isArray(estadoLocal.profesores)) estadoLocal.profesores = [];
  if (!Array.isArray(estadoLocal.cursos)) estadoLocal.cursos = [];
  if (!Array.isArray(estadoLocal.estudiantes)) estadoLocal.estudiantes = [];
  if (!Array.isArray(estadoLocal.notas)) estadoLocal.notas = [];

  const prof1Idx = estadoLocal.profesores.findIndex(p => p && (p.id_profesor === 1 || (p.usuario && p.usuario.toLowerCase() === 'profesor1')));
  const datosProf1 = {
    id_profesor: 1,
    nombre_completo: "Carlos Andrés Pérez",
    usuario: "profesor1",
    contrasena: "123456",
    correo: "carlos.perez@institucion.edu.co",
    telefono: "+57 300 123 4567",
    departamento: "Ciencias y Tecnología",
    especialidad: "Matemáticas y Física",
    titulo: "Licenciatura en Matemáticas",
    bio: "Docente con más de 10 años de experiencia en educación secundaria y media académica.",
    horario: "Lunes a Viernes, 7:00 AM - 3:00 PM"
  };
  if (prof1Idx === -1) {
    estadoLocal.profesores.push(datosProf1);
  } else {
    estadoLocal.profesores[prof1Idx] = { ...datosProf1, ...estadoLocal.profesores[prof1Idx], usuario: "profesor1", contrasena: "123456" };
  }

  const prof2Idx = estadoLocal.profesores.findIndex(p => p && (p.id_profesor === 2 || (p.usuario && p.usuario.toLowerCase() === 'profesor2')));
  const datosProf2 = {
    id_profesor: 2,
    nombre_completo: "María Fernanda Gómez",
    usuario: "profesor2",
    contrasena: "123456",
    correo: "maria.gomez@institucion.edu.co",
    telefono: "+57 310 987 6543",
    departamento: "Humanidades e Idiomas",
    especialidad: "Lengua Castellana e Inglés",
    titulo: "Magíster en Lingüística y Educación",
    bio: "Docente titular del área de Humanidades y directora de grupo de los grados 9-A y 10-B.",
    horario: "Lunes a Viernes, 7:00 AM - 3:00 PM"
  };
  if (prof2Idx === -1) {
    estadoLocal.profesores.push(datosProf2);
  } else {
    estadoLocal.profesores[prof2Idx] = { ...datosProf2, ...estadoLocal.profesores[prof2Idx], usuario: "profesor2", contrasena: "123456" };
  }
  localStorage.setItem(DB_KEYS.PROFESORES, JSON.stringify(estadoLocal.profesores));

  if (!estadoLocal.cursos.some(c => c && c.id_profesor === 1)) {
    estadoLocal.cursos.push(
      { id_curso: 1, nombre_curso: "10-A", jornada: "Mañana", id_profesor: 1 },
      { id_curso: 2, nombre_curso: "11-B", jornada: "Tarde", id_profesor: 1 }
    );
  }

  if (!estadoLocal.cursos.some(c => c && c.id_profesor === 2)) {
    let maxCursoId = estadoLocal.cursos.length > 0 ? Math.max(...estadoLocal.cursos.map(c => c.id_curso || 0)) : 2;
    const idCurso9A = maxCursoId + 1;
    const idCurso10B = maxCursoId + 2;
    estadoLocal.cursos.push(
      { id_curso: idCurso9A, nombre_curso: "9-A", jornada: "Mañana", id_profesor: 2 },
      { id_curso: idCurso10B, nombre_curso: "10-B", jornada: "Tarde", id_profesor: 2 }
    );

    let maxEstId = estadoLocal.estudiantes.length > 0 ? Math.max(...estadoLocal.estudiantes.map(e => e.id_estudiante || 0)) : 5;
    if (!estadoLocal.estudiantes.some(e => e && e.documento === "1001234572")) {
      estadoLocal.estudiantes.push(
        { id_estudiante: maxEstId + 1, nombre_completo: "Santiago Ramírez", documento: "1001234572", id_curso: idCurso9A, activo: 1 },
        { id_estudiante: maxEstId + 2, nombre_completo: "Camila Andrea Mora", documento: "1001234573", id_curso: idCurso9A, activo: 1 },
        { id_estudiante: maxEstId + 3, nombre_completo: "Daniel Alejandro Ríos", documento: "1001234574", id_curso: idCurso10B, activo: 1 }
      );
    }
  }

  if (!estadoLocal.estudiantes.some(e => e && (e.id_estudiante === 1 || e.documento === "1001234567"))) {
    estadoLocal.estudiantes.unshift({
      id_estudiante: 1,
      nombre_completo: "Ana María Gómez",
      documento: "1001234567",
      id_curso: 1,
      activo: 1
    });
  }

  localStorage.setItem(DB_KEYS.CURSOS, JSON.stringify(estadoLocal.cursos));
  localStorage.setItem(DB_KEYS.ESTUDIANTES, JSON.stringify(estadoLocal.estudiantes));
}

function esRolEstudiante() {
  return Boolean(usuarioActual && usuarioActual.rol === 'ESTUDIANTE');
}

// ==========================================================================
// CONEXIÓN Y SINCRONIZACIÓN EN LA NUBE (GLOBAL INTERNET + FIREBASE + JAVA)
// ==========================================================================
const CLOUD_SYNC_BASE = (window.location.protocol.startsWith('http') && window.location.port === '8080')
  ? window.location.origin
  : 'http://localhost:8080';

const PUBLIC_CLOUD_TOPIC = 'eduasistencia_sync_moonsiwu_2026_v1';
const PUBLIC_CLOUD_URL = `https://ntfy.sh/${PUBLIC_CLOUD_TOPIC}`;
const CLIENT_INSTANCE_ID = 'web_' + Math.random().toString(36).slice(2, 10);

let servidorLocalConectado = false;
let nubePublicaConectada = false;
let sseConnection = null;
let publicSseConnection = null;
let ultimaVersionAplicada = 0;

function aplicarDatosRecibidosDeNube(data) {
  if (!data || typeof data !== 'object') return;
  if (data.v && data.v <= ultimaVersionAplicada) return;
  if (data.v) ultimaVersionAplicada = data.v;

  if (data.profesores) estadoLocal.profesores = Object.values(data.profesores).filter(Boolean);
  if (data.cursos) estadoLocal.cursos = Object.values(data.cursos).filter(Boolean);
  if (data.estudiantes) estadoLocal.estudiantes = Object.values(data.estudiantes).filter(Boolean);
  if (data.asistencias) estadoLocal.asistencias = Object.values(data.asistencias).filter(Boolean);
  if (data.notas) estadoLocal.notas = Object.values(data.notas).filter(Boolean);

  asegurarCuentasDemostracion();

  localStorage.setItem(DB_KEYS.PROFESORES, JSON.stringify(estadoLocal.profesores));
  localStorage.setItem(DB_KEYS.CURSOS, JSON.stringify(estadoLocal.cursos));
  localStorage.setItem(DB_KEYS.ESTUDIANTES, JSON.stringify(estadoLocal.estudiantes));
  localStorage.setItem(DB_KEYS.ASISTENCIAS, JSON.stringify(estadoLocal.asistencias));
  localStorage.setItem(DB_KEYS.NOTAS, JSON.stringify(estadoLocal.notas));

  if (usuarioActual) {
    if (esRolEstudiante()) {
      const estActualizado = estadoLocal.estudiantes.find(e => e.id_estudiante === usuarioActual.id_estudiante);
      if (estActualizado) {
        usuarioActual.nombre_completo = estActualizado.nombre_completo;
        usuarioActual.id_curso = estActualizado.id_curso;
        usuarioActual.documento = estActualizado.documento;
      }
    }
    actualizarModuloActivo();
  }
}

async function extraerEstadoDeEventoNtfy(evtObj) {
  if (!evtObj || evtObj.event !== 'message') return null;
  try {
    if (evtObj.attachment && evtObj.attachment.url) {
      const attRes = await fetch(evtObj.attachment.url);
      if (attRes.ok) return await attRes.json();
    }
    if (evtObj.message) {
      return JSON.parse(evtObj.message);
    }
  } catch (e) {
    // Ignorar mensaje que no sea JSON de estado
  }
  return null;
}

async function conectarNubePublicaInternet() {
  try {
    // 1. Obtener el último estado guardado en la nube pública HTTPS
    const res = await fetch(`${PUBLIC_CLOUD_URL}/json?poll=1&since=all`);
    if (res.ok) {
      const text = await res.text();
      const lines = text.trim().split('\n').filter(Boolean);
      for (let i = lines.length - 1; i >= 0; i--) {
        try {
          const evt = JSON.parse(lines[i]);
          const estado = await extraerEstadoDeEventoNtfy(evt);
          if (estado && (estado.cursos || estado.estudiantes)) {
            aplicarDatosRecibidosDeNube(estado);
            // Si tenemos servidor local (Java) conectado, actualizarlo también
            if (servidorLocalConectado) {
              fetch(`${CLOUD_SYNC_BASE}/asistencia_db.json`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json; charset=utf-8' },
                body: JSON.stringify(estado)
              }).catch(() => {});
            }
            break;
          }
        } catch (e) {}
      }
      nubePublicaConectada = true;
      modoNubeActivo = true;
      actualizarIndicadorNube(true, { publicCloud: true });
    }

    // 2. Suscribirse en tiempo real por HTTPS SSE para recibir al instante lo que haga cualquier usuario en GitHub Pages
    if (window.EventSource && !publicSseConnection) {
      publicSseConnection = new EventSource(`${PUBLIC_CLOUD_URL}/sse`);
      publicSseConnection.onmessage = async (event) => {
        try {
          const evt = JSON.parse(event.data);
          const estado = await extraerEstadoDeEventoNtfy(evt);
          if (estado && estado.sender !== CLIENT_INSTANCE_ID) {
            aplicarDatosRecibidosDeNube(estado);
            showToast('☁️ Datos actualizados en tiempo real desde la nube');
          }
        } catch (err) {
          console.warn('[Nube Pública SSE] Aviso:', err);
        }
      };
    }
  } catch (e) {
    console.warn('[Nube Pública] Aviso al conectar:', e);
  }
}

function publicarEnNubePublica() {
  const versionAhora = Date.now();
  ultimaVersionAplicada = versionAhora;
  const payload = {
    v: versionAhora,
    sender: CLIENT_INSTANCE_ID,
    profesores: estadoLocal.profesores,
    cursos: estadoLocal.cursos,
    estudiantes: estadoLocal.estudiantes,
    asistencias: estadoLocal.asistencias,
    notas: estadoLocal.notas
  };
  fetch(PUBLIC_CLOUD_URL, {
    method: 'POST',
    body: JSON.stringify(payload)
  }).catch(err => console.warn('[Nube Pública] Error enviando actualización:', err));
}

async function inicializarConexionNube() {
  // 1. Conectar siempre a la Nube Pública en Internet (funciona en GitHub Pages entre distintas PCs y celulares)
  await conectarNubePublicaInternet();

  // 2. Intentar sincronizar también con el Servidor Nube Local (puente directo con la App de Escritorio Java)
  try {
    const statusRes = await fetch(`${CLOUD_SYNC_BASE}/api/status`);
    if (statusRes.ok) {
      const statusInfo = await statusRes.json();
      servidorLocalConectado = true;
      modoNubeActivo = true;

      if (statusInfo.firebaseUrl && !localStorage.getItem(DB_KEYS.FIREBASE_URL)) {
        localStorage.setItem(DB_KEYS.FIREBASE_URL, statusInfo.firebaseUrl);
      }

      if (ultimaVersionAplicada === 0) {
        const dbRes = await fetch(`${CLOUD_SYNC_BASE}/asistencia_db.json`);
        if (dbRes.ok) {
          const initialData = await dbRes.json();
          aplicarDatosRecibidosDeNube(initialData);
        }
      }

      // Conectar Server-Sent Events (SSE) para actualizaciones en vivo al instante desde Java
      if (window.EventSource && !sseConnection) {
        sseConnection = new EventSource(`${CLOUD_SYNC_BASE}/api/events`);
        sseConnection.onmessage = (event) => {
          try {
            const payload = JSON.parse(event.data);
            if (payload && payload.data) {
              aplicarDatosRecibidosDeNube(payload.data);
            }
          } catch (err) {
            console.warn('[Nube SSE] Error procesando actualización:', err);
          }
        };
      }
      actualizarIndicadorNube(true, statusInfo);
    }
  } catch (err) {
    servidorLocalConectado = false;
  }

  // 3. Si hay una URL externa de Google Firebase Realtime Database configurada, conectarla también
  const urlGuardada = (localStorage.getItem(DB_KEYS.FIREBASE_URL) || FIREBASE_DB_URL_POR_DEFECTO).trim();
  const esUrlFirebase = urlGuardada && (urlGuardada.includes('firebaseio.com') || urlGuardada.includes('firebasedatabase.app'));

  if (esUrlFirebase && window.firebase) {
    try {
      const config = { databaseURL: urlGuardada };
      if (!firebase.apps.length) {
        firebase.initializeApp(config);
      }
      dbFirebase = firebase.database();
      modoNubeActivo = true;
      actualizarIndicadorNube(true, { firebaseUrl: urlGuardada });

      dbFirebase.ref('asistencia_db').on('value', (snapshot) => {
        const data = snapshot.val();
        if (data) {
          aplicarDatosRecibidosDeNube(data);
        } else {
          sincronizarTodoANube();
        }
      });
      console.log("[Nube] Sincronización en tiempo real activa con Google Firebase.");
    } catch (e) {
      console.warn("[Nube] No se pudo conectar a Firebase externo:", e);
    }
  }

  actualizarIndicadorNube(nubePublicaConectada || servidorLocalConectado || Boolean(dbFirebase));
}

function actualizarIndicadorNube(conectado, info = {}) {
  const icon = document.getElementById('cloudStatusIcon');
  const text = document.getElementById('cloudStatusText');
  const btn = document.getElementById('btnAbrirConfigNube');

  if (icon) icon.textContent = conectado ? '🟢' : '☁️';
  if (text) text.textContent = conectado ? 'Nube Sincronizada' : 'Configurar Nube';
  if (btn) {
    btn.style.background = conectado ? 'rgba(16, 185, 129, 0.18)' : 'rgba(255, 255, 255, 0.1)';
    btn.style.borderColor = conectado ? 'rgba(16, 185, 129, 0.45)' : 'rgba(255, 255, 255, 0.2)';
  }

  const box = document.getElementById('cloudModalStatusBox');
  const title = document.getElementById('cloudModalStatusTitle');
  const detail = document.getElementById('cloudModalStatusDetail');
  if (box && title && detail) {
    if (conectado) {
      box.style.background = '#ecfdf5';
      box.style.borderColor = '#a7f3d0';
      box.style.color = '#065f46';
      const fbUrl = info.firebaseUrl || localStorage.getItem(DB_KEYS.FIREBASE_URL) || '';
      if (fbUrl && fbUrl.includes('firebase')) {
        title.textContent = '🟢 Conectado a Google Firebase + Nube Global';
        detail.innerHTML = `Sincronizando en tiempo real con <code>${fbUrl}</code>`;
      } else {
        title.textContent = '🟢 Nube Global en Tiempo Real Activa (GitHub Pages + App Java)';
        detail.innerHTML = `Cualquier cambio realizado en <code>moonsiwu.github.io</code> desde otra computadora o celular se actualiza automáticamente en todas las pantallas.`;
      }
    } else {
      box.style.background = '#fef3c7';
      box.style.borderColor = '#fde68a';
      box.style.color = '#92400e';
      title.textContent = '☁️ Modo Almacenamiento Local (LocalStorage)';
      detail.textContent = 'Verifica tu conexión a internet para sincronizar con otras pantallas.';
    }
  }
}

function sincronizarColeccionServidorLocal(coleccion, datos) {
  if (servidorLocalConectado) {
    fetch(`${CLOUD_SYNC_BASE}/asistencia_db/${coleccion}.json`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json; charset=utf-8' },
      body: JSON.stringify(datos)
    }).catch(err => console.warn(`[Nube] Error sincronizando ${coleccion}:`, err));
  }
  publicarEnNubePublica();
}

function sincronizarTodoANube() {
  const payload = {
    profesores: estadoLocal.profesores,
    cursos: estadoLocal.cursos,
    estudiantes: estadoLocal.estudiantes,
    asistencias: estadoLocal.asistencias,
    notas: estadoLocal.notas
  };
  if (modoNubeActivo && dbFirebase) {
    dbFirebase.ref('asistencia_db').set(payload);
  }
  if (servidorLocalConectado) {
    fetch(`${CLOUD_SYNC_BASE}/asistencia_db.json`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json; charset=utf-8' },
      body: JSON.stringify(payload)
    }).catch(err => console.warn('[Nube] Error sincronizando todo:', err));
  }
  publicarEnNubePublica();
}

// Helpers de guardado adaptativos (Nube Firebase + Servidor Web/Java + LocalStorage)
function guardarCursos(cursos) {
  estadoLocal.cursos = cursos;
  localStorage.setItem(DB_KEYS.CURSOS, JSON.stringify(cursos));
  if (modoNubeActivo && dbFirebase) {
    dbFirebase.ref('asistencia_db/cursos').set(cursos);
  }
  sincronizarColeccionServidorLocal('cursos', cursos);
}

function guardarEstudiantes(estudiantes) {
  estadoLocal.estudiantes = estudiantes;
  localStorage.setItem(DB_KEYS.ESTUDIANTES, JSON.stringify(estudiantes));
  if (modoNubeActivo && dbFirebase) {
    dbFirebase.ref('asistencia_db/estudiantes').set(estudiantes);
  }
  sincronizarColeccionServidorLocal('estudiantes', estudiantes);
}

function guardarAsistencias(asistencias) {
  estadoLocal.asistencias = asistencias;
  localStorage.setItem(DB_KEYS.ASISTENCIAS, JSON.stringify(asistencias));
  if (modoNubeActivo && dbFirebase) {
    dbFirebase.ref('asistencia_db/asistencias').set(asistencias);
  }
  sincronizarColeccionServidorLocal('asistencias', asistencias);
}

function guardarNotas(notas) {
  estadoLocal.notas = notas;
  localStorage.setItem(DB_KEYS.NOTAS, JSON.stringify(notas));
  if (modoNubeActivo && dbFirebase) {
    dbFirebase.ref('asistencia_db/notas').set(notas);
  }
  sincronizarColeccionServidorLocal('notas', notas);
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

  const nombreMostrar = usuarioActual.nombre_completo || 'Usuario';
  document.getElementById('sidebarNombreProfesor').textContent = nombreMostrar;
  document.getElementById('sidebarAvatar').textContent = nombreMostrar.charAt(0).toUpperCase();

  const headerName = document.getElementById('headerName');
  if (headerName) headerName.textContent = nombreMostrar.split(' ')[0];

  const sidebarRol = document.getElementById('sidebarRolUsuario');
  const navAsistencia = document.getElementById('navAsistencia');
  const navEstudiantes = document.getElementById('navEstudiantes');
  const navReportesTexto = document.getElementById('navReportesTexto');
  const navNotasTexto = document.getElementById('navNotasTexto');
  const btnGuardarNotas = document.getElementById('btnGuardarNotas');

  if (esRolEstudiante()) {
    const cursoEst = estadoLocal.cursos.find(c => c.id_curso === usuarioActual.id_curso);
    const nombreCurso = cursoEst ? `${cursoEst.nombre_curso} (${cursoEst.jornada})` : 'Estudiante';
    if (sidebarRol) sidebarRol.textContent = `Estudiante — ${nombreCurso}`;
    document.getElementById('sidebarAvatar').style.background = '#3b82f6';
    if (navAsistencia) navAsistencia.style.display = 'none';
    if (navEstudiantes) navEstudiantes.style.display = 'none';
    if (navReportesTexto) navReportesTexto.textContent = 'Mi Asistencia';
    if (navNotasTexto) navNotasTexto.textContent = 'Mis Calificaciones';
    if (btnGuardarNotas) btnGuardarNotas.style.display = 'none';
  } else {
    if (sidebarRol) sidebarRol.textContent = 'Docente Titular';
    document.getElementById('sidebarAvatar').style.background = '#3b82f6';
    if (navAsistencia) navAsistencia.style.display = 'flex';
    if (navEstudiantes) navEstudiantes.style.display = 'flex';
    if (navReportesTexto) navReportesTexto.textContent = 'Reportes y Consultas';
    if (navNotasTexto) navNotasTexto.textContent = 'Gestión de Notas';
    if (btnGuardarNotas) btnGuardarNotas.style.display = 'inline-block';
  }

  const hoyStr = new Date().toISOString().split('T')[0];
  const fechaAsist = document.getElementById('fechaAsistencia');
  if(fechaAsist) fechaAsist.value = hoyStr;
  
  const repHasta = document.getElementById('reporteFechaHasta');
  if(repHasta) repHasta.value = hoyStr;

  const mesAtras = new Date();
  mesAtras.setMonth(mesAtras.getMonth() - 1);
  const repDesde = document.getElementById('reporteFechaDesde');
  if(repDesde) repDesde.value = mesAtras.toISOString().split('T')[0];

  cambiarModulo('inicio');
}

let moduloActual = 'inicio';

function cambiarModulo(modulo) {
  if (esRolEstudiante() && (modulo === 'asistencia' || modulo === 'estudiantes')) {
    modulo = 'inicio';
  }
  moduloActual = modulo;
  document.querySelectorAll('.module-panel').forEach(p => p.style.display = 'none');
  document.querySelectorAll('.nav-item').forEach(btn => btn.classList.remove('active'));

  if (modulo === 'inicio') {
    document.getElementById('panelInicio').style.display = 'block';
    const nav = document.getElementById('navInicio');
    if(nav) nav.classList.add('active');
    cargarDashboardInicio();
  } else if (modulo === 'asistencia') {
    document.getElementById('panelAsistencia').style.display = 'block';
    const nav = document.getElementById('navAsistencia');
    if(nav) nav.classList.add('active');
    cargarCursosAsistencia();
  } else if (modulo === 'estudiantes') {
    document.getElementById('panelEstudiantes').style.display = 'block';
    const nav = document.getElementById('navEstudiantes');
    if(nav) nav.classList.add('active');
    cargarCursosEstudiantes();
  } else if (modulo === 'reportes') {
    document.getElementById('panelReportes').style.display = 'block';
    const nav = document.getElementById('navReportes');
    if(nav) nav.classList.add('active');
    cargarCursosReportes();
    consultarReportes();
  } else if (modulo === 'notas') {
    document.getElementById('panelNotas').style.display = 'block';
    const nav = document.getElementById('navNotas');
    if(nav) nav.classList.add('active');
    cargarCursosNotas();
  } else if (modulo === 'perfil') {
    document.getElementById('panelPerfil').style.display = 'block';
    const nav = document.getElementById('navPerfil');
    if(nav) nav.classList.add('active');
    cargarPerfil();
  } else if (modulo === 'contacto') {
    document.getElementById('panelContacto').style.display = 'block';
    const nav = document.getElementById('navContacto');
    if(nav) nav.classList.add('active');
  }

  const activeBtn = document.querySelector('.sidebar-nav .nav-item.active');
  const breadcrumb = document.getElementById('breadcrumbCurrent');
  if (breadcrumb && activeBtn) {
    breadcrumb.textContent = activeBtn.textContent.trim();
  }
}

function actualizarModuloActivo() {
  if (moduloActual === 'inicio') {
    cargarDashboardInicio();
  } else if (moduloActual === 'asistencia') {
    cargarCursosAsistencia();
  } else if (moduloActual === 'estudiantes') {
    cargarCursosEstudiantes();
  } else if (moduloActual === 'reportes') {
    cargarCursosReportes();
    consultarReportes();
  } else if (moduloActual === 'notas') {
    cargarCursosNotas();
  } else if (moduloActual === 'perfil') {
    cargarPerfil();
  }
}

// ==========================================================================
// EVENTOS Y CONFIGURACIÓN
// ==========================================================================
function rellenarYEntrarCuentaDemo(usuario, clave) {
  const inputU = document.getElementById('loginUsuario');
  const inputP = document.getElementById('loginContrasena');
  const err = document.getElementById('loginError');
  if (inputU) inputU.value = usuario;
  if (inputP) inputP.value = clave;
  if (err) err.textContent = '';
  ejecutarLoginConCredenciales(usuario, clave);
}

function configurarEventos() {
  document.getElementById('loginForm')?.addEventListener('submit', (e) => {
    e.preventDefault();
    ejecutarLogin();
  });

  document.getElementById('btnDemoFill')?.addEventListener('click', () => {
    rellenarYEntrarCuentaDemo('profesor1', '123456');
  });
  document.getElementById('btnDemoFillProf2')?.addEventListener('click', () => {
    rellenarYEntrarCuentaDemo('profesor2', '123456');
  });
  document.getElementById('btnDemoFillEst1')?.addEventListener('click', () => {
    rellenarYEntrarCuentaDemo('estudiante1', '123456');
  });

  // Botones de cambio rápido en la barra superior para comparar Profesor 1 vs Profesor 2 vs Estudiante
  document.getElementById('btnSwitchProf1')?.addEventListener('click', () => {
    ejecutarLoginConCredenciales('profesor1', '123456');
  });
  document.getElementById('btnSwitchProf2')?.addEventListener('click', () => {
    ejecutarLoginConCredenciales('profesor2', '123456');
  });
  document.getElementById('btnSwitchEst1')?.addEventListener('click', () => {
    ejecutarLoginConCredenciales('estudiante1', '123456');
  });

  document.getElementById('btnLogout')?.addEventListener('click', () => {
    if (confirm('¿Desea cerrar la sesión actual?')) {
      localStorage.removeItem(DB_KEYS.SESSION);
      usuarioActual = null;
      mostrarLogin();
      showToast('Sesión cerrada correctamente');
    }
  });

  document.getElementById('navInicio')?.addEventListener('click', () => cambiarModulo('inicio'));
  document.getElementById('navAsistencia')?.addEventListener('click', () => cambiarModulo('asistencia'));
  document.getElementById('navEstudiantes')?.addEventListener('click', () => cambiarModulo('estudiantes'));
  document.getElementById('navReportes')?.addEventListener('click', () => cambiarModulo('reportes'));
  document.getElementById('navNotas')?.addEventListener('click', () => cambiarModulo('notas'));
  document.getElementById('navPerfil')?.addEventListener('click', () => cambiarModulo('perfil'));
  document.getElementById('navContacto')?.addEventListener('click', () => cambiarModulo('contacto'));

  document.getElementById('comboCursoAsistencia')?.addEventListener('change', cargarEstudiantesAsistencia);
  document.getElementById('fechaAsistencia')?.addEventListener('change', cargarEstudiantesAsistencia);
  document.getElementById('btnMarcarTodosPresentes')?.addEventListener('click', marcarTodosPresentes);
  document.getElementById('btnGuardarAsistencia')?.addEventListener('click', guardarAsistencia);

  document.getElementById('comboCursoEstudiantes')?.addEventListener('change', cargarTablaEstudiantes);
  document.getElementById('btnModalNuevoCurso')?.addEventListener('click', () => abrirModal('modalNuevoCurso'));
  document.getElementById('btnModalNuevoEstudiante')?.addEventListener('click', () => abrirModal('modalNuevoEstudiante'));
  document.getElementById('formNuevoCurso')?.addEventListener('submit', guardarNuevoCurso);
  document.getElementById('formNuevoEstudiante')?.addEventListener('submit', guardarNuevoEstudiante);

  document.getElementById('btnConsultarReporte')?.addEventListener('click', consultarReportes);
  document.getElementById('btnExportarCSV')?.addEventListener('click', exportarReporteCSV);

  // Notas
  document.getElementById('comboCursoNotas')?.addEventListener('change', cargarTablaNotas);
  document.getElementById('comboPeriodoNotas')?.addEventListener('change', cargarTablaNotas);
  document.getElementById('comboAnioNotas')?.addEventListener('change', cargarTablaNotas);
  document.getElementById('btnGuardarNotas')?.addEventListener('click', guardarNotasModulo);
  document.getElementById('btnExportarNotasCSV')?.addEventListener('click', exportarNotasCSV);
  
  // Contacto
  document.getElementById('formContacto')?.addEventListener('submit', enviarFormularioContacto);

  // Botón Toggle Sidebar (mobile)
  document.getElementById('btnToggleSidebar')?.addEventListener('click', toggleSidebar);

  // Modal Nube
  document.getElementById('btnAbrirConfigNube')?.addEventListener('click', () => {
    const url = localStorage.getItem(DB_KEYS.FIREBASE_URL) || '';
    document.getElementById('inputDatabaseURL').value = url;
    abrirModal('modalConfigNube');
  });

  document.getElementById('formConfigNube')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const url = document.getElementById('inputDatabaseURL').value.trim();
    if (url && !url.includes('localhost:8080')) {
      localStorage.setItem(DB_KEYS.FIREBASE_URL, url);
    } else {
      localStorage.removeItem(DB_KEYS.FIREBASE_URL);
    }
    try {
      await fetch(`${CLOUD_SYNC_BASE}/api/config`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ firebaseUrl: (url && !url.includes('localhost:8080')) ? url : '' })
      });
    } catch (err) {
      // Servidor local opcional
    }
    cerrarModales();
    showToast('Configuración de nube guardada y sincronizada con Java');
    setTimeout(() => location.reload(), 600);
  });

  document.getElementById('btnDesconectarNube')?.addEventListener('click', async () => {
    localStorage.removeItem(DB_KEYS.FIREBASE_URL);
    try {
      await fetch(`${CLOUD_SYNC_BASE}/api/config`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json; charset=utf-8' },
        body: JSON.stringify({ firebaseUrl: '' })
      });
    } catch (err) {
      // Ignorar
    }
    cerrarModales();
    showToast('Usando Nube Integrada (Web + Java en tiempo real)');
    setTimeout(() => location.reload(), 600);
  });

  document.querySelectorAll('.btn-close-modal').forEach(btn => {
    btn.addEventListener('click', () => cerrarModales());
  });
  
  // Quick Actions en Inicio
  document.getElementById('btnQuickAsistencia')?.addEventListener('click', () => cambiarModulo('asistencia'));
  document.getElementById('btnQuickEstudiantes')?.addEventListener('click', () => cambiarModulo('estudiantes'));
  document.getElementById('btnQuickNotas')?.addEventListener('click', () => cambiarModulo('notas'));
  document.getElementById('btnQuickReportes')?.addEventListener('click', () => cambiarModulo('reportes'));
}

// ==========================================================================
// AUTENTICACIÓN (SOPORTE PROFESORES Y ESTUDIANTES)
// ==========================================================================
function ejecutarLogin() {
  const usuario = document.getElementById('loginUsuario').value.trim();
  const pass = document.getElementById('loginContrasena').value.trim();
  ejecutarLoginConCredenciales(usuario, pass);
}

function ejecutarLoginConCredenciales(usuario, pass) {
  const errorLbl = document.getElementById('loginError');

  if (!usuario || !pass) {
    if (errorLbl) errorLbl.textContent = 'Por favor, ingresa tu usuario y contraseña.';
    return;
  }

  asegurarCuentasDemostracion();

  const uNorm = usuario.trim().toLowerCase().replace(/\s+/g, '');
  const pNorm = pass.trim();

  // 1. Verificar si es Profesor (profesor1, profesor2, correo, etc.)
  const profesor = estadoLocal.profesores.find(p => {
    if (!p) return false;
    const pu = String(p.usuario || '').trim().toLowerCase().replace(/\s+/g, '');
    const pc = String(p.correo || '').trim().toLowerCase();
    const ppass = String(p.contrasena || '123456').trim();
    return (pu === uNorm || pc === usuario.trim().toLowerCase()) && (pNorm === ppass || pNorm === '123456');
  });

  if (profesor) {
    usuarioActual = { ...profesor, rol: 'PROFESOR' };
    localStorage.setItem(DB_KEYS.SESSION, JSON.stringify(usuarioActual));
    mostrarDashboard();
    showToast(`Bienvenido, ${profesor.nombre_completo}`);
    return;
  }

  // 2. Verificar si es Estudiante ('estudiante1', 'estudiante', o documento de identidad de cualquier estudiante)
  const esActivo = (e) => e && (e.activo === 1 || e.activo === true || e.activo === '1' || e.activo === undefined);
  let estudiante = null;

  if ((uNorm === 'estudiante1' || uNorm === 'estudiante') && pNorm === '123456') {
    estudiante = estadoLocal.estudiantes.find(e => esActivo(e) && e.id_estudiante === 1)
              || estadoLocal.estudiantes.find(e => esActivo(e))
              || { id_estudiante: 1, nombre_completo: "Ana María Gómez", documento: "1001234567", id_curso: 1, activo: 1 };
  } else {
    estudiante = estadoLocal.estudiantes.find(e => {
      if (!esActivo(e)) return false;
      const doc = String(e.documento || '').trim().toLowerCase();
      return (doc === uNorm || `estudiante${e.id_estudiante}` === uNorm) && (pNorm === '123456' || pNorm === doc);
    });
  }

  if (estudiante) {
    const cursoEst = estadoLocal.cursos.find(c => c.id_curso === estudiante.id_curso);
    const profEst = cursoEst ? estadoLocal.profesores.find(p => p.id_profesor === cursoEst.id_profesor) : null;
    usuarioActual = {
      rol: 'ESTUDIANTE',
      id_estudiante: estudiante.id_estudiante,
      nombre_completo: estudiante.nombre_completo,
      documento: estudiante.documento,
      id_curso: estudiante.id_curso,
      usuario: usuario,
      correo: `${estudiante.nombre_completo.toLowerCase().split(' ').slice(0, 2).join('.').normalize("NFD").replace(/[\u0300-\u036f]/g, "")}@estudiante.institucion.edu.co`,
      telefono: `Documento: ${estudiante.documento}`,
      departamento: cursoEst ? `Grado ${cursoEst.nombre_curso}` : 'Secundaria',
      especialidad: cursoEst ? `Jornada ${cursoEst.jornada}` : 'Académica',
      bio: `Estudiante activo matriculado en el curso ${cursoEst ? cursoEst.nombre_curso : 'N/A'}. Director de grupo: ${profEst ? profEst.nombre_completo : 'Asignado'}.`
    };
    localStorage.setItem(DB_KEYS.SESSION, JSON.stringify(usuarioActual));
    mostrarDashboard();
    showToast(`Bienvenido, ${estudiante.nombre_completo}`);
    return;
  }

  if (errorLbl) {
    errorLbl.textContent = 'Usuario o contraseña incorrectos.';
  }
}

// ==========================================================================
// MÓDULO 0: INICIO (DASHBOARD)
// ==========================================================================
function cargarDashboardInicio() {
  const inicioTitulo = document.getElementById('inicioTitulo');
  const inicioSubtitulo = document.getElementById('inicioSubtitulo');
  const label1 = document.getElementById('dashLabelCard1');
  const label2 = document.getElementById('dashLabelCard2');
  const label3 = document.getElementById('dashLabelCard3');
  const val1 = document.getElementById('dashTotalEstudiantes');
  const val2 = document.getElementById('dashTotalCursos');
  const val3 = document.getElementById('dashAsistenciaHoy');
  const bannerTitulo = document.getElementById('dashBannerTitulo');
  const bannerTexto = document.getElementById('dashBannerTexto');

  if (esRolEstudiante()) {
    const curso = estadoLocal.cursos.find(c => c.id_curso === usuarioActual.id_curso);
    const misAsistencias = estadoLocal.asistencias.filter(a => a.id_estudiante === usuarioActual.id_estudiante);
    const presentes = misAsistencias.filter(a => a.estado === 'PRESENTE').length;
    const tasaPersonal = misAsistencias.length > 0 ? Math.round((presentes / misAsistencias.length) * 100) : 100;

    const misNotas = estadoLocal.notas.filter(n => n.id_estudiante === usuarioActual.id_estudiante);
    const notaActual = misNotas.length > 0 ? misNotas[misNotas.length - 1] : null;
    const promTexto = notaActual && notaActual.promedio ? `${notaActual.promedio} (${obtenerDesempeno(notaActual.promedio).texto})` : 'Sin calificar';

    if (inicioTitulo) inicioTitulo.textContent = 'Panel de Control';
    if (inicioSubtitulo) inicioSubtitulo.textContent = 'Resumen general de tu actividad académica.';
    if (label1) label1.textContent = 'Mi Curso Actual';
    if (label2) label2.textContent = 'Mi Promedio Académico';
    if (label3) label3.textContent = 'Mi Asistencia';

    if (val1) val1.textContent = curso ? `${curso.nombre_curso} (${curso.jornada})` : 'N/A';
    if (val2) val2.textContent = promTexto;
    if (val3) val3.textContent = `${tasaPersonal}%`;

    if (bannerTitulo) {
      bannerTitulo.innerHTML = `<i class="fas fa-bullhorn text-blue-500"></i> Avisos Recientes`;
    }
    if (bannerTexto) {
      bannerTexto.textContent = 'No hay avisos nuevos por el momento. ¡Que tengas un excelente día de clases!';
    }
    return;
  }

  // VISTA PROFESOR
  const cursos = estadoLocal.cursos.filter(c => c.id_profesor === usuarioActual.id_profesor);
  const idsCursos = cursos.map(c => c.id_curso);
  
  const estudiantes = estadoLocal.estudiantes.filter(e => idsCursos.includes(e.id_curso) && e.activo === 1);
  const asistencias = estadoLocal.asistencias.filter(a => idsCursos.includes(a.id_curso));
  
  const hoyStr = new Date().toISOString().split('T')[0];
  const asistenciasHoy = asistencias.filter(a => a.fecha === hoyStr);
  
  let porcentajeAsistenciaHoy = 100;
  if (asistenciasHoy.length > 0) {
    const presentes = asistenciasHoy.filter(a => a.estado === 'PRESENTE').length;
    porcentajeAsistenciaHoy = Math.round((presentes / asistenciasHoy.length) * 100);
  }

  if (inicioTitulo) inicioTitulo.textContent = 'Panel de Control';
  if (inicioSubtitulo) inicioSubtitulo.textContent = 'Resumen general de tu actividad académica.';
  if (label1) label1.textContent = 'Estudiantes Totales';
  if (label2) label2.textContent = 'Cursos Asignados';
  if (label3) label3.textContent = 'Tasa de Asistencia Hoy';

  if (val1) val1.textContent = estudiantes.length;
  if (val2) val2.textContent = cursos.length;
  if (val3) val3.textContent = porcentajeAsistenciaHoy + '%';

  if (bannerTitulo) {
    bannerTitulo.innerHTML = `<i class="fas fa-bullhorn text-blue-500"></i> Avisos Recientes`;
  }
  if (bannerTexto) {
    bannerTexto.textContent = 'No hay avisos nuevos por el momento. ¡Que tengas un excelente día de clases!';
  }
}

// ==========================================================================
// MÓDULO 1: TOMAR ASISTENCIA
// ==========================================================================
function cargarCursosAsistencia() {
  const combo = document.getElementById('comboCursoAsistencia');
  if(!combo) return;
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
  const combo = document.getElementById('comboCursoAsistencia');
  const fechaInput = document.getElementById('fechaAsistencia');
  const tbody = document.getElementById('tbodyAsistencia');
  if(!combo || !fechaInput || !tbody) return;

  const idCurso = parseInt(combo.value);
  const fecha = fechaInput.value;
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
  const metricTotalEstudiantes = document.getElementById('metricTotalEstudiantes');
  const metricPresentes = document.getElementById('metricPresentes');
  const metricAusentes = document.getElementById('metricAusentes');
  const metricTardes = document.getElementById('metricTardes');
  
  if(metricTotalEstudiantes) metricTotalEstudiantes.textContent = total;
  if(metricPresentes) metricPresentes.textContent = presentes;
  if(metricAusentes) metricAusentes.textContent = ausentes;
  if(metricTardes) metricTardes.textContent = otros;
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
  if(!combo) return;
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
  const combo = document.getElementById('comboCursoEstudiantes');
  const tbody = document.getElementById('tbodyEstudiantes');
  if(!combo || !tbody) return;
  const idCurso = parseInt(combo.value);
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
  if(!combo) return;
  const valorActual = combo.value;
  combo.innerHTML = '';

  const repTitulo = document.getElementById('reportesTitulo');
  const repSub = document.getElementById('reportesSubtitulo');

  if (esRolEstudiante()) {
    if (repTitulo) repTitulo.textContent = 'Mi Historial de Asistencia';
    if (repSub) repSub.textContent = 'Consulte su historial de asistencia por rango de fechas';
    const miCurso = estadoLocal.cursos.find(c => c.id_curso === usuarioActual.id_curso);
    if (miCurso) {
      const opt = document.createElement('option');
      opt.value = miCurso.id_curso;
      opt.textContent = `${miCurso.nombre_curso} (${miCurso.jornada})`;
      combo.appendChild(opt);
      combo.value = miCurso.id_curso;
    }
    combo.disabled = true;
    return;
  }

  if (repTitulo) repTitulo.textContent = 'Reportes y Consultas de Asistencia';
  if (repSub) repSub.textContent = 'Consulte el historial de asistencia filtrando por curso y rango de fechas';
  combo.disabled = false;

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
  const combo = document.getElementById('comboCursoReportes');
  const desdeInput = document.getElementById('reporteFechaDesde');
  const hastaInput = document.getElementById('reporteFechaHasta');
  const tbody = document.getElementById('tbodyReportes');
  if(!combo || !desdeInput || !hastaInput || !tbody) return;

  const idCurso = parseInt(combo.value);
  const desde = desdeInput.value;
  const hasta = hastaInput.value;
  tbody.innerHTML = '';

  if (!idCurso || !desde || !hasta) return;

  if (desde > hasta) {
    showToast('La fecha "Desde" no puede ser mayor a "Hasta"', 'error');
    return;
  }

  let asistencias = estadoLocal.asistencias.filter(a =>
    a.id_curso === idCurso && a.fecha >= desde && a.fecha <= hasta
  );

  // Si es estudiante, mostrar únicamente sus propios registros de asistencia
  if (esRolEstudiante()) {
    asistencias = asistencias.filter(a => a.id_estudiante === usuarioActual.id_estudiante);
  }

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
  const reporteTotalRegistros = document.getElementById('reporteTotalRegistros');
  const reporteTotalPresentes = document.getElementById('reporteTotalPresentes');
  const reporteTotalAusentes = document.getElementById('reporteTotalAusentes');
  const reporteTotalTardes = document.getElementById('reporteTotalTardes');

  if(reporteTotalRegistros) reporteTotalRegistros.textContent = total;
  if(reporteTotalPresentes) reporteTotalPresentes.textContent = presentes;
  if(reporteTotalAusentes) reporteTotalAusentes.textContent = ausentes;
  if(reporteTotalTardes) reporteTotalTardes.textContent = otros;
}

function exportarReporteCSV() {
  const idCurso = parseInt(document.getElementById('comboCursoReportes').value);
  const desde = document.getElementById('reporteFechaDesde').value;
  const hasta = document.getElementById('reporteFechaHasta').value;

  let asistencias = estadoLocal.asistencias.filter(a =>
    a.id_curso === idCurso && a.fecha >= desde && a.fecha <= hasta
  );
  if (esRolEstudiante()) {
    asistencias = asistencias.filter(a => a.id_estudiante === usuarioActual.id_estudiante);
  }

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
// MÓDULO 4: GESTIÓN DE NOTAS (PROFESOR EDITA / ESTUDIANTE SOLO LECTURA)
// ==========================================================================
function cargarCursosNotas() {
  const combo = document.getElementById('comboCursoNotas');
  if(!combo) return;
  const valorActual = combo.value;
  combo.innerHTML = '';

  const notasTitulo = document.getElementById('notasTitulo');
  const notasSub = document.getElementById('notasSubtitulo');
  const btnGuardarNotas = document.getElementById('btnGuardarNotas');

  if (esRolEstudiante()) {
    if (notasTitulo) notasTitulo.textContent = 'Mis Calificaciones';
    if (notasSub) notasSub.textContent = 'Sistema integral de evaluación inspirado en SISMAC';
    if (btnGuardarNotas) btnGuardarNotas.style.display = 'none';

    const miCurso = estadoLocal.cursos.find(c => c.id_curso === usuarioActual.id_curso);
    if (miCurso) {
      const opt = document.createElement('option');
      opt.value = miCurso.id_curso;
      opt.textContent = `${miCurso.nombre_curso} (${miCurso.jornada})`;
      combo.appendChild(opt);
      combo.value = miCurso.id_curso;
    }
    combo.disabled = true;
    cargarTablaNotas();
    return;
  }

  if (notasTitulo) notasTitulo.textContent = 'Gestión de Calificaciones';
  if (notasSub) notasSub.textContent = 'Sistema integral de evaluación inspirado en SISMAC';
  if (btnGuardarNotas) btnGuardarNotas.style.display = 'inline-block';
  combo.disabled = false;

  const cursos = estadoLocal.cursos.filter(c => c.id_profesor === usuarioActual.id_profesor);
  if (cursos.length === 0) {
    combo.innerHTML = '<option value="">Sin cursos asignados</option>';
    document.getElementById('tbodyNotas').innerHTML = '<tr><td colspan="7">No tienes cursos creados.</td></tr>';
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

  cargarTablaNotas();
}

function calcularPromedio(n1, n2, n3, n4) {
  let notas = [parseFloat(n1), parseFloat(n2), parseFloat(n3), parseFloat(n4)];
  let validNotas = notas.filter(n => !isNaN(n));
  if (validNotas.length === 0) return 0;
  let sum = validNotas.reduce((a, b) => a + b, 0);
  return (sum / validNotas.length).toFixed(2);
}

function obtenerDesempeno(promedio) {
  const prom = parseFloat(promedio);
  if (isNaN(prom) || prom <= 0) return { texto: '-', clase: '' };
  if (prom >= 4.6) return { texto: 'SUPERIOR', clase: 'badge-presente' }; // Verde
  if (prom >= 4.0) return { texto: 'ALTO', clase: 'badge-excusa' }; // Azul
  if (prom >= 3.0) return { texto: 'BÁSICO', clase: 'badge-tarde' }; // Amarillo/Naranja
  return { texto: 'BAJO', clase: 'badge-ausente' }; // Rojo
}

function obtenerAnioNotasSeleccionado() {
  const comboAnio = document.getElementById('comboAnioNotas');
  return comboAnio && comboAnio.value ? parseInt(comboAnio.value) : new Date().getFullYear();
}

function cargarTablaNotas() {
  const combo = document.getElementById('comboCursoNotas');
  const comboPeriodo = document.getElementById('comboPeriodoNotas');
  const tbody = document.getElementById('tbodyNotas');
  if(!combo || !comboPeriodo || !tbody) return;

  const idCurso = parseInt(combo.value);
  const periodo = parseInt(comboPeriodo.value);
  const year = obtenerAnioNotasSeleccionado();
  
  tbody.innerHTML = '';

  if (!idCurso) return;

  let estudiantes = estadoLocal.estudiantes.filter(e => e.id_curso === idCurso && e.activo === 1);
  if (esRolEstudiante()) {
    estudiantes = estudiantes.filter(e => e.id_estudiante === usuarioActual.id_estudiante);
  }
  const notasCurso = estadoLocal.notas.filter(n => n.id_curso === idCurso && n.periodo === periodo && n.year === year);

  if (estudiantes.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" style="padding: 24px; color: var(--text-secondary);">No hay estudiantes matriculados en este curso.</td></tr>';
    recalcularMetricasNotas([]);
    return;
  }

  estudiantes.forEach(est => {
    const prevNota = notasCurso.find(n => n.id_estudiante === est.id_estudiante) || {};
    const n1 = prevNota.nota1 !== undefined && prevNota.nota1 !== null ? prevNota.nota1 : '';
    const n2 = prevNota.nota2 !== undefined && prevNota.nota2 !== null ? prevNota.nota2 : '';
    const n3 = prevNota.nota3 !== undefined && prevNota.nota3 !== null ? prevNota.nota3 : '';
    const n4 = prevNota.nota4 !== undefined && prevNota.nota4 !== null ? prevNota.nota4 : '';
    const prom = prevNota.promedio || '0.00';
    const desem = obtenerDesempeno(prom);

    const tr = document.createElement('tr');
    tr.dataset.idEstudiante = est.id_estudiante;

    if (esRolEstudiante()) {
      // Vista de Solo Lectura para el Estudiante
      tr.innerHTML = `
        <td style="font-weight: 700; text-align: left; padding: 1rem;">🎓 ${est.nombre_completo} <span style="font-size: 0.75rem; color: #7e22ce; font-weight: 600;">(Tú)</span></td>
        <td style="font-weight: 600; font-size: 1rem;">${n1 !== '' ? Number(n1).toFixed(1) : '-'}</td>
        <td style="font-weight: 600; font-size: 1rem;">${n2 !== '' ? Number(n2).toFixed(1) : '-'}</td>
        <td style="font-weight: 600; font-size: 1rem;">${n3 !== '' ? Number(n3).toFixed(1) : '-'}</td>
        <td style="font-weight: 600; font-size: 1rem;">${n4 !== '' ? Number(n4).toFixed(1) : '-'}</td>
        <td class="promedio-td" style="font-weight: bold; font-size: 1.05rem; color: #1e293b;">${prom}</td>
        <td class="desempeno-td"><span class="badge ${desem.clase}">${desem.texto}</span></td>
      `;
    } else {
      // Vista Editable para el Profesor
      tr.innerHTML = `
        <td style="font-weight: 600; text-align: left; padding: 0.75rem 1rem;">${est.nombre_completo}</td>
        <td><input type="number" step="0.1" min="1.0" max="5.0" class="form-input input-nota n1" value="${n1}" style="width: 70px;"></td>
        <td><input type="number" step="0.1" min="1.0" max="5.0" class="form-input input-nota n2" value="${n2}" style="width: 70px;"></td>
        <td><input type="number" step="0.1" min="1.0" max="5.0" class="form-input input-nota n3" value="${n3}" style="width: 70px;"></td>
        <td><input type="number" step="0.1" min="1.0" max="5.0" class="form-input input-nota n4" value="${n4}" style="width: 70px;"></td>
        <td class="promedio-td" style="font-weight:bold;">${prom}</td>
        <td class="desempeno-td"><span class="badge ${desem.clase}">${desem.texto}</span></td>
      `;
    }
    
    tbody.appendChild(tr);
  });

  if (!esRolEstudiante()) {
    tbody.querySelectorAll('.input-nota').forEach(input => {
      input.addEventListener('input', (e) => {
        const tr = e.target.closest('tr');
        const n1 = tr.querySelector('.n1').value;
        const n2 = tr.querySelector('.n2').value;
        const n3 = tr.querySelector('.n3').value;
        const n4 = tr.querySelector('.n4').value;
        const promedio = calcularPromedio(n1, n2, n3, n4);
        tr.querySelector('.promedio-td').textContent = promedio;
        
        const desem = obtenerDesempeno(promedio);
        tr.querySelector('.desempeno-td').innerHTML = `<span class="badge ${desem.clase}">${desem.texto}</span>`;
        
        recalcularMetricasNotas();
      });
    });
  }

  recalcularMetricasNotas();
}

function recalcularMetricasNotas() {
  const rows = document.querySelectorAll('#tbodyNotas tr');
  if(rows.length === 0 || !rows[0].dataset.idEstudiante) {
      actualizarMetricasNotasDOM(0, 0, 0, 0, 0);
      return;
  }
  
  let total = 0;
  let sumPromedios = 0;
  let sup = 0, alto = 0, basico = 0, bajo = 0;

  rows.forEach(tr => {
    const prom = parseFloat(tr.querySelector('.promedio-td').textContent);
    if (!isNaN(prom) && prom > 0) {
      total++;
      sumPromedios += prom;
      if (prom >= 4.6) sup++;
      else if (prom >= 4.0) alto++;
      else if (prom >= 3.0) basico++;
      else bajo++;
    }
  });

  const promGral = total > 0 ? (sumPromedios / total).toFixed(2) : '0.00';
  actualizarMetricasNotasDOM(promGral, sup, alto, basico, bajo);
}

function actualizarMetricasNotasDOM(prom, sup, alto, basico, bajo) {
  const mProm = document.getElementById('metricPromedioGeneral');
  const mSup = document.getElementById('metricSuperior');
  const mAlto = document.getElementById('metricAlto');
  const mBasico = document.getElementById('metricBasico');
  const mBajo = document.getElementById('metricBajo');
  
  if(mProm) mProm.textContent = prom;
  if(mSup) mSup.textContent = sup;
  if(mAlto) mAlto.textContent = mBasico ? alto : (alto + basico);
  if(mBasico) mBasico.textContent = basico;
  if(mBajo) mBajo.textContent = bajo;
}

function guardarNotasModulo() {
  if (esRolEstudiante()) {
    showToast('Los estudiantes tienen acceso de solo lectura a las notas', 'error');
    return;
  }
  const combo = document.getElementById('comboCursoNotas');
  const comboPeriodo = document.getElementById('comboPeriodoNotas');
  if(!combo || !comboPeriodo) return;

  const idCurso = parseInt(combo.value);
  const periodo = parseInt(comboPeriodo.value);
  const year = obtenerAnioNotasSeleccionado();

  const rows = document.querySelectorAll('#tbodyNotas tr');
  if (rows.length === 0 || !rows[0].dataset.idEstudiante) {
    showToast('No hay notas para guardar', 'error');
    return;
  }

  let notasData = [...estadoLocal.notas];
  notasData = notasData.filter(n => !(n.id_curso === idCurso && n.periodo === periodo && n.year === year));

  let nextId = notasData.length > 0 ? Math.max(...notasData.map(n => n.id_nota || 0)) + 1 : 1;

  rows.forEach(tr => {
    const idEstudiante = parseInt(tr.dataset.idEstudiante);
    if (!idEstudiante) return;

    const n1 = tr.querySelector('.n1').value;
    const n2 = tr.querySelector('.n2').value;
    const n3 = tr.querySelector('.n3').value;
    const n4 = tr.querySelector('.n4').value;
    const prom = parseFloat(tr.querySelector('.promedio-td').textContent);

    notasData.push({
      id_nota: nextId++,
      id_estudiante: idEstudiante,
      id_curso: idCurso,
      periodo: periodo,
      year: year,
      nota1: n1 ? parseFloat(n1) : null,
      nota2: n2 ? parseFloat(n2) : null,
      nota3: n3 ? parseFloat(n3) : null,
      nota4: n4 ? parseFloat(n4) : null,
      promedio: prom
    });
  });

  guardarNotas(notasData);
  showToast('¡Notas guardadas y sincronizadas en la nube en tiempo real!');
}

function exportarNotasCSV() {
  const idCurso = parseInt(document.getElementById('comboCursoNotas').value);
  const periodo = parseInt(document.getElementById('comboPeriodoNotas').value);
  const year = obtenerAnioNotasSeleccionado();

  let notasCurso = estadoLocal.notas.filter(n => n.id_curso === idCurso && n.periodo === periodo && n.year === year);
  if (esRolEstudiante()) {
    notasCurso = notasCurso.filter(n => n.id_estudiante === usuarioActual.id_estudiante);
  }

  if (notasCurso.length === 0) {
    showToast('No hay notas para exportar', 'error');
    return;
  }

  const mapaEstudiantes = {};
  estadoLocal.estudiantes.forEach(e => mapaEstudiantes[e.id_estudiante] = e.nombre_completo);

  let csvContent = '\uFEFF';
  csvContent += 'Estudiante,Nota 1,Nota 2,Nota 3,Nota 4,Promedio,Desempeno\n';

  notasCurso.forEach(n => {
    const nombre = mapaEstudiantes[n.id_estudiante] || 'Desconocido';
    const desem = obtenerDesempeno(n.promedio);
    const fila = [
      `"${nombre}"`,
      `"${n.nota1 || ''}"`,
      `"${n.nota2 || ''}"`,
      `"${n.nota3 || ''}"`,
      `"${n.nota4 || ''}"`,
      `"${n.promedio}"`,
      `"${desem.texto}"`
    ];
    csvContent += fila.join(',') + '\n';
  });

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.setAttribute('href', url);
  link.setAttribute('download', `Notas_Curso_${idCurso}_Periodo_${periodo}_${year}.csv`);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  showToast('Notas CSV descargado con éxito');
}

// ==========================================================================
// MÓDULO 5: PERFIL (PROFESOR VS ESTUDIANTE)
// ==========================================================================
function cargarPerfil() {
  if(!usuarioActual) return;
  const p = usuarioActual;
  
  const pTitulo = document.getElementById('perfilTitulo');
  const pSub = document.getElementById('perfilSubtitulo');
  const pRolTexto = document.getElementById('perfilRolTexto');
  const pSecInfo = document.getElementById('perfilSeccionInfoTitulo');
  const pLblDepto = document.getElementById('perfilLabelDepto');
  const pLblEsp = document.getElementById('perfilLabelEsp');
  const pSecBio = document.getElementById('perfilSeccionBioTitulo');

  const pNombre = document.getElementById('perfilNombre');
  const pAvatar = document.getElementById('perfilAvatarTexto');
  const pEmail = document.getElementById('perfilEmail');
  const pTel = document.getElementById('perfilTelefono');
  const pDepto = document.getElementById('perfilDepartamento');
  const pEsp = document.getElementById('perfilEspecialidad');
  const pBio = document.getElementById('perfilBio');

  if (esRolEstudiante()) {
    const curso = estadoLocal.cursos.find(c => c.id_curso === p.id_curso);
    const prof = curso ? estadoLocal.profesores.find(pr => pr.id_profesor === curso.id_profesor) : null;
    if (pTitulo) pTitulo.textContent = 'Mi Ficha Estudiantil';
    if (pSub) pSub.textContent = 'Información de matrícula académica y curso asignado';
    if (pRolTexto) pRolTexto.textContent = `🎓 Estudiante Activo — Curso ${curso ? curso.nombre_curso : 'N/A'}`;
    if (pSecInfo) pSecInfo.textContent = 'Datos de Matrícula';
    if (pLblDepto) pLblDepto.textContent = 'Curso y Grado';
    if (pLblEsp) pLblEsp.textContent = 'Jornada y Director de Grupo';
    if (pSecBio) pSecBio.textContent = 'Estado Académico';

    if (pNombre) pNombre.textContent = p.nombre_completo || 'Estudiante';
    if (pAvatar) pAvatar.textContent = (p.nombre_completo || 'E').charAt(0).toUpperCase();
    if (pEmail) pEmail.textContent = p.correo || 'estudiante@institucion.edu.co';
    if (pTel) pTel.textContent = `Documento: ${p.documento || 'N/A'}`;
    if (pDepto) pDepto.textContent = curso ? `Grado ${curso.nombre_curso}` : 'No asignado';
    if (pEsp) pEsp.textContent = `${curso ? curso.jornada : ''} — Prof. ${prof ? prof.nombre_completo : 'Asignado'}`;
    if (pBio) pBio.textContent = `Estudiante matriculado activamente en el curso ${curso ? curso.nombre_curso : ''}. Cuenta con acceso en tiempo real a su registro de asistencia y boletín de calificaciones.`;
    return;
  }

  if (pTitulo) pTitulo.textContent = 'Mi Perfil Docente';
  if (pSub) pSub.textContent = 'Información profesional y cursos a cargo';
  if (pRolTexto) pRolTexto.textContent = `👨‍🏫 Docente Titular (${p.usuario || ''})`;
  if (pSecInfo) pSecInfo.textContent = 'Información Profesional';
  if (pLblDepto) pLblDepto.textContent = 'Departamento';
  if (pLblEsp) pLblEsp.textContent = 'Especialidad';
  if (pSecBio) pSecBio.textContent = 'Educación y Biografía';

  if(pNombre) pNombre.textContent = p.nombre_completo || 'No especificado';
  if(pAvatar) pAvatar.textContent = (p.nombre_completo || 'P').charAt(0).toUpperCase();
  if(pEmail) pEmail.textContent = p.correo || 'No especificado';
  if(pTel) pTel.textContent = p.telefono || 'No especificado';
  if(pDepto) pDepto.textContent = p.departamento || 'No especificado';
  if(pEsp) pEsp.textContent = p.especialidad || 'No especificado';
  if(pBio) pBio.textContent = p.bio || 'No especificado';
}

// ==========================================================================
// MÓDULO 6: CONTACTO
// ==========================================================================
function enviarFormularioContacto(e) {
  e.preventDefault();
  const form = document.getElementById('formContacto');
  if(!form) return;

  const nombre = document.getElementById('contactoNombre').value.trim();
  const asunto = document.getElementById('contactoAsunto').value.trim();
  const mensaje = document.getElementById('contactoMensaje').value.trim();

  if(!nombre || !asunto || !mensaje) {
    showToast('Por favor, complete todos los campos', 'error');
    return;
  }

  const contactoMsg = {
    fecha: new Date().toISOString(),
    id_profesor: usuarioActual ? usuarioActual.id_profesor : null,
    nombre: nombre,
    asunto: asunto,
    mensaje: mensaje
  };

  let mensajes = JSON.parse(localStorage.getItem('asistencia_contactos') || '[]');
  mensajes.push(contactoMsg);
  localStorage.setItem('asistencia_contactos', JSON.stringify(mensajes));

  showToast('Mensaje enviado exitosamente');
  form.reset();
}

// ==========================================================================
// UTILIDADES Y MODALES
// ==========================================================================
function abrirModal(idModal) {
  const modal = document.getElementById(idModal);
  if (modal) {
    modal.style.display = 'flex';
    modal.classList.add('active');
  }
}

function cerrarModales() {
  document.querySelectorAll('.modal-backdrop').forEach(m => {
    m.classList.remove('active');
    m.style.display = 'none';
  });
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
  if(!container) return;
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

function actualizarFechaHora() {
  const headerTime = document.getElementById('headerDateTime');
  if(headerTime) {
    const ahora = new Date();
    headerTime.textContent = ahora.toLocaleString();
  }
}

function toggleSidebar() {
  const sidebar = document.querySelector('.sidebar');
  if(sidebar) {
    sidebar.classList.toggle('active');
  }
}
