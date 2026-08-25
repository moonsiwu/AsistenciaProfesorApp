package com.institucion.asistencia.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase encargada de abrir y cerrar la conexión con la base de datos MySQL.
 *
 * Lee los datos de conexión desde el archivo "config.properties" ubicado
 * en la raíz del classpath, para no dejar el usuario/contraseña escritos
 * directamente en el código (buena práctica).
 */
public final class ConexionBD {

    private static final String ARCHIVO_CONFIG = "/config.properties";
    private static String url;
    private static String usuario;
    private static String contrasena;
    private static boolean inicializado = false;
    private static String errorInicializacion = null;

    static {
        try {
            registrarDriverMySQL();
            cargarConfiguracion();
            inicializado = true;
        } catch (Exception e) {
            errorInicializacion = e.getMessage();
            System.err.println("═══════════════════════════════════════════════════════");
            System.err.println(" ERROR AL INICIALIZAR LA CONEXIÓN A LA BASE DE DATOS");
            System.err.println("═══════════════════════════════════════════════════════");
            System.err.println(" " + e.getMessage());
            System.err.println("═══════════════════════════════════════════════════════");
        }
    }

    private ConexionBD() {
        // Clase de utilidad: no se instancia
    }

    /**
     * Registra el driver de MySQL directamente (en vez de depender de
     * Class.forName + auto-registro por ServiceLoader). Esto evita el
     * clásico error "No suitable driver found" que puede aparecer por
     * problemas de classloader al ejecutar desde distintos entornos
     * (NetBeans, jar ejecutable, etc.).
     */
    private static void registrarDriverMySQL() {
        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo registrar el driver de MySQL (mysql-connector-j). " +
                "Verifica que la dependencia esté en el proyecto.", e);
        }
    }

    private static void cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream input = ConexionBD.class.getResourceAsStream(ARCHIVO_CONFIG)) {
            if (input == null) {
                throw new RuntimeException(
                    "No se encontró el archivo config.properties en el classpath. " +
                    "Verifica que esté en src/main/resources (o junto a las clases compiladas).");
            }
            props.load(input);
            url = props.getProperty("db.url");
            usuario = props.getProperty("db.usuario");
            contrasena = props.getProperty("db.contrasena");

            if (url == null || url.isBlank()) {
                throw new RuntimeException("La propiedad 'db.url' está vacía en config.properties.");
            }
            if (usuario == null || usuario.isBlank()) {
                throw new RuntimeException("La propiedad 'db.usuario' está vacía en config.properties.");
            }

            System.out.println("[ConexionBD] Configuración cargada:");
            System.out.println("  URL:     " + url);
            System.out.println("  Usuario: " + usuario);

        } catch (IOException e) {
            throw new RuntimeException("Error leyendo config.properties: " + e.getMessage(), e);
        }
    }

    /**
     * Abre y devuelve una nueva conexión. El código que la use debe
     * cerrarla (se recomienda try-with-resources).
     */
    public static Connection obtenerConexion() throws SQLException {
        if (!inicializado) {
            throw new SQLException(
                "La conexión a la base de datos no fue inicializada correctamente. " +
                (errorInicializacion != null ? errorInicializacion :
                 "Revisa la consola para más detalles."));
        }

        try {
            Connection con = DriverManager.getConnection(url, usuario, contrasena);
            return con;
        } catch (SQLException e) {
            // Mensaje de ayuda detallado según el tipo de error
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            StringBuilder ayuda = new StringBuilder();
            ayuda.append("No se pudo conectar a la base de datos.\n\n");
            ayuda.append("Detalle: ").append(e.getMessage()).append("\n\n");

            if (msg.contains("communications link failure") || msg.contains("connect")) {
                ayuda.append("POSIBLES CAUSAS:\n");
                ayuda.append("• MySQL no está corriendo. Ábrelo desde MySQL Workbench o Servicios de Windows.\n");
                ayuda.append("• El puerto 3306 está bloqueado o MySQL usa otro puerto.\n");
                ayuda.append("• La URL en config.properties es incorrecta.\n");
            } else if (msg.contains("access denied")) {
                ayuda.append("POSIBLES CAUSAS:\n");
                ayuda.append("• El usuario o contraseña en config.properties son incorrectos.\n");
                ayuda.append("• Tu usuario actual: '").append(usuario).append("'\n");
            } else if (msg.contains("unknown database")) {
                ayuda.append("POSIBLES CAUSAS:\n");
                ayuda.append("• La base de datos 'asistencia_db' no existe.\n");
                ayuda.append("• Ejecuta el archivo sql/schema.sql en MySQL Workbench primero.\n");
            }

            System.err.println("═══════════════════════════════════════════════════════");
            System.err.println(" ERROR DE CONEXIÓN A MYSQL");
            System.err.println("═══════════════════════════════════════════════════════");
            System.err.println(ayuda);
            System.err.println("═══════════════════════════════════════════════════════");

            throw new SQLException(ayuda.toString(), e);
        }
    }

    /**
     * Prueba la conexión a la base de datos y muestra el resultado.
     * Útil para diagnóstico.
     *
     * @return true si la conexión fue exitosa
     */
    public static boolean probarConexion() {
        try (Connection con = obtenerConexion()) {
            System.out.println("[ConexionBD] ✓ Conexión exitosa a: " + con.getMetaData().getURL());
            System.out.println("[ConexionBD] ✓ MySQL versión: " + con.getMetaData().getDatabaseProductVersion());
            return true;
        } catch (SQLException e) {
            System.err.println("[ConexionBD] ✗ Falló la conexión: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("--- Probando conexion a la base de datos ---");
        boolean ok = probarConexion();
        if (ok) {
            System.out.println("--- PRUEBA EXITOSA: Todo listo para usar la app ---");
        } else {
            System.err.println("--- PRUEBA FALLIDA: Revisa el error de arriba ---");
        }
    }
}