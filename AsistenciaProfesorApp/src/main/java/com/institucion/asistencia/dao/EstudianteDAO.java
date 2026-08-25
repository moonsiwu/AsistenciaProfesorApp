package com.institucion.asistencia.dao;

import com.institucion.asistencia.model.Estudiante;
import com.institucion.asistencia.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    public List<Estudiante> listarPorCurso(int idCurso) throws SQLException {
        String sql = "SELECT id_estudiante, nombre_completo, documento, id_curso, activo " +
                     "FROM estudiantes WHERE id_curso = ? AND activo = 1 ORDER BY nombre_completo";
        List<Estudiante> lista = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public Estudiante guardar(Estudiante est) throws SQLException {
        String sql = "INSERT INTO estudiantes (nombre_completo, documento, id_curso, activo) " +
                     "VALUES (?, ?, ?, 1)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, est.getNombreCompleto());
            ps.setString(2, est.getDocumento());
            ps.setInt(3, est.getIdCurso());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    est.setIdEstudiante(keys.getInt(1));
                }
            }
        }
        return est;
    }

    public void actualizar(Estudiante est) throws SQLException {
        String sql = "UPDATE estudiantes SET nombre_completo = ?, documento = ? WHERE id_estudiante = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, est.getNombreCompleto());
            ps.setString(2, est.getDocumento());
            ps.setInt(3, est.getIdEstudiante());
            ps.executeUpdate();
        }
    }

    /** Baja lógica: se marca como inactivo en vez de borrar el historial de asistencia. */
    public void desactivar(int idEstudiante) throws SQLException {
        String sql = "UPDATE estudiantes SET activo = 0 WHERE id_estudiante = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            ps.executeUpdate();
        }
    }

    private Estudiante mapear(ResultSet rs) throws SQLException {
        return new Estudiante(
                rs.getInt("id_estudiante"),
                rs.getString("nombre_completo"),
                rs.getString("documento"),
                rs.getInt("id_curso"),
                rs.getBoolean("activo")
        );
    }
}
