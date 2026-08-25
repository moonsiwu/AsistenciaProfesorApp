package com.institucion.asistencia.dao;

import com.institucion.asistencia.model.Asistencia;
import com.institucion.asistencia.util.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaDAO {

    /**
     * Registra la asistencia de un estudiante en una fecha. Si ya existía
     * un registro para ese estudiante en esa fecha, lo actualiza en vez
     * de duplicarlo (gracias a la restricción UNIQUE(id_estudiante, fecha)).
     */
    public void registrar(Asistencia a) throws SQLException {
        String sql = "INSERT INTO asistencias (id_estudiante, id_curso, id_profesor, fecha, estado, observacion) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE estado = VALUES(estado), observacion = VALUES(observacion), " +
                     "id_profesor = VALUES(id_profesor)";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, a.getIdEstudiante());
            ps.setInt(2, a.getIdCurso());
            ps.setInt(3, a.getIdProfesor());
            ps.setDate(4, Date.valueOf(a.getFecha()));
            ps.setString(5, a.getEstado().name());
            ps.setString(6, a.getObservacion());
            ps.executeUpdate();
        }
    }

    /** Guarda una lista completa de registros (toma de asistencia de un curso en un día). */
    public void registrarLote(List<Asistencia> registros) throws SQLException {
        String sql = "INSERT INTO asistencias (id_estudiante, id_curso, id_profesor, fecha, estado, observacion) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE estado = VALUES(estado), observacion = VALUES(observacion), " +
                     "id_profesor = VALUES(id_profesor)";

        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Asistencia a : registros) {
                    ps.setInt(1, a.getIdEstudiante());
                    ps.setInt(2, a.getIdCurso());
                    ps.setInt(3, a.getIdProfesor());
                    ps.setDate(4, Date.valueOf(a.getFecha()));
                    ps.setString(5, a.getEstado().name());
                    ps.setString(6, a.getObservacion());
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /** Trae lo ya registrado para un curso en una fecha (para precargar la pantalla de toma de asistencia). */
    public List<Asistencia> listarPorCursoYFecha(int idCurso, LocalDate fecha) throws SQLException {
        String sql = "SELECT a.*, e.nombre_completo AS nombre_estudiante " +
                     "FROM asistencias a JOIN estudiantes e ON a.id_estudiante = e.id_estudiante " +
                     "WHERE a.id_curso = ? AND a.fecha = ?";
        List<Asistencia> lista = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Historial de un curso entre dos fechas (para el reporte). */
    public List<Asistencia> reportePorCurso(int idCurso, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT a.*, e.nombre_completo AS nombre_estudiante " +
                     "FROM asistencias a JOIN estudiantes e ON a.id_estudiante = e.id_estudiante " +
                     "WHERE a.id_curso = ? AND a.fecha BETWEEN ? AND ? " +
                     "ORDER BY a.fecha, e.nombre_completo";
        List<Asistencia> lista = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            ps.setDate(2, Date.valueOf(desde));
            ps.setDate(3, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Asistencia mapear(ResultSet rs) throws SQLException {
        Asistencia a = new Asistencia();
        a.setIdAsistencia(rs.getInt("id_asistencia"));
        a.setIdEstudiante(rs.getInt("id_estudiante"));
        a.setNombreEstudiante(rs.getString("nombre_estudiante"));
        a.setIdCurso(rs.getInt("id_curso"));
        a.setIdProfesor(rs.getInt("id_profesor"));
        a.setFecha(rs.getDate("fecha").toLocalDate());
        a.setEstado(Asistencia.Estado.valueOf(rs.getString("estado")));
        a.setObservacion(rs.getString("observacion"));
        return a;
    }
}
