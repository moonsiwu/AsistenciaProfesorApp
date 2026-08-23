package com.institucion.asistencia.model;

public class Profesor {

    private int idProfesor;
    private String nombreCompleto;
    private String usuario;
    private String contrasena; // hash
    private String correo;

    public Profesor() {
    }

    public Profesor(int idProfesor, String nombreCompleto, String usuario, String correo) {
        this.idProfesor = idProfesor;
        this.nombreCompleto = nombreCompleto;
        this.usuario = usuario;
        this.correo = correo;
    }

    public int getIdProfesor() {
        return idProfesor;
    }

    public void setIdProfesor(int idProfesor) {
        this.idProfesor = idProfesor;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    @Override
    public String toString() {
        return nombreCompleto;
    }
}
