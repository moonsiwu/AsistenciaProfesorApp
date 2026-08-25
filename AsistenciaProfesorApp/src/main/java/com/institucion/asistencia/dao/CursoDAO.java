package com.institucion.asistencia.dao;

import com.institucion.asistencia.model.Curso;
import com.institucion.asistencia.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {

    public List<Curso> listarPorProfesor(int idProfesor) throws SQLException {
        String sql = "SELECT id_curso, nombre_curso, jornada, id_profesor " +
                     "FROM cursos WHERE id_profesor = ? ORDER BY nombre_curso";
        List<Curso> cursos = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProfesor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cursos.add(mapear(rs));
                }
            }
        }
        return cursos;
    }

    public Curso guardar(Curso curso) throws SQLException {
        String sql = "INSERT INTO cursos (nombre_curso, jornada, id_profesor) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, curso.getNombreCurso());
            ps.setString(2, curso.getJornada());
            ps.setInt(3, curso.getIdProfesor());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    curso.setIdCurso(keys.getInt(1));
                }
            }
        }
        return curso;
    }

    public void eliminar(int idCurso) throws SQLException {
        String sql = "DELETE FROM cursos WHERE id_curso = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            ps.executeUpdate();
        }
    }

    private Curso mapear(ResultSet rs) throws SQLException {
        return new Curso(
                rs.getInt("id_curso"),
                rs.getString("nombre_curso"),
                rs.getString("jornada"),
                rs.getInt("id_profesor")
        );
    }
}
