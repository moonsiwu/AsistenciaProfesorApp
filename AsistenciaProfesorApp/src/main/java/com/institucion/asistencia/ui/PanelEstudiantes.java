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

/**
 * Panel institucional para la administración de cursos y estudiantes,
 * con diseño armónico y centrado.
 */
public class PanelEstudiantes extends JPanel {

    private final Profesor profesor;
    private final CursoDAO cursoDAO = new CursoDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();

    private final JComboBox<Curso> comboCurso = new JComboBox<>();
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre Completo del Estudiante", "Documento de Identidad"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JLabel lblContador = new JLabel("0 estudiantes matriculados", SwingConstants.CENTER);

    public PanelEstudiantes(Profesor profesor) {
        this.profesor = profesor;
        setLayout(new BorderLayout(0, 14));
        setBackground(Tema.FONDO);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirContenidoCentral(), BorderLayout.CENTER);
        add(construirPiePagina(), BorderLayout.SOUTH);

        cargarCursos();
        comboCurso.addActionListener(e -> cargarEstudiantes());
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Título y subtítulo centrados
        JLabel titulo = new JLabel("Gestión de Estudiantes y Cursos", SwingConstants.CENTER);
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Tema.TEXTO_PRINCIPAL);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Administre la matrícula de estudiantes y cree nuevos cursos académicos", SwingConstants.CENTER);
        subtitulo.setFont(Tema.FUENTE_SUBTITULO);
        subtitulo.setForeground(Tema.TEXTO_SECUNDARIO);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titulo);
        panel.add(Box.createVerticalStrut(2));
        panel.add(subtitulo);
        panel.add(Box.createVerticalStrut(12));

        // Tarjeta de acciones y filtros centrada
        JPanel tarjetaAcciones = Tema.crearTarjeta();
        tarjetaAcciones.setLayout(new FlowLayout(FlowLayout.CENTER, 16, 4));

        JLabel lblCurso = new JLabel("Curso Seleccionado:");
        lblCurso.setFont(Tema.FUENTE_BOLD);
        lblCurso.setForeground(Tema.TEXTO_PRINCIPAL);

        comboCurso.setFont(Tema.FUENTE_NORMAL);
        comboCurso.setPreferredSize(new Dimension(200, 36));

        JButton botonNuevoCurso = Tema.crearBotonSecundario("Nuevo Curso");
        botonNuevoCurso.addActionListener(e -> crearCurso());

        JButton botonAgregar = Tema.crearBotonPrimario("Agregar Estudiante");
        botonAgregar.addActionListener(e -> agregarEstudiante());

        tarjetaAcciones.add(lblCurso);
        tarjetaAcciones.add(comboCurso);
        tarjetaAcciones.add(Box.createHorizontalStrut(10));
        tarjetaAcciones.add(botonNuevoCurso);
        tarjetaAcciones.add(Box.createHorizontalStrut(6));
        tarjetaAcciones.add(botonAgregar);

        panel.add(tarjetaAcciones);
        return panel;
    }

    private JPanel construirContenidoCentral() {
        JPanel contenedorTarjeta = Tema.crearTarjeta();
        contenedorTarjeta.setLayout(new BorderLayout());

        Tema.estilizarTabla(tabla);
        tabla.removeColumn(tabla.getColumnModel().getColumn(0)); // Oculta columna ID

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        contenedorTarjeta.add(scroll, BorderLayout.CENTER);
        return contenedorTarjeta;
    }

    private JPanel construirPiePagina() {
        JPanel panel = new JPanel(new BorderLayout(10, 8));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(4, 0, 0, 0));

        lblContador.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblContador.setForeground(Tema.TEXTO_SECUNDARIO);
        lblContador.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setOpaque(false);

        JButton botonEliminar = Tema.crearBotonPeligro("Dar de Baja Estudiante");
        botonEliminar.addActionListener(e -> darDeBajaSeleccionado());
        panelBoton.add(botonEliminar);

        panel.add(lblContador, BorderLayout.NORTH);
        panel.add(panelBoton, BorderLayout.CENTER);
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
            lblContador.setText("0 estudiantes matriculados");
            return;
        }
        modeloTabla.setRowCount(0);
        try {
            List<Estudiante> estudiantes = estudianteDAO.listarPorCurso(curso.getIdCurso());
            for (Estudiante est : estudiantes) {
                modeloTabla.addRow(new Object[]{est.getIdEstudiante(), est.getNombreCompleto(), est.getDocumento()});
            }
            lblContador.setText(estudiantes.size() + " estudiante(s) activo(s) en " + curso.getNombreCurso());
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar la lista de estudiantes", ex);
        }
    }

    private void agregarEstudiante() {
        Curso curso = (Curso) comboCurso.getSelectedItem();
        if (curso == null) {
            JOptionPane.showMessageDialog(this, "Primero cree o seleccione un curso académico.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panelForm = new JPanel(new GridLayout(4, 1, 6, 6));
        panelForm.setPreferredSize(new Dimension(340, 160));

        JTextField campoNombre = new JTextField();
        Tema.estilizarCampoTexto(campoNombre);

        JTextField campoDocumento = new JTextField();
        Tema.estilizarCampoTexto(campoDocumento);

        JLabel lblNom = new JLabel("Nombre Completo del Estudiante:");
        lblNom.setFont(Tema.FUENTE_BOLD);
        JLabel lblDoc = new JLabel("Documento de Identidad:");
        lblDoc.setFont(Tema.FUENTE_BOLD);

        panelForm.add(lblNom);
        panelForm.add(campoNombre);
        panelForm.add(lblDoc);
        panelForm.add(campoDocumento);

        int resultado = JOptionPane.showConfirmDialog(this, panelForm,
                "Registrar Nuevo Estudiante", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            String nombre = campoNombre.getText().trim();
            String documento = campoDocumento.getText().trim();
            if (nombre.isEmpty() || documento.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre y el documento son campos obligatorios.",
                        "Campos Requeridos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Estudiante nuevo = new Estudiante();
                nuevo.setNombreCompleto(nombre);
                nuevo.setDocumento(documento);
                nuevo.setIdCurso(curso.getIdCurso());
                estudianteDAO.guardar(nuevo);
                cargarEstudiantes();
                JOptionPane.showMessageDialog(this, "El estudiante fue registrado correctamente.",
                        "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                mostrarError("No se pudo guardar el estudiante (verifique si el documento ya existe)", ex);
            }
        }
    }

    private void darDeBajaSeleccionado() {
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un estudiante de la tabla para continuar.",
                    "Selección Requerida", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int idEstudiante = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Desea dar de baja al estudiante '" + nombre + "'?\n(Su historial de asistencia permanecerá en el sistema)",
                "Confirmar Baja de Estudiante", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                estudianteDAO.desactivar(idEstudiante);
                cargarEstudiantes();
            } catch (SQLException ex) {
                mostrarError("No se pudo procesar la baja del estudiante", ex);
            }
        }
    }

    private void crearCurso() {
        JPanel panelForm = new JPanel(new GridLayout(4, 1, 6, 6));
        panelForm.setPreferredSize(new Dimension(340, 160));

        JTextField campoNombre = new JTextField();
        Tema.estilizarCampoTexto(campoNombre);

        JTextField campoJornada = new JTextField("Mañana");
        Tema.estilizarCampoTexto(campoJornada);

        JLabel lblNom = new JLabel("Nombre del Curso (ej. Grado 10-A):");
        lblNom.setFont(Tema.FUENTE_BOLD);
        JLabel lblJor = new JLabel("Jornada (ej. Mañana, Tarde):");
        lblJor.setFont(Tema.FUENTE_BOLD);

        panelForm.add(lblNom);
        panelForm.add(campoNombre);
        panelForm.add(lblJor);
        panelForm.add(campoJornada);

        int resultado = JOptionPane.showConfirmDialog(this, panelForm,
                "Crear Nuevo Curso Académico", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            String nombre = campoNombre.getText().trim();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre del curso es obligatorio.",
                        "Campo Requerido", JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Curso '" + nombre + "' creado exitosamente.",
                        "Curso Creado", JOptionPane.INFORMATION_MESSAGE);
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
