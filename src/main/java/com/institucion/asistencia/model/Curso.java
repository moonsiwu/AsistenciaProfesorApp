package com.institucion.asistencia.model;

public class Curso {

    private int idCurso;
    private String nombreCurso;
    private String jornada;
    private int idProfesor;

    public Curso() {
    }

    public Curso(int idCurso, String nombreCurso, String jornada, int idProfesor) {
        this.idCurso = idCurso;
        this.nombreCurso = nombreCurso;
        this.jornada = jornada;
        this.idProfesor = idProfesor;
    }

    public int getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(int idCurso) {
        this.idCurso = idCurso;
    }

    public String getNombreCurso() {
        return nombreCurso;
    }

    public void setNombreCurso(String nombreCurso) {
        this.nombreCurso = nombreCurso;
    }

    public String getJornada() {
        return jornada;
    }

    public void setJornada(String jornada) {
        this.jornada = jornada;
    }

    public int getIdProfesor() {
        return idProfesor;
    }

    public void setIdProfesor(int idProfesor) {
        this.idProfesor = idProfesor;
    }

    /** Usado por los JComboBox para mostrar el texto del curso. */
    @Override
    public String toString() {
        return nombreCurso + (jornada != null && !jornada.isEmpty() ? " (" + jornada + ")" : "");
    }
}
