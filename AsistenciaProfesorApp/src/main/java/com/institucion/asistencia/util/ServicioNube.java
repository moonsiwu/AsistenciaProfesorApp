package com.institucion.asistencia.util;

import com.institucion.asistencia.model.Asistencia;
import com.institucion.asistencia.model.Curso;
import com.institucion.asistencia.model.Estudiante;
import com.institucion.asistencia.model.Profesor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Cliente de sincronización en tiempo real con la Nube (Firebase Realtime Database
 * y Servidor Nube Sincronizado Web + Java).
 *
 * Permite que la aplicación de escritorio en Java y la aplicación Web (js/app.js)
 * compartan exactamente la misma base de datos en tiempo real sin requerir MySQL local.
 */
public final class ServicioNube {

    private static final String DEFAULT_CLOUD_URL = "http://localhost:8080";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    private static String cloudUrl = DEFAULT_CLOUD_URL;
    private static boolean modoNubeHabilitado = true;
    private static boolean ultimoEstadoConectado = false;
    private static long ultimaVerificacionMs = 0;

    static {
        cargarConfiguracion();
        asegurarServidorLocalSiAplica();
    }

    private ServicioNube() {
    }

    public static synchronized void cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream in = ServicioNube.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
        }

        // Verificar también archivo en disco para cambios en caliente
        File[] posiblesConfigs = new File[]{
                new File("src/main/resources/config.properties"),
                new File("AsistenciaProfesorApp/src/main/resources/config.properties"),
                new File("bin/config.properties")
        };
        for (File f : posiblesConfigs) {
            if (f.exists() && f.isFile()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    props.load(fis);
                    break;
                } catch (Exception ignored) {
                }
            }
        }

        String urlProp = props.getProperty("cloud.url");
        if (urlProp != null && !urlProp.isBlank()) {
            cloudUrl = limpiarUrl(urlProp);
        }

        String modoProp = props.getProperty("db.modo", "NUBE");
        modoNubeHabilitado = !"MYSQL_ONLY".equalsIgnoreCase(modoProp.trim());
    }

    private static String limpiarUrl(String raw) {
        if (raw == null) return DEFAULT_CLOUD_URL;
        String u = raw.trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        if (u.endsWith("/asistencia_db.json")) {
            u = u.substring(0, u.length() - "/asistencia_db.json".length());
        } else if (u.endsWith("/asistencia_db")) {
            u = u.substring(0, u.length() - "/asistencia_db".length());
        }
        return u.isEmpty() ? DEFAULT_CLOUD_URL : u;
    }

    /**
     * Si la URL configurada es localhost:8080 y el servidor nube local no está corriendo,
     * intenta iniciarlo automáticamente en segundo plano para garantizar sincronización inmediata.
     */
    public static void asegurarServidorLocalSiAplica() {
        if (!modoNubeHabilitado) return;
        if (probarConexionRapida(cloudUrl)) {
            ultimoEstadoConectado = true;
            return;
        }
        if (cloudUrl.contains("localhost:8080") || cloudUrl.contains("127.0.0.1:8080")) {
            File script = buscarArchivoProyecto("servidor-nube.js");
            if (script != null && script.exists()) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("node", script.getAbsolutePath());
                    pb.directory(script.getParentFile());
                    pb.redirectErrorStream(true);
                    pb.start();
                    // Esperar brevemente a que levante el puerto 8080
                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(150);
                        if (probarConexionRapida(cloudUrl)) {
                            ultimoEstadoConectado = true;
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[ServicioNube] Aviso al iniciar servidor nube local: " + e.getMessage());
                }
            }
        }
    }

    private static File buscarArchivoProyecto(String nombreArchivo) {
        File[] candidatos = new File[]{
                new File(nombreArchivo),
                new File("..", nombreArchivo),
                new File(System.getProperty("user.dir"), nombreArchivo),
                new File(new File(System.getProperty("user.dir")).getParentFile(), nombreArchivo)
        };
        for (File c : candidatos) {
            if (c != null && c.exists() && c.isFile()) {
                return c;
            }
        }
        return null;
    }

    public static boolean esModoNubeActivo() {
        return modoNubeHabilitado;
    }

    public static String obtenerUrlActual() {
        return cloudUrl;
    }

    public static boolean esFirebaseExterno() {
        return cloudUrl != null && (cloudUrl.contains("firebaseio.com") || cloudUrl.contains("firebasedatabase.app"));
    }

    public static boolean verificarConexion() {
        long ahora = System.currentTimeMillis();
        if (ahora - ultimaVerificacionMs < 1500) {
            return ultimoEstadoConectado;
        }
        ultimaVerificacionMs = ahora;
        ultimoEstadoConectado = probarConexionRapida(cloudUrl);
        return ultimoEstadoConectado;
    }

    public static boolean probarConexionRapida(String baseUrl) {
        try {
            String endpoint = limpiarUrl(baseUrl) + "/asistencia_db/profesores.json";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> res = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return res.statusCode() >= 200 && res.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Guarda una nueva URL de base de datos en la nube (por ejemplo Firebase Realtime Database)
     * y la sincroniza tanto con config.properties como con el servidor Web local.
     */
    public static boolean configurarNuevaUrlNube(String nuevaUrl) {
        String limpia = limpiarUrl(nuevaUrl);
        cloudUrl = limpia;

        // 1. Guardar en archivos config.properties
        File[] configs = new File[]{
                new File("src/main/resources/config.properties"),
                new File("../AsistenciaProfesorApp/src/main/resources/config.properties"),
                new File("bin/config.properties"),
                new File("../AsistenciaProfesorApp/bin/config.properties")
        };

        String contenido = "# Configuracion de conexion Hibrida: Nube (Firebase / Cloud Sync) + MySQL\n"
                + "db.modo=NUBE\n"
                + "cloud.url=" + limpia + "\n"
                + "db.driver=com.mysql.cj.jdbc.Driver\n"
                + "db.url=jdbc:mysql://localhost:3306/asistencia_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC\n"
                + "db.usuario=root\n"
                + "db.contrasena=123456\n";

        for (File f : configs) {
            try {
                if (f.getParentFile() != null && f.getParentFile().exists()) {
                    try (FileOutputStream fos = new FileOutputStream(f)) {
                        fos.write(contenido.getBytes(StandardCharsets.UTF_8));
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Notificar al servidor Web local si está activo para que la Web también use esta URL
        try {
            String payload = "{\"firebaseUrl\":\"" + escaparJson(limpia.contains("localhost") ? "" : limpia) + "\"}";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(DEFAULT_CLOUD_URL + "/api/config"))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .timeout(Duration.ofSeconds(2))
                    .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                    .build();
            HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }

        ultimaVerificacionMs = 0;
        boolean ok = verificarConexion();

        // Si es un Firebase nuevo que está vacío, inicializarlo con los datos actuales
        if (ok && esFirebaseExterno()) {
            inicializarFirebaseSiVacio();
        }
        return ok;
    }

    private static void inicializarFirebaseSiVacio() {
        try {
            String raw = hacerGet("/asistencia_db.json");
            if (raw == null || raw.trim().isEmpty() || "null".equals(raw.trim())) {
                File localDb = buscarArchivoProyecto("cloud_db.json");
                if (localDb != null && localDb.exists()) {
                    String contenidoInicial = Files.readString(localDb.toPath(), StandardCharsets.UTF_8);
                    hacerPut("/asistencia_db.json", contenidoInicial);
                }
            }
        } catch (Exception ignored) {
        }
    }

    // =========================================================================
    // OPERACIONES HTTP / ARCHIVO DE RESPALDO
    // =========================================================================

    private static String hacerGet(String rutaRelativa) throws IOException {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(cloudUrl + rutaRelativa))
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();
            HttpResponse<String> res = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                ultimoEstadoConectado = true;
                return res.body();
            }
            throw new IOException("HTTP " + res.statusCode() + " desde " + cloudUrl + rutaRelativa);
        } catch (Exception e) {
            ultimoEstadoConectado = false;
            throw new IOException("No se pudo conectar a la nube (" + cloudUrl + "): " + e.getMessage(), e);
        }
    }

    private static void hacerPut(String rutaRelativa, String jsonBody) throws IOException {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(cloudUrl + rutaRelativa))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .timeout(Duration.ofSeconds(5))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> res = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                ultimoEstadoConectado = true;
                // Si estamos escribiendo en Firebase externo, actualizar también el servidor local si corre
                if (!cloudUrl.equals(DEFAULT_CLOUD_URL)) {
                    replicarEnServidorLocalSilencioso(rutaRelativa, jsonBody);
                }
                return;
            }
            throw new IOException("HTTP " + res.statusCode() + " al guardar en " + cloudUrl + rutaRelativa);
        } catch (Exception e) {
            ultimoEstadoConectado = false;
            throw new IOException("No se pudo guardar en la nube (" + cloudUrl + "): " + e.getMessage(), e);
        }
    }

    private static void replicarEnServidorLocalSilencioso(String rutaRelativa, String jsonBody) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(DEFAULT_CLOUD_URL + rutaRelativa))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .timeout(Duration.ofSeconds(2))
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();
            HTTP_CLIENT.sendAsync(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> obtenerColeccion(String nombreColeccion) throws IOException {
        try {
            String json = hacerGet("/asistencia_db/" + nombreColeccion + ".json");
            Object parsed = SimpleJson.parse(json);
            return normalizarListaMapas(parsed);
        } catch (IOException ex) {
            // Respaldo: leer de cloud_db.json local si existe
            File localDb = buscarArchivoProyecto("cloud_db.json");
            if (localDb != null && localDb.exists()) {
                String raw = Files.readString(localDb.toPath(), StandardCharsets.UTF_8);
                Object root = SimpleJson.parse(raw);
                if (root instanceof Map) {
                    Object col = ((Map<String, Object>) root).get(nombreColeccion);
                    return normalizarListaMapas(col);
                }
            }
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private static void guardarColeccion(String nombreColeccion, String jsonArray) throws IOException {
        try {
            hacerPut("/asistencia_db/" + nombreColeccion + ".json", jsonArray);
        } catch (IOException ex) {
            // Guardar también en cloud_db.json local si el servidor no estaba disponible
            File localDb = buscarArchivoProyecto("cloud_db.json");
            if (localDb != null && localDb.exists()) {
                String raw = Files.readString(localDb.toPath(), StandardCharsets.UTF_8);
                Object root = SimpleJson.parse(raw);
                if (root instanceof Map) {
                    Map<String, Object> mapRoot = (Map<String, Object>) root;
                    mapRoot.put(nombreColeccion, SimpleJson.parse(jsonArray));
                    Files.writeString(localDb.toPath(), SimpleJson.stringify(mapRoot), StandardCharsets.UTF_8);
                    return;
                }
            }
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> normalizarListaMapas(Object parsed) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        if (parsed instanceof List) {
            for (Object item : (List<?>) parsed) {
                if (item instanceof Map) {
                    resultado.add((Map<String, Object>) item);
                }
            }
        } else if (parsed instanceof Map) {
            for (Object item : ((Map<?, ?>) parsed).values()) {
                if (item instanceof Map) {
                    resultado.add((Map<String, Object>) item);
                }
            }
        }
        return resultado;
    }

    // =========================================================================
    // MÉTODOS DE NEGOCIO PARA LOS DAOs (PROFESORES, CURSOS, ESTUDIANTES, ASISTENCIAS)
    // =========================================================================

    public static Profesor autenticarProfesor(String usuario, String contrasenaPlano) throws IOException {
        List<Map<String, Object>> profesores = obtenerColeccion("profesores");
        for (Map<String, Object> m : profesores) {
            String u = comoTexto(m.get("usuario"));
            String passGuardada = comoTexto(m.get("contrasena"));
            if (usuario.equalsIgnoreCase(u)) {
                boolean coincide = contrasenaPlano.equals(passGuardada)
                        || PasswordUtil.coincide(contrasenaPlano, passGuardada);
                if (coincide) {
                    Profesor p = new Profesor();
                    p.setIdProfesor(comoEntero(m.get("id_profesor"), 1));
                    p.setNombreCompleto(comoTexto(m.get("nombre_completo")));
                    p.setUsuario(u);
                    p.setCorreo(comoTexto(m.get("correo")));
                    return p;
                }
            }
        }
        return null;
    }

    public static List<Curso> listarCursosPorProfesor(int idProfesor) throws IOException {
        List<Map<String, Object>> raw = obtenerColeccion("cursos");
        List<Curso> cursos = new ArrayList<>();
        for (Map<String, Object> m : raw) {
            int idProf = comoEntero(m.get("id_profesor"), 1);
            if (idProf == idProfesor) {
                cursos.add(new Curso(
                        comoEntero(m.get("id_curso"), 0),
                        comoTexto(m.get("nombre_curso")),
                        comoTexto(m.get("jornada")),
                        idProf
                ));
            }
        }
        return cursos;
    }

    public static Curso guardarCurso(Curso nuevo) throws IOException {
        List<Map<String, Object>> raw = obtenerColeccion("cursos");
        int maxId = 0;
        for (Map<String, Object> m : raw) {
            int id = comoEntero(m.get("id_curso"), 0);
            if (id > maxId) maxId = id;
        }
        nuevo.setIdCurso(maxId + 1);

        Map<String, Object> mapaNuevo = new LinkedHashMap<>();
        mapaNuevo.put("id_curso", nuevo.getIdCurso());
        mapaNuevo.put("nombre_curso", nuevo.getNombreCurso());
        mapaNuevo.put("jornada", nuevo.getJornada());
        mapaNuevo.put("id_profesor", nuevo.getIdProfesor());
        raw.add(mapaNuevo);

        guardarColeccion("cursos", SimpleJson.stringify(raw));
        return nuevo;
    }

    public static void eliminarCurso(int idCurso) throws IOException {
        List<Map<String, Object>> raw = obtenerColeccion("cursos");
        raw.removeIf(m -> comoEntero(m.get("id_curso"), 0) == idCurso);
        guardarColeccion("cursos", SimpleJson.stringify(raw));
    }

    public static List<Estudiante> listarEstudiantesPorCurso(int idCurso) throws IOException {
        List<Map<String, Object>> raw = obtenerColeccion("estudiantes");
        List<Estudiante> lista = new ArrayList<>();
        for (Map<String, Object> m : raw) {
            int cursoId = comoEntero(m.get("id_curso"), 0);
            boolean activo = comoActivo(m.get("activo"));
            if (cursoId == idCurso && activo) {
                lista.add(new Estudiante(
                        comoEntero(m.get("id_estudiante"), 0),
                        comoTexto(m.get("nombre_completo")),
                        comoTexto(m.get("documento")),
                        cursoId,
                        true
                ));
            }
        }
        return lista;
    }

    public static Estudiante guardarEstudiante(Estudiante est) throws IOException {
        List<Map<String, Object>> raw = obtenerColeccion("estudiantes");
        int maxId = 0;
        for (Map<String, Object> m : raw) {
            int id = comoEntero(m.get("id_estudiante"), 0);
            if (id > maxId) maxId = id;
            String doc = comoTexto(m.get("documento"));
            if (doc.equalsIgnoreCase(est.getDocumento()) && comoActivo(m.get("activo"))) {
                throw new IOException("Ya existe un estudiante activo con el documento " + est.getDocumento());
            }
        }
        est.setIdEstudiante(maxId + 1);
        est.setActivo(true);

        Map<String, Object> mapaNuevo = new LinkedHashMap<>();
        mapaNuevo.put("id_estudiante", est.getIdEstudiante());
        mapaNuevo.put("nombre_completo", est.getNombreCompleto());
        mapaNuevo.put("documento", est.getDocumento());
        mapaNuevo.put("id_curso", est.getIdCurso());
        mapaNuevo.put("activo", 1);
        raw.add(mapaNuevo);

        guardarColeccion("estudiantes", SimpleJson.stringify(raw));
        return est;
    }

    public static void actualizarEstudiante(Estudiante est) throws IOException {
        List<Map<String, Object>> raw = obtenerColeccion("estudiantes");
        for (Map<String, Object> m : raw) {
            if (comoEntero(m.get("id_estudiante"), 0) == est.getIdEstudiante()) {
                m.put("nombre_completo", est.getNombreCompleto());
                m.put("documento", est.getDocumento());
            }
        }
        guardarColeccion("estudiantes", SimpleJson.stringify(raw));
    }

    public static void desactivarEstudiante(int idEstudiante) throws IOException {
        List<Map<String, Object>> raw = obtenerColeccion("estudiantes");
        for (Map<String, Object> m : raw) {
            if (comoEntero(m.get("id_estudiante"), 0) == idEstudiante) {
                m.put("activo", 0);
            }
        }
        guardarColeccion("estudiantes", SimpleJson.stringify(raw));
    }

    public static void registrarAsistenciasLote(List<Asistencia> registros) throws IOException {
        if (registros == null || registros.isEmpty()) return;
        List<Map<String, Object>> raw = obtenerColeccion("asistencias");

        int maxId = 0;
        for (Map<String, Object> m : raw) {
            int id = comoEntero(m.get("id_asistencia"), 0);
            if (id > maxId) maxId = id;
        }

        for (Asistencia a : registros) {
            String fechaStr = a.getFecha().toString();
            // Eliminar registro previo del mismo estudiante y fecha para reemplazarlo
            raw.removeIf(m -> comoEntero(m.get("id_estudiante"), 0) == a.getIdEstudiante()
                    && fechaStr.equals(comoTexto(m.get("fecha"))));

            Map<String, Object> nuevo = new LinkedHashMap<>();
            nuevo.put("id_asistencia", ++maxId);
            nuevo.put("id_estudiante", a.getIdEstudiante());
            nuevo.put("id_curso", a.getIdCurso());
            nuevo.put("id_profesor", a.getIdProfesor());
            nuevo.put("fecha", fechaStr);
            nuevo.put("estado", a.getEstado().name());
            nuevo.put("observacion", a.getObservacion() != null ? a.getObservacion() : "");
            raw.add(nuevo);
        }

        guardarColeccion("asistencias", SimpleJson.stringify(raw));
    }

    public static List<Asistencia> listarAsistenciasPorCursoYFecha(int idCurso, LocalDate fecha) throws IOException {
        String fechaBuscada = fecha.toString();
        List<Map<String, Object>> rawAsist = obtenerColeccion("asistencias");
        List<Map<String, Object>> rawEst = obtenerColeccion("estudiantes");

        Map<Integer, String> nombresEstudiantes = new LinkedHashMap<>();
        for (Map<String, Object> e : rawEst) {
            nombresEstudiantes.put(comoEntero(e.get("id_estudiante"), 0), comoTexto(e.get("nombre_completo")));
        }

        List<Asistencia> resultado = new ArrayList<>();
        for (Map<String, Object> m : rawAsist) {
            if (comoEntero(m.get("id_curso"), 0) == idCurso && fechaBuscada.equals(comoTexto(m.get("fecha")))) {
                Asistencia a = mapearAsistencia(m, nombresEstudiantes);
                if (a != null) {
                    resultado.add(a);
                }
            }
        }
        return resultado;
    }

    public static List<Asistencia> reporteAsistenciasPorCurso(int idCurso, LocalDate desde, LocalDate hasta) throws IOException {
        List<Map<String, Object>> rawAsist = obtenerColeccion("asistencias");
        List<Map<String, Object>> rawEst = obtenerColeccion("estudiantes");

        Map<Integer, String> nombresEstudiantes = new LinkedHashMap<>();
        for (Map<String, Object> e : rawEst) {
            nombresEstudiantes.put(comoEntero(e.get("id_estudiante"), 0), comoTexto(e.get("nombre_completo")));
        }

        List<Asistencia> resultado = new ArrayList<>();
        for (Map<String, Object> m : rawAsist) {
            if (comoEntero(m.get("id_curso"), 0) == idCurso) {
                Asistencia a = mapearAsistencia(m, nombresEstudiantes);
                if (a != null && !a.getFecha().isBefore(desde) && !a.getFecha().isAfter(hasta)) {
                    resultado.add(a);
                }
            }
        }
        resultado.sort((a, b) -> {
            int cmp = a.getFecha().compareTo(b.getFecha());
            if (cmp != 0) return cmp;
            String na = a.getNombreEstudiante() != null ? a.getNombreEstudiante() : "";
            String nb = b.getNombreEstudiante() != null ? b.getNombreEstudiante() : "";
            return na.compareToIgnoreCase(nb);
        });
        return resultado;
    }

    private static Asistencia mapearAsistencia(Map<String, Object> m, Map<Integer, String> nombresEstudiantes) {
        try {
            Asistencia a = new Asistencia();
            int idEst = comoEntero(m.get("id_estudiante"), 0);
            a.setIdAsistencia(comoEntero(m.get("id_asistencia"), 0));
            a.setIdEstudiante(idEst);
            a.setNombreEstudiante(nombresEstudiantes.getOrDefault(idEst, "Estudiante #" + idEst));
            a.setIdCurso(comoEntero(m.get("id_curso"), 0));
            a.setIdProfesor(comoEntero(m.get("id_profesor"), 1));
            a.setFecha(LocalDate.parse(comoTexto(m.get("fecha"))));
            String estStr = comoTexto(m.get("estado")).toUpperCase();
            a.setEstado(Asistencia.Estado.valueOf(estStr.isEmpty() ? "PRESENTE" : estStr));
            a.setObservacion(comoTexto(m.get("observacion")));
            return a;
        } catch (Exception e) {
            return null;
        }
    }

    private static String comoTexto(Object val) {
        return val == null ? "" : String.valueOf(val);
    }

    private static int comoEntero(Object val, int defecto) {
        if (val == null) return defecto;
        if (val instanceof Number) return ((Number) val).intValue();
        try {
            return (int) Double.parseDouble(String.valueOf(val).trim());
        } catch (Exception e) {
            return defecto;
        }
    }

    private static boolean comoActivo(Object val) {
        if (val == null) return true;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        String s = String.valueOf(val).trim();
        return "1".equals(s) || "true".equalsIgnoreCase(s);
    }

    private static String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // =========================================================================
    // PARSER Y SERIALIZADOR JSON LIGERO (CERO DEPENDENCIAS EXTERNAS)
    // =========================================================================
    private static final class SimpleJson {
        private final String src;
        private int pos = 0;

        private SimpleJson(String src) {
            this.src = src != null ? src : "";
        }

        static Object parse(String json) {
            if (json == null || json.isBlank()) return null;
            return new SimpleJson(json).parseValue();
        }

        static String stringify(Object obj) {
            StringBuilder sb = new StringBuilder();
            writeJson(obj, sb);
            return sb.toString();
        }

        @SuppressWarnings("unchecked")
        private static void writeJson(Object obj, StringBuilder sb) {
            if (obj == null) {
                sb.append("null");
            } else if (obj instanceof String) {
                sb.append('"').append(escaparJson((String) obj)).append('"');
            } else if (obj instanceof Number || obj instanceof Boolean) {
                sb.append(obj.toString());
            } else if (obj instanceof List) {
                sb.append('[');
                List<?> list = (List<?>) obj;
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) sb.append(',');
                    writeJson(list.get(i), sb);
                }
                sb.append(']');
            } else if (obj instanceof Map) {
                sb.append('{');
                Map<?, ?> map = (Map<?, ?>) obj;
                boolean first = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!first) sb.append(',');
                    first = false;
                    sb.append('"').append(escaparJson(String.valueOf(entry.getKey()))).append("\":");
                    writeJson(entry.getValue(), sb);
                }
                sb.append('}');
            } else {
                sb.append('"').append(escaparJson(String.valueOf(obj))).append('"');
            }
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }

        private Object parseValue() {
            skipWhitespace();
            if (pos >= src.length()) return null;
            char c = src.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            return parseNumber();
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // '{'
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == '}') {
                pos++;
                return map;
            }
            while (pos < src.length()) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ':') pos++;
                Object val = parseValue();
                map.put(key, val);
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ',') {
                    pos++;
                } else if (pos < src.length() && src.charAt(pos) == '}') {
                    pos++;
                    break;
                } else {
                    break;
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // '['
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ']') {
                pos++;
                return list;
            }
            while (pos < src.length()) {
                list.add(parseValue());
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ',') {
                    pos++;
                } else if (pos < src.length() && src.charAt(pos) == ']') {
                    pos++;
                    break;
                } else {
                    break;
                }
            }
            return list;
        }

        private String parseString() {
            skipWhitespace();
            if (pos >= src.length() || src.charAt(pos) != '"') return "";
            pos++; // '"'
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\' && pos < src.length()) {
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (pos + 4 <= src.length()) {
                                String hex = src.substring(pos, pos + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                pos += 4;
                            }
                            break;
                        default: sb.append(esc); break;
                    }
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (src.startsWith("true", pos)) {
                pos += 4;
                return Boolean.TRUE;
            }
            if (src.startsWith("false", pos)) {
                pos += 5;
                return Boolean.FALSE;
            }
            return Boolean.FALSE;
        }

        private Object parseNull() {
            if (src.startsWith("null", pos)) {
                pos += 4;
            }
            return null;
        }

        private Number parseNumber() {
            int start = pos;
            while (pos < src.length()) {
                char c = src.charAt(pos);
                if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                    pos++;
                } else {
                    break;
                }
            }
            String numStr = src.substring(start, pos);
            try {
                if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                    return Double.parseDouble(numStr);
                }
                return Long.parseLong(numStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}
