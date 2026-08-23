package com.institucion.asistencia.dao;

import com.institucion.asistencia.model.Profesor;
import com.institucion.asistencia.util.ConexionBD;
import com.institucion.asistencia.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfesorDAO {

    /**
     * Valida usuario y contraseña contra la base de datos.
     * Devuelve el Profesor si las credenciales son correctas, o null si no.
     */
    public Profesor autenticar(String usuario, String contrasenaPlano) throws SQLException {
        String sql = "SELECT id_profesor, nombre_completo, usuario, contrasena, correo " +
                     "FROM profesores WHERE usuario = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("contrasena");
                    if (PasswordUtil.coincide(contrasenaPlano, hashGuardado)) {
                        Profesor p = new Profesor();
                        p.setIdProfesor(rs.getInt("id_profesor"));
                        p.setNombreCompleto(rs.getString("nombre_completo"));
                        p.setUsuario(rs.getString("usuario"));
                        p.setCorreo(rs.getString("correo"));
                        return p;
                    }
                }
            }
        }
        return null; // usuario no existe o contraseña incorrecta
    }
}
