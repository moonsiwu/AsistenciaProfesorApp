/**
 * ============================================================================
 * SERVIDOR DE SINCRONIZACIÓN EN LA NUBE (Compatible con Firebase REST API)
 * Sincroniza en tiempo real la Aplicación Web y la Aplicación de Escritorio Java
 * ============================================================================
 */

const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');
const os = require('os');

const PORT = process.env.PORT ? parseInt(process.env.PORT, 10) : 8080;
const DB_FILE = path.join(__dirname, 'cloud_db.json');
const CONFIG_JSON_FILE = path.join(__dirname, 'cloud_config.json');
const JAVA_CONFIG_PATHS = [
  path.join(__dirname, 'AsistenciaProfesorApp', 'src', 'main', 'resources', 'config.properties'),
  path.join(__dirname, 'AsistenciaProfesorApp', 'bin', 'config.properties')
];

// Clientes conectados por Server-Sent Events (SSE) para tiempo real instantáneo
const sseClients = new Set();
let dbVersion = Date.now();

function obtenerDatosIniciales() {
  const hoy = new Date().toISOString().split('T')[0];
  return {
    profesores: [
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
        bio: "Docente con más de 10 años de experiencia en educación secundaria...",
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
    ],
    cursos: [
      { id_curso: 1, nombre_curso: "10-A", jornada: "Mañana", id_profesor: 1 },
      { id_curso: 2, nombre_curso: "11-B", jornada: "Tarde", id_profesor: 1 },
      { id_curso: 3, nombre_curso: "9-A", jornada: "Mañana", id_profesor: 2 },
      { id_curso: 4, nombre_curso: "10-B", jornada: "Tarde", id_profesor: 2 }
    ],
    estudiantes: [
      { id_estudiante: 1, nombre_completo: "Ana María Gómez", documento: "1001234567", id_curso: 1, activo: 1 },
      { id_estudiante: 2, nombre_completo: "Juan Esteban Rojas", documento: "1001234568", id_curso: 1, activo: 1 },
      { id_estudiante: 3, nombre_completo: "Laura Sofía Díaz", documento: "1001234569", id_curso: 1, activo: 1 },
      { id_estudiante: 4, nombre_completo: "Miguel Ángel Torres", documento: "1001234570", id_curso: 2, activo: 1 },
      { id_estudiante: 5, nombre_completo: "Valentina Castro", documento: "1001234571", id_curso: 2, activo: 1 }
    ],
    asistencias: [
      { id_asistencia: 1, id_estudiante: 1, id_curso: 1, id_profesor: 1, fecha: hoy, estado: "PRESENTE", observacion: "Llegó puntual" },
      { id_asistencia: 2, id_estudiante: 2, id_curso: 1, id_profesor: 1, fecha: hoy, estado: "TARDE", observacion: "Ingreso 10 min tarde" },
      { id_asistencia: 3, id_estudiante: 3, id_curso: 1, id_profesor: 1, fecha: hoy, estado: "PRESENTE", observacion: "" }
    ],
    notas: [
      { id_nota: 1, id_estudiante: 1, id_curso: 1, periodo: 1, nota1: 4.5, nota2: 3.8, nota3: 4.2, nota4: 4.0, promedio: 4.13, year: 2026 },
      { id_nota: 2, id_estudiante: 2, id_curso: 1, periodo: 1, nota1: 3.0, nota2: 2.8, nota3: 3.5, nota4: 3.2, promedio: 3.13, year: 2026 },
      { id_nota: 3, id_estudiante: 3, id_curso: 1, periodo: 1, nota1: 4.8, nota2: 4.9, nota3: 5.0, nota4: 4.7, promedio: 4.85, year: 2026 },
      { id_nota: 4, id_estudiante: 4, id_curso: 2, periodo: 1, nota1: 3.5, nota2: 3.0, nota3: 3.8, nota4: 3.3, promedio: 3.40, year: 2026 },
      { id_nota: 5, id_estudiante: 5, id_curso: 2, periodo: 1, nota1: 2.5, nota2: 2.0, nota3: 2.8, nota4: 2.3, promedio: 2.40, year: 2026 }
    ]
  };
}

function normalizarColeccion(col) {
  if (!col) return [];
  if (Array.isArray(col)) return col.filter(item => item !== null && item !== undefined);
  if (typeof col === 'object') return Object.values(col).filter(item => item !== null && item !== undefined);
  return [];
}

