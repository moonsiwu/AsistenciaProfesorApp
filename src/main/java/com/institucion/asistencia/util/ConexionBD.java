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

    static {
        registrarDriverMySQL();
        cargarConfiguracion();
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
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo config.properties: " + e.getMessage(), e);
        }
    }

    /**
     * Abre y devuelve una nueva conexión. El código que la use debe
     * cerrarla (se recomienda try-with-resources).
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(url, usuario, contrasena);
    }
}