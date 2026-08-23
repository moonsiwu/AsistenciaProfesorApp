package com.institucion.asistencia.ui;

import com.institucion.asistencia.dao.CursoDAO;
import com.institucion.asistencia.dao.EstudianteDAO;
import com.institucion.asistencia.model.Curso;
import com.institucion.asistencia.model.Estudiante;
import com.institucion.asistencia.model.Profesor;
import com.institucion.asistencia.util.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PanelEstudiantes extends JPanel {

    private final Profesor profesor;
    private final CursoDAO cursoDAO = new CursoDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    private final JComboBox<Curso> comboCurso = new JComboBox<>();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre completo", "Documento"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    public PanelEstudiantes(Profesor profesor) {
        this.profesor = profesor;
        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirPiePagina(), BorderLayout.SOUTH);

        cargarCursos();
        comboCurso.addActionListener(e -> cargarEstudiantes());
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Tema.FONDO);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titulo = new JLabel("Estudiantes");
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Tema.TEXTO_PRINCIPAL);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filtros.setBackground(Tema.FONDO);
        JLabel etiquetaCurso = new JLabel("Curso:");
        etiquetaCurso.setFont(Tema.FUENTE_BOLD);
        filtros.add(etiquetaCurso);
        filtros.add(comboCurso);

        JButton botonNuevoCurso = new JButton("+ Nuevo curso");
        botonNuevoCurso.setFocusPainted(false);
        botonNuevoCurso.addActionListener(e -> crearCurso());
        filtros.add(botonNuevoCurso);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(filtros, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane construirTabla() {
        tabla.setRowHeight(30);
        tabla.setFont(Tema.FUENTE_NORMAL);
        tabla.getTableHeader().setFont(Tema.FUENTE_BOLD);
        tabla.getTableHeader().setBackground(Tema.AZUL_PRINCIPAL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.removeColumn(tabla.getColumnModel().getColumn(0)); // oculta columna ID pero la deja en el modelo
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        return scroll;
    }

    private JPanel construirPiePagina() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(Tema.FONDO);
        panel.setBorder(new EmptyBorder(14, 0, 0, 0));

        JButton botonEliminar = new JButton("Dar de baja");
        botonEliminar.setFocusPainted(false);
        botonEliminar.addActionListener(e -> darDeBajaSeleccionado());

        JButton botonAgregar = new JButton("+ Agregar estudiante");
        botonAgregar.setBackground(Tema.AZUL_PRINCIPAL);
        botonAgregar.setForeground(Color.WHITE);
        botonAgregar.setFont(Tema.FUENTE_BOTON);
        botonAgregar.setFocusPainted(false);
        botonAgregar.setBorderPainted(false);
        botonAgregar.setOpaque(true);
        botonAgregar.addActionListener(e -> agregarEstudiante());

        panel.add(botonEliminar);
        panel.add(botonAgregar);
        return panel;
    }

    private void cargarCursos() {
        try {
            comboCurso.removeAllItems();
            List<Curso> cursos = cursoDAO.listarPorProfesor(profesor.getIdProfesor());
            for (Curso c : cursos) {
                comboCurso.addItem(c);
            }
            if (!cursos.isEmpty()) {
                cargarEstudiantes();
            }
        } catch (SQLException ex) {
            mostrarError("No se pudieron cargar los cursos", ex);
        }
    }

    private void cargarEstudiantes() {
        Curso curso = (Curso) comboCurso.getSelectedItem();
        if (curso == null) {
            return;
        }
        modeloTabla.setRowCount(0);
        try {
            List<Estudiante> estudiantes = estudianteDAO.listarPorCurso(curso.getIdCurso());
            for (Estudiante est : estudiantes) {
                modeloTabla.addRow(new Object[]{est.getIdEstudiante(), est.getNombreCompleto(), est.getDocumento()});
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar la lista de estudiantes", ex);
        }
    }

    private void agregarEstudiante() {
        Curso curso = (Curso) comboCurso.getSelectedItem();
        if (curso == null) {
            JOptionPane.showMessageDialog(this, "Primero crea o selecciona un curso.");
            return;
        }

        JTextField campoNombre = new JTextField();
        JTextField campoDocumento = new JTextField();
        Object[] mensaje = {
                "Nombre completo:", campoNombre,
                "Documento:", campoDocumento
        };

        int resultado = JOptionPane.showConfirmDialog(this, mensaje, "Agregar estudiante",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            String nombre = campoNombre.getText().trim();
            String documento = campoDocumento.getText().trim();
            if (nombre.isEmpty() || documento.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre y el documento son obligatorios.");
                return;
            }
            try {
                Estudiante nuevo = new Estudiante();
                nuevo.setNombreCompleto(nombre);
                nuevo.setDocumento(documento);
                nuevo.setIdCurso(curso.getIdCurso());
                estudianteDAO.guardar(nuevo);
                cargarEstudiantes();
            } catch (SQLException ex) {
                mostrarError("No se pudo guardar el estudiante (¿documento repetido?)", ex);
            }
        }
    }

    private void darDeBajaSeleccionado() {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un estudiante de la tabla primero.");
            return;
        }
        int idEstudiante = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Dar de baja a " + nombre + "? Su historial de asistencia se conserva.",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                estudianteDAO.desactivar(idEstudiante);
                cargarEstudiantes();
            } catch (SQLException ex) {
                mostrarError("No se pudo dar de baja al estudiante", ex);
            }
        }
    }

    private void crearCurso() {
        JTextField campoNombre = new JTextField();
        JTextField campoJornada = new JTextField();
        Object[] mensaje = {
                "Nombre del curso (ej. 10-A):", campoNombre,
                "Jornada (ej. Mañana):", campoJornada
        };

        int resultado = JOptionPane.showConfirmDialog(this, mensaje, "Nuevo curso",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            String nombre = campoNombre.getText().trim();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del curso es obligatorio.");
                return;
            }
            try {
                Curso curso = new Curso();
                curso.setNombreCurso(nombre);
                curso.setJornada(campoJornada.getText().trim());
                curso.setIdProfesor(profesor.getIdProfesor());
                cursoDAO.guardar(curso);
                cargarCursos();
                comboCurso.setSelectedItem(curso);
            } catch (SQLException ex) {
                mostrarError("No se pudo crear el curso", ex);
            }
        }
    }

    private void mostrarError(String mensaje, Exception ex) {
        JOptionPane.showMessageDialog(this, mensaje + ":\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