function normalizarDB(data) {
  const base = obtenerDatosIniciales();
  if (!data || typeof data !== 'object') return base;
  return {
    profesores: data.profesores ? normalizarColeccion(data.profesores) : base.profesores,
    cursos: data.cursos ? normalizarColeccion(data.cursos) : [],
    estudiantes: data.estudiantes ? normalizarColeccion(data.estudiantes) : [],
    asistencias: data.asistencias ? normalizarColeccion(data.asistencias) : [],
    notas: data.notas ? normalizarColeccion(data.notas) : base.notas
  };
}

function leerDB() {
  try {
    if (fs.existsSync(DB_FILE)) {
      const raw = fs.readFileSync(DB_FILE, 'utf8');
      return normalizarDB(JSON.parse(raw));
    }
  } catch (e) {
    console.error('[Nube] Error leyendo cloud_db.json:', e.message);
  }
  const inicial = obtenerDatosIniciales();
  guardarDB(inicial, false);
  return inicial;
}

function guardarDB(datos, notificar = true) {
  const normalizados = normalizarDB(datos);
  dbVersion = Date.now();
  try {
    fs.writeFileSync(DB_FILE, JSON.stringify(normalizados, null, 2), 'utf8');
  } catch (e) {
    console.error('[Nube] Error escribiendo cloud_db.json:', e.message);
  }

  if (notificar) {
    notificarClientesSSE(normalizados);
    replicarAFirebaseExterno(normalizados);
  }
  return normalizados;
}

function leerConfigNube() {
  try {
    if (fs.existsSync(CONFIG_JSON_FILE)) {
      return JSON.parse(fs.readFileSync(CONFIG_JSON_FILE, 'utf8'));
    }
  } catch (e) {
    // Ignorar error
  }
  return {
    firebaseUrl: '',
    cloudServerUrl: `http://localhost:${PORT}`,
    modoActivo: 'CLOUD_SYNC'
  };
}

function guardarConfigNube(nuevaConfig) {
  const actual = leerConfigNube();
  const actualizada = {
    ...actual,
    ...nuevaConfig,
    cloudServerUrl: `http://localhost:${PORT}`,
    updatedAt: new Date().toISOString()
  };
  fs.writeFileSync(CONFIG_JSON_FILE, JSON.stringify(actualizada, null, 2), 'utf8');

  // Sincronizar con config.properties de la aplicación Java
  const urlEfectiva = (actualizada.firebaseUrl && actualizada.firebaseUrl.trim())
    ? actualizada.firebaseUrl.trim().replace(/\/+$/, '')
    : `http://localhost:${PORT}`;

  actualizarConfigPropertiesJava(urlEfectiva);
  return actualizada;
}

function actualizarConfigPropertiesJava(cloudUrl) {
  const contenido = [
    '# Configuracion de conexion Hibrida: Nube (Firebase / Cloud Sync) + MySQL',
    'db.modo=NUBE',
    `cloud.url=${cloudUrl}`,
    'db.driver=com.mysql.cj.jdbc.Driver',
    'db.url=jdbc:mysql://localhost:3306/asistencia_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC',
    'db.usuario=root',
    'db.contrasena=123456',
    ''
  ].join('\n');

  for (const configPath of JAVA_CONFIG_PATHS) {
    try {
      const dir = path.dirname(configPath);
      if (fs.existsSync(dir)) {
        fs.writeFileSync(configPath, contenido, 'utf8');
      }
    } catch (e) {
      console.warn('[Nube] No se pudo actualizar:', configPath, e.message);
    }
  }
}

const PUBLIC_CLOUD_TOPIC = 'eduasistencia_sync_moonsiwu_2026_v1';
const PUBLIC_CLOUD_URL = `https://ntfy.sh/${PUBLIC_CLOUD_TOPIC}`;
const SERVER_SENDER_ID = 'node_srv_' + Math.random().toString(36).slice(2, 10);

