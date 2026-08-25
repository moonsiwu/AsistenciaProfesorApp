package com.institucion.asistencia.model;

import java.time.LocalDate;

public class Asistencia {

    public enum Estado {
        PRESENTE, AUSENTE, TARDE, EXCUSA
    }

    private int idAsistencia;
    private int idEstudiante;
    private String nombreEstudiante; // solo para mostrar en tablas/reportes
    private int idCurso;
    private int idProfesor;
    private LocalDate fecha;
    private Estado estado;
    private String observacion;

    public Asistencia() {
    }

    public Asistencia(int idEstudiante, int idCurso, int idProfesor, LocalDate fecha,
                       Estado estado, String observacion) {
        this.idEstudiante = idEstudiante;
        this.idCurso = idCurso;
        this.idProfesor = idProfesor;
        this.fecha = fecha;
        this.estado = estado;
        this.observacion = observacion;
    }

    public int getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(int idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public void setNombreEstudiante(String nombreEstudiante) {
        this.nombreEstudiante = nombreEstudiante;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public int getIdProfesor() {
        return idProfesor;
    }

    public void setIdProfesor(int idProfesor) {
        this.idProfesor = idProfesor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
