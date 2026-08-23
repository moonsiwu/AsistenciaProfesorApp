package com.institucion.asistencia.model;

public class Estudiante {

    private int idEstudiante;
    private String nombreCompleto;
    private String documento;
    private int idCurso;
    private boolean activo;

    public Estudiante() {
    }

    public Estudiante(int idEstudiante, String nombreCompleto, String documento, int idCurso, boolean activo) {
        this.idEstudiante = idEstudiante;
        this.nombreCompleto = nombreCompleto;
        this.documento = documento;
        this.idCurso = idCurso;
        this.activo = activo;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return nombreCompleto + " - " + documento;
    }
}