function replicarAFirebaseExterno(datos) {
  // 1. Replicar siempre al canal público HTTPS en internet (para sincronizar con GitHub Pages en otras PCs/celulares)
  fetch(PUBLIC_CLOUD_URL, {
    method: 'POST',
    body: JSON.stringify({
      v: dbVersion,
      sender: SERVER_SENDER_ID,
      ...datos
    })
  }).catch(() => {});

  // 2. Replicar también a Firebase Realtime Database si el usuario configuró una URL propia
  const cfg = leerConfigNube();
  const fbUrl = (cfg.firebaseUrl || '').trim().replace(/\/+$/, '');
  if (!fbUrl || !fbUrl.startsWith('https://') || fbUrl.includes('localhost')) {
    return;
  }

  try {
    const endpoint = new URL(`${fbUrl}/asistencia_db.json`);
    const payload = JSON.stringify(datos);
    const req = https.request(
      endpoint,
      {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json; charset=utf-8',
          'Content-Length': Buffer.byteLength(payload)
        },
        timeout: 6000
      },
      (res) => {
        res.resume();
      }
    );
    req.on('error', (err) => {
      console.warn('[Nube] Error replicando a Firebase externo:', err.message);
    });
    req.write(payload);
    req.end();
  } catch (e) {
    console.warn('[Nube] URL de Firebase inválida para replicación:', e.message);
  }
}

async function iniciarEscuchaNubePublica() {
  try {
    // 1. Cargar estado más reciente de internet al iniciar
    const pollRes = await fetch(`${PUBLIC_CLOUD_URL}/json?poll=1&since=all`);
    if (pollRes.ok) {
      const txt = await pollRes.text();
      const lines = txt.trim().split('\n').filter(Boolean);
      for (let i = lines.length - 1; i >= 0; i--) {
        try {
          const evt = JSON.parse(lines[i]);
          if (evt.event === 'message' && evt.message) {
            const parsed = JSON.parse(evt.message);
            if (parsed && (parsed.cursos || parsed.estudiantes)) {
              guardarDB(parsed, false);
              break;
            }
          }
        } catch (e) {}
      }
    }
  } catch (e) {
    // Sin internet temporalmente
  }

  // 2. Escuchar eventos cada 4 segundos para mantener cloud_db.json y la App Java al día con GitHub Pages
  setInterval(async () => {
    try {
      const res = await fetch(`${PUBLIC_CLOUD_URL}/json?poll=1&since=10s`);
      if (!res.ok) return;
      const txt = await res.text();
      const lines = txt.trim().split('\n').filter(Boolean);
      for (let i = lines.length - 1; i >= 0; i--) {
        const evt = JSON.parse(lines[i]);
        if (evt.event === 'message' && evt.message) {
          const parsed = JSON.parse(evt.message);
          if (parsed && parsed.sender !== SERVER_SENDER_ID && parsed.v && parsed.v > dbVersion) {
            dbVersion = parsed.v;
            const norm = normalizarDB(parsed);
            fs.writeFileSync(DB_FILE, JSON.stringify(norm, null, 2), 'utf8');
            notificarClientesSSE(norm);
            break;
          }
        }
      }
    } catch (e) {}
  }, 4000);
}

function notificarClientesSSE(datos) {
  const msg = `data: ${JSON.stringify({ version: dbVersion, data: datos })}\n\n`;
  for (const res of sseClients) {
    try {
      res.write(msg);
    } catch (e) {
      sseClients.delete(res);
    }
  }
}

function obtenerIpLocal() {
  const interfaces = os.networkInterfaces();
  for (const name of Object.keys(interfaces)) {
    for (const iface of interfaces[name]) {
      if (iface.family === 'IPv4' && !iface.internal) {
        return iface.address;
      }
    }
  }
  return 'localhost';
}

function leerBodyJSON(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString('utf8');
    });
    req.on('end', () => {
      if (!body || !body.trim()) return resolve(null);
      try {
        resolve(JSON.parse(body));
      } catch (e) {
        reject(e);
      }
    });
    req.on('error', reject);
  });
}

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
  '.png': 'image/png',
  '.ico': 'image/x-icon'
};

// Inicializar base de datos, configuración y sincronización pública en internet al arrancar
leerDB();
guardarConfigNube(leerConfigNube());
iniciarEscuchaNubePublica();

