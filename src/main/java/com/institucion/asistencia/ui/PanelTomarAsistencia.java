package com.institucion.asistencia.ui;

import com.institucion.asistencia.dao.AsistenciaDAO;
import com.institucion.asistencia.dao.CursoDAO;
import com.institucion.asistencia.dao.EstudianteDAO;
import com.institucion.asistencia.model.Asistencia;
import com.institucion.asistencia.model.Curso;
import com.institucion.asistencia.model.Estudiante;
import com.institucion.asistencia.model.Profesor;
import com.institucion.asistencia.util.Tema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanelTomarAsistencia extends JPanel {

    private final Profesor profesor;
    private final CursoDAO cursoDAO = new CursoDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    private final JComboBox<Curso> comboCurso = new JComboBox<>();
    private final JSpinner selectorFecha;
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Estudiante", "Documento", "Estado", "Observación"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 || column == 3;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JLabel etiquetaEstado = new JLabel(" ");

    // Mapa fila de la tabla -> id_estudiante, para guardar sin depender del texto mostrado
    private final Map<Integer, Integer> filaAIdEstudiante = new HashMap<>();

    public PanelTomarAsistencia(Profesor profesor) {
        this.profesor = profesor;

        SpinnerDateModel modeloFecha = new SpinnerDateModel();
        selectorFecha = new JSpinner(modeloFecha);
        selectorFecha.setEditor(new JSpinner.DateEditor(selectorFecha, "dd/MM/yyyy"));

        setLayout(new BorderLayout());
        setBackground(Tema.FONDO);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirPiePagina(), BorderLayout.SOUTH);

        cargarCursos();

        comboCurso.addActionListener(e -> cargarEstudiantesYAsistencia());
        selectorFecha.addChangeListener(e -> cargarEstudiantesYAsistencia());
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Tema.FONDO);
        panel.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel titulo = new JLabel("Tomar asistencia");
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Tema.TEXTO_PRINCIPAL);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filtros.setBackground(Tema.FONDO);
        filtros.add(etiqueta("Curso:"));
        filtros.add(comboCurso);
        filtros.add(Box.createHorizontalStrut(20));
        filtros.add(etiqueta("Fecha:"));
        filtros.add(selectorFecha);

        panel.add(titulo, BorderLayout.NORTH);
        panel.add(filtros, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.FUENTE_BOLD);
        l.setForeground(Tema.TEXTO_PRINCIPAL);
        return l;
    }

    private JScrollPane construirTabla() {
        tabla.setRowHeight(32);
        tabla.setFont(Tema.FUENTE_NORMAL);
        tabla.getTableHeader().setFont(Tema.FUENTE_BOLD);
        tabla.getTableHeader().setBackground(Tema.AZUL_PRINCIPAL);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(new Color(0xDE, 0xEA, 0xF6));
        tabla.setGridColor(Tema.BORDE);

        JComboBox<Asistencia.Estado> comboEstados = new JComboBox<>(Asistencia.Estado.values());
        tabla.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboEstados));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(Tema.BORDE));
        return scroll;
    }

    private JPanel construirPiePagina() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Tema.FONDO);
        panel.setBorder(new EmptyBorder(14, 0, 0, 0));

        etiquetaEstado.setFont(Tema.FUENTE_SUBTITULO);
        etiquetaEstado.setForeground(Tema.TEXTO_SECUNDARIO);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.setBackground(Tema.FONDO);

        JButton botonTodosPresentes = new JButton("Marcar todos presentes");
        botonTodosPresentes.setFocusPainted(false);
        botonTodosPresentes.addActionListener(e -> marcarTodos(Asistencia.Estado.PRESENTE));

        JButton botonGuardar = new JButton("Guardar asistencia");
        botonGuardar.setBackground(Tema.AZUL_PRINCIPAL);
        botonGuardar.setForeground(Color.WHITE);
        botonGuardar.setFont(Tema.FUENTE_BOTON);
        botonGuardar.setFocusPainted(false);
        botonGuardar.setBorderPainted(false);
        botonGuardar.setOpaque(true);
        botonGuardar.addActionListener(e -> guardarAsistencia());

        botones.add(botonTodosPresentes);
        botones.add(botonGuardar);

        panel.add(etiquetaEstado, BorderLayout.WEST);
        panel.add(botones, BorderLayout.EAST);
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
                cargarEstudiantesYAsistencia();
            }
        } catch (SQLException ex) {
            mostrarError("No se pudieron cargar los cursos", ex);
        }
    }

    private void cargarEstudiantesYAsistencia() {
        Curso curso = (Curso) comboCurso.getSelectedItem();
        if (curso == null) {
            return;
        }
        LocalDate fecha = obtenerFechaSeleccionada();

        modeloTabla.setRowCount(0);
        filaAIdEstudiante.clear();

        try {
            List<Estudiante> estudiantes = estudianteDAO.listarPorCurso(curso.getIdCurso());
            List<Asistencia> yaRegistrado = asistenciaDAO.listarPorCursoYFecha(curso.getIdCurso(), fecha);

            Map<Integer, Asistencia> registradoPorEstudiante = new HashMap<>();
            for (Asistencia a : yaRegistrado) {
                registradoPorEstudiante.put(a.getIdEstudiante(), a);
            }

            int fila = 0;
            for (Estudiante est : estudiantes) {
                Asistencia previo = registradoPorEstudiante.get(est.getIdEstudiante());
                Asistencia.Estado estado = previo != null ? previo.getEstado() : Asistencia.Estado.PRESENTE;
                String observacion = previo != null && previo.getObservacion() != null ? previo.getObservacion() : "";

                modeloTabla.addRow(new Object[]{est.getNombreCompleto(), est.getDocumento(), estado, observacion});
                filaAIdEstudiante.put(fila, est.getIdEstudiante());
                fila++;
            }

            etiquetaEstado.setText(estudiantes.size() + " estudiante(s) en " + curso.getNombreCurso());
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar la lista de estudiantes", ex);
        }
    }

    private void marcarTodos(Asistencia.Estado estado) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            modeloTabla.setValueAt(estado, i, 2);
        }
    }

    private void guardarAsistencia() {
        Curso curso = (Curso) comboCurso.getSelectedItem();
        if (curso == null) {
            return;
        }
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }

        LocalDate fecha = obtenerFechaSeleccionada();
        java.util.List<Asistencia> registros = new java.util.ArrayList<>();

        for (int fila = 0; fila < modeloTabla.getRowCount(); fila++) {
            Integer idEstudiante = filaAIdEstudiante.get(fila);
            Asistencia.Estado estado = (Asistencia.Estado) modeloTabla.getValueAt(fila, 2);
            String observacion = (String) modeloTabla.getValueAt(fila, 3);

            Asistencia a = new Asistencia(idEstudiante, curso.getIdCurso(), profesor.getIdProfesor(),
                    fecha, estado, observacion);
            registros.add(a);
        }

        try {
            asistenciaDAO.registrarLote(registros);
            etiquetaEstado.setText("Asistencia guardada correctamente - "
                    + fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            JOptionPane.showMessageDialog(this, "Asistencia guardada correctamente.",
                    "Listo", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            mostrarError("No se pudo guardar la asistencia", ex);
        }
    }

    private LocalDate obtenerFechaSeleccionada() {
        java.util.Date fecha = (java.util.Date) selectorFecha.getValue();
        return fecha.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    private void mostrarError(String mensaje, Exception ex) {
        JOptionPane.showMessageDialog(this, mensaje + ":\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
