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
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel interactivo y profesional para el registro diario de asistencia,
 * con diseño armónico y elementos centrados.
 */
public class PanelTomarAsistencia extends JPanel {

    private final Profesor profesor;
    private final CursoDAO cursoDAO = new CursoDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    private final JComboBox<Curso> comboCurso = new JComboBox<>();
    private final JSpinner selectorFecha;
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"Estudiante", "Documento", "Estado de Asistencia", "Observaciones"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 2 || column == 3;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 2) {
                return Asistencia.Estado.class;
            }
            return String.class;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JLabel etiquetaEstado = new JLabel(" ", SwingConstants.CENTER);

    // Tarjetas de métricas
    private JPanel cardTotal;
    private JPanel cardPresentes;
    private JPanel cardAusentes;
    private JPanel cardTarde;

    private final Map<Integer, Integer> filaAIdEstudiante = new HashMap<>();

    public PanelTomarAsistencia(Profesor profesor) {
        this.profesor = profesor;

        SpinnerDateModel modeloFecha = new SpinnerDateModel();
        selectorFecha = new JSpinner(modeloFecha);
        selectorFecha.setEditor(new JSpinner.DateEditor(selectorFecha, "dd/MM/yyyy"));
        selectorFecha.setFont(Tema.FUENTE_NORMAL);

        setLayout(new BorderLayout(0, 14));
        setBackground(Tema.FONDO);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        add(construirEncabezado(), BorderLayout.NORTH);
        add(construirContenidoCentral(), BorderLayout.CENTER);
        add(construirPiePagina(), BorderLayout.SOUTH);

        // Actualizar métricas cuando cambie la tabla
        modeloTabla.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 2) {
                actualizarMetricasEnVivo();
            }
        });

        cargarCursos();

        comboCurso.addActionListener(e -> cargarEstudiantesYAsistencia());
        selectorFecha.addChangeListener(e -> cargarEstudiantesYAsistencia());
    }

    private JPanel construirEncabezado() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // Título y descripción centrados
        JLabel titulo = new JLabel("Registro de Asistencia", SwingConstants.CENTER);
        titulo.setFont(Tema.FUENTE_TITULO);
        titulo.setForeground(Tema.TEXTO_PRINCIPAL);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Seleccione el curso y la fecha para registrar o actualizar la asistencia", SwingConstants.CENTER);
        subtitulo.setFont(Tema.FUENTE_SUBTITULO);
        subtitulo.setForeground(Tema.TEXTO_SECUNDARIO);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titulo);
        panel.add(Box.createVerticalStrut(2));
        panel.add(subtitulo);
        panel.add(Box.createVerticalStrut(12));

        // Barra de filtros centrada
        JPanel tarjetaFiltros = Tema.crearTarjeta();
        tarjetaFiltros.setLayout(new FlowLayout(FlowLayout.CENTER, 18, 4));

        JLabel lblCurso = new JLabel("Curso:");
        lblCurso.setFont(Tema.FUENTE_BOLD);
        lblCurso.setForeground(Tema.TEXTO_PRINCIPAL);

        comboCurso.setFont(Tema.FUENTE_NORMAL);
        comboCurso.setPreferredSize(new Dimension(190, 36));

        JLabel lblFecha = new JLabel("Fecha:");
        lblFecha.setFont(Tema.FUENTE_BOLD);
        lblFecha.setForeground(Tema.TEXTO_PRINCIPAL);

        selectorFecha.setPreferredSize(new Dimension(140, 36));

        tarjetaFiltros.add(lblCurso);
        tarjetaFiltros.add(comboCurso);
        tarjetaFiltros.add(Box.createHorizontalStrut(16));
        tarjetaFiltros.add(lblFecha);
        tarjetaFiltros.add(selectorFecha);

        panel.add(tarjetaFiltros);
        panel.add(Box.createVerticalStrut(10));

        // Tarjetas de Métricas centradas
        JPanel panelMetricas = new JPanel(new GridLayout(1, 4, 12, 0));
        panelMetricas.setOpaque(false);

        cardTotal = Tema.crearTarjetaMetrica("Total Estudiantes", "0", Tema.AZUL_PRINCIPAL);
        cardPresentes = Tema.crearTarjetaMetrica("Presentes", "0", Tema.VERDE_PRESENTE);
        cardAusentes = Tema.crearTarjetaMetrica("Ausentes", "0", Tema.ROJO_AUSENTE);
        cardTarde = Tema.crearTarjetaMetrica("Tardes / Excusas", "0", Tema.NARANJA_TARDE);

        panelMetricas.add(cardTotal);
        panelMetricas.add(cardPresentes);
        panelMetricas.add(cardAusentes);
        panelMetricas.add(cardTarde);

        panel.add(panelMetricas);
        return panel;
    }

    private JPanel construirContenidoCentral() {
        JPanel contenedorTarjeta = Tema.crearTarjeta();
        contenedorTarjeta.setLayout(new BorderLayout());

        Tema.estilizarTabla(tabla);

        JComboBox<Asistencia.Estado> comboEstados = new JComboBox<>(Asistencia.Estado.values());
        comboEstados.setFont(Tema.FUENTE_NORMAL);
        tabla.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboEstados));
        tabla.getColumnModel().getColumn(0).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(160);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(240);

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

        etiquetaEstado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        etiquetaEstado.setForeground(Tema.TEXTO_SECUNDARIO);
        etiquetaEstado.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        botones.setOpaque(false);

        JButton botonTodosPresentes = Tema.crearBotonSecundario("Marcar Todos Presentes");
        botonTodosPresentes.addActionListener(e -> marcarTodos(Asistencia.Estado.PRESENTE));

        JButton botonGuardar = Tema.crearBotonPrimario("Guardar Asistencia");
        botonGuardar.addActionListener(e -> guardarAsistencia());

        botones.add(botonTodosPresentes);
        botones.add(botonGuardar);

        panel.add(etiquetaEstado, BorderLayout.NORTH);
        panel.add(botones, BorderLayout.CENTER);
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

            actualizarMetricasEnVivo();
            etiquetaEstado.setText(estudiantes.size() + " estudiante(s) cargados en el curso " + curso.getNombreCurso());
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar la lista de estudiantes", ex);
        }
    }

    private void actualizarMetricasEnVivo() {
        int total = modeloTabla.getRowCount();
        int presentes = 0;
        int ausentes = 0;
        int otros = 0;

        for (int i = 0; i < total; i++) {
            Object val = modeloTabla.getValueAt(i, 2);
            if (val instanceof Asistencia.Estado) {
                Asistencia.Estado st = (Asistencia.Estado) val;
                if (st == Asistencia.Estado.PRESENTE) presentes++;
                else if (st == Asistencia.Estado.AUSENTE) ausentes++;
                else otros++;
            }
        }

        actualizarValorCard(cardTotal, String.valueOf(total));
        actualizarValorCard(cardPresentes, String.valueOf(presentes));
        actualizarValorCard(cardAusentes, String.valueOf(ausentes));
        actualizarValorCard(cardTarde, String.valueOf(otros));
    }

    private void actualizarValorCard(JPanel card, String valor) {
        if (card == null) return;
        for (Component c : card.getComponents()) {
            if (c instanceof JLabel && "valor".equals(c.getName())) {
                ((JLabel) c).setText(valor);
            }
        }
    }

    private void marcarTodos(Asistencia.Estado estado) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            modeloTabla.setValueAt(estado, i, 2);
        }
        actualizarMetricasEnVivo();
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
            String fechaStr = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            etiquetaEstado.setText("Asistencia guardada correctamente (" + fechaStr + ")");
            JOptionPane.showMessageDialog(this, "La asistencia fue registrada exitosamente.",
                    "Registro Guardado", JOptionPane.INFORMATION_MESSAGE);
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