const server = http.createServer(async (req, res) => {
  // Cabeceras CORS y Private Network Access (PNA) para permitir conexión desde https://moonsiwu.github.io, file:// y localhost
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, Accept');
  res.setHeader('Access-Control-Allow-Private-Network', 'true');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  const pathname = parsedUrl.pathname;

  // 1. Endpoint SSE para actualizaciones en tiempo real
  if (pathname === '/api/events' && req.method === 'GET') {
    res.writeHead(200, {
      'Content-Type': 'text/event-stream; charset=utf-8',
      'Cache-Control': 'no-cache',
      'Connection': 'keep-alive'
    });
    const dbActual = leerDB();
    res.write(`data: ${JSON.stringify({ version: dbVersion, data: dbActual })}\n\n`);
    sseClients.add(res);
    req.on('close', () => sseClients.delete(res));
    return;
  }

  // 2. Estado del servidor en la nube
  if (pathname === '/api/status' && req.method === 'GET') {
    const cfg = leerConfigNube();
    const ip = obtenerIpLocal();
    res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify({
      ok: true,
      version: dbVersion,
      localUrl: `http://localhost:${PORT}`,
      lanUrl: `http://${ip}:${PORT}`,
      firebaseUrl: cfg.firebaseUrl || '',
      clientesConectados: sseClients.size
    }));
    return;
  }

  // 3. Configuración compartida Web + Java
  if (pathname === '/api/config') {
    if (req.method === 'GET') {
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify(leerConfigNube()));
      return;
    }
    if (req.method === 'POST' || req.method === 'PUT') {
      try {
        const body = await leerBodyJSON(req);
        const nueva = guardarConfigNube(body || {});
        if (nueva.firebaseUrl) {
          replicarAFirebaseExterno(leerDB());
        }
        res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify({ ok: true, config: nueva }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify({ ok: false, error: e.message }));
      }
      return;
    }
  }

  // 4. API REST Compatible con Firebase Realtime Database (/asistencia_db.json y /asistencia_db/:tabla.json)
  if (pathname === '/asistencia_db.json' || pathname === '/asistencia_db') {
    const dbActual = leerDB();
    if (req.method === 'GET') {
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify(dbActual));
      return;
    }
    if (req.method === 'PUT') {
      try {
        const body = await leerBodyJSON(req);
        const guardado = guardarDB(body || {});
        res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify(guardado));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify({ error: e.message }));
      }
      return;
    }
    if (req.method === 'PATCH') {
      try {
        const body = await leerBodyJSON(req);
        const combinado = { ...dbActual, ...(body || {}) };
        const guardado = guardarDB(combinado);
        res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify(guardado));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify({ error: e.message }));
      }
      return;
    }
  }

  const matchTabla = pathname.match(/^\/asistencia_db\/([a-zA-Z0-9_]+?)(?:\.json)?$/);
  if (matchTabla) {
    const tabla = matchTabla[1];
    const dbActual = leerDB();
    if (req.method === 'GET') {
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify(dbActual[tabla] !== undefined ? dbActual[tabla] : []));
      return;
    }
    if (req.method === 'PUT' || req.method === 'POST') {
      try {
        const body = await leerBodyJSON(req);
        dbActual[tabla] = normalizarColeccion(body);
        guardarDB(dbActual);
        res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify(dbActual[tabla]));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json; charset=utf-8' });
        res.end(JSON.stringify({ error: e.message }));
      }
      return;
    }
  }

  // 5. Servidor de archivos estáticos para la aplicación Web
  let safePath = pathname === '/' ? '/index.html' : pathname;
  const filePath = path.join(__dirname, path.normalize(safePath));
  if (filePath.startsWith(__dirname) && fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    res.writeHead(200, { 'Content-Type': contentType });
    fs.createReadStream(filePath).pipe(res);
    return;
  }

  res.writeHead(404, { 'Content-Type': 'application/json; charset=utf-8' });
  res.end(JSON.stringify({ error: 'Ruta no encontrada' }));
});

server.listen(PORT, '0.0.0.0', () => {
  const ip = obtenerIpLocal();
  console.log('===================================================================');
  console.log(' ☁️  SERVIDOR NUBE EDUASISTENCIA ACTIVO (Tiempo Real Web + Java)');
  console.log('===================================================================');
  console.log(` 🌐 Portal Web Local:       http://localhost:${PORT}`);
  console.log(` 📱 Portal en Red (Wi-Fi):  http://${ip}:${PORT}`);
  console.log(` 🔗 Endpoint REST Firebase: http://localhost:${PORT}/asistencia_db.json`);
  console.log('===================================================================');
});
